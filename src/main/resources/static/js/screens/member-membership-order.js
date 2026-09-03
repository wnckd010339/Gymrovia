document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector("#membership-order-form");
    if (!form) return;

    const productSelect = document.querySelector("#membership-product");
    const startDateInput = document.querySelector("#membership-start-date");
    const summary = document.querySelector("#membership-order-summary");
    const message = document.querySelector("#membership-order-message");
    const submitButton = document.querySelector("#membership-order-submit");
    const result = document.querySelector("#membership-order-result");
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    const tossClientKey =
        document.querySelector(
            'meta[name="toss-client-key"]'
        )?.content;

    const tossSuccessUrl =
        document.querySelector(
            'meta[name="toss-success-url"]'
        )?.content;

    const tossFailUrl =
        document.querySelector(
            'meta[name="toss-fail-url"]'
        )?.content;

    let countdownTimer;
    let currentOrder = null;

    let tossPayment = null;

    if (tossClientKey && window.TossPayments) {
        const tossPayments = TossPayments(tossClientKey);

        tossPayment = tossPayments.payment({
            customerKey: TossPayments.ANONYMOUS
        });
    }

    const now = new Date();
    const localToday = new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
    startDateInput.min = localToday;
    if (!startDateInput.value) startDateInput.value = localToday;

    const formatPrice = (value) => `${Number(value).toLocaleString("ko-KR")}원`;
    const selectedProduct = () => {
        const option = productSelect.options[productSelect.selectedIndex];
        return option?.value ? option : null;
    };

    function updateSummary() {
        const option = selectedProduct();
        summary.hidden = !option;
        if (!option) return;
        summary.querySelector("[data-order-name]").textContent = option.dataset.name;
        summary.querySelector("[data-order-duration]").textContent = `${option.dataset.duration}일 · ${option.dataset.type}`;
        summary.querySelector("[data-order-price]").textContent = formatPrice(option.dataset.price);
    }

    function showMessage(text, type = "") {
        message.textContent = text;
        message.className = `order-message ${type}`.trim();
    }

    function startCountdown(expiresAt) {
        const expiresAtMillis = new Date(expiresAt).getTime();
        const countdown = result.querySelector("[data-result-countdown]");
        const resultStatus = result.querySelector("[data-result-status]");

        clearInterval(countdownTimer);
        result.classList.remove("expired");
        result.querySelector("[data-confirm-order]").disabled = false;

        const updateCountdown = () => {
            const remainingSeconds = Math.max(
                0,
                Math.ceil((expiresAtMillis - Date.now()) / 1000)
            );
            const minutes = String(Math.floor(remainingSeconds / 60)).padStart(2, "0");
            const seconds = String(remainingSeconds % 60).padStart(2, "0");
            countdown.textContent = `${minutes}:${seconds}`;

            if (remainingSeconds > 0) return;

            clearInterval(countdownTimer);
            countdown.textContent = "만료됨";
            resultStatus.textContent = "결제 주문의 유효시간이 만료되었습니다.";
            result.classList.add("expired");
            result.querySelector("[data-confirm-order]").disabled = true;
            form.hidden = false;
            submitButton.disabled = false;
            showMessage("동일한 상품을 다시 주문할 수 있습니다.", "error");
        };

        updateCountdown();
        countdownTimer = setInterval(updateCountdown, 1000);
    }

    productSelect.addEventListener("change", updateSummary);
    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const option = selectedProduct();
        if (!option || !startDateInput.value) {
            showMessage("회원권 상품과 이용 시작일을 확인해 주세요.", "error");
            return;
        }
        if (!csrfToken || !csrfHeader) {
            showMessage("보안 토큰을 확인할 수 없습니다. 페이지를 새로고침해 주세요.", "error");
            return;
        }

        submitButton.disabled = true;
        showMessage("결제 주문을 생성하고 있습니다.");
        try {
            const response = await fetch("/api/member/payment-orders", {
                method: "POST",
                headers: {"Content-Type": "application/json", [csrfHeader]: csrfToken},
                body: JSON.stringify({productId: Number(option.value), startDate: startDateInput.value})
            });
            const payload = await response.json();
            if (!response.ok || !payload.success) {
                throw new Error(payload.message || payload.error?.detail || "결제 주문을 생성하지 못했습니다.");
            }

            const order = payload.data;
            currentOrder = order;
            result.querySelector("[data-result-order-id]").textContent = order.orderId;
            result.querySelector("[data-result-order-name]").textContent = order.orderName;
            result.querySelector("[data-result-amount]").textContent = formatPrice(order.amount);
            result.querySelector("[data-result-expires-at]").textContent = new Date(order.expiresAt).toLocaleString("ko-KR");
            result.hidden = false;
            form.hidden = true;
            startCountdown(order.expiresAt);
        } catch (error) {
            showMessage(error.message, "error");
            submitButton.disabled = false;
        }
    });

    result.querySelector("[data-confirm-order]")
        .addEventListener("click", async () => {
            if (!currentOrder) {
                showMessage(
                    "먼저 결제 주문을 생성해 주세요.",
                    "error"
                );
                return;
            }

            if (Date.now() >= new Date(currentOrder.expiresAt).getTime()) {
                showMessage("결제 주문의 유효시간이 만료되었습니다. 새 주문을 만들어 주세요.", "error");
                return;
            }

            if (!tossPayment) {
                showMessage(
                    "토스페이먼츠 설정을 불러오지 못했습니다.",
                    "error"
                );
                return;
            }

            if (!tossSuccessUrl || !tossFailUrl) {
                showMessage(
                    "결제 결과 URL이 설정되지 않았습니다.",
                    "error"
                );
                return;
            }

            const confirmButton =
                result.querySelector(
                    "[data-confirm-order]"
                );

            confirmButton.disabled = true;
            showMessage("결제창을 열고 있습니다.");

            try {
                await tossPayment.requestPayment({
                    method: "CARD",

                    amount: {
                        currency: "KRW",
                        value: Number(currentOrder.amount)
                    },

                    orderId: currentOrder.orderId,
                    orderName: currentOrder.orderName,

                    successUrl: tossSuccessUrl,
                    failUrl: tossFailUrl
                });

            } catch (error) {
                confirmButton.disabled = false;

                showMessage(
                    error?.message
                        || "결제창이 취소되었거나 열리지 않았습니다.",
                    "error"
                );
            }
        });

    document.querySelectorAll(
        ".pending-membership-cancel"
    ).forEach((button) => {
        button.addEventListener("click", async () => {
            const membershipId =
                button.dataset.membershipId;

            if (!membershipId) {
                showMessage(
                    "취소할 회원권 정보를 확인할 수 없습니다.",
                    "error"
                );
                return;
            }

            const confirmed = window.confirm(
                "결제 대기 회원권을 취소하고"
                + "새로 구매하시겠습니까?"
            );

            if (!confirmed) {
                return;
            }

            if (!csrfToken || !csrfHeader) {
                showMessage(
                    "보안 토큰을 확인할 수 없습니다."
                    + "페이지를 새로고침해 주세요.",
                    "error"
                );
                return;
            }

            button.disabled = true;
            button.textContent = "취소 중...";

            try {
                const response = await fetch(
                    `/api/member/memberships/${
                        encodeURIComponent(membershipId)
                    }/cancel`,
                    {
                        method: "PATCH",
                        headers: {
                            [csrfHeader]: csrfToken
                        }
                    }
                );

                const payload = await response.json();

                if (!response.ok || !payload.success) {
                    throw new Error(
                        payload.error?.detail
                        || payload.message
                        || "결제 대기 회원권을 취소하지 못했습니다."
                    );
                }

                window.location.reload();
            } catch (error) {
                button.disabled = false;
                button.textContent =
                    "취소하고 다시 구매";

                showMessage(
                    error?.message
                    || "회원권 취소 중 오류가 발생했습니다.",
                    "error"
                );
            }
        });
    });

    window.addEventListener(
        "pagehide",
        () => clearInterval(countdownTimer)
    );
    updateSummary();
});
