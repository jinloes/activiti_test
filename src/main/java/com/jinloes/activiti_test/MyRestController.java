package com.jinloes.activiti_test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.websocket.server.PathParam;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by jinloes on 4/10/15.
 */
@RestController
public class MyRestController {

    @Autowired
    private MyTaskService myService;

    @RequestMapping(value = "/process", method = RequestMethod.POST)
    public void startProcessInstance(@RequestBody StartProcessRepresentation representation) {
        myService.startProcess(representation.getAssignee(),
                representation.getMigrationTargetType());
    }

    @RequestMapping(value = "/os_process", method = RequestMethod.POST)
    public void startMigration(@RequestBody StartProcessRepresentation representation,
            @RequestParam("os_type") String osType) {
        myService.startOsProcess(representation.getAssignee(), osType);
    }

    @RequestMapping(value = "/preflight", method = RequestMethod.POST)
    public void startPreflight(@RequestBody StartRequest request) {
        myService.startPreflight(request);
    }

    public static class StartRequest {
        private final boolean warnUser;
        private String assignUser;

        @JsonCreator
        public StartRequest(@JsonProperty("warnUser") boolean warnUser,
                @JsonProperty("assignUser") String assignUser) {
            this.warnUser = warnUser;
            this.assignUser = assignUser;
        }

        public boolean isWarnUser() {
            return warnUser;
        }

        public String getAssignUser() {
            return assignUser;
        }
    }

    @RequestMapping(value = "/tasks", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TaskRepresentation> getTasks(@RequestParam(required = false) String assignee) {
        List<Task> tasks = myService.getTasks(assignee);
        List<TaskRepresentation> dtos = new ArrayList<TaskRepresentation>();
        for (Task task : tasks) {
            dtos.add(new TaskRepresentation(task.getId(), task.getName()));
        }
        return dtos;
    }

    @RequestMapping(value = "/users/{userId}/processes")
    public Map<String, List<HistoricActivityInstance>> getProcesses(@PathParam("userId") String userId,
            @RequestParam("process_id") String processId) {
        return myService.getProcceses(userId, processId);
    }


    static class TaskRepresentation {

        private String id;
        private String name;

        public TaskRepresentation(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    static class StartProcessRepresentation {

        private UUID assignee;
        private String migrationTargetType;

        public UUID getAssignee() {
            return assignee;
        }

        @JsonProperty("assignee")
        public void setAssignee(UUID assignee) {
            this.assignee = assignee;
        }

        public String getMigrationTargetType() {
            return migrationTargetType;
        }

        @JsonProperty("migrationTargetType")
        public void setMigrationTargetType(String migrationTargetType) {
            this.migrationTargetType = migrationTargetType;
        }
    }

}
