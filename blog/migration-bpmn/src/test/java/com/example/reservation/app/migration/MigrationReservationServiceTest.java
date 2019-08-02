package com.example.reservation.app.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ActivityInstance;
import org.flowable.engine.runtime.EventSubscription;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.reservation.app.ReservationApplication;

/**
 * @author Simon Amport
 */
@SpringBootTest(classes = ReservationApplication.class)
public class MigrationReservationServiceTest {

    private final String PROCESS_DEFINITION_KEY = "P001-tableReservation";
    private final String NEW_PROCESS_MODEL = "migration/Table_Reservation_v4.bpmn20.xml";

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private MigrationReservationService migrationReservationService;

    @BeforeEach
    public void setUp() {
        runtimeService.createProcessInstanceBuilder()
            .processDefinitionKey(PROCESS_DEFINITION_KEY)
            .variable("reservationTime", Date.from(Instant.now().plus(7, ChronoUnit.DAYS)))
            .start();

        deployProcessModel(NEW_PROCESS_MODEL);
    }

    @AfterEach
    public void tearDown() {
        runtimeService.createProcessInstanceQuery()
            .processDefinitionKey(PROCESS_DEFINITION_KEY)
            .list()
            .forEach(p -> runtimeService.deleteProcessInstance(p.getId(), "Test"));
    }

    @Test
    public void migrateProcessInstancesToLatestDefinition() {

        migrationReservationService.migrateProcessInstancesToLatestDefinition();

        List<ProcessInstance> migratedProcessInstances = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).list();

        for (ProcessInstance migratedProcessInstance : migratedProcessInstances) {
            assertThat(migratedProcessInstance.getProcessDefinitionName())
                .describedAs("Process instance must running with the latest process definition")
                .isEqualTo("Table Reservation v4");

            assertThat(runtimeService.createActivityInstanceQuery().processInstanceId(migratedProcessInstance.getId()).unfinished().list())
                .describedAs("There must be two unfinished activity instances")
                .extracting(ActivityInstance::getActivityId)
                .containsExactlyInAnyOrder("subprocess1", "intermediatetimereventcatching1");

            EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery()
                .processInstanceId(migratedProcessInstance.getId())
                .activityId("intermediatesignaleventboundary1")
                .singleResult();
            runtimeService.signalEventReceived(eventSubscription.getEventName(), eventSubscription.getExecutionId());

            assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).list().isEmpty())
                .describedAs("Reservation was cancelled and therefore completed")
                .isTrue();
        }
    }

    private Deployment deployProcessModel(String classpath) {
        return repositoryService.createDeployment().addClasspathResource(classpath).deploy();
    }

}
