(() => {
    const verificationCard =
        document.querySelector("#qr-verification");

    const countdownElement =
        document.querySelector("#verification-countdown");

    const expiredMessage =
        document.querySelector("#verification-expired-message");

    const actionButton =
        document.querySelector(".qr-action-button");

    if (
        !verificationCard ||
        !countdownElement ||
        !actionButton
    ) {
        return;
    }

    let remainingSeconds = Number(
        verificationCard.dataset.seconds
    );

    if (
        !Number.isFinite(remainingSeconds) ||
        remainingSeconds <= 0
    ) {
        remainingSeconds = 120;
    }

    const renderCountdown = () => {
        const minutes = Math.floor(
            remainingSeconds / 60
        );

        const seconds =
            remainingSeconds % 60;

        countdownElement.textContent =
            `${String(minutes).padStart(2, "0")}:` +
            `${String(seconds).padStart(2, "0")}`;
    };

    const expireVerification = () => {
        actionButton.disabled = true;
        countdownElement.textContent = "00:00";

        if (expiredMessage) {
            expiredMessage.hidden = false;
        }
    };

    renderCountdown();

    const timerId = window.setInterval(() =>  {
        remainingSeconds -= 1;

        if (remainingSeconds <= 0) {
            window.clearInterval(timerId);
            expireVerification();
            return;
        }

        renderCountdown();
    }, 1000);

    const form =
        document.querySelector(".qr-attendance-form");

    form?.addEventListener("submit", () => {
        actionButton.disabled = true;
        actionButton.textContent = "처리 중...";
    });


})();