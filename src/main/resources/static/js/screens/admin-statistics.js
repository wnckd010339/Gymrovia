document.addEventListener("DOMContentLoaded", () => {
    initializeQuickPeriods();

    const statistics = readStatistics();

    if (typeof Chart === "undefined") {
        showChartLibraryError();
        return;
    }

    configureChartDefaults();

    drawMemberAttendanceChart(statistics);
    drawSalesChart(statistics);
});

function  readStatistics() {
    const rows = [
        ...document.querySelectorAll(
            "#statistics-data tr"
        )
    ];

    return rows.map(row => ({
        date: row.dataset.date,
        members: toNumber(
            row.dataset.members
        ),
        visitors: toNumber(
            row.dataset.visitors
        ),
        attendances: toNumber(
            row.dataset.attendances
        ),
        sales: toNumber(
            row.dataset.sales
        )
    }));
}

function toNumber(value) {
    const number = Number(value);

    return Number.isFinite(number)
        ? number
        : 0;
}

function configureChartDefaults() {
    Chart.defaults.font.family =
        '"Noto Sans KR", Arial, sans-serif';

    Chart.defaults.color =
        "#74837e";

    Chart.defaults.borderColor =
        "#e8eeeb";
}

function drawMemberAttendanceChart(statistics) {
    const canvas =
        document.querySelector(
            "#member-attendance-chart"
        );

    if (!canvas) {
        return;
    }

    if (statistics.length === 0) {
        showEmptyChart(
            canvas,
            "#member-attendance-chart-empty"
        );

        return;
    }

    const labels =
        statistics.map(item =>
            formatChartDate(item.date)
        );

    const hidePoints =
        statistics.length > 31;

    new Chart(canvas, {
        type: "line",

        data: {
            labels,

            datasets: [
                {
                    label: "신규 가입",
                    data: statistics.map(
                        item => item.members
                    ),
                    borderColor: "#3578e5",
                    backgroundColor:
                        "rgba(53, 120, 229, 0.12)",
                    pointBackgroundColor: "#3578e5",
                    pointBorderColor: "#ffffff",
                    pointBorderWidth: 2,
                    pointRadius: hidePoints ? 0 : 3,
                    pointHoverRadius: 5,
                    borderWidth: 2,
                    tension: 0.32,
                    fill: false,
                    yAxisID: "memberAxis"
                },
                {
                    label: "총 출석",
                        data: statistics.map(
                    item => item.attendances
                    ),
                    borderColor: "#28725f",
                    backgroundColor:
                    "rgba(40, 114, 95, 0.12)",
                        pointBackgroundColor: "#28725f",
                    pointBorderColor: "#ffffff",
                    pointBorderWidth: 2,
                    pointRadius: hidePoints ? 0 : 3,
                    pointHoverRadius: 5,
                    borderWidth: 2,
                    tension: 0.32,
                    fill: true,
                    yAxisID: "attendanceAxis"
                }
            ]
        },

        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                mode: "index",
                intersect: false
            },
            animation: {
                duration: 450
            },
            plugins: {
                legend: {
                    position: "top",
                    align: "start",
                    labels: {
                        usePointStyle: true,
                        pointStyle: "circle",
                        boxWidth: 7,
                        boxHeight: 7,
                        padding: 18
                    }
                },
                tooltip: {
                    callbacks: {
                        label(context) {
                            const unit =
                                context.dataset.label ===
                                "신규 가입"
                                    ? "명"
                                    : "회";

                            return (
                                `${context.dataset.label}: ` +
                                `${context.parsed.y.toLocaleString("ko-KR")}` +
                                 unit
                            );
                        }
                    }
                }
            },
            scales: {
                x: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        maxRotation: 0,
                        autoSkip: true,
                        maxTicksLimit: 10
                    }
                },
                memberAxis: {
                    type: "linear",
                    position: "left",
                    beginAtZero: true,
                    grace: "10%",
                    ticks: {
                        precision: 0,
                        callback(value) {
                            return `${value} 명`;
                        }
                    },
                    title: {
                        display: true,
                        text: "신규 가입"
                    }
                },
                 attendanceAxis: {
                    type: "linear",
                     position: "right",
                     beginAtZero: true,
                     grace: "10%",
                     grid: {
                        drawOnChartArea: false
                     },
                     ticks: {
                        precision: 0,
                         callback(value) {
                            return `${value}회`;
                         }
                     },
                     title: {
                        display: true,
                         text: "총 출석"
                     }
                 }
            }
        }
    });
}

