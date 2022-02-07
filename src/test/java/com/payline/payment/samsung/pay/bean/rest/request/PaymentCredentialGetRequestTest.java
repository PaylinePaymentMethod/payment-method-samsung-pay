package com.payline.payment.samsung.pay.bean.rest.request;

import com.payline.payment.samsung.pay.exception.InvalidRequestException;
import com.payline.payment.samsung.pay.utils.Utils;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.REF_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by Thales on 27/08/2018.
 */
public class PaymentCredentialGetRequestTest {

    @Test
    public void fromRedirectionPaymentRequest() throws InvalidRequestException {
        RedirectionPaymentRequest request = Utils.createRedirectionPaymentRequest();
        PaymentCredentialGetRequest samsungRequest = new PaymentCredentialGetRequest.Builder().fromRedirectionPaymentRequest(request);
        assertNotNull(samsungRequest);
    }

    @Test
    public void fromRedirectionPaymentRequestNoHttpParameter() {
        try {

            RedirectionPaymentRequest request = Utils.createRedirectionPaymentRequestBuilder().withHttpRequestParametersMap(null).build();
            PaymentCredentialGetRequest samsungRequest = new PaymentCredentialGetRequest.Builder().fromRedirectionPaymentRequest(request);
            assertNotNull(samsungRequest);
        }catch (Exception e){
            assertEquals(InvalidRequestException.class, e.getClass());
            assertEquals("Missing HttpRequestParameters", e.getMessage());
        }
    }


    @Test
    public void fromRedirectionPaymentRequestNoReferenceId() {
        try {
            Map<String, String[]> parameters = new HashMap<>();
            parameters.put(REF_ID, null);
            RedirectionPaymentRequest request = Utils.createRedirectionPaymentRequestBuilder().withHttpRequestParametersMap(parameters).build();
            PaymentCredentialGetRequest samsungRequest = new PaymentCredentialGetRequest.Builder().fromRedirectionPaymentRequest(request);
            assertNotNull(samsungRequest);
        }catch (Exception e){
            assertEquals(InvalidRequestException.class, e.getClass());
            assertEquals("Missing HttpRequestParameters property: ref_id", e.getMessage());
        }
    }
}