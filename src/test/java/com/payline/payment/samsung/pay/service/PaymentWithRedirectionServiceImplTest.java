package com.payline.payment.samsung.pay.service;

import com.payline.payment.samsung.pay.exception.DecryptException;
import com.payline.payment.samsung.pay.exception.ExternalCommunicationException;
import com.payline.payment.samsung.pay.exception.InvalidRequestException;
import com.payline.payment.samsung.pay.utils.JweDecrypt;
import com.payline.payment.samsung.pay.utils.Utils;
import com.payline.payment.samsung.pay.utils.http.SamsungPayHttpClient;
import com.payline.payment.samsung.pay.utils.http.StringResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentModeCard;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseDoPayment;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
public class PaymentWithRedirectionServiceImplTest {

    @Mock
    private SamsungPayHttpClient httpClient;

    @Mock
    private JweDecrypt jweDecrypt;

    @InjectMocks
    private PaymentWithRedirectionServiceImpl service = spy(new PaymentWithRedirectionServiceImpl());

    @Test
    public void createSendRequest() throws URISyntaxException, InvalidRequestException, ExternalCommunicationException {
        RedirectionPaymentRequest request = Utils.createRedirectionPaymentRequest();
        String content = "thisIsAResponse";
        StringResponse response = Utils.createStringResponse(content, 200);
        Mockito.when(httpClient.doGet(anyString(), anyString(), anyMap(), anyString())).thenReturn(response);
        service.setRedirectionPaymentRequest(Utils.createRedirectionPaymentRequest());

        StringResponse httpResponse = service.createSendRequest(request);
        assertEquals(content, httpResponse.getContent());
    }

    @Test
    public void processResponse() throws DecryptException {
        String jsonContent = "{" +
                "    'resultCode': '0'," +
                "    'resultMessage': 'SUCCESS'," +
                "    'method': '3DS'," +
                "    'card_brand': 'VI'," +
                "    'card_last4digits': '9406'," +
                "    '3DS': { 'type': 'S', 'version': '100', 'data': 'longStringWithEncryptedData' }," +
                "    'wallet_dm_id': 'f2hvqp-QkfN2_0='" +
                "}";
        StringResponse response = Utils.createStringResponse(jsonContent, 200);

        String goodDecrypt = "{'amount':'100','cryptogram':'AgAAAAAABQrqUtnic6MLQAAAAAA=','currency_code':'USD','eci_indicator':'07','tokenPanExpiration':'1225','utc':'1542290658225','tokenPAN':'4895370013341927'}";
        Mockito.when(jweDecrypt.getDecryptedData(anyString(),any())).thenReturn(goodDecrypt);

        service.setRedirectionPaymentRequest(Utils.createRedirectionPaymentRequestBuilder().build());

        PaymentResponse paymentResponse = service.processResponse(response);
        assertNotNull(paymentResponse);
        assertEquals(PaymentResponseDoPayment.class, paymentResponse.getClass());
        PaymentResponseDoPayment responseDoPayment  = (PaymentResponseDoPayment) paymentResponse;
        assertNotNull(responseDoPayment);
        assertNotNull(responseDoPayment.getPaymentMode());

        PaymentModeCard paymentModeCard = (PaymentModeCard) responseDoPayment.getPaymentMode();
        assertNotNull(paymentModeCard.getCard());
        assertNotNull(paymentModeCard.getPaymentData3DS());
        assertEquals("07",paymentModeCard.getPaymentData3DS().getEci());
        assertEquals("AgAAAAAABQrqUtnic6MLQAAAAAA=",paymentModeCard.getPaymentData3DS().getCavv());
        assertEquals("",paymentModeCard.getCard().getHolder());
    }

