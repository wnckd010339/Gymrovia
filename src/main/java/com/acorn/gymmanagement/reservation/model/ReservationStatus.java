package com.acorn.gymmanagement.reservation.model;

public enum ReservationStatus {
    PENDING("미확정"),
    CONFIRMED("예약 확정"),
    COMPLETED("방문 완료"),
    CANCELLED("취소"),
    NO_SHOW("노쇼");

    private final String label;

    ReservationStatus(String label){
        this.label = label;
    }

    public String getLabel(){
        return label;
    }
}
