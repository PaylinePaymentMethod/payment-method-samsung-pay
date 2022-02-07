package com.payline.payment.samsung.pay.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PluginUtilsTest {

    @Test
    public void truncateOk() {

        assertEquals("", PluginUtils.truncate("foo", 0));
        assertEquals("foo", PluginUtils.truncate("foo", 3));
        assertEquals("foo", PluginUtils.truncate("foo", 5));
        assertEquals("fo", PluginUtils.truncate("foo", 2));
    }
}
