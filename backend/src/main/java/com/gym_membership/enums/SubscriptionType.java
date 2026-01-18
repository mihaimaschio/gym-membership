package com.gym_membership.enums;

public enum SubscriptionType {
    ONE_DAY(30),
    FULL_MONTHLY(200),
    SIX_MONTHS(1000),
    ONE_YEAR(1800),
    STUDENT_MONTHLY(150);

    private final int price;

    SubscriptionType(int price) {
        this.price = price;
    }

    public int getPrice() {
        return price;
    }
}
