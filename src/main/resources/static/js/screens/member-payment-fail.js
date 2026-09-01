document.addEventListener(
    "DOMContentLoaded",
    () => {
        const params =
            new URLSearchParams(window.location.search);

        const code =
            params.get("code");

        const originalMessage =
            params.get("message");

        const errorElement =
            document.querySelector("#payment-error");

        const safeMessage = switchMessage(
            code,
            originalMessage
        );

        errorElement.textContent = safeMessage;

        window.history.replaceState(
            {},
            document.title,
            window.location.pathname
        );
    }
);

function switchMessage(code, originalMessage) {
    switch (code) {
        case "PAY_PROCESS_CANCELED":
            return"결제가 취소되었습니다.";

        case "PAY_PROCESS_ABORTED":
            return originalMessage
                || "결제 진행 중 오류가 발생했습니다.";

        case "REJECT_CARD_COMPANY":
            return "카드사에서 결제를 거절했습니다.";

        default:
            return originalMessage
            || "결제가 취소되었거나 실패했습니다.";
    }
}