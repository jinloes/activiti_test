package com.jinloes.activiti_test;

import java.util.List;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * Created by jinloes on 5/6/15.
 */
@Component
public class BeforeProcess {
    public void execute(ExecutionEntity executionEntity) {
        System.out.println("Before Process");
        List<String> executables = (List<String>) executionEntity.getVariable("before_executables");

        if (CollectionUtils.isNotEmpty(executables)) {
            for (String executable : executables) {
                System.out.println("Before Process Executable: " + executable);
            }
        }
    }
}
