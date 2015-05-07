package com.jinloes.activiti_test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.engine.impl.rules.RulesDeployer;
import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.JpaProcessEngineAutoConfiguration;
import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.ManagementSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Created by jinloes on 4/9/15.
 */
@SpringBootApplication(exclude =
        {SecurityAutoConfiguration.class, ManagementSecurityAutoConfiguration.class})
public class Application extends JpaProcessEngineAutoConfiguration.JpaConfiguration {
    public static Person person;
    @Autowired
    private MyTaskService myTaskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void setUp() {
        myTaskService.createDemoUsers();
        repositoryService.createDeployment().addClasspathResource("processes/test.drl")
                .addClasspathResource("processes/one-task-process.bpmn20.xml").deploy();
        // Create process at run time
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

        // Bootstrap and testing
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
        variables.put("test", new Test(true));
        identityService.setAuthenticatedUserId(Application.person.getId().toString());
        runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
        System.out.println("Number of tasks after process start: " + taskService
                .createTaskQuery().count());
    }

    @Bean
    @Override
    public SpringProcessEngineConfiguration springProcessEngineConfiguration(
            DataSource dataSource, EntityManagerFactory entityManagerFactory,
            PlatformTransactionManager transactionManager, SpringAsyncExecutor springAsyncExecutor)
            throws IOException {
        SpringProcessEngineConfiguration config = super.springProcessEngineConfiguration(
                dataSource, entityManagerFactory, transactionManager, springAsyncExecutor);
        List<Deployer> deployers = new ArrayList<>();
        deployers.add(new RulesDeployer());
        config.setCustomPostDeployers(deployers);
        return config;
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
}
