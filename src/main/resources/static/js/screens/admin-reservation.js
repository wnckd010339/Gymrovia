(() => {
    const dialog = document.querySelector("#reservation-dialog");
    const form = document.querySelector("#reservation-form");
    if (!dialog || !form) return;

    const title = document.querySelector("#reservation-dialog-title");
    const statusActions = document.querySelector("#reservation-status-actions");
    const statusForm = statusActions?.querySelector("[data-status-form]");
    const memberSelect = document.querySelector("#memberId");
    const memberKeyword = document.querySelector("#memberKeyword");
    const customerName = document.querySelector("#customerName");
    const customerPhone = document.querySelector("#customerPhone");
    const typeSelect = document.querySelector("#reservationType");
    const trainerSelect = document.querySelector("#trainerId");
    const startsAt = document.querySelector("#startsAt");
    const endsAt = document.querySelector("#endsAt");

    const setValue = (id, value) => {
        const element = document.getElementById(id);
        if (element) element.value = value == null ? "" : value;
    };

    const toDateTimeInput = (value) => value ? value.slice(0, 16) : "";
    const formatDateTimeInput = (date) => {
        const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
        return local.toISOString().slice(0, 16);
    };

    const updateTrainerRequirement = () => {
        const pt = typeSelect.value === "TRIAL_PT" || typeSelect.value === "REGULAR_PT";
        trainerSelect.required = pt;
    };

    const updateCustomerFields = () => {
        const option = memberSelect.selectedOptions[0];
        const selected = Boolean(memberSelect.value);
        if (selected) {
            customerName.value = option.dataset.name || customerName.value;
            customerPhone.value = option.dataset.phone || customerPhone.value;
        }
        customerName.readOnly = selected;
        customerPhone.readOnly = selected;
    };

    const resetDialog = () => {
        form.reset();
        form.action = "/admin/reservations";
        title.textContent = "새 예약 등록";
        statusActions.hidden = true;
        customerName.readOnly = false;
        customerPhone.readOnly = false;
        updateTrainerRequirement();
    };

    document.querySelector("[data-reservation-create]")?.addEventListener("click", () => {
        resetDialog();
        dialog.showModal();
    });

    document.querySelectorAll("[data-dialog-close]").forEach((button) => {
        button.addEventListener("click", () => dialog.close());
    });

    dialog.addEventListener("click", (event) => {
        if (event.target === dialog) dialog.close();
    });

    const ensureMemberOption = (button) => {
        const memberId = button.dataset.memberId;
        if (!memberId) return;
        let option = memberSelect.querySelector(`option[value="${memberId}"]`);
        if (!option) {
            option = new Option(`${button.dataset.customerName} · ${button.dataset.customerPhone}`, memberId);
            option.dataset.name = button.dataset.customerName || "";
            option.dataset.phone = button.dataset.customerPhone || "";
            memberSelect.add(option);
        }
    };

    const configureStatusButtons = (currentStatus) => {
        if (!statusForm) return;
        statusForm.querySelectorAll("button[name='status']").forEach((button) => {
            const next = button.value;
            button.hidden = !(
                (currentStatus === "PENDING" && (next === "CONFIRMED" || next === "CANCELLED")) ||
                (currentStatus === "CONFIRMED" && (next === "COMPLETED" || next === "NO_SHOW" || next === "CANCELLED"))
            );
        });
    };

    const openEditDialog = (button) => {
        const id = button.dataset.reservationId;
        form.action = `/admin/reservations/${id}`;
        title.textContent = "예약 수정";
        statusActions.hidden = false;
        if (statusForm) statusForm.action = `/admin/reservations/${id}/status`;

        ensureMemberOption(button);
        setValue("memberId", button.dataset.memberId);
        setValue("trainerId", button.dataset.trainerId);
        setValue("customerName", button.dataset.customerName);
        setValue("customerPhone", button.dataset.customerPhone);
        setValue("reservationType", button.dataset.reservationType);
        setValue("startsAt", toDateTimeInput(button.dataset.startsAt));
        setValue("endsAt", toDateTimeInput(button.dataset.endsAt));
        setValue("memo", button.dataset.memo);
        updateCustomerFields();
        updateTrainerRequirement();
        configureStatusButtons(button.dataset.reservationStatus);
        dialog.showModal();
    };

    document.querySelectorAll(".calendar-event").forEach((button) => {
        button.addEventListener("click", () => openEditDialog(button));
    });

    document.querySelectorAll("[data-open-reservation]").forEach((button) => {
        button.addEventListener("click", () => {
            const target = document.querySelector(`.calendar-event[data-reservation-id="${button.dataset.openReservation}"]`);
            if (target) openEditDialog(target);
        });
    });

    document.querySelector("#memberSearchButton")?.addEventListener("click", async () => {
        const keyword = memberKeyword.value.trim();
        const response = await fetch(`/admin/reservations/member-options?keyword=${encodeURIComponent(keyword)}`);
        if (!response.ok) return;
        const members = await response.json();
        memberSelect.replaceChildren(new Option("비회원 예약", ""));
        members.forEach((member) => {
            const option = new Option(`${member.name} · ${member.description}`, member.id);
            option.dataset.name = member.name;
            option.dataset.phone = member.description;
            memberSelect.add(option);
        });
        updateCustomerFields();
    });

    memberKeyword?.addEventListener("keydown", (event) => {
        if (event.key === "Enter") {
            event.preventDefault();
            document.querySelector("#memberSearchButton")?.click();
        }
    });
    memberSelect?.addEventListener("change", updateCustomerFields);
    typeSelect?.addEventListener("change", updateTrainerRequirement);
    startsAt?.addEventListener("change", () => {
        if (!startsAt.value) return;
        const end = new Date(startsAt.value);
        end.setMinutes(end.getMinutes() + 60);
        endsAt.value = formatDateTimeInput(end);
    });

    const positionCalendarEvents = () => {
        const startHour = 9;
        const dayWidth = 100 / 6;
        document.querySelectorAll(".calendar-event").forEach((event) => {
            const start = new Date(event.dataset.startsAt);
            const end = new Date(event.dataset.endsAt);
            const day = start.getDay();
            if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime()) || day < 1 || day > 6) {
                event.hidden = true;
                return;
            }
            const top = (start.getHours() - startHour) * 60 + start.getMinutes();
            const duration = Math.max(34, (end.getTime() - start.getTime()) / 60000);
            if (top < 0 || top >= 720) {
                event.hidden = true;
                return;
            }
            event.style.left = `calc(${(day - 1) * dayWidth}% + 3px)`;
            event.style.width = `calc(${dayWidth}% - 6px)`;
            event.style.top = `${top}px`;
            event.style.height = `${Math.min(duration, 720 - top)}px`;
        });
    };

    positionCalendarEvents();
})();
