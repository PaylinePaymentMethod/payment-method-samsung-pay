package com.payline.payment.samsung.pay.service;

import com.payline.payment.samsung.pay.bean.rest.request.NotificationPostRequest;
import com.payline.payment.samsung.pay.bean.rest.response.NotificationPostResponse;
import com.payline.payment.samsung.pay.exception.ExternalCommunicationException;
import com.payline.payment.samsung.pay.exception.InvalidRequestException;
import com.payline.payment.samsung.pay.utils.http.SamsungPayHttpClient;
import com.payline.payment.samsung.pay.utils.http.StringResponse;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.notification.response.NotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.IgnoreNotificationResponse;
import com.payline.pmapi.bean.payment.request.NotifyTransactionStatusRequest;
import com.payline.pmapi.service.NotificationService;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.*;

/**
 * Created by Thales on 16/08/2018.
 */
public class NotificationServiceImpl implements NotificationService {

    private final static Logger LOGGER = LogManager.getLogger(PaymentServiceImpl.class);

    private SamsungPayHttpClient httpClient;

    private NotificationPostRequest.Builder requestBuilder;

    private NotifyTransactionStatusRequest notifyTransactionStatusRequest;

    /**
     * Default public constructor
     */
    public NotificationServiceImpl() {
        this.httpClient = SamsungPayHttpClient.getInstance();
        this.requestBuilder = new NotificationPostRequest.Builder();
    }

    @Override
    public NotificationResponse parse(NotificationRequest notificationRequest) {
        return new IgnoreNotificationResponse();
    }

    @Override
    public void notifyTransactionStatus(NotifyTransactionStatusRequest notifyTransactionStatusRequest) {
        this.notifyTransactionStatusRequest = notifyTransactionStatusRequest;

        try {

            // Mandate the child class to create and send the request (which is specific to each implementation)
            StringResponse response = this.createRequest(notifyTransactionStatusRequest);

            if (response != null && response.getCode() == HTTP_CREATED && response.getContent() != null) {
                this.processResponse(response);
            } else if (response != null && response.getCode() != HTTP_CREATED) {
                LOGGER.error("An HTTP error occurred while sending the request: " + response.getContent());
                // Nothing to do, no response to return
            } else {
                LOGGER.error("The HTTP response or its body is null and should not be");
                // Nothing to do, no response to return
            }

        } catch (InvalidRequestException e) {
            LOGGER.error("The input payment request is invalid: ", e);
            // Nothing to do, no response to return
        } catch (IOException e) {
            LOGGER.error("An IOException occurred while sending the HTTP request or receiving the response", e);
            // Nothing to do, no response to return
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred", e);
            // Nothing to do, no response to return
        }
    }

    public StringResponse createRequest(NotifyTransactionStatusRequest notificationRequest) throws IOException, InvalidRequestException, URISyntaxException, ExternalCommunicationException {

        // Create Notification request from Payline request
        NotificationPostRequest notificationPostRequest = this.requestBuilder.fromNotifyTransactionStatusRequest(notificationRequest);

        // Send Notification request
        // get all variables needed to call Samsung API
        String hostKey = notificationRequest.getEnvironment().isSandbox() ? PARTNER_URL_API_SANDBOX : PARTNER_URL_API_PROD;
        String host = notificationRequest.getPartnerConfiguration().getProperty(hostKey);
        return this.httpClient.doPost(host, NOTIFICATION_PATH, notificationPostRequest.buildBody(), notificationRequest.getPartnerTransactionId());

    }

    public void processResponse(StringResponse response) {
        // Parse response
        NotificationPostResponse notificationPostResponse = new NotificationPostResponse.Builder().fromJson(response.getContent());

        if (notificationPostResponse.isResultOk()) {
            LOGGER.info("Samsung Pay notification OK: " + notificationPostResponse.getResultMessage());
        } else {
            LOGGER.error("Samsung Pay notification KO: " + notificationPostResponse.getResultMessage());
        }

    }

    public void setNotifyTransactionStatusRequest(NotifyTransactionStatusRequest notifyTransactionStatusRequest) {
        this.notifyTransactionStatusRequest = notifyTransactionStatusRequest;
    }
}
