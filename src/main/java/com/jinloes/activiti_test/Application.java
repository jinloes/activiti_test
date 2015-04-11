package com.jinloes.activiti_test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.ManagementSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Created by jinloes on 4/9/15.
 */
@SpringBootApplication(exclude =
        {SecurityAutoConfiguration.class, ManagementSecurityAutoConfiguration.class})
public class Application {
    @Autowired
    private MyTaskService myTaskService;

    @Autowired
    private PersonRepository personRepository;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner init(final RepositoryService repositoryService,
            final RuntimeService runtimeService, final TaskService taskService) {

        return new CommandLineRunner() {
            @Override
            public void run(String... strings) throws Exception {
                myTaskService.createDemoUsers();
                System.out.println("Number of process definitions : "
                        + repositoryService.createProcessDefinitionQuery().count());
                System.out.println("Number of tasks : " + taskService.createTaskQuery().count());
                Person person = new Person("jbarrez", "Joram", "Barrez", new Date());
                personRepository.save(person);
                System.out.println("Person uuid: " + person.getId());
                Map<String, Object> variables = new HashMap<String, Object>();
                variables.put("person", person);
                runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
                System.out.println("Number of tasks after process start: " + taskService
                        .createTaskQuery().count());
            }
        };

    }
}
