package com.example.demoflowfest.risk;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

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
class RiskCalculatorTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void calculateRisk() {
        ObjectNode request = objectMapper.createObjectNode();

        request.put("age", 18);
        request.put("category", "HIGH");
        request.put("debtReview", false);

        ResponseEntity<String> response = restTemplate
            .withBasicAuth("flowfest", "test")
            .postForEntity("/riskCalculator", request, String.class);

        assertThat(response.getStatusCode()).as(response.toString()).isEqualTo(HttpStatus.OK);

        assertThatJson(response.getBody())
            .isEqualTo("{"
                + "reason:'High risk application',"
                + "reviewLevel:'LEVEL 1',"
                + "routing:'REFER'"
                + "}");
    }
}
