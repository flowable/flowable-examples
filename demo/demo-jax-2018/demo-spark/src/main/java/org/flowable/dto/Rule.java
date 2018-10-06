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
package org.flowable.dto;

import java.util.LinkedList;

/**
 * @author Joram Barrez
 */
public class Rule {

    private LinkedList<Condition> conditions = new LinkedList<>();
    private Object outcome;
    private double probability;

    public Rule() {
    }

    public void addConditionAsFirst(Condition condition) {
        conditions.addFirst(condition);
    }

    public LinkedList<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(LinkedList<Condition> conditions) {
        this.conditions = conditions;
    }

    public Object getOutcome() {
        return outcome;
    }

    public void setOutcome(Object outcome) {
        this.outcome = outcome;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < conditions.size(); i++) {
            stringBuilder.append(conditions.get(i).toString());
            if (i != conditions.size() - 1) {
                stringBuilder.append(" AND ");
            }
        }
        stringBuilder.append(" -> ");
        stringBuilder.append(outcome);
        stringBuilder.append(" (with probablity ");
        stringBuilder.append(String.format("%.2f", probability * 100) + "%");
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

}