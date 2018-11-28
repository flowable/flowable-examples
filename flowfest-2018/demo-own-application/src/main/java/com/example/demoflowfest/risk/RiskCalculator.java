package com.example.demoflowfest.risk;

import java.util.Map;
import java.util.function.Function;

import org.flowable.dmn.api.DmnRuleService;
import org.springframework.stereotype.Component;

/**
 * @author Filip Hrisafov
 */
@Component
public class RiskCalculator implements Function<RiskInput, RiskOutput> {

    protected final DmnRuleService ruleService;

    public RiskCalculator(DmnRuleService ruleService) {
        this.ruleService = ruleService;
    }

    @Override
    public RiskOutput apply(RiskInput riskInput) {
        Map<String, Object> result = ruleService.createExecuteDecisionBuilder()
            .decisionKey("RiskRatingDecisionTable")
            .variable("age", riskInput.getAge())
            .variable("riskcategory", riskInput.getCategory())
            .variable("debtreview", riskInput.isDebtReview())
            .executeWithSingleResult();
        RiskOutput output = new RiskOutput();
        output.setRouting((String) result.get("routing"));
        output.setReviewLevel((String) result.get("reviewlevel"));
        output.setReason((String) result.get("reason"));
        return output;
    }
}
