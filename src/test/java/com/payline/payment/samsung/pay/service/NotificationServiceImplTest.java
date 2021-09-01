package com.payline.payment.samsung.pay.service;

import com.payline.payment.samsung.pay.exception.ExternalCommunicationException;
import com.payline.payment.samsung.pay.exception.InvalidRequestException;
import com.payline.payment.samsung.pay.utils.Utils;
import com.payline.payment.samsung.pay.utils.http.SamsungPayHttpClient;
import com.payline.payment.samsung.pay.utils.http.StringResponse;
import com.payline.pmapi.bean.notification.response.NotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.IgnoreNotificationResponse;
import com.payline.pmapi.bean.payment.request.NotifyTransactionStatusRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by Thales on 27/08/2018.
 */
@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceImplTest {
    @Mock
    private SamsungPayHttpClient httpClient;

    @InjectMocks
    @Spy
    private NotificationServiceImpl service = new NotificationServiceImpl();

    @Test
    public void parse(){
        NotificationResponse response = service.parse(null);
        Assert.assertNotNull(response);
        Assert.assertEquals(IgnoreNotificationResponse.class, response.getClass());
    }

    @Test
    public void createRequest() throws URISyntaxException, IOException, InvalidRequestException, ExternalCommunicationException {
        String content = "thisIsAContent";
        StringResponse response = Utils.createStringResponse(content, 200);
        Mockito.when(httpClient.doPost(anyString(), anyString(), anyString(), anyString())).thenReturn(response);

        NotifyTransactionStatusRequest request = Utils.createNotifyTransactionRequest();
        StringResponse httpResponse = service.createRequest(request);
        Assert.assertEquals(content, httpResponse.getContent());
    }

    @Test
    public void notifyTransactionStatusOK() throws URISyntaxException, IOException, InvalidRequestException, ExternalCommunicationException {
        NotifyTransactionStatusRequest request = mock(NotifyTransactionStatusRequest.class);
        String jsonContent = "{" +
                "   'resultCode': '0'," +
                "   'resultMessage': 'SUCCESS'" +
                "}";
        StringResponse response = Utils.createStringResponse(jsonContent, 201);
        doReturn(response).when(service).createRequest(any(NotifyTransactionStatusRequest.class));
        service.notifyTransactionStatus(request);
        verify(service).notifyTransactionStatus(request);

    }

    @Test
    public void notifyTransactionStatusEmptyResponse() throws URISyntaxException, IOException, InvalidRequestException, ExternalCommunicationException {
        NotifyTransactionStatusRequest request = mock(NotifyTransactionStatusRequest.class);
        StringResponse response = Utils.createStringResponse(null, 201);
        doReturn(response).when(service).createRequest(any(NotifyTransactionStatusRequest.class));

        service.notifyTransactionStatus(request);
        verify(service).notifyTransactionStatus(request);
    }

    @Test
    public void notifyTransactionStatusBadResponse() throws URISyntaxException, IOException, InvalidRequestException, ExternalCommunicationException {
        NotifyTransactionStatusRequest request = mock(NotifyTransactionStatusRequest.class);
        String jsonContent = "{" +
                "   'resultCode': 'ERROR'," +
                "   'resultMessage': 'This is an error message'" +
                "}";
        StringResponse response = Utils.createStringResponse(jsonContent, 400);
        doReturn(response).when(service).createRequest(any(NotifyTransactionStatusRequest.class));

        service.notifyTransactionStatus(request);
        verify(service).notifyTransactionStatus(request);
    }

    @Test
    public void notifyTransactionStatusIOExceptionResponse() throws URISyntaxException, IOException, InvalidRequestException, ExternalCommunicationException {
        NotifyTransactionStatusRequest request = mock(NotifyTransactionStatusRequest.class);
        doThrow(IOException.class).when(service).createRequest(any(NotifyTransactionStatusRequest.class));

        service.notifyTransactionStatus(request);
        verify(service).notifyTransactionStatus(request);
    }

    @Test
    public void processResponseOK() {
        String jsonContent = "{" +
                "   'resultCode': '0'," +
                "   'resultMessage': 'SUCCESS'" +
                "}";
        StringResponse response = Utils.createStringResponse(jsonContent, 201);

        service.processResponse(response);
        verify(service).processResponse(response);
    }

    @Test
    public void processResponseKO() {
        String jsonContent = "{" +
                "   'resultCode': 'OPM1N1033'," +
                "   'resultMessage': 'this is an error message'" +
                "}";
        StringResponse response = new StringResponse();
        response.setCode(400);
        response.setContent(jsonContent);

        service.processResponse(response);
        verify(service).processResponse(response);
    }

}