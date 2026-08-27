package com.acorn.gymmanagement.reservation.form;

import com.acorn.gymmanagement.reservation.model.ReservationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationForm {

    private Long memberId;

    @Size(max = 100, message = "고객 이름은 100자 이하여야 합니다.")
    private String customerName;

    @Size(max = 30, message = "연락처는 30자 이하여야 합니다.")
    private String customerPhone;

    private Long trainerId;

    @NotNull(message = "예약 종류를 선택해 주세요.")
    private ReservationType reservationType;

    @NotNull(message = "시작 시간을 선택해 주세요.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startsAt;

    @NotNull(message = "종료 시간을 선택해 주세요.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endsAt;

    @Size(max = 1000, message = "메모는 1000자 이하여야 합니다.")
    private String memo;

}
