package com.example.demoflowfest.risk;

/**
 * @author Filip Hrisafov
 */
public class RiskInput {

    protected int age;
    protected String category;
    protected boolean debtReview;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isDebtReview() {
        return debtReview;
    }

    public void setDebtReview(boolean debtReview) {
        this.debtReview = debtReview;
    }
}
