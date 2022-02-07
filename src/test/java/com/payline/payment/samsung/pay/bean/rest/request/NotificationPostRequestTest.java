package com.payline.payment.samsung.pay.bean.rest.request;

import com.payline.payment.samsung.pay.exception.InvalidRequestException;
import com.payline.payment.samsung.pay.utils.Utils;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.request.NotifyTransactionStatusRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by Thales on 27/08/2018.
 */
public class NotificationPostRequestTest {
    @Test
    public void fromPaymentRequest() throws InvalidRequestException {
        NotifyTransactionStatusRequest request = Utils.createNotifyTransactionRequest();
        NotificationPostRequest samsungRequest = new NotificationPostRequest.Builder().fromNotifyTransactionStatusRequest(request);
        assertNotNull(samsungRequest);
    }


    @Test
    public void fromPaymentRequestNoPropertyServiceId() {
        try {
            PartnerConfiguration configuration = new PartnerConfiguration(null, null);
            NotifyTransactionStatusRequest request = Utils.createNotifyTransactionRequestBuilder().withPartnerConfiguration(configuration).build();
            new NotificationPostRequest.Builder().fromNotifyTransactionStatusRequest(request);
        }catch (Exception e){
            assertEquals("Missing PartnerConfiguration property: service id", e.getMessage());
        }
    }
}