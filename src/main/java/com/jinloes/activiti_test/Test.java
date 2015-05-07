package com.jinloes.activiti_test;

import java.io.Serializable;

/**
 * Created by jinloes on 5/6/15.
 */
public class Test implements Serializable {
    private static final long serialVersionUID = 2837375472973023220L;
    private boolean foo;

    public Test(boolean foo) {
        this.foo = foo;
    }

    public boolean isFoo() {
        return foo;
    }

    public void setFoo(boolean foo) {
        this.foo = foo;
    }
}
