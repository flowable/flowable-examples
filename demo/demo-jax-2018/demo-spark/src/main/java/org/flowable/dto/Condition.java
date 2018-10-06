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

import java.util.Iterator;

/**
 * @author Joram Barrez
 */
public class Condition {

    public static enum ConditionOperation {
        IN, NOT_IN, LTE, GT
    };

    private String variableName;
    private ConditionOperation operation;
    private Object rightValue;

    public Condition(String variableName, ConditionOperation operation, Object rightValue) {
        this.variableName = variableName;
        this.operation = operation;
        this.rightValue = rightValue;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public ConditionOperation getOperation() {
        return operation;
    }

    public void setOperation(ConditionOperation operation) {
        this.operation = operation;
    }

    public Object getRightValue() {
        return rightValue;
    }

    public void setRightValue(Object rightValue) {
        this.rightValue = rightValue;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(variableName);

        if (operation.equals(ConditionOperation.IN)) {
            stringBuilder.append(" in ");
        } else if (operation.equals(ConditionOperation.NOT_IN)) {
            stringBuilder.append(" not in ");
        } else if (operation.equals(ConditionOperation.LTE)) {
            stringBuilder.append(" <= ");
        } else if (operation.equals(ConditionOperation.GT)) {
            stringBuilder.append(" > ");
        }

        if (rightValue instanceof Iterable) {
            Iterator<Object> iterator = ((Iterable) rightValue).iterator();
            stringBuilder.append("[");
            while (iterator.hasNext()) {
                stringBuilder.append(iterator.next());
                stringBuilder.append(", ");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            stringBuilder.append("]");
        } else {
            stringBuilder.append(rightValue);
        }

        return stringBuilder.toString();
    }

}