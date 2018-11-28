package com.example.demoflowfest.vacation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;

import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Filip Hrisafov
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VacationRequestTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HistoryService historyService;

    @Test
    void requestVacation() {
        assertThat(historyService.createHistoricProcessInstanceQuery().list())
            .isEmpty();

        ObjectNode request = objectMapper.createObjectNode();
        request.put("employeeName", "Kermit the Frog");
        request.put("numberOfDays", 5);
        request.put("vacationMotivation", "Ski holidays");

        ResponseEntity<String> response = restTemplate.withBasicAuth("flowfest", "test")
            .postForEntity("/vacationRequest", request, String.class);

        assertThat(response.getStatusCode()).as(response.toString()).isEqualTo(HttpStatus.OK);

        List<HistoricProcessInstance> historicProcesses = historyService.createHistoricProcessInstanceQuery()
            .includeProcessVariables()
            .list();
        assertThat(historicProcesses)
            .extracting(HistoricProcessInstance::getProcessDefinitionKey)
            .containsExactly("vacationRequest");

        HistoricProcessInstance vacationRequest = historicProcesses.get(0);

        assertThat(vacationRequest.getProcessVariables())
            .as("process variables")
            .containsAnyOf(
                entry("employeeName", "Kermit the Frog"),
                entry("numberOfDays", 5),
                entry("vacationMotivation", "Ski holidays"),
                entry("vacationApproved", true)
            );

        assertThat(vacationRequest.getEndTime())
            .as("endTime")
            .isNotNull();
    }
}
