package com.jinloes.activiti_test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.apache.commons.lang3.RandomStringUtils;
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
    public static Person person;
    @Autowired
    private MyTaskService myTaskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private PersonRepository personRepository;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void setUp() {
        myTaskService.createDemoUsers();
        BpmnModel model = new BpmnModel();
        Process process = new Process();
        model.addProcess(process);
        process.setId("preflightProcess");

        process.addFlowElement(createStartEvent());
        process.addFlowElement(createSequenceFlow("start", "end"));

        process.addFlowElement(createEndEvent());

        repositoryService.createDeployment().addBpmnModel("dynamic-model.bpmn", model)
                .name("Dynamic process deployment")
                .deploy();
    }

    protected SequenceFlow createSequenceFlow(String from, String to) {
        SequenceFlow flow = new SequenceFlow();
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        return flow;
    }

    protected StartEvent createStartEvent() {
        StartEvent startEvent = new StartEvent();
        startEvent.setId("start");
        return startEvent;
    }

    protected EndEvent createEndEvent() {
        EndEvent endEvent = new EndEvent();
        endEvent.setId("end");
        return endEvent;
    }

    @Bean
    public CommandLineRunner init(final RepositoryService repositoryService,
            final RuntimeService runtimeService, final TaskService taskService,
            final ProcessEngineConfiguration processEngineConfiguration) {
        return new CommandLineRunner() {
            @Override
            public void run(String... strings) throws Exception {
                processEngineConfiguration.setHistoryLevel(HistoryLevel.FULL);
                System.out.println("Number of process definitions : "
                        + repositoryService.createProcessDefinitionQuery().count());
                System.out.println("Number of tasks : " + taskService.createTaskQuery().count());
                person = new Person("jbarrez", "Joram", "Barrez", new Date());
                personRepository.save(person);
                System.out.println("Person uuid: " + person.getId());
                Map<String, Object> variables = new HashMap<>();
                variables.put("person", person);
                variables.put("var1", "test");
                variables.put("name", RandomStringUtils.randomAlphanumeric(6));
                variables.put("migration_document", new MyTaskService.MigrationDocument(
                        "bootstrap doc", "vcloud"));
                identityService.setAuthenticatedUserId(Application.person.getId().toString());
                runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
                System.out.println("Number of tasks after process start: " + taskService
                        .createTaskQuery().count());
            }
        };
    }
}
