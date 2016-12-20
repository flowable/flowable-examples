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
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngines;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricVariableInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.Test;

public class IntroTest {

  @Test
  public void testIntroProcess() {
    // Create Flowable engine
    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    
    // Get main service interfaces
    RepositoryService repositoryService = processEngine.getRepositoryService();
    RuntimeService runtimeService = processEngine.getRuntimeService();
    HistoryService historyService = processEngine.getHistoryService();
    
    // Deploy intro process definition
    repositoryService.createDeployment().name("intro")
        .addClasspathResource("intro.bpmn20.xml")
        .deploy();
    
    // Start intro process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("intro");
    assertTrue(processInstance.isEnded());
    
    HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();
    assertEquals("variablePresent", historicVariable.getVariableName());
    assertEquals(false, historicVariable.getValue());
    
    processInstance = runtimeService.startProcessInstanceByKey("intro", Collections.singletonMap("intro", (Object) "a test intro value"));
    List<HistoricVariableInstance> historicVariables = historyService
        .createHistoricVariableInstanceQuery()
        .processInstanceId(processInstance.getId())
        .orderByVariableName()
        .asc()
        .list();
    
    assertEquals("intro", historicVariables.get(0).getVariableName());
    assertEquals("a test intro value", historicVariables.get(0).getValue());
    assertEquals("variablePresent", historicVariables.get(1).getVariableName());
    assertEquals(true, historicVariables.get(1).getValue());
    
    processEngine.close();
  }
}
