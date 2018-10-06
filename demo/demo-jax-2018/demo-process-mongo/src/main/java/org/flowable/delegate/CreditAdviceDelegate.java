package org.flowable.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class CreditAdviceDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        Integer age = (Integer) execution.getVariable("age");
        String home = (String) execution.getVariable("home");

        String guidance = null;
        if (age < 25) {
            guidance = "Young, needs further checks";
        } else if ("rented".equals(home)) {
            guidance = "No collateral, so consider viability";
        } else if (age > 60 && "Mortgaged".equals(home)) {
            guidance = "Potentially overstretched debt to consider";
        } else {
            guidance = "No guidance";
        }
        execution.setVariable("guidance", guidance);
    }

}