function drawSalesChart(statistics) {
    const canvas =
        document.querySelector(
            "#sales-chart"
        );

    if (!canvas) {
        return;
    }

    const hasSalesData = statistics.some(item => item.sales !== 0);

    if (
        statistics.length === 0 ||
        !hasSalesData
    ) {
        showEmptyChart(
            canvas,
            "#sales-chart-empty"
        );

        return;
    }

    new Chart(canvas, {
        type: "bar",

        data: {
            labels: statistics.map(item =>
                formatChartDate(item.date)
            ),

            datasets: [
                {
                    label: "순매출",
                    data: statistics.map(
                        item => item.sales
                    ),
                    backgroundColor(context) {
                        return context.raw < 0
                            ? "rgba(216, 92, 84, 0.75)"
                            : "rgba(217, 243, 106, 0.85)";
                    },
                    borderColor(context) {
                        return context.raw < 0
                            ? "#d85c54"
                            : "#597a31";
                    },
                    borderWidth: 1,
                    borderRadius: 6,
                    borderSkipped: false,
                    maxBarThickness: 34
                }
            ]
        },

        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                mode: "index",
                intersect: false
            },
            animation: {
                duration: 450
            },
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label(context) {
                            return (
                                "순매출: " +
                                formatCurrency(
                                    context.parsed.y
                                )
                            );
                        }
                    }
                }
            },
            scales: {
                x: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        maxRotation: 0,
                        autoSkip: true,
                        maxTicksLimit: 10
                    }
                },
                y: {
                    beginAtZero: true,
                    grace: "10%",
                    ticks: {
                        callback(value) {
                            return formatCompactCurrency(
                                value
                            );
                        }
                    }
                }
            }
        }
    });
}

function formatChartDate(dateValue) {
    if (!dateValue) {
        return "";
    }

    const parts = dateValue.split("-");

    if (parts.length !== 3) {
        return dateValue;
    }

    return `${parts[1]}.${parts[2]}`;
}

function formatCurrency(value) {
    return new Intl.NumberFormat(
        "ko-KR",
        {
            style: "currency",
            currency: "KRW",
            maximumFractionDigits: 0
        }
    ).format(value);
}

function formatCompactCurrency(value) {
    const absoluteValue =
        Math.abs(value);

    if (absoluteValue >= 100000000) {
        return `${formatCompactNumber(
            value / 100000000
        )}억`;
    }

    if (absoluteValue >= 10000) {
        return `${formatCompactNumber(
            value / 10000
        )}만`;
    }

    return value.toLocaleString("ko-KR");
}

function formatCompactNumber(value) {
    return Number.isInteger(value)
        ? String(value)
        : value.toFixed(1);
}

function showEmptyChart(
    canvas,
    messageSelector
) {
    canvas.hidden = true;

    const message =
        document.querySelector(
            messageSelector
        );

    if (message) {
        message.hidden = false;
    }
}

function showChartLibraryError() {
    document.querySelectorAll(
        ".chart-container canvas"
    ).forEach(canvas => {
        canvas.hidden = true;
    });

    document.querySelectorAll(
        ".chart-empty"
    ).forEach(message => {
        message.hidden = false;
        message.textContent =
            "차트 라이브러리를 불러오지 못했습니다.";
    });

    console.error(
        "Chart.js가 로드되지 않았습니다."
    );
}

function initializeQuickPeriods() {
    document.querySelectorAll(
        "[data-period-days]"
    ).forEach(button => {
        button.addEventListener(
            "click",
            () => {
                const days =
                    Number(
                        button.dataset.periodDays
                    );

                const endDate =
                    startOfToday();

                const startDate =
                    new Date(endDate);

                startDate.setDate(
                    endDate.getDate()
                    - days
                    + 1
                );

                moveToPeriod(
                    startDate,
                    endDate
                );
            }
        );
    });

    document.querySelector(
        '[data-period="current-month"]'
    )?.addEventListener(
        "click",
        () => {
            const today =
                startOfToday();

            moveToPeriod(
                new Date(
                    today.getFullYear(),
                    today.getMonth(),
                    1
                ),
                today
            );
        }
    );

    document.querySelector(
        '[data-period="previous-month"]'
    )?.addEventListener(
        "click",
        () => {
            const today =
                startOfToday();

            moveToPeriod(
                new Date(
                    today.getFullYear(),
                    today.getMonth() - 1,
                    1
                ),
                new Date(
                    today.getFullYear(),
                    today.getMonth(),
                    0
                )
            );
        }
    );
}

function startOfToday() {
    const today = new Date();

    return new Date(
        today.getFullYear(),
        today.getMonth(),
        today.getDate()
    );
}

function moveToPeriod(
    startDate,
    endDate
) {
    const parameters =
        new URLSearchParams({
            startDate:
                formatRequestDate(startDate),
            endDate:
                formatRequestDate(endDate)
        });

    window.location.href =
        `/admin/statistics?${parameters}`;
}

function formatRequestDate(date) {
    const year =
        date.getFullYear();

    const month =
        String(
            date.getMonth() + 1
        ).padStart(2, "0");

    const day =
        String(
            date.getDate()
        ).padStart(2, "0");

    return `${year}-${month}-${day}`;
}