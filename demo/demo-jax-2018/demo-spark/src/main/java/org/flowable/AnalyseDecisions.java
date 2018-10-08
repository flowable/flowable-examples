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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.DecisionTree;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.apache.spark.sql.SparkSession;
import org.flowable.dto.Rule;
import org.flowable.helper.RuleHelper;

import com.mongodb.spark.MongoSpark;

import scala.Tuple2;

/**
 * Spark application that determines automatic rules based on human task + forms.
 * Triggered by the Decision Analysis Service. 
 * 
 * @author Joram Barrez
 */
public class AnalyseDecisions {

    public static void main(String[] args) {
        
        // Parse arguments : general process/task data + variables + categorical values

        final String[] arguments = args[0].split("#");
        final String processDefinitionId = arguments[0];
        final String taskKey = arguments[1];
        final String outcomeVariable = arguments[2];
        final List<String> outcomes = Arrays.asList(arguments[3].split(";"));
        final List<String> variables = Arrays.asList(arguments[4].split(";"));
        final List<List<String>> variablePossibleValues = new ArrayList<>();
        for (String variableValueString : arguments[5].split(";")) {
            List<String> values = new ArrayList<>();
            variablePossibleValues.add(values);
            if (StringUtils.isNotBlank(variableValueString)) {
                for (String value : variableValueString.split("&")) {
                    values.add(value);
                }
            }
        }

        System.out.println();
        System.out.println("---------------------------");
        System.out.println("Parameters:");
        System.out.println("ProcessDefinitionId: " + processDefinitionId);
        System.out.println("TaskKey: " + taskKey);
        System.out.println("OutcomeVariable: " + outcomeVariable);
        System.out.println("Potential outcomes: ");
        outcomes.forEach(outcome -> System.out.println("  " + outcome));
        System.out.println("---------------------------");
        System.out.println("Variables: ");
        for (int i=0; i<variables.size(); i++) {
            System.out.println(variables.get(i));
            if (variablePossibleValues.get(i).isEmpty()) {
                System.out.println("  -> possible values : infinite");
            } else {
                variablePossibleValues.get(i).forEach(value -> System.out.println("  -> possible value:" + value));
            }
        }
        System.out.println("---------------------------");
        System.out.println();
        
        
        /*
         * Lookup maps.
         * Created once, immutable. Will be passed as-is to all Spark workers.
         * The index of each feature (variable) in the vector given to the decision tree algorithm
         */
        
        final Map<String, Integer> featureIndices = new HashMap<>(); // variableName -> index in vector
        final Map<String, Map<String, Double>> categoricalValues = new HashMap<>(); // variableName -> {variableValue;categorical_value_for_vector}
        for (int i=0; i<variables.size(); i++) {
            String variable = variables.get(i);
            featureIndices.put(variable, i);
            
            List<String> possibleValues = variablePossibleValues.get(i);
            if (!possibleValues.isEmpty()) {
                Map<String, Double> vectorValues = new HashMap<>();
                categoricalValues.put(variable, vectorValues);
                
                double d = 0.0;
                for (String possibleValue : possibleValues) {
                    vectorValues.put(possibleValue, d++);
                }
            }
        }
        
        inferDecisionTree(processDefinitionId, taskKey, outcomeVariable, outcomes, featureIndices, categoricalValues);
    }

