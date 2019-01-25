package com.payline.payment.samsung.pay.integration;

import com.payline.pmapi.integration.exception.TestException;
import com.payline.pmapi.integration.service.test.PaymentWithRedirectionServiceTest;
import org.junit.jupiter.api.Test;

public class FullTestIT {

    @Test
    public void testPayment() throws TestException {
        PaymentWithRedirectionServiceTest.getInstance().finalizeRedirectionPaymentOkTest();
    }
}