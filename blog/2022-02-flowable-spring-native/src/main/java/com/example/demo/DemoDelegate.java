package com.example.demo;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Filip Hrisafov
 * @author Joram Barrez
 */
public class DemoDelegate implements JavaDelegate {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(DelegateExecution execution) {
        logger.info("Hello " + execution.getVariable("user"));
    }
}
