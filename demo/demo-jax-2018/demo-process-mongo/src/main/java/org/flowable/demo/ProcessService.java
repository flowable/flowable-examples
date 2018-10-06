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
package org.flowable.demo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipInputStream;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ProcessService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessService.class);
    
    private static final List<String> COUNTRIES 
        = Arrays.asList("Belgian", "Dutch", "UK", "German", "French", "Italian", "Spanish", "Other");
    
    private static Random random = new Random();
    
    private ProcessEngine processEngine;
    
    @Autowired
    public ProcessService(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    @Async
    public void execute(int nrOfInstances) {
        
        LOGGER.info("Starting " + nrOfInstances + " process instances");
        deployProcessAppIfNeeded();
        
        for (int i = 0; i < nrOfInstances; i++) {
            processEngine.getRuntimeService().startProcessInstanceByKey("loan");
            randomlyCompleteTasks(random);
        }
        
        LOGGER.info("Started " + nrOfInstances + " process instances");
    }
    
    private void deployProcessAppIfNeeded() {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        long count = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("loan")
                .count();
        if (count == 0) {
            processEngine.getRepositoryService().createDeployment()
                .addClasspathResource("demo-app/loan.bpmn")
                .addClasspathResource("demo-app/form-advreview.form")
                .addClasspathResource("demo-app/form-loanin.form")
                .addClasspathResource("demo-app/form-loanreview.form")
                .addClasspathResource("demo-app/form-name.form")
                .deploy();
        }
    }

    private void randomlyCompleteTasks(Random random) {
        List<Task> tasks = processEngine.getTaskService().createTaskQuery().list();
        if (!tasks.isEmpty()) {
            int nrOfTasks = random.nextInt(tasks.size());
            for (int i=0; i<nrOfTasks; i++) {
                
                Task task = tasks.get(i);
                Map<String, Object> vars = new HashMap<>();
                
                if ("Capture application details".equals(task.getName())) {
                    vars.put("income", 10000);
                    vars.put("loan", 50000);
                    vars.put("age", random.nextInt(60));
                    vars.put("nationality", COUNTRIES.get(random.nextInt(3)));
                    vars.put("fullname", "John Doe " + random.nextInt(1000));
                    vars.put("home", random.nextBoolean() ? "Owned" : "Rented");
                    
                } else if("Loan Review".equals(task.getName())) {
                    String countryValue = processEngine.getTaskService().getVariable(task.getId(), "nationality").toString();
                    Integer ageValue = (Integer) processEngine.getTaskService().getVariable(task.getId(), "age");
                    if ("UK".equals(countryValue)) {
                        vars.put("form_loanreview_outcome", "Accept");
                    } else if (ageValue <= 30) {
                        vars.put("form_loanreview_outcome", "Reject");
                    } else {
                        vars.put("form_loanreview_outcome", "Consider");
                    }
                    
                } else if ("Advanced Loan Review".equals(task.getName())) {
                    Integer ageValue = (Integer) processEngine.getTaskService().getVariable(task.getId(), "age");
                    if (ageValue > 50) {
                        vars.put("form_advreview_outcome", "Reject");
                    } else {
                        vars.put("form_advreview_outcome", "Accept");
                    }
                    
                }
                
                processEngine.getTaskService().complete(task.getId(), vars);
            }
        }
    }

}
