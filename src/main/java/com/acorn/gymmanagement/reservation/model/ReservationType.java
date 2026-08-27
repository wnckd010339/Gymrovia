package com.acorn.gymmanagement.reservation.model;

public enum ReservationType {
    CONSULTATION("상담"),
    TRIAL_PT("체험 PT"),
    REGULAR_PT("정규 PT");

    private final String label;

    ReservationType(String label){
        this.label = label;
    }

    public String getLabel(){
        return label;
    }
}
