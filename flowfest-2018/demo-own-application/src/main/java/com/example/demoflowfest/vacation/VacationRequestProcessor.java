package com.example.demoflowfest.vacation;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Filip Hrisafov
 */
@Component("vacationRequestProcessor")
public class VacationRequestProcessor implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(VacationRequestProcessor.class);

    @Override
    public void execute(DelegateExecution execution) {
        String employeeName = execution.getVariable("employeeName", String.class);
        Integer numberOfDays = execution.getVariable("numberOfDays", Integer.class);
        String vacationMotivation = execution.getVariable("vacationMotivation", String.class);

        LOGGER.info("{} requested vacation for {} days. Motivation {}", employeeName, numberOfDays, vacationMotivation);

        execution.setVariable("vacationApproved", true);
    }

}
