package org.flowable.eventdemo.adapter;

import org.flowable.cmmn.api.CmmnHistoryService;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.CmmnTaskService;
import org.flowable.engine.RuntimeService;
import org.flowable.eventdemo.controller.ReviewEventCounter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

    private CmmnRuntimeService cmmnRuntimeService;
    private CmmnTaskService cmmnTaskService;
    private CmmnHistoryService cmmnHistoryService;
    private RuntimeService runtimeService;
    private ReviewEventCounter reviewEventCounter;

    public DashboardController(CmmnRuntimeService cmmnRuntimeService, CmmnTaskService cmmnTaskService, CmmnHistoryService cmmnHistoryService,
            RuntimeService runtimeService, ReviewEventCounter reviewEventCounter) {
        this.cmmnRuntimeService = cmmnRuntimeService;
        this.cmmnTaskService = cmmnTaskService;
        this.cmmnHistoryService = cmmnHistoryService;
        this.runtimeService = runtimeService;
        this.reviewEventCounter = reviewEventCounter;
    }

    @GetMapping("/dashboard")
    public DashboardData getDashboardData() {
        long caseInstanceCount = cmmnRuntimeService.createCaseInstanceQuery().count();
        long processInstanceCount = runtimeService.createProcessInstanceQuery().count();
        long taskCount = cmmnHistoryService.createHistoricTaskInstanceQuery().count();
        long reviewEventCount = reviewEventCounter.getEventCount();

        return new DashboardData(caseInstanceCount, processInstanceCount, taskCount, reviewEventCount);
    }

    public static class DashboardData {

        private long caseInstanceCount;
        private long processInstanceCount;
        private long taskCount;
        private long reviewEventCount;

        public DashboardData(long caseInstanceCount, long processInstanceCount, long taskCount, long reviewEventCount) {
            this.caseInstanceCount = caseInstanceCount;
            this.processInstanceCount = processInstanceCount;
            this.taskCount = taskCount;
            this.reviewEventCount = reviewEventCount;
        }

        public long getCaseInstanceCount() {
            return caseInstanceCount;
        }
        public void setCaseInstanceCount(long caseInstanceCount) {
            this.caseInstanceCount = caseInstanceCount;
        }
        public long getProcessInstanceCount() {
            return processInstanceCount;
        }
        public void setProcessInstanceCount(long processInstanceCount) {
            this.processInstanceCount = processInstanceCount;
        }
        public long getTaskCount() {
            return taskCount;
        }
        public void setTaskCount(long taskCount) {
            this.taskCount = taskCount;
        }
        public long getReviewEventCount() {
            return reviewEventCount;
        }
        public void setReviewEventCount(long reviewEventCount) {
            this.reviewEventCount = reviewEventCount;
        }
    }

}
