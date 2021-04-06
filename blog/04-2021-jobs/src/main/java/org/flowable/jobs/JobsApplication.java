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
package org.flowable.jobs;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.flowable.engine.ManagementService;
import org.flowable.engine.ProcessEngine;
import org.flowable.job.service.impl.asyncexecutor.AbstractAsyncExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JobsApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobsApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ProcessEngine processEngine,
            @Value("${mode}")String mode,
            @Value("${generate.number-of-jobs:1000000}") int numberOfJobs,
            @Value("${generate.threads:128}") int numberOfGenerationThreads,
            @Value("${generate.process:asyncNNoop}") String processType,
            @Value("${jobPageSize:100}")int jobPageSize,
            @Value("${globalLock:false}") boolean globalLock,
            @Value("${lockPollRate:500}") long lockPollRate,
            @Value("${timerLockPollRate:500}") long timerLockPollRate) {

        if ("generate".equalsIgnoreCase(mode)) {
            return generateJobsCommandLineRunner(processEngine, numberOfJobs, numberOfGenerationThreads, processType);

        } else if ("execute".equalsIgnoreCase(mode)) {
            return executeCommandLineRunner(processEngine, jobPageSize, globalLock, lockPollRate, timerLockPollRate);

        } else if ("monitor".equalsIgnoreCase(mode)) {
            return monitorCommandLineRunner(processEngine);

        }
        else {
            throw new RuntimeException("Unknown mode: " + mode);

        }
    }

    private CommandLineRunner generateJobsCommandLineRunner(ProcessEngine processEngine, int numberOfJobs, int numberOfGenerationThreads, String processType) {
        return args -> {

            ExecutorService executorService = Executors.newFixedThreadPool(numberOfGenerationThreads);
            for (int i = 0; i < numberOfJobs; i++) {
                executorService.submit(() -> {
                    if ("asyncNoop".equalsIgnoreCase(processType)) {
                        processEngine.getRuntimeService().startProcessInstanceByKey("asyncNoop");

                    } else if ("asyncWait".equalsIgnoreCase(processType)) {
                        processEngine.getRuntimeService().startProcessInstanceByKey("asyncWait");

                    } else if ("timer".equalsIgnoreCase(processType)) {
                        Date date = new Date();
                        processEngine.getRuntimeService().createProcessInstanceBuilder()
                            .processDefinitionKey("timerProcess")
                            .variable("timerDate", date)
                            .start();

                    } else {
                        throw new RuntimeException("Invalid process type: " + processType);
                    }

                });
            }

            System.out.println("Submitted " + numberOfJobs + " process instance starts of type " + processType + ". Waiting until all are completed.");

            executorService.shutdown();
            executorService.awaitTermination(2, TimeUnit.HOURS);

            System.out.println("DONE");


        };
    }

    private CommandLineRunner executeCommandLineRunner(ProcessEngine processEngine, int jobPageSize, boolean globalLock, long lockPollRate, long timerLockPollRate) {
        return args -> {

            System.out.println("Setting page size to " + jobPageSize);

            AbstractAsyncExecutor asyncExecutor = (AbstractAsyncExecutor) processEngine.getProcessEngineConfiguration().getAsyncExecutor();

            asyncExecutor.setMoveTimerExecutorPoolSize(32);

            asyncExecutor.setMaxAsyncJobsDuePerAcquisition(jobPageSize);
            asyncExecutor.setMaxTimerJobsPerAcquisition(jobPageSize);

            System.out.println("GLOBAL LOCK ENABLED = " + globalLock);
            asyncExecutor.setGlobalAcquireLockEnabled(globalLock);

            System.out.println("Setting LOCK POLL RATE:" + lockPollRate);
            asyncExecutor.setAsyncJobsGlobalLockPollRate(Duration.ofMillis(lockPollRate));

            System.out.println("Setting TIMER LOCK POLL RATE:" + timerLockPollRate);
            asyncExecutor.setTimerLockPollRate(Duration.ofMillis(timerLockPollRate));

            processEngine.getProcessEngineConfiguration().getAsyncExecutor().start();

        };
    }

    private CommandLineRunner monitorCommandLineRunner(ProcessEngine processEngine) {
        return args -> {

            System.out.println("Monitoring jobs ...");

            ManagementService managementService = processEngine.getManagementService();

            long initialJobCount = managementService.createJobQuery().count();
            long initialTimerCount = managementService.createTimerJobQuery().count();

            long previousJobCount = initialJobCount;
            long previousTimerCount = initialTimerCount;

            Date start = new Date();
            while (true) {
                Thread.sleep(60000);

                Date now = new Date();

                long jobCount = managementService.createJobQuery().count();
                long timerCount = managementService.createTimerJobQuery().count();

                double lastMinuteJobThroughput = ((double) (previousJobCount - jobCount)) / 60.0;
                double lastMinuteTimerThroughput = ((double) (previousTimerCount - timerCount)) / 60.0;

                double secondsSinceStart = (now.getTime() - start.getTime()) / 1000.0;
                double jobThroughputSinceStart = ((double) (initialJobCount - jobCount)) / secondsSinceStart;
                double timerThroughputSinceStart = ((double) (initialTimerCount - timerCount)) / secondsSinceStart;

                System.out.println();
                System.out.println("-----------------------");
                System.out.println("Time: " + new Date());
                System.out.println("Number of remaining jobs: " + jobCount);
                System.out.println("Job throughput last minute: " + lastMinuteJobThroughput);
                System.out.println("Job throughput since start: " + jobThroughputSinceStart);
                System.out.println();
                System.out.println("Number of remaining timers: " + timerCount);
                System.out.println("Timer throughput last minute: " + lastMinuteTimerThroughput);
                System.out.println("Timer throughput since start: " + timerThroughputSinceStart);
                System.out.println("-----------------------");
                System.out.println();

                previousJobCount = jobCount;
                previousTimerCount = timerCount;
            }

        };
    }

}
