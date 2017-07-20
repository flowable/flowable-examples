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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.task.Task;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Example {
    
    /* Some hardcoded constants */
    
    private static final int nrOfProcessInstances = 100;
    
    private static final List<String> assignees = Arrays.asList("assigneeOne", "assigneeTwo", 
            "assigneeThree", "assigneeFour", "assigneeFive", "assigneeSix", "assigneeSeven", "assigneeEight", "assigneeNine");
    
    private static final Random random = new Random();
    
    private static ExecutorService executorService = Executors.newCachedThreadPool();

    
    public static void main(String[] args) {
        
        // Boot the Spring application context and retrieve the ProcessEngine
        ConfigurableApplicationContext applicationContext = new AnnotationConfigApplicationContext(Configuration.class);
        final ProcessEngine processEngine = applicationContext.getBean(ProcessEngine.class);
        
        // Deploy process definition
        deployProcessDefinition(processEngine);
        
        /*
         * We're mimicking a 'semi-real' system here. Processes are started concurrently, 
         * but always with some small wait time.
         * 
         * Same for handling the user tasks: there is one thread / assignee, and each assignee
         * has a certain random wait time between two subsequent task completes.
         * 
         * (Note that making the wait time lower makes the the chance on optimistic locking
         * exceptions higher (for the parallel gateways that join when two users concurrently 
         * complete a task. This can be solved by making the join async, but adding the async executor 
         * to this mix is out of scope for this example). 
         */
        startProcessInstances(processEngine);
        completeTasks(processEngine);
        
        // All Runnables submitted
        executorService.shutdown();
        try {
            executorService.awaitTermination(1L, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Wait until all process instances and tasks have been processed
        long historicProcessInstancesCount = processEngine.getHistoryService().createHistoricProcessInstanceQuery().finished().count(); 
        while (historicProcessInstancesCount != nrOfProcessInstances) {
            System.out.println("Not all process instances finished yet ... (" + historicProcessInstancesCount + "/" + nrOfProcessInstances + "))");
            System.out.println("Nr of completed tasks = " + processEngine.getHistoryService().createHistoricTaskInstanceQuery().count());
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            historicProcessInstancesCount = processEngine.getHistoryService().createHistoricProcessInstanceQuery().finished().count();
        }
        
        System.out.println("All Done");
        System.out.println(processEngine.getHistoryService().createHistoricProcessInstanceQuery().unfinished().count() + " finished process instances");
        System.out.println(processEngine.getHistoryService().createHistoricProcessInstanceQuery().finished().count() + " finished process instances");
        
        applicationContext.close();
    }

    private static void deployProcessDefinition(ProcessEngine processEngine) {
        processEngine.getRepositoryService().createDeployment().addClasspathResource("test-process.bpmn20.xml").deploy();
    }
    
    private static void startProcessInstance(final ProcessEngine processEngine) {
        Map<String, Object> startVars = new HashMap<String, Object>();
        startVars.put("initiator", assignees.get(0));
        for (int j = 1; j < assignees.size(); j++) {
            startVars.put("assignee0" + (j+1), assignees.get(j));
        }
        processEngine.getRuntimeService().startProcessInstanceByKey("testProcess", startVars);
        
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private static void startProcessInstances(final ProcessEngine processEngine) {
        executorService.execute(new Runnable() {
            public void run() {
                for (int i=0; i < nrOfProcessInstances; i++) {
                    startProcessInstance(processEngine);
                    if ((i+1) % 100 == 0) {
                        System.out.println("Started " + (i+1) + " process instances");
                    }
                }
            }
        });
    }
    
    private static void completeTasks(final ProcessEngine processEngine) {
        for (int i = 0; i < assignees.size(); i++) {
            final int index = i;
            executorService.execute(new Runnable() {
                public void run() {
                    String assignee = assignees.get(index);
                    int nrOfFinishedTasks = 0;
                    
                    while (nrOfFinishedTasks < nrOfProcessInstances) {
                        try {
                            
                            Thread.sleep(100 + random.nextInt(250));
                            
                            List<Task> tasks = processEngine.getTaskService().createTaskQuery().taskAssignee(assignee).orderByTaskCreateTime().asc().listPage(0, 1);
                            if (!tasks.isEmpty()) {
                                Map<String, Object> vars = new HashMap<String, Object>();
                                vars.put("SomeVar1" + assignee, random.nextDouble());
                                vars.put("SomeVar2" + assignee, random.nextInt());
                                vars.put("SomeVar3" + assignee, random.nextLong() + " ");
                                
                                processEngine.getTaskService().complete(tasks.get(0).getId(), vars);
                                nrOfFinishedTasks++;
                            }
                            
                        }  catch (Exception e) {
                            // Ignoring optimistic locking exceptions and simply retrying
                        }
                        
                        if (nrOfFinishedTasks != 0 && nrOfFinishedTasks % 10 == 0) {
                            System.out.println(assignee + " finished " + nrOfFinishedTasks + " tasks");
                        }
                        
                    }
                    
                    System.out.println(assignee + " has finished");
                }
            });
        }
    }

}
