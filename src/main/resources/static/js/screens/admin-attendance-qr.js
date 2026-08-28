(() => {
    const qrImage =
        document.querySelector("#attendance-qr-image");

    const loading =
        document.querySelector("#qr-loading");

    const countdown =
        document.querySelector("#qr-countdown");

    const status =
        document.querySelector("#qr-status");

    const refreshButton =
        document.querySelector("#qr-refresh-button");

    const errorMessage =
        document.querySelector("#qr-error");

    const csrfToken =
        document.querySelector(
            'meta[name="_csrf"]'
        )?.content;

    const csrfHeader =
        document.querySelector(
            'meta[name="_csrf_header"]'
        )?.content;

    if (
        !qrImage ||
        !countdown ||
        !status ||
        !refreshButton
    ) {
        return;
    }

    let remainingSeconds = 0;
    let countdownTimer = null;
    let refreshTimer = null;
    let issuing = false;

    const formatTime = (seconds) => {
        const minutes =
            Math.floor(seconds / 60);

        const remainder =
            seconds % 60;

        return (
            `${String(minutes).padStart(2, "0")}:` +
            `${String(remainder).padStart(2, "0")}`
        );
    };

    const stopTimers = () => {
        if (countdownTimer !== null) {
            window.clearInterval(countdownTimer);
            countdownTimer = null;
        }

        if (refreshTimer !== null) {
            window.clearTimeout(refreshTimer);
            refreshTimer = null;
        }
    };

    const startCountdown = (seconds) => {
        stopTimers();

        remainingSeconds = seconds;
        countdown.textContent =
            formatTime(remainingSeconds);

        countdownTimer =
            window.setInterval(() => {
                remainingSeconds -= 1;

                if (remainingSeconds <= 0) {
                    window.clearInterval(countdownTimer);
                    countdownTimer = null;

                    countdown.textContent = "00:00";
                    status.textContent = "갱신 중";
                    status.classList.add("expired");

                    return;
                }

                countdown.textContent =
                    formatTime(remainingSeconds);
            }, 1000);

        /*
         * 서버 만료 직전에 끊기는 것을 피하기 위해
         * 유효시간보다 5초 일찍 새 QR을 발급합니다.
         */
        refreshTimer =
            window.setTimeout(
                issueQr,
                Math.max(1, seconds - 5) * 1000
            );
    };

    const showError = (message) => {
        if (!errorMessage) {
            return;
        }

        errorMessage.textContent = message;
        errorMessage.hidden = false;
    };

    const clearError = () => {
        if (errorMessage) {
            errorMessage.hidden = true;
            errorMessage.textContent = "";
        }
    };

    async function issueQr() {
        if (issuing) {
            return;
        }

        issuing = true;
        refreshButton.disabled = true;
        clearError();

        status.textContent = "QR 생성 중";
        status.classList.remove("expired");

        if (loading) {
            loading.hidden = false;
        }

        try {
            const headers = {
                Accept: "application/json"
            };

            if (csrfToken && csrfHeader) {
                headers[csrfHeader] = csrfToken;
            }

            const response = await fetch(
                "/admin/attendance/qr/issue",
                {
                    method: "POST",
                    headers
                }
            );

            if (!response.ok) {
                throw new Error(
                    "QR 발급 요청에 실패했습니다."
                );
            }

            const data = await response.json();

            qrImage.src = data.imageDataUrl;
            qrImage.hidden = false;

            if (loading) {
                loading.hidden = true;
            }

            status.textContent = "사용 가능";
            status.classList.remove("expired");

            startCountdown(
                Number(data.expiresInSeconds)
            );
        } catch (error) {
            status.textContent = "발급 실패";
            status.classList.add("expired");

            showError(
                error instanceof Error
                    ? error.message
                    : "QR 발급 중 오류가 발생했습니다."
            );

            /*
             * 일시적인 네트워크 오류라면
             * 5초 후 자동 재시도합니다.
             */
            refreshTimer =
                window.setTimeout(issueQr, 5000);
        } finally {
            issuing = false;
            refreshButton.disabled = false;
        }
    }

    refreshButton.addEventListener(
        "click",
        issueQr
    );

    issueQr();
})();