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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

import de.codecentric.boot.admin.config.EnableAdminServer;

/**
 * Having the spring-boot-admin-xyz dependency in the pom.xml 
 * is enough to have the server and ui being booted up automatically.
 * 
 * The UI is available on http://localhost:8080/
 * 
 * @author Joram Barrez 
 */
@SpringBootApplication
@Configuration
@EnableAutoConfiguration
@EnableAdminServer
public class AdminApplication {
    
    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(AdminApplication.class, args);
    }

}