    private static void inferDecisionTree(final String processDefinitionId, 
            final String taskKey,
            final String outcomeVariable, 
            final List<String> outcomes, 
            final Map<String, Integer> featureIndices,
            final Map<String, Map<String, Double>> categoricalValues) {

        SparkSession spark = SparkSession.builder()
            .master("local")
            .appName("Demo")
            .config("spark.mongodb.input.uri", "mongodb://127.0.0.1:27017/flowable.historicVariableInstances")
            .config("spark.mongodb.output.uri", "mongodb://127.0.0.1/flowable.historicVariableInstances")
            .getOrCreate();

        JavaSparkContext javaSparkContext = new JavaSparkContext(spark.sparkContext());

        JavaRDD<LabeledPoint> data = MongoSpark.load(javaSparkContext) // starts as a JavaMongoRDD<Document>
                 // to JavaPairRDD<String, Iterable<Document>>
                .groupBy(historicVariableData -> (String) historicVariableData.get("processInstanceId")) 
                
                // to JavaRDD<Iterable<Document>
                .values()
                .filter(iterable -> {
                    boolean hasOutcomeVariable = false;
                    for (Map<String, Object> dataMap : iterable) {
                        if (dataMap.containsKey("name") && dataMap.get("name").equals(outcomeVariable)) {
                            hasOutcomeVariable = true;
                        }
                    };
                    return hasOutcomeVariable;
                })

                // Map it to a LabeledPoint
                .map(iterable -> {
                    
                    double labelValue = -1.0;
                    double[] values = new double[featureIndices.size()];
                    for (Map<String, Object> historyDataMap : iterable) {
                        String variableName = historyDataMap.get("name").toString();

                        String type = "";
                        if (historyDataMap.containsKey("typeName")) {
                            type = historyDataMap.get("typeName").toString();
                        }

                        if (variableName.equals(outcomeVariable) || featureIndices.containsKey(variableName)) {
                            String variableValue = historyDataMap.get("textValue").toString();
                            double value = -1.0;
                            if ("integer".equals(type)) {
                                value = Double.parseDouble(variableValue);
                            } else if ("string".equals(type)) {
                                if (categoricalValues.containsKey(variableName)) {
                                    value = categoricalValues.get(variableName).get(variableValue);
                                } else {
                                    value = 0;
                                }
                            }
                            
                            if (outcomeVariable.equals(variableName)) {
                                labelValue = (double) outcomes.indexOf(variableValue);
                            } else { 
                                values[featureIndices.get(variableName)] = value;
                            }
                        }
                    }
                    return new LabeledPoint(labelValue, Vectors.dense(values));
                });

        // Split the data into training and test sets (20% held out for testing)
        JavaRDD<LabeledPoint>[] splits = data.randomSplit(new double[] { 0.7, 0.3 });
        JavaRDD<LabeledPoint> trainingData = splits[0];
        JavaRDD<LabeledPoint> testData = splits[1];

        Map<Integer, Integer> categoricalFeaturesInfo = new HashMap<>();
        for (String variable : featureIndices.keySet()) {
            Integer index = featureIndices.get(variable);
            if (categoricalValues.containsKey(variable)) {
                categoricalFeaturesInfo.put(index, categoricalValues.get(variable).size());
            }
        }

        String impurityAlgorithm = "gini";
        int maxDepth = 2; // max depth of tree (to make it easier for demo purposes)
        int maxBins = 32; // max number of different values before considered continuous

        DecisionTreeModel model = DecisionTree.trainClassifier(trainingData, outcomes.size(), 
                categoricalFeaturesInfo, impurityAlgorithm, maxDepth, maxBins);

        // Evaluate model on test instances and compute test error
        JavaPairRDD<Double, Double> predictionAndLabel = testData.mapToPair(p -> new Tuple2<>(model.predict(p.features()), p.label()));
        double testErr = predictionAndLabel.filter(pl -> !pl._1().equals(pl._2())).count() / (double) testData.count();

        System.out.println("Test Error: " + testErr);
        System.out.println("Learned classification tree model:\n" + model.toDebugString());

        // Save and load model
//        model.save(javaSparkContext.sc(), "target/tmp/myDecisionTreeClassificationModel");
//        DecisionTreeModel sameModel = DecisionTreeModel.load(javaSparkContext.sc(),"target/tmp/myDecisionTreeClassificationModel");
        
        List<Rule> rules = RuleHelper.determineLearnedRules(model, featureIndices, outcomeVariable, categoricalValues, outcomes);
        
        System.out.println("Learned rules:");
        rules.forEach(rule -> System.out.println(rule));
        System.out.println();
        System.out.println();
        
        System.out.println("Simplified rules: ");
        RuleHelper.simplifyRules(rules);
        rules.forEach(rule -> System.out.println(rule));
        System.out.println();
//        RuleHelper.storeRules(rules, processDefinitionId, taskKey);
    }

}
