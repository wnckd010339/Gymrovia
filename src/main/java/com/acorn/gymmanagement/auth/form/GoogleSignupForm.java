package com.acorn.gymmanagement.auth.form;

import com.acorn.gymmanagement.member.model.MemberGender;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class GoogleSignupForm {

    @NotBlank(message = "이름을 입력해 주세요.")
    @Size(max = 100, message = "이름은 100자 이하로 입력해 주세요.")
    private String name;

    @NotBlank(message = "연락처를 입력해 주세요.")
    @Pattern(
            regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
            message = "올바른 휴대전화 번호를 입력해 주세요."
    )
    private String phone;

    @NotNull(message = "생년월일을 입력해 주세요.")
    @Past(message = "생년월일은 오늘보다 이전이어야 합니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotNull(message = "성별을 선택해 주세요.")
    private MemberGender gender;

    private boolean trainerRequested;

    @AssertTrue(message = "필수 약관에 동의해 주세요.")
    private boolean termsAgreed;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public MemberGender getGender() {
        return gender;
    }

    public void setGender(MemberGender gender) {
        this.gender = gender;
    }

    public boolean isTrainerRequested() {
        return trainerRequested;
    }

    public void setTrainerRequested(boolean trainerRequested) {
        this.trainerRequested = trainerRequested;
    }

    public boolean isTermsAgreed() {
        return termsAgreed;
    }

    public void setTermsAgreed(boolean termsAgreed) {
        this.termsAgreed = termsAgreed;
    }
}
