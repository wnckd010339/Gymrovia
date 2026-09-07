const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const source = fs.readFileSync(path.join(__dirname, '../../main/resources/static/js/screens/member-payment-success.js'), 'utf8');
const html = fs.readFileSync(path.join(__dirname, '../../main/resources/templates/member/payment-success.html'), 'utf8');

async function run(fetchResult) {
    const elements = new Map();
    for (const id of ['payment-result', 'payment-complete-actions', 'payment-retry-actions', 'payment-pending-actions']) {
        assert.ok(html.includes(`id="${id}"`), `Template must contain ${id}`);
        const classes = new Set();
        elements.set(`#${id}`, {
            hidden: id !== 'payment-result', textContent: '',
            classList: { add: value => classes.add(value), remove: (...values) => values.forEach(value => classes.delete(value)) }
        });
    }
    elements.set('meta[name="_csrf"]', { content: 'test-csrf' });
    elements.set('meta[name="_csrf_header"]', { content: 'X-CSRF-TOKEN' });
    let ready;
    let calls = 0;
    vm.runInNewContext(source, {
        URLSearchParams,
        document: { title: 'Test', querySelector: selector => elements.get(selector), addEventListener: (event, callback) => { ready = callback; } },
        window: { location: { search: '?paymentKey=test-key&orderId=test-order&amount=80000', pathname: '/member/payments/success' }, history: { replaceState() {} } },
        fetch: async (url, options) => {
            calls++;
            assert.equal(options.headers['X-CSRF-TOKEN'], 'test-csrf');
            return fetchResult();
        }
    });
    await ready();
    assert.equal(calls, 1, 'Never automatically retry approval');
    return Object.fromEntries(elements);
}

const response = (status, payload) => () => ({ status, ok: status < 400, json: async () => payload });

test('confirmed payment shows completion actions', async () => {
    const ui = await run(response(200, { success: true }));
    assert.equal(ui['#payment-complete-actions'].hidden, false);
    assert.equal(ui['#payment-retry-actions'].hidden, true);
    assert.equal(ui['#payment-pending-actions'].hidden, true);
});

test('definite rejection shows retry actions', async () => {
    const ui = await run(response(502, { success: false, error: { code: 'REJECT_CARD_COMPANY', detail: '카드사 거절' } }));
    assert.equal(ui['#payment-retry-actions'].hidden, false);
    assert.equal(ui['#payment-pending-actions'].hidden, true);
    assert.equal(ui['#payment-result'].textContent, '카드사 거절');
});

for (const [name, result] of [
    ['unknown outcome', response(409, { success: false, error: { code: 'PAYMENT_RESULT_UNKNOWN' } })],
    ['duplicate request conflict', response(409, { success: false, error: { code: 'CONFLICT' } })],
    ['server failure after processing', response(500, { success: false, error: { code: 'INTERNAL_ERROR' } })],
    ['proxy failure', response(502, {})],
    ['lost browser response', () => { throw new TypeError('network error'); }],
    ['unreadable server response', () => ({ status: 502, ok: false, json: async () => { throw new SyntaxError('not JSON'); } })]
]) {
    test(`${name} keeps retry hidden and directs to payment history`, async () => {
        const ui = await run(result);
        assert.equal(ui['#payment-pending-actions'].hidden, false);
        assert.equal(ui['#payment-retry-actions'].hidden, true);
        assert.equal(ui['#payment-complete-actions'].hidden, true);
        assert.match(ui['#payment-result'].textContent, /다시 결제하지 말고/);
    });
}
