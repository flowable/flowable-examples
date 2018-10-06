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
package org.flowable.decision;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkAppHandle.State;
import org.apache.spark.launcher.SparkLauncher;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExclusiveGateway;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.form.model.OptionFormField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author Joram Barrez
 */
@Service
public class DecisionAnalysisService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionAnalysisService.class);
  
    private RepositoryService repositoryService;

    @Autowired
    public DecisionAnalysisService(ProcessEngine processEngine) {
        this.repositoryService = processEngine.getRepositoryService();
    }
    
    @Scheduled(initialDelay = 1000, fixedDelay = 30000)
    public void analyze() {

        LOGGER.info("Starting decision analysis ...");
        
        // Hardcoded process definition for demo, but obviouysly can be parameterized
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("loan").latestVersion().singleResult();
        if (processDefinition == null) {
            LOGGER.info("No process definition found. No analysis done.");
            return;
        }
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
        
        /*

            1) Look for pattern: user tasks with form + exclusive gateway + sequenceflows with form outcomes

            2) Calculate all paths from that user task to the start + gather the variables (form fields) along those paths
            
            3) Hand over to Spark ML

        */
        
        // Step (1)
        LOGGER.info("Analyzing process definition " + processDefinition.getId());
        List<UserTask> matchingUserTasks = findUserTasksMatchingPattern(bpmnModel);

        // For demo, only show 'Loan Review' task
        matchingUserTasks.removeIf(userTask -> "Loan Review".equals(userTask.getName()));
        
        
        // Step (2) 
        Map<String, List<String>> outcomesMap = new HashMap<>(); // userTask.id -> outcomes
        Map<String, Map<String, List<String>>> possibleValueCounts // userTask.id -> {variable, distinct_values_count}
            = determinePossibleVariablesForUserTasks(processDefinition, matchingUserTasks, outcomesMap);  
            
        // Step (3)
        submitSparkAppsForTasks(processDefinition, outcomesMap, matchingUserTasks, possibleValueCounts);
    }

    private List<UserTask> findUserTasksMatchingPattern(BpmnModel bpmnModel) {
        List<UserTask> userTasks = bpmnModel.getMainProcess().findFlowElementsOfType(UserTask.class, true);
        List<UserTask> matchingUserTasks = userTasks.stream().filter(userTask -> {
            
            if (userTask.getFormKey() != null
                    && userTask.getOutgoingFlows().size() == 1 
                    && userTask.getOutgoingFlows().get(0).getTargetFlowElement() instanceof ExclusiveGateway) {
            
                ExclusiveGateway gw = (ExclusiveGateway) userTask.getOutgoingFlows().get(0).getTargetFlowElement();
                
                // All outgoing flows must either have an outcome or be the default flow
                for (SequenceFlow flow : gw.getOutgoingFlows()) {
                    if (!isOutcomeExpression(flow.getConditionExpression(), userTask.getFormKey())
                            && !flow.getId().equals(gw.getDefaultFlow())) {
                        return false;
                    }
                };
                
                return true;
            }
            return false;
            
        }).collect(Collectors.toList());
        return matchingUserTasks;
    }
    
    private Map<String, Map<String, List<String>>> determinePossibleVariablesForUserTasks(ProcessDefinition processDefinition, 
            List<UserTask> matchingUserTasks,
            Map<String, List<String>> outcomesMap) {
        
        Map<String, Map<String, List<String>>> possibleValueCounts = new HashMap<>();
        for (UserTask matchingUserTask : matchingUserTasks) {
            
            LOGGER.info("Found matching pattern for user task " 
                    + matchingUserTask.getName() + ". Collecting variables...");
            
            Map<String, List<String>> taskVariableValueCount = new HashMap<>();
            possibleValueCounts.put(matchingUserTask.getId(), taskVariableValueCount);
            
//            FormDefinition formDefinition = formRepositoryService.createFormDefinitionQuery()
//                    .formDefinitionKey(matchingUserTask.getFormKey())
//                    .singleResult();
//            FormInfo formInfo = formRepositoryService.getFormModelById(formDefinition.getId());
//            FormModel formModel = formInfo.getFormModel();
            
            List<String> outcomes = new ArrayList<>();

            // Not yet supported on MongoDB
            outcomes.add("Reject");
            outcomes.add("Consider");
            outcomes.add("Accept");
            outcomesMap.put(matchingUserTask.getId(), outcomes);

//            for (FormOutcome formOutcome : ((SimpleFormModel) formModel).getOutcomes()) {
//                outcomes.add(formOutcome.getName());
//            }

            taskVariableValueCount.put("fullname", new ArrayList<>());
            taskVariableValueCount.put("loan", new ArrayList<>());
            taskVariableValueCount.put("home", Arrays.asList("Rented", "Mortgaged", "Owned"));
            taskVariableValueCount.put("age", new ArrayList<>());
            taskVariableValueCount.put("nationality", Arrays.asList("Belgian", "Dutch", "French", "German", "Italian", "Spanish", "UK", "other"));
            taskVariableValueCount.put("Income", new ArrayList<>());

//            for (UserTask userTaskBeforeTask : findUserTasksBefore(matchingUserTask)) {
//
////                formDefinition = formRepositoryService.createFormDefinitionQuery()
////                        .formDefinitionKey(userTaskBeforeTask.getFormKey())
////                        .singleResult();
////                formModel = formRepositoryService.getFormModelById(formDefinition.getId()).getFormModel();
//
//                for (FormField formField : ((SimpleFormModel) formModel).getFields()) {
//
//                    String variableName = formField.getId();
//                    if (!taskVariableValueCount.containsKey(variableName)) {
//                        taskVariableValueCount.put(variableName, new ArrayList<>());
//                    }
//                    List<String> variableValues = taskVariableValueCount.get(variableName);
//
//                    if (formField.getType().equals("radio-buttons") || formField.getType().equals("dropdown")) {
//                        OptionFormField optionFormField = (OptionFormField) formField;
//                        optionFormField.getOptions().forEach(option -> {
//                            if (!variableValues.contains(option.getName()) && !isEmptyOption(optionFormField, option.getName())) {
//                                variableValues.add(option.getName());
//                            }
//                        });
//
//                    }
//                }
//
//            }
            
            LOGGER.info("Found following variable preceding user task " + matchingUserTask.getName() + ":");
            taskVariableValueCount.keySet().forEach(var -> LOGGER.info(var));
        }
        return possibleValueCounts;
    }
    
    private void submitSparkAppsForTasks(ProcessDefinition processDefinition, Map<String, List<String>> outcomesMap,
            List<UserTask> matchingUserTasks, Map<String, Map<String, List<String>>> possibleValueCounts) {
        for (UserTask matchingUserTask : matchingUserTasks) {            
            LOGGER.info("Submitting Spark ML app for task " + matchingUserTask.getId() + "...");
            try {
                
                // Not so pretty: generating a long argument string to pass info to spark job. Should be handled with a persistent store really.
                
                /*
                 * Format (separated by # character):
                 * 
                 * - processDefinitionId
                 * - taskKey
                 * - outcome variable
                 * - outcome variable possibilities
                 * - variable names
                 * - variable possibilities
                 */
                
                StringBuilder argumentBuilder = new StringBuilder();
                argumentBuilder.append(processDefinition.getId()).append("#") // process definition id
                    .append(matchingUserTask.getId()).append("#") // task key
                    .append("form_" + matchingUserTask.getFormKey() + "_outcome").append("#"); // outcome variable
                
                List<String> outcomes = outcomesMap.get(matchingUserTask.getId());
                for (int i=0; i<outcomes.size(); i++) {
                    argumentBuilder.append(outcomes.get(i)); // outcome variable output possibilities
                    if (i != outcomes.size() - 1) {
                        argumentBuilder.append(";");
                    }
                }
                argumentBuilder.append("#");
                
                Map<String, List<String>> variableToPotentialValues = possibleValueCounts.get(matchingUserTask.getId());
                List<String> variableNames = new ArrayList<>(variableToPotentialValues.keySet());
                for (int i=0; i<variableNames.size(); i++) {
                    argumentBuilder.append(variableNames.get(i)); // variable names
                    if (i != variableNames.size() - 1) {
                        argumentBuilder.append(";");
                    }
                }
                argumentBuilder.append("#");
                for (int i=0; i<variableNames.size(); i++) {
                    List<String> possibleValues = variableToPotentialValues.get(variableNames.get(i));
                    for (int j=0; j<possibleValues.size(); j++) {
                        argumentBuilder.append(possibleValues.get(j)); // variable possibilities
                        if (j != possibleValues.size() - 1) {
                            argumentBuilder.append("&");
                        }
                    }
                    if (i != variableNames.size() - 1) {
                        argumentBuilder.append(";");
                    }
                }
                
                LOGGER.info("Arguments for Spark app: " + argumentBuilder.toString());
                
                SparkAppHandle sparkAppHandle = new SparkLauncher()
                        .setSparkHome(System.getProperty("sparkHome"))
                        .setAppResource(System.getProperty("appResource"))
                        .setMainClass("org.flowable.AnalyseDecisions")
                        .setMaster("local[4]")
//                        .setVerbose(true)
                        .addAppArgs(argumentBuilder.toString())
                        .redirectOutput(Redirect.INHERIT)
                        .startApplication(new SparkAppHandle.Listener() {
                            
                            @Override
                            public void stateChanged(SparkAppHandle handle) {
                                LOGGER.info(handle.getState() + " new  state");
                            }
                            
                            @Override
                            public void infoChanged(SparkAppHandle handle) {
                                LOGGER.info(handle.getState() + " new  state");
                            }
                        });
                
                // For demo: make sure the tasks are processed sequentially to not have the console output mixed for all tasks 
                while (!sparkAppHandle.getState().equals(State.FINISHED) && !sparkAppHandle.getState().equals(State.FAILED)) {
                    Thread.sleep(5000L);
                }
                
            } catch (IOException e) {
                LOGGER.error("Could not submit app to Spark", e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
        }
    }
    
    private boolean isOutcomeExpression(String expression, String formKey) {
        if (StringUtils.isNotBlank(expression)
                && expression.startsWith("${form_" + formKey + "_outcome") 
                && expression.endsWith("}")) {
            return true;
        }
        return false;
    }
    
    private boolean isEmptyOption(OptionFormField optionFormField, String value) {
        if (optionFormField.getHasEmptyValue() != null && optionFormField.getHasEmptyValue()) {
            Object defaultValue = optionFormField.getValue();
            if (defaultValue != null) {
                return defaultValue.equals(value);
            }
        }
        return false;
    }

    private List<UserTask> findUserTasksBefore(UserTask startUserTask) {
        List<UserTask> result = new ArrayList<>();

        Set<String> visitedFlowElements = new HashSet<>();
        LinkedList<FlowElement> elementsToVisit = new LinkedList<>();
        for (SequenceFlow flow : startUserTask.getIncomingFlows()) {
            elementsToVisit.add(flow.getSourceFlowElement());
        }

        while (!elementsToVisit.isEmpty()) {
            FlowElement flowElement = elementsToVisit.removeFirst();
            if (!visitedFlowElements.contains(flowElement.getId())) {

                if (flowElement instanceof FlowNode) {
                    
                    FlowNode flowNode = (FlowNode) flowElement;
                    if (!flowNode.getIncomingFlows().isEmpty()) {
                        for (SequenceFlow flow : flowNode.getIncomingFlows()) {
                            elementsToVisit.add(flow.getSourceFlowElement());
                        }
                    }

                    if (flowElement instanceof UserTask) {
                        UserTask userTask = (UserTask) flowElement;
                        if (userTask.getFormKey() != null) {
                            result.add(userTask);
                        }
                    }

                }
            }
            visitedFlowElements.add(flowElement.getId());
        }
        return result;
    }
    
}
