package com.example.demoflowfest.vacation;

/**
 * @author Filip Hrisafov
 */
public class VacationRequestInput {

    protected String employeeName;
    protected int numberOfDays;
    protected String vacationMotivation;

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public int getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(int numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public String getVacationMotivation() {
        return vacationMotivation;
    }

    public void setVacationMotivation(String vacationMotivation) {
        this.vacationMotivation = vacationMotivation;
    }
}
