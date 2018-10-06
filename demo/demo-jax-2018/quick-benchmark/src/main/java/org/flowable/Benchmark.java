/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.flowable.common.engine.impl.history.HistoryLevel;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.mongodb.cfg.MongoDbProcessEngineConfiguration;
import org.flowable.task.api.Task;

import com.zaxxer.hikari.HikariDataSource;

public class Benchmark {

    private static Random random = new Random();

    public static void main(String[] args) throws Exception {

        final ProcessEngine processEngine = createProcessEngine();
        final RepositoryService repositoryService = processEngine.getRepositoryService();
        final RuntimeService runtimeService = processEngine.getRuntimeService();
        final TaskService taskService = processEngine.getTaskService();

        System.out.println("Deploying process definition");
        repositoryService.createDeployment().addClasspathResource("benchmark-process.bpmn20.xml").deploy();

        List<String> processInstanceIds = startProcessInstances(runtimeService);
        executeQuerying(runtimeService, taskService, processInstanceIds);

        System.out.println("DONE");
    }

    private static ProcessEngine createProcessEngine() {
        // Lazily getting all from system props
        String mode = System.getProperty("mode");
        if ("jdbc".equals(mode)) {

            System.out.println("JDBC mode");
            String jdbcUrl = System.getProperty("jdbcUrl");
            String jdbcUser = System.getProperty("jdbcUser");
            String jdbcPassword = System.getProperty("jdbcPassword");

            System.out.println("Jdbc url = " + jdbcUrl);
            System.out.println("Jdbc user = " + jdbcUser);

            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl(jdbcUrl);
            ds.setUsername(jdbcUser);
            ds.setPassword(jdbcPassword);
            ds.setMaximumPoolSize(50);

            return new StandaloneProcessEngineConfiguration()
                .setDataSource(ds)
                .setHistoryLevel(HistoryLevel.AUDIT)
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
                .buildProcessEngine();

        } else if ("mongo".equals(mode)) {

            System.out.println("MONGODB mode");
            String serverUrls = System.getProperty("serverUrls");
            System.out.println("Server urls = " + serverUrls);

            return new MongoDbProcessEngineConfiguration()
                .setConnectionUrl(serverUrls)
                .setDisableIdmEngine(true)
                .setHistoryLevel(HistoryLevel.AUDIT)
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
                .buildProcessEngine();

        } else {
            throw new RuntimeException("Invalid mode " + mode);
        }
    }

    private static List<String> startProcessInstances(RuntimeService runtimeService) throws InterruptedException {

        int nrOfProcessInstances = Integer.valueOf(System.getProperty("nrOfProcessInstances"));
        System.out.println("Number of process instances = " + nrOfProcessInstances);

        List<String> processInstanceIds = Collections.synchronizedList(new ArrayList<>(nrOfProcessInstances));

        int nrOfThreads = Integer.valueOf(System.getProperty("nrOfThreads"));
        System.out.println("Number of threads = " + nrOfThreads);

        System.out.println("Starting process instances");
        long start = System.currentTimeMillis();
        if (nrOfThreads == 1) {

            for (int i = 0; i < nrOfProcessInstances; i++) {
                try {
                    ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                        .processDefinitionKey("testProcess")
                        .variables(generateRandomStartVariables(10))
                        .start();
                    processInstanceIds.add(processInstance.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else {

            ExecutorService executorService = Executors.newFixedThreadPool(nrOfThreads);

            for (int i = 0; i < nrOfProcessInstances; i++) {
                executorService.submit(() -> {
                    try {
                        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                            .processDefinitionKey("testProcess")
                            .variables(generateRandomStartVariables(10))
                            .start();
                        processInstanceIds.add(processInstance.getId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            System.out.println("All tasks submitted. Waiting for termination");
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.DAYS);

        }

        long end = System.currentTimeMillis();
        long totalTime = end - start;
        double avg = (double) totalTime / (double) nrOfProcessInstances;
        System.out.println("Took " + totalTime + " ms. (avg = " + avg + " ms)");

        return processInstanceIds;
    }

    private static void executeQuerying(RuntimeService runtimeService, TaskService taskService, List<String> processInstanceIds) throws InterruptedException {

        System.out.println("List of process instance ids contains " + processInstanceIds.size() + " elements");

        int nrOfThreads = Integer.valueOf(System.getProperty("nrOfThreads"));
        System.out.println("Number of threads = " + nrOfThreads);

        System.out.println("Querying process instances");
        long start = System.currentTimeMillis();
        if (nrOfThreads == 1) {

            processInstanceIds.forEach(processInstanceId -> executeQueries(runtimeService, taskService, processInstanceId));

        } else {

            ExecutorService executorService = Executors.newFixedThreadPool(nrOfThreads);

            for (String processInstanceId : processInstanceIds) {
                executorService.submit(() -> {
                    try {
                        executeQueries(runtimeService, taskService, processInstanceId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            System.out.println("All querying tasks submitted. Waiting for termination");
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.DAYS);

        }

        long end = System.currentTimeMillis();
        long totalTime = end - start;
        double avg = (double) totalTime / (double) processInstanceIds.size();
        System.out.println("Querying took " + totalTime + " ms. (avg = " + avg + " ms)");
    }

    private static void executeQueries(RuntimeService runtimeService, TaskService taskService, String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (processInstance == null) {
            throw new RuntimeException("Null process instance for id " + processInstanceId);
        }

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstanceId).list();
        if (executions.size() != 6) {
            throw new RuntimeException("Wrong number of executions, found  " + executions.size());
        }

        long count = runtimeService.createExecutionQuery().processInstanceId(processInstanceId).count();
        if (count != 6) {
            throw new RuntimeException("Wrong number of taskCount : " + count);
        }

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        if (tasks.size() != 2) {
            throw new RuntimeException("Wrong number of tasks : " + tasks.size());
        }

        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        if (variables.size() != 31) {
            throw new RuntimeException("Wrong number of variables " + variables.size());
        }
    }

    private static Map<String, Object> generateRandomStartVariables(int nrOfVariables) {
        Map<String, Object> variables = new HashMap<>();
        for (int i = 0; i < nrOfVariables; i++) {
            if (random.nextBoolean()) {
                variables.put("stringVariable_" + i, i + "");
            } else {
                variables.put("intVariable_" + i, i);
            }
        }
        return variables;
    }

}
