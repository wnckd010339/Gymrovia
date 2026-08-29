(() => {
    const form =
        document.querySelector("#qr-attendance-form");

    const verificationCard =
        document.querySelector("#qr-verification");

    const processingMessage =
        document.querySelector(
            ".qr-processing-indicator strong"
        );

    if (!form || !verificationCard) {
        return;
    }

    if (form.dataset.submitted === "true") {
        return;
    }

    form.dataset.submitted = "true";

    const action =
        verificationCard.dataset.action;

    if (processingMessage) {
        processingMessage.textContent =
            action === "CHECK_OUT"
                ? "체크아웃을 처리하고 있습니다."
                : "체크인을 처리하고 있습니다.";
    }

    window.setTimeout(() => {
        form.requestSubmit();
    }, 300);
})();