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
package org.flowable.intro.test;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricVariableInstance;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.Test;

public class DmnTest {

  @Test
  public void testDmnProcess() {
    // Create Flowable engine
    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) 
        ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("flowable.dmn.cfg.xml");

    ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();
    
    // Get main service interfaces
    RepositoryService repositoryService = processEngine.getRepositoryService();
    RuntimeService runtimeService = processEngine.getRuntimeService();
    HistoryService historyService = processEngine.getHistoryService();
    
    // Deploy intro process definition
    repositoryService.createDeployment().name("dmn")
        .addClasspathResource("dmn.bpmn20.xml")
        .addClasspathResource("intro.dmn")
        .deploy();
    
    // Start intro process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dmn", Collections.singletonMap("name", (Object) "Flowable"));
    List<HistoricVariableInstance> historicVariables = historyService
        .createHistoricVariableInstanceQuery()
        .processInstanceId(processInstance.getId())
        .orderByVariableName()
        .asc()
        .list();
    
    assertEquals("name", historicVariables.get(0).getVariableName());
    assertEquals("Flowable", historicVariables.get(0).getValue());
    assertEquals("resultVariable", historicVariables.get(1).getVariableName());
    assertEquals("is cool", historicVariables.get(1).getValue());
    
    processInstance = runtimeService.startProcessInstanceByKey("dmn", Collections.singletonMap("name", (Object) "Another OSS project"));
    historicVariables = historyService
        .createHistoricVariableInstanceQuery()
        .processInstanceId(processInstance.getId())
        .orderByVariableName()
        .asc()
        .list();
    
    assertEquals("name", historicVariables.get(0).getVariableName());
    assertEquals("Another OSS project", historicVariables.get(0).getValue());
    assertEquals("resultVariable", historicVariables.get(1).getVariableName());
    assertEquals("really?", historicVariables.get(1).getValue());
    
    processEngine.close();
  }
}
