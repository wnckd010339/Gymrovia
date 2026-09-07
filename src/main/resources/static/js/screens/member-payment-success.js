document.addEventListener(
    "DOMContentLoaded",
    async () => {
        const params =
            new URLSearchParams(window.location.search);

        const paymentKey =
            params.get("paymentKey");

        const orderId =
            params.get("orderId");

        const amountText =
            params.get("amount");

        const amount =
            Number(amountText);

        const csrfToken =
            document.querySelector(
                'meta[name="_csrf"]'
            )?.content;

        const csrfHeader =
            document.querySelector(
                'meta[name="_csrf_header"]'
            )?.content;

        const result =
            document.querySelector("#payment-result");

        const completeActions =
            document.querySelector(
                "#payment-complete-actions"
            );

        const retryActions =
            document.querySelector(
                "#payment-retry-actions"
            );

        function fail(message) {
            result.textContent = message;
            result.classList.add("error");
            retryActions.hidden = false;
        }

        function pending() {
            result.textContent =
                "결제가 처리되었을 수 있어 결과 확인이 필요합니다. "
                + "다시 결제하지 말고 결제 내역을 확인하거나 관리자에게 문의해 주세요.";
            result.classList.remove("error", "success");
            retryActions.hidden = true;
            completeActions.hidden = true;
            document.querySelector("#payment-pending-actions").hidden = false;
        }

        if (!paymentKey || !orderId) {
            fail("결제 인증정보가 올바르지 않습니다.");
            return;
        }

        if (!Number.isFinite(amount) || amount <= 0) {
            fail("결제 금액정보가 올바르지 않습니다.");
            return;
        }

        if (!csrfToken || !csrfHeader) {
            fail(
                "보안 토큰을 확인할 수 없습니다. "
                + "페이지를 새로고침해 주세요."
            );
            return;
        }

        try {
            const response = await fetch(
                `/api/member/payment-orders/${
                    encodeURIComponent(orderId)
            }/confirm`,
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json",
                        [csrfHeader]: csrfToken
                    },

                    body: JSON.stringify({
                        paymentKey,
                        amount
                    })
                }
            );

            const payload = await response.json();

            const definiteRejection = response.status === 502
                && payload.success === false
                && payload.error?.code
                && payload.error.code !== "PAYMENT_RESULT_UNKNOWN";

            if (payload.error?.code === "PAYMENT_RESULT_UNKNOWN"
                    || response.status === 409
                    || (response.status >= 500 && !definiteRejection)) {
                pending();
                return;
            }

            if (!response.ok || !payload.success) {
                fail(
                    payload.error?.detail
                    || payload.message
                    || "결제 승인에 실패했습니다."
                );
                return;
            }

            result.textContent =
                "결제가 완료되어 회원권이 활성화되었습니다.";

            result.classList.add("success");
            completeActions.hidden = false;

            window.history.replaceState(
                {},
                document.title,
                window.location.pathname
            );
        } catch (error) {
            pending();
        }
    }
);
