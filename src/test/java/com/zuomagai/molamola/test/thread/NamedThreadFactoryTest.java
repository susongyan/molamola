package com.zuomagai.molamola.test.thread;

import com.zuomagai.molamola.thread.NamedThreadFactory;
import org.junit.Assert;
import org.junit.Test;

public class NamedThreadFactoryTest {

    @Test
    public void test() {
        Thread thread = new NamedThreadFactory("test-thread-").newThread(() -> {
            System.out.println("hello " + Thread.currentThread().getName());
        });
        thread.start();
        Assert.assertTrue(thread.isDaemon());
        Assert.assertEquals(thread.getName(), "test-thread-0");
    }
}