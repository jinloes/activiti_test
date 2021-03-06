package com.jinloes.activiti_test;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jinloes on 4/10/15.
 */
@Service
public class MyTaskService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private PersonRepository personRepository;

    @Transactional
    public void startProcess(UUID assignee, String migrationTargetType) {
        Person person = personRepository.getOne(assignee);
        Map<String, Object> variables = new HashMap<>();
        variables.put("person", person);
        variables.put("var1", "test");
        variables.put("name", RandomStringUtils.randomAlphanumeric(6));
        variables.put("migration_document", new MigrationDocument(
                RandomStringUtils.randomAlphanumeric(6), migrationTargetType));
        identityService.setAuthenticatedUserId(Application.person.getId().toString());
        runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    }

    @Transactional
    public List<Task> getTasks(String assignee) {
        if (StringUtils.isEmpty(assignee)) {
            return taskService.createTaskQuery().list();
        } else {
            return taskService.createTaskQuery().taskAssignee(assignee).list();
        }
    }

    public void createDemoUsers() {
        if (personRepository.findAll().size() == 0) {
            personRepository.save(new Person("jbarrez", "Joram", "Barrez", new Date()));
            personRepository.save(new Person("trademakers", "Tijs", "Rademakers", new Date()));
        }
    }

    public Map<String, List<HistoricActivityInstance>> getProcceses(String userId,
            String processId) {
        Map<String, List<HistoricActivityInstance>> response = new HashMap<>();
        List<HistoricProcessInstance> processes =
                historyService.createHistoricProcessInstanceQuery()
                        .processDefinitionKey(processId)
                        .includeProcessVariables()
                        .list();
        for (HistoricProcessInstance processInstance : processes) {
            List<HistoricActivityInstance> activityInstances =
                    historyService.createHistoricActivityInstanceQuery()
                            .processInstanceId(processInstance.getId())
                            .orderByHistoricActivityInstanceStartTime()
                            .asc()
                            .list();
            Map<String, Object> variables = processInstance.getProcessVariables();
            response.put(MapUtils.getString(variables, "name"), activityInstances);
        }
        return response;
    }

    public void startPreflight(MyRestController.StartRequest request) {
        //Person person = personRepository.getOne(UUID.fromString(request.getAssignUser()));
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", RandomStringUtils.randomAlphanumeric(6));
        identityService.setAuthenticatedUserId(Application.person.getId().toString());
        runtimeService.startProcessInstanceByKey("preflightProcess2", variables);
    }

    public void startOsProcess(UUID assignee, String osType) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", RandomStringUtils.randomAlphanumeric(6));
        Map<String, Object> document = new HashMap<>();
        document.put("osType", osType);
        variables.put("document", document);
        identityService.setAuthenticatedUserId(Application.person.getId().toString());
        runtimeService.startProcessInstanceByKey("migrationFlow", variables);
    }

    public static class MigrationDocument implements Serializable {
        private static final long serialVersionUID = -786519957903736106L;

        private final String name;
        private final String type;

        public MigrationDocument(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }
}
