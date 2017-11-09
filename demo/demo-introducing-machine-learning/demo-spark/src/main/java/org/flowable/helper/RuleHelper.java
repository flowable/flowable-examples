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
package org.flowable.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.spark.mllib.tree.configuration.FeatureType;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.apache.spark.mllib.tree.model.Node;
import org.apache.spark.mllib.tree.model.Split;
import org.flowable.dto.Condition;
import org.flowable.dto.Rule;
import org.flowable.dto.Condition.ConditionOperation;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Joram Barrez
 */
public class RuleHelper {

    public static void simplifyRules(List<Rule> rules) {
        for (int ruleIndex=0; ruleIndex<rules.size(); ruleIndex++) {
            Rule rule = rules.get(ruleIndex);
            LinkedList<Condition> conditions = rule.getConditions();
            for (int i=0; i<conditions.size(); i++) {
                Condition currentCondition = conditions.get(i);
                Iterator<Condition> iterator = conditions.iterator();
                
                // Move iterator to current condition
                for (int j=0; j<=i; j++) {
                    iterator.next();
                }
                
                while (iterator.hasNext()) {
                    Condition otherCondition = iterator.next();
                    if (currentCondition.getOperation().equals(Condition.ConditionOperation.IN)
                            && otherCondition.getOperation().equals(Condition.ConditionOperation.IN)) {
                        
                        // The second condition will make the first one more specific. 
                        // The first one takes the values of the second one, and the other one is removed
                        currentCondition.setRightValue(otherCondition.getRightValue());
                        iterator.remove();
                        
                    } 
                }
                
            }
        }
    }
    
    public static void storeRules(List<Rule> rules, String processDefinitionId, String taskKey) {
        
        // Quick and dirty store of the result
        
        Connection connection = null;
        try {
            
           Class.forName("com.mysql.jdbc.Driver"); 
           connection = DriverManager.getConnection("jdbc:mysql://localhost/demo?useSSL=false", "flowable", "flowable");
           
           Statement createTableStatement = connection.createStatement();
           createTableStatement.executeUpdate("create table if not exists RULES (PROC_DEF_ID_ varchar(255), TASK_KEY_ varchar(255), TIME_STAMP_ timestamp, RULES_ TEXT);");
           
           PreparedStatement insertStatement = connection.prepareStatement("insert into RULES values (?, ?, ?, ?);");
           insertStatement.setString(1, processDefinitionId);
           insertStatement.setString(2, taskKey);
           insertStatement.setTimestamp(3, new Timestamp(new java.util.Date().getTime()));
           insertStatement.setString(4, new ObjectMapper().writeValueAsString(rules));
           insertStatement.executeUpdate();
           
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
    }
    
    public static List<Rule> determineLearnedRules(final DecisionTreeModel model, 
            final Map<String, Integer> featureIndices, 
            final String outcomeVariable, 
            final Map<String, Map<String, Double>> categoricalVariablesToValues,  
            final List<String> outcomes) {
        
        List<Rule> rules = new ArrayList<>();
        
        LinkedList<Node> nodesToVisit = new LinkedList<>();
        nodesToVisit.add(model.topNode());
        while (!nodesToVisit.isEmpty()) {
            Node currentNode = nodesToVisit.removeFirst();
            if (currentNode.isLeaf()) {
                
                // Determine parent nodes
                List<Node> parentNodes = new ArrayList<>();
                int parentIndex = Node.parentIndex(currentNode.id()); 
                while (parentIndex != 0) {
                    Node parentNode = Node.getNode(parentIndex, model.topNode());
                    parentNodes.add(parentNode);
                    parentIndex = Node.parentIndex(parentNode.id());
                }
                
                // Determine conditions
                Rule rule = new Rule();
                for (int i=0; i<parentNodes.size(); i++) {
                    
                    Node parentNode = parentNodes.get(i);
                    Node previousNode = i == 0 ? currentNode : parentNodes.get(i - 1);
                    boolean isLeftChild = parentNode.leftNode().isDefined() && parentNode.leftNode().get().id() == previousNode.id();
                    
                    Condition condition = determineCondition(featureIndices, categoricalVariablesToValues,  parentNode, isLeftChild);
                    if (condition != null) {
                        rule.addConditionAsFirst(condition);
                    }
                };
                
                // Outcome
                rule.setOutcome(outcomes.get(((int) currentNode.predict().predict())));
                rule.setProbability(1.0 - currentNode.impurity());
                rules.add(rule);
                    
            } else {
                if (currentNode.rightNode().isDefined()) {
                    nodesToVisit.addFirst(currentNode.rightNode().get());
                }
                if (currentNode.leftNode().isDefined()) {
                    nodesToVisit.addFirst(currentNode.leftNode().get());
                }
                
            }
        }
        return rules;
    }

    public static Condition determineCondition(
            Map<String, Integer> featureIndices,
            Map<String, Map<String, Double>> categoricalVariablesToValues, 
            Node currentNode, boolean isLeftChild) {
        
        if (currentNode.split().isDefined()) {
            Split split = currentNode.split().get();
            
            String variable = null;
            for (String key : featureIndices.keySet()) {
                if (featureIndices.get(key).equals(split.feature())) {
                    variable = key;
                }
            }
            
            if (split.featureType().equals(FeatureType.Categorical())) {
                
                // Scala <-> Java ...
                List<Object> values = new ArrayList<>();
                scala.collection.Iterator<Object> iterator = split.categories().iterator();
                while (iterator.hasNext()) {
                    Object categoryValue = iterator.next();
                    if (categoricalVariablesToValues.containsKey(variable)) { // Swap with acutal value if it's categorical
                        for (Entry<String, Double> entry : categoricalVariablesToValues.get(variable).entrySet()) {
                            if (entry.getValue().equals(categoryValue)) {
                                categoryValue = entry.getKey();
                            }
                        }
                    }
                    values.add(categoryValue);
                }
                
                if (isLeftChild) {
                    return new Condition(variable, ConditionOperation.IN, values);
                } else {
                    return  new Condition(variable, ConditionOperation.NOT_IN, values);
                }
                
            } else {
                
                Object value = split.threshold();
                if (categoricalVariablesToValues.containsKey(variable)) {
                    for (Entry<String, Double> entry : categoricalVariablesToValues.get(variable).entrySet()) {
                        if (entry.getValue().equals(split.threshold())) {
                            value = entry.getKey();
                        }
                    }
                }
                
                if (isLeftChild) {
                    return new Condition(variable, ConditionOperation.LTE, value);
                } else {
                    return new Condition(variable, ConditionOperation.GT, value);
                }
                
            }
        }
        return null;
    }
    
}
