package com.example.demoflowfest.risk;
/**
 * @author Filip Hrisafov
 */
public class RiskOutput {

    protected String routing;
    protected String reviewLevel;
    protected String reason;

    public String getRouting() {
        return routing;
    }

    public void setRouting(String routing) {
        this.routing = routing;
    }

    public String getReviewLevel() {
        return reviewLevel;
    }

    public void setReviewLevel(String reviewLevel) {
        this.reviewLevel = reviewLevel;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
