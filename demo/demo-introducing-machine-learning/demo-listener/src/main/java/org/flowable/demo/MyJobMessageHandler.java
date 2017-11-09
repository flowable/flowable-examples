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

import java.io.IOException;

import org.elasticsearch.action.support.WriteRequest.RefreshPolicy;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;
import org.flowable.engine.impl.history.async.HistoryJsonConstants;
import org.flowable.engine.impl.history.async.message.AsyncHistoryJobMessageHandler;
import org.flowable.job.service.impl.persistence.entity.HistoryJobEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@link AsyncHistoryJobMessageHandler} that filters on variable creation and sends the data to Elasticsearch.
 * 
 * @author Joram Barrez
 */
public class MyJobMessageHandler implements AsyncHistoryJobMessageHandler {
    
    private Client client;
    private ObjectMapper objectMapper = new ObjectMapper();
    
    public MyJobMessageHandler(Client client) {
        this.client = client;
    }

    @Override
    public boolean handleJob(HistoryJobEntity historyJobEntity, JsonNode historyData) {
        
        byte[] historicalData = null;
        
        // Filter on variable history only
        try {
            JsonNode historyJsonNode = objectMapper
                    .readTree(historyJobEntity.getAdvancedJobHandlerConfigurationByteArrayRef().getBytes());
            if (historyJsonNode != null 
                    && historyJsonNode.has("type") 
                    && HistoryJsonConstants.TYPE_VARIABLE_CREATED.equals(historyJsonNode.get("type").asText())) {
                historicalData = objectMapper.writeValueAsBytes(historyJsonNode.get("data"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Send to ES
        if (historicalData != null) {
            client.prepareIndex("flowable", "variables")
                .setSource(historicalData, XContentType.JSON)
                .setRefreshPolicy(RefreshPolicy.IMMEDIATE) // for demo purpose
                .get();
        }
        
        return true;
    }

}
