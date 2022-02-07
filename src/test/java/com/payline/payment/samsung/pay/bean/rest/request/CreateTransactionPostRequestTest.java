package com.payline.payment.samsung.pay.bean.rest.request;

import com.payline.payment.samsung.pay.exception.InvalidRequestException;
import com.payline.payment.samsung.pay.utils.Utils;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.Environment;
import com.payline.pmapi.bean.payment.Order;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HashMap;

import static com.payline.payment.samsung.pay.utils.Utils.NOTIFICATION_URL;
import static com.payline.pmapi.integration.AbstractPaymentIntegration.CANCEL_URL;
import static org.junit.jupiter.api.Assertions.*;

public class CreateTransactionPostRequestTest {

    @Nested
    class fromPaymentRequest {
        @Test
        public void fromPaymentRequest() throws InvalidRequestException {
            final PaymentRequest request = Utils.createCompletePaymentBuilder().build();

            final CreateTransactionPostRequest samsungRequest = new CreateTransactionPostRequest.Builder().fromPaymentRequest(request);
            assertNotNull(samsungRequest);
        }

        @Test
        public void fromPaymentRequestNoOrderReference() {
            final Order emptyOrder = Order.OrderBuilder.anOrder().build();
            final PaymentRequest request = Utils.createCompletePaymentBuilder().withOrder(emptyOrder).build();
            assertThrows(InvalidRequestException.class, () -> {
                new CreateTransactionPostRequest.Builder().fromPaymentRequest(request);
            });
        }

        @Test
        public void fromPaymentRequestNoMerchantNameProperties() {
            final ContractConfiguration configuration = new ContractConfiguration("", new HashMap<>());
            final PaymentRequest request = Utils.createCompletePaymentBuilder().withContractConfiguration(configuration).build();
            assertThrows(InvalidRequestException.class, () -> {
                new CreateTransactionPostRequest.Builder().fromPaymentRequest(request);
            });
        }

        @Test
        public void fromPaymentRequestNoCallBackUrl() {
            final Environment environment = new Environment(NOTIFICATION_URL,null,CANCEL_URL,true);
            final PaymentRequest request = Utils.createCompletePaymentBuilder().withEnvironment(environment).build();
            assertThrows(InvalidRequestException.class, () -> {
                new CreateTransactionPostRequest.Builder().fromPaymentRequest(request);
            });
        }

        @Test
        public void fromPaymentRequestNoServiceId() {
            final PartnerConfiguration configuration = new PartnerConfiguration(new HashMap<>(),new HashMap<>());
            final PaymentRequest request = Utils.createCompletePaymentBuilder().withPartnerConfiguration(configuration).build();
            assertThrows(InvalidRequestException.class, () -> {
                new CreateTransactionPostRequest.Builder().fromPaymentRequest(request);
            });
        }

        @Test
        public void fromPaymentRequestNullRequest() {
            assertThrows(InvalidRequestException.class, () -> {
                new CreateTransactionPostRequest.Builder().fromPaymentRequest(null);
            });
        }

        @Test
        public void fromPaymentRequestNoAmountSmallestUnit() {
            final PaymentRequest request = Utils.createCompletePaymentBuilder().withAmount(new Amount(null,null)).build();
            assertThrows(InvalidRequestException.class, () -> {
                new CreateTransactionPostRequest.Builder().fromPaymentRequest(request);
            });
        }

        @Test
        public void fromPaymentRequestNoAmountCurrency() {
            final PaymentRequest request = Utils.createCompletePaymentBuilder().withAmount(new Amount(new BigInteger("1"),null)).build();
            assertThrows(InvalidRequestException.class, () -> {
                new CreateTransactionPostRequest.Builder().fromPaymentRequest(request);
            });
        }
    }

    @Nested
    class fromCheckRequest {
        @Test
        public void fromCheckRequest() throws InvalidRequestException {
            final ContractParametersCheckRequest request = Utils.createContractParametersCheckRequest("aMerchant");
            assertNotNull(new CreateTransactionPostRequest.Builder().fromCheckRequest(request));
        }

        @Test
        public void fromCheckRequestNullRequest() {
            assertThrows(InvalidRequestException.class, () -> {
                new CreateTransactionPostRequest.Builder().fromCheckRequest(null);
            });
        }

        @Test
        public void fromCheckRequestNullPartnerConfiguration() {
            final ContractParametersCheckRequest request = Utils.createContractParametersCheckRequestBuilder("aMerchant")
                    .withPartnerConfiguration(null)
                    .build();

            assertThrows(InvalidRequestException.class, () -> {
                new CreateTransactionPostRequest.Builder().fromCheckRequest(request);
            });
        }

        @Test
        public void fromCheckRequestNullServiceId() {
            final PartnerConfiguration partnerConfiguration = new PartnerConfiguration(new HashMap<>(), new HashMap<>());
            final ContractParametersCheckRequest request = Utils.createContractParametersCheckRequestBuilder("aMerchant")
                    .withPartnerConfiguration(partnerConfiguration)
                    .build();
            assertThrows(InvalidRequestException.class, () -> {
                new CreateTransactionPostRequest.Builder().fromCheckRequest(request);
            });
        }
    }

    @Test
    public void buildBody() throws InvalidRequestException {
        String bodyToCompare = "{\"callback\":\"http://default.callback.url\",\"paymentDetails\":{\"service\":{\"id\":\"db1294c3c8bc42fe9ce762\"},\"orderNumber\":\"0001\",\"protocol\":{\"type\":\"3DS\",\"version\":\"80\"},\"amount\":{\"currency\":\"USD\",\"total\":\"1\"},\"merchant\":{\"name\":\"aMerchant\"}}}";

        ContractParametersCheckRequest request = Utils.createContractParametersCheckRequest("aMerchant");
        CreateTransactionPostRequest createTransactionPostRequest = new CreateTransactionPostRequest.Builder().fromCheckRequest(request);
        String body = createTransactionPostRequest.buildBody();

        assertEquals(bodyToCompare, body);
    }
}