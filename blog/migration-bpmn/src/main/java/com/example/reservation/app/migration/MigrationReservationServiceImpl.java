package com.example.reservation.app.migration;

import java.util.List;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.migration.ActivityMigrationMapping;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;

/**
 * @author Simon Amport
 */
@Service("migrationReservationService")
public class MigrationReservationServiceImpl implements MigrationReservationService {

    private final String PROCESS_DEFINITION_KEY = "P001-tableReservation";

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;

    public MigrationReservationServiceImpl(RepositoryService repositoryService, RuntimeService runtimeService) {
        this.repositoryService = repositoryService;
        this.runtimeService = runtimeService;
    }

    @Override
    public void migrateProcessInstancesToLatestDefinition() {
        List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).list();

        // Get the latest process definition
        ProcessDefinition latestProcessDefinition = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(PROCESS_DEFINITION_KEY)
            .latestVersion()
            .singleResult();

        // Do migration
        for (ProcessInstance processInstance : processInstances) {
            runtimeService.createProcessInstanceMigrationBuilder()
                .migrateToProcessDefinition(latestProcessDefinition.getId())
                .withProcessInstanceVariable("customerEmail", "customer@email.com")
                .addActivityMigrationMapping(ActivityMigrationMapping.createMappingFor("intermediatetimereventcatching1", "emailtask1"))
                .migrate(processInstance.getId());
        }

    }
}
