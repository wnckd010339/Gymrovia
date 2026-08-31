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

            if (!response.ok || !payload.success) {
                throw new Error(
                    payload.error?.detail
                    || payload.message
                    || "결제 승인에 실패했습니다."
                );
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
            fail(
                error?.message
                || "결제 승인 중 오류가 발생했습니다."
            );
        }
    }
);