    @Test
    public void processResponseWithoutECIVISA() throws DecryptException {
        String jsonContent = "{" +
                "    'resultCode': '0'," +
                "    'resultMessage': 'SUCCESS'," +
                "    'method': '3DS'," +
                "    'card_brand': 'VI'," +
                "    'card_last4digits': '9406'," +
                "    '3DS': { 'type': 'S', 'version': '100', 'data': 'longStringWithEncryptedData' }," +
                "    'wallet_dm_id': 'f2hvqp-QkfN2_0='" +
                "}";
        StringResponse response = Utils.createStringResponse(jsonContent, 200);

        String goodDecrypt = "{'amount':'100','cryptogram':'AgAAAAAABQrqUtnic6MLQAAAAAA=','currency_code':'USD','tokenPanExpiration':'1225','utc':'1542290658225','tokenPAN':'4895370013341927'}";
        Mockito.when(jweDecrypt.getDecryptedData(anyString(),any())).thenReturn(goodDecrypt);

        service.setRedirectionPaymentRequest(Utils.createRedirectionPaymentRequestBuilder().build());

        PaymentResponse paymentResponse = service.processResponse(response);
        assertNotNull(paymentResponse);
        assertEquals(PaymentResponseDoPayment.class, paymentResponse.getClass());
        PaymentResponseDoPayment responseDoPayment  = (PaymentResponseDoPayment) paymentResponse;
        assertNotNull(responseDoPayment);
        assertNotNull(responseDoPayment.getPaymentMode());

        PaymentModeCard paymentModeCard = (PaymentModeCard) responseDoPayment.getPaymentMode();
        assertNotNull(paymentModeCard.getCard());
        assertNotNull(paymentModeCard.getPaymentData3DS());
        assertEquals("05",paymentModeCard.getPaymentData3DS().getEci());
        assertEquals("AgAAAAAABQrqUtnic6MLQAAAAAA=",paymentModeCard.getPaymentData3DS().getCavv());
        assertEquals("",paymentModeCard.getCard().getHolder());
    }

    @Test
    public void processResponseWithoutECIMasterCard() throws DecryptException {
        String jsonContent = "{" +
                "    'resultCode': '0'," +
                "    'resultMessage': 'SUCCESS'," +
                "    'method': '3DS'," +
                "    'card_brand': 'MC'," +
                "    'card_last4digits': '9406'," +
                "    '3DS': { 'type': 'S', 'version': '100', 'data': 'longStringWithEncryptedData' }," +
                "    'wallet_dm_id': 'f2hvqp-QkfN2_0='" +
                "}";
        StringResponse response = Utils.createStringResponse(jsonContent, 200);

        String goodDecrypt = "{'amount':'100','cryptogram':'AgAAAAAABQrqUtnic6MLQAAAAAA=','currency_code':'USD','tokenPanExpiration':'1225','utc':'1542290658225','tokenPAN':'4895370013341927'}";
        Mockito.when(jweDecrypt.getDecryptedData(anyString(),any())).thenReturn(goodDecrypt);

        service.setRedirectionPaymentRequest(Utils.createRedirectionPaymentRequestBuilder().build());

        PaymentResponse paymentResponse = service.processResponse(response);
        assertNotNull(paymentResponse);
        assertEquals(PaymentResponseDoPayment.class, paymentResponse.getClass());
        PaymentResponseDoPayment responseDoPayment  = (PaymentResponseDoPayment) paymentResponse;
        assertNotNull(responseDoPayment);
        assertNotNull(responseDoPayment.getPaymentMode());

        PaymentModeCard paymentModeCard = (PaymentModeCard) responseDoPayment.getPaymentMode();
        assertNotNull(paymentModeCard.getCard());
        assertNotNull(paymentModeCard.getPaymentData3DS());
        assertEquals("02",paymentModeCard.getPaymentData3DS().getEci());
        assertEquals("AgAAAAAABQrqUtnic6MLQAAAAAA=",paymentModeCard.getPaymentData3DS().getCavv());
        assertEquals("",paymentModeCard.getCard().getHolder());
    }

    @Test
    public void processResponseWithException() throws DecryptException {
        String jsonContent = "{" +
                "    'resultCode': '0'," +
                "    'resultMessage': 'SUCCESS'," +
                "    'method': '3DS'," +
                "    'card_brand': 'VI'," +
                "    'card_last4digits': '9406'," +
                "    '3DS': { 'type': 'S', 'version': '100', 'data': 'longStringWithEncryptedData' }," +
                "    'wallet_dm_id': 'f2hvqp-QkfN2_0='" +
                "}";
        StringResponse response = Utils.createStringResponse(jsonContent, 200);

        service.setRedirectionPaymentRequest(Utils.createRedirectionPaymentRequestBuilder().build());

        Mockito.when(jweDecrypt.getDecryptedData(anyString(),any())).thenThrow(DecryptException.class);

        PaymentResponse paymentResponse = service.processResponse(response);
        assertNotNull(paymentResponse);
        assertEquals(PaymentResponseFailure.class, paymentResponse.getClass());

        PaymentResponseFailure responseFailure = (PaymentResponseFailure) paymentResponse;
        assertEquals(FailureCause.INVALID_FIELD_FORMAT, responseFailure.getFailureCause());
    }

    @Test
    public void handleSessionExpired() {
        TransactionStatusRequest request = Utils.createTransactionStatusRequest();
        PaymentResponse response = service.handleSessionExpired(request);

        assertNotNull(response);
        assertEquals(PaymentResponseFailure.class, response.getClass());
        assertEquals(FailureCause.SESSION_EXPIRED, ((PaymentResponseFailure) response).getFailureCause());
    }
}
