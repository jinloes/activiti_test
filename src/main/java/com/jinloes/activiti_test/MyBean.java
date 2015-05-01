package com.jinloes.activiti_test;

import org.springframework.stereotype.Component;

/**
 * Created by jinloes on 4/14/15.
 */
@Component
public class MyBean {

    public void execute() {
        System.out.println("I'm a delegate!");
    }
}
