package com.payline.payment.samsung.pay.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SamsungPayStringUtilsTest {

    @Test
    public void isEmpty() {
        assertTrue(SamsungPayStringUtils.isEmpty(null));
        assertTrue(SamsungPayStringUtils.isEmpty(""));
        assertTrue(SamsungPayStringUtils.isEmpty(" "));
        assertTrue(SamsungPayStringUtils.isEmpty("      "));
        assertFalse(SamsungPayStringUtils.isEmpty("     . "));
    }
}
