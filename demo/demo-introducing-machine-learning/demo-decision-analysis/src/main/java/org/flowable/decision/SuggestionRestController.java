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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * (Very) Quick-n-Dirty controller for exposing the results of the decision analysis.
 */
@RestController
public class SuggestionRestController {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @GetMapping("/suggestions/{taskKey}")
    public String getSuggestions(@PathVariable String taskKey) {
        try {
            return jdbcTemplate.queryForObject("SELECT RULES_ from RULES where TASK_KEY_ = ? order by TIME_STAMP_ desc LIMIT 1", new String[] { taskKey}, String.class);
        } catch (IncorrectResultSizeDataAccessException ire) {
            // ignore
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
}
