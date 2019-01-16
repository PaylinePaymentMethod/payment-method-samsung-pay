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
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.HashMap;

import static com.payline.payment.samsung.pay.utils.Utils.NOTIFICATION_URL;
import static com.payline.pmapi.integration.AbstractPaymentIntegration.CANCEL_URL;

/**
 * Created by Thales on 27/08/2018.
 */
public class CreateTransactionPostRequestTest {

    @Test
    public void fromPaymentRequest() throws InvalidRequestException {
        PaymentRequest request = Utils.createCompletePaymentBuilder().build();

        CreateTransactionPostRequest samsungRequest = new CreateTransactionPostRequest.Builder().fromPaymentRequest(request);
        Assert.assertNotNull(samsungRequest);
    }

    @Test(expected = InvalidRequestException.class)
    public void fromPaymentRequestNoOrderReference() throws InvalidRequestException {
        Order emptyOrder = Order.OrderBuilder.anOrder().build();
        PaymentRequest request = Utils.createCompletePaymentBuilder().withOrder(emptyOrder).build();
        new CreateTransactionPostRequest.Builder().fromPaymentRequest(request);
    }

    @Test(expected = InvalidRequestException.class)
    public void fromPaymentRequestNoMerchantNameProperties() throws InvalidRequestException {
        ContractConfiguration configuration = new ContractConfiguration("", new HashMap<>());
        PaymentRequest request = Utils.createCompletePaymentBuilder().withContractConfiguration(configuration).build();
        new CreateTransactionPostRequest.Builder().fromPaymentRequest(request);
    }

    @Test(expected = InvalidRequestException.class)
    public void fromPaymentRequestNoCallBackUrl() throws InvalidRequestException {
        Environment environment = new Environment(NOTIFICATION_URL,null,CANCEL_URL,true);
        PaymentRequest request = Utils.createCompletePaymentBuilder().withEnvironment(environment).build();
        new CreateTransactionPostRequest.Builder().fromPaymentRequest(request);
    }

    @Test(expected = InvalidRequestException.class)
    public void fromPaymentRequestNoServiceId() throws InvalidRequestException {
        PartnerConfiguration configuration = new PartnerConfiguration(new HashMap<>(),new HashMap<>());
        PaymentRequest request = Utils.createCompletePaymentBuilder().withPartnerConfiguration(configuration).build();
        new CreateTransactionPostRequest.Builder().fromPaymentRequest(request);
    }

    @Test(expected = InvalidRequestException.class)
    public void fromPaymentRequestNullRequest() throws InvalidRequestException {
        new CreateTransactionPostRequest.Builder().fromPaymentRequest(null);
    }

    @Test(expected = InvalidRequestException.class)
    public void fromPaymentRequestNoAmountSmallestUnit() throws InvalidRequestException {
        PaymentRequest request = Utils.createCompletePaymentBuilder().withAmount(new Amount(null,null)).build();
        new CreateTransactionPostRequest.Builder().fromPaymentRequest(request);
    }

    @Test(expected = InvalidRequestException.class)
    public void fromPaymentRequestNoAmountCurrency() throws InvalidRequestException {
        PaymentRequest request = Utils.createCompletePaymentBuilder().withAmount(new Amount(new BigInteger("1"),null)).build();
        new CreateTransactionPostRequest.Builder().fromPaymentRequest(request);
    }

    // fromCheckRequest test
    @Test
    public void fromCheckRequest() throws InvalidRequestException {
        ContractParametersCheckRequest request = Utils.createContractParametersCheckRequest("aMerchant");
        new CreateTransactionPostRequest.Builder().fromCheckRequest(request);
    }

    @Test(expected = InvalidRequestException.class)
    public void fromCheckRequestNullRequest() throws InvalidRequestException {
        new CreateTransactionPostRequest.Builder().fromCheckRequest(null);
    }

    @Test(expected = InvalidRequestException.class)
    public void fromCheckRequestNullPartnerConfiguration() throws InvalidRequestException {
        ContractParametersCheckRequest request = Utils.createContractParametersCheckRequestBuilder("aMerchant")
                .withPartnerConfiguration(null)
                .build();
        new CreateTransactionPostRequest.Builder().fromCheckRequest(request);
    }

    @Test(expected = InvalidRequestException.class)
    public void fromCheckRequestNullServiceId() throws InvalidRequestException {
        PartnerConfiguration partnerConfiguration = new PartnerConfiguration(new HashMap<>(), new HashMap<>());
        ContractParametersCheckRequest request = Utils.createContractParametersCheckRequestBuilder("aMerchant")
                .withPartnerConfiguration(partnerConfiguration)
                .build();
        new CreateTransactionPostRequest.Builder().fromCheckRequest(request);
    }

    @Test
    public void buildBody() throws InvalidRequestException {
        String bodyToCompare = "{\"callback\":\"http://default.callback.url\",\"paymentDetails\":{\"service\":{\"id\":\"db1294c3c8bc42fe9ce762\"},\"orderNumber\":\"0001\",\"protocol\":{\"type\":\"3DS\",\"version\":\"80\"},\"amount\":{\"currency\":\"USD\",\"total\":1},\"merchant\":{\"name\":\"aMerchant\"}}}";

        ContractParametersCheckRequest request = Utils.createContractParametersCheckRequest("aMerchant");
        CreateTransactionPostRequest createTransactionPostRequest = new CreateTransactionPostRequest.Builder().fromCheckRequest(request);
        String body = createTransactionPostRequest.buildBody();

        Assert.assertEquals(bodyToCompare, body);
    }
}