package com.jinloes.activiti_test;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.stereotype.Component;

/**
 * Created by jinloes on 4/14/15.
 */
@Component
public class MyBean {

    public void execute(ExecutionEntity execution) {
        System.out.println("I'm a delegate!");
        MyTaskService.MigrationDocument document = (MyTaskService.MigrationDocument)
                execution.getVariable("migration_document");
        if (document != null) {
            System.out.println("migration name: " + document.getName());
        }
    }
}
