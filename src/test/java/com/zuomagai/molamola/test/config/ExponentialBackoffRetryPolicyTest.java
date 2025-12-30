package com.zuomagai.molamola.test.config;

import org.junit.Assert;
import org.junit.Test;

import com.zuomagai.molamola.config.retry.ExponentialBackoffRetryPolicy;

public class ExponentialBackoffRetryPolicyTest {

    @Test
    public void testDelayIncreases() {
        ExponentialBackoffRetryPolicy policy = new ExponentialBackoffRetryPolicy(3, 100L, 1000L, 2.0d);
        Assert.assertEquals(100L, policy.nextDelayMillis(1, null));
        Assert.assertEquals(200L, policy.nextDelayMillis(2, null));
        Assert.assertEquals(400L, policy.nextDelayMillis(3, null));
        Assert.assertEquals(-1L, policy.nextDelayMillis(4, null));
    }

    @Test
    public void testDelayCapped() {
        ExponentialBackoffRetryPolicy policy = new ExponentialBackoffRetryPolicy(5, 300L, 700L, 2.0d);
        Assert.assertEquals(300L, policy.nextDelayMillis(1, null));
        Assert.assertEquals(600L, policy.nextDelayMillis(2, null));
        Assert.assertEquals(700L, policy.nextDelayMillis(3, null));
        Assert.assertEquals(700L, policy.nextDelayMillis(4, null));
    }
}
