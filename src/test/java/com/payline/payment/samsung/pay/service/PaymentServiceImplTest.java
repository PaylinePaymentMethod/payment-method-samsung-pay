package com.payline.payment.samsung.pay.service;

import com.payline.payment.samsung.pay.bean.rest.response.CreateTransactionPostResponse;
import com.payline.payment.samsung.pay.exception.ExternalCommunicationException;
import com.payline.payment.samsung.pay.exception.InvalidRequestException;
import com.payline.payment.samsung.pay.utils.Utils;
import com.payline.payment.samsung.pay.utils.http.SamsungPayHttpClient;
import com.payline.payment.samsung.pay.utils.http.StringResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.Email;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFormUpdated;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.PAYMENTDATA_TOKENDATA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

/**
 * Created by Thales on 27/08/2018.
 */
@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

    @Mock
    private SamsungPayHttpClient httpClient;

    @InjectMocks
    @Spy
    private PaymentServiceImpl service;

    @Test
    public void paymentRequest(){
        PaymentResponse response = PaymentResponseSuccess.PaymentResponseSuccessBuilder.aPaymentResponseSuccess().withTransactionDetails(Email.EmailBuilder.anEmail().withEmail("foo@bar.baz").build()).withPartnerTransactionId("normal").build();
        doReturn(response).when(service).processRequest(any());

        PaymentResponse paymentResponse = service.paymentRequest(Utils.createCompletePaymentBuilder().build());
        PaymentResponseSuccess responseSuccess =  (PaymentResponseSuccess) paymentResponse;
        assertEquals("normal", responseSuccess.getPartnerTransactionId());

    }

    @Test
    public void paymentRequestDirect() throws URISyntaxException, ExternalCommunicationException {
        PaymentResponse responseDirect = PaymentResponseSuccess.PaymentResponseSuccessBuilder.aPaymentResponseSuccess().withTransactionDetails(Email.EmailBuilder.anEmail().withEmail("foo@bar.baz").build()).withPartnerTransactionId("direct").build();
        final PaymentRequest request = Utils.createCompletePaymentBuilder().build();
        request.getPaymentFormContext().getPaymentFormParameter().put(PAYMENTDATA_TOKENDATA, "{\"REFERENCE_ID\": \"667ae458c01b4d95bdc76af81e3cd11e\"}");
        final StringResponse response = new StringResponse();
        response.setContent("thisIsAResponse");
        doReturn(response).when(service).createGetCredentialRequest(request, "667ae458c01b4d95bdc76af81e3cd11e");
        doReturn(responseDirect).when(service).processDirectResponse(request, response);

        PaymentResponse paymentResponse = service.paymentRequest(request);
        PaymentResponseSuccess responseSuccess =  (PaymentResponseSuccess) paymentResponse;
        assertEquals("direct", responseSuccess.getPartnerTransactionId());
    }

    @Test
    public void createSendRequest() throws URISyntaxException, IOException, InvalidRequestException, ExternalCommunicationException {
        String content = "thisIsAResponse";
        StringResponse response = new StringResponse();
        response.setCode(200);
        response.setContent(content);
        Mockito.when(httpClient.doPost(anyString(), anyString(), anyString(), anyString())).thenReturn(response);
        PaymentRequest request = Utils.createCompletePaymentBuilder().build();
        StringResponse httpResponse = service.createSendRequest(request);
        assertEquals(content, httpResponse.getContent());
    }

    @Test
    public void processResponse() throws MalformedURLException {
        String jsonContent = "{" +
                "   'resultCode': '0'," +
                "   'resultMessage': 'SUCCESS'," +
                "   'id': '000'," +
                "   'href': 'http://a.simple.url'," +
                "   'encInfo': { 'mod': '111', 'exp': '222', 'keyId': '333' }" +
                "}";
        StringResponse response = new StringResponse();
        response.setCode(200);
        response.setContent(jsonContent);

        service.setPaymentRequest(Utils.createCompletePaymentBuilder().build());

        PaymentResponse paymentResponse = service.processResponse(response);
        assertNotNull(paymentResponse);
        assertEquals(PaymentResponseFormUpdated.class, paymentResponse.getClass());

    }

    @Test
    public void processResponseFail() throws MalformedURLException {
        String jsonContent = "{" +
                "   'resultCode': 'OPM1N1001'," +
                "   'resultMessage': 'FAIL'," +
                "   'id': '000'," +
                "   'href': 'http://a.simple.url'," +
                "   'encInfo': { 'mod': '111', 'exp': '222', 'keyId': '333' }" +
                "}";
        StringResponse response = new StringResponse();
        response.setCode(400);
        response.setContent(jsonContent);

        service.setPaymentRequest(Utils.createCompletePaymentBuilder().build());

        PaymentResponse paymentResponse = service.processResponse(response);
        assertEquals(PaymentResponseFailure.class, paymentResponse.getClass());
        PaymentResponseFailure responseFailure = (PaymentResponseFailure) paymentResponse;
        assertEquals(FailureCause.INVALID_DATA, responseFailure.getFailureCause());

    }


    @Test
    public void createConnectCall() {
        String goodConnectCall = "(function(){\n" +
                "    SamsungPay.connect('000' ,'http://a.simple.url' ,'" + Utils.SERVICE_ID + "' ,'" + Utils.SUCCESS_URL + "' ,'" + Utils.FAILURE_URL + "' ,'FR' ,'111' ,'222' ,'333' );\n" +
                "})()";

        String jsonResponse = "{" +
                "   'resultCode': '0'," +
                "   'resultMessage': 'SUCCESS'," +
                "   'id': '000'," +
                "   'href': 'http://a.simple.url'," +
                "   'encInfo': { 'mod': '111', 'exp': '222', 'keyId': '333' }" +
                "}";
        CreateTransactionPostResponse response = new CreateTransactionPostResponse.Builder().fromJson(jsonResponse);
        PaymentRequest request = Utils.createCompletePaymentBuilder().build();

        String connectCall = service.createConnectCall(request, response);
        assertEquals(goodConnectCall, connectCall);
    }

    @Test
    public void getRefIdFromRequest(){
        String data = "{\"REFERENCE_ID\": \"667ae458c01b4d95bdc76af81e3cd11e\"}";
        PaymentRequest request = Utils.createCompletePaymentBuilder().build();
        request.getPaymentFormContext().getPaymentFormParameter().put(PAYMENTDATA_TOKENDATA, data);

        String refId = service.getRefIdFromRequest(request);
        assertEquals("667ae458c01b4d95bdc76af81e3cd11e", refId);

    }
}