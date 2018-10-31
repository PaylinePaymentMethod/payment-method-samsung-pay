package com.payline.payment.samsung.pay.service;

import com.payline.payment.samsung.pay.bean.rest.request.NotificationPostRequest;
import com.payline.payment.samsung.pay.bean.rest.response.NotificationPostResponse;
import com.payline.payment.samsung.pay.exception.InvalidRequestException;
import com.payline.payment.samsung.pay.utils.config.ConfigEnvironment;
import com.payline.payment.samsung.pay.utils.config.ConfigProperties;
import com.payline.payment.samsung.pay.utils.http.SamsungPayHttpClient;
import com.payline.payment.samsung.pay.utils.http.StringResponse;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.notification.response.NotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.IgnoreNotificationResponse;
import com.payline.pmapi.bean.payment.request.NotifyTransactionStatusRequest;
import com.payline.pmapi.service.NotificationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.*;

/**
 * Created by Thales on 16/08/2018.
 */
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LogManager.getLogger( PaymentServiceImpl.class );

    private SamsungPayHttpClient httpClient;

    private NotificationPostRequest.Builder requestBuilder;

    private NotifyTransactionStatusRequest notifyTransactionStatusRequest;

    /**
     * Default public constructor
     */
    public NotificationServiceImpl() {

        this.httpClient = new SamsungPayHttpClient(
                Integer.parseInt( ConfigProperties.get(CONFIG__HTTP_CONNECT_TIMEOUT) ),
                Integer.parseInt( ConfigProperties.get(CONFIG__HTTP_WRITE_TIMEOUT) ),
                Integer.parseInt( ConfigProperties.get(CONFIG__HTTP_READ_TIMEOUT) )
        );

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
            StringResponse response = this.createSendRequest( notifyTransactionStatusRequest );

            if ( response != null && response.getCode() == HTTP_OK && response.getContent() != null ) {
                this.processResponse( response );
            } else if ( response != null && response.getCode() != HTTP_OK ) {
                this.logger.error( "An HTTP error occurred while sending the request: " + response.getContent() );
                // Nothing to do, no response to return
            } else {
                this.logger.error( "The HTTP response or its body is null and should not be" );
                // Nothing to do, no response to return
            }

        } catch ( InvalidRequestException e ) {
            this.logger.error( "The input payment request is invalid: " + e.getMessage() );
            // Nothing to do, no response to return
        } catch ( IOException e ) {
            this.logger.error( "An IOException occurred while sending the HTTP request or receiving the response: " + e.getMessage() );
            // Nothing to do, no response to return
        } catch ( Exception e ) {
            this.logger.error( "An unexpected error occurred: ", e );
            // Nothing to do, no response to return
        }

    }

    private StringResponse createSendRequest(NotifyTransactionStatusRequest notificationRequest) throws IOException, InvalidRequestException, URISyntaxException {

        // Create Notification request from Payline request
        NotificationPostRequest notificationPostRequest = this.requestBuilder.fromNotifyTransactionStatusRequest(notificationRequest);

        // Send Notification request
        ConfigEnvironment environment = Boolean.FALSE.equals( notificationRequest.getEnvironment().isSandbox() ) ? ConfigEnvironment.PROD : ConfigEnvironment.DEV;
        String scheme   = ConfigProperties.get(CONFIG__SHEME, environment);
        String host     = ConfigProperties.get(CONFIG__HOST, environment);
        String path     = ConfigProperties.get(CONFIG__PATH_NOTIFICATION);

        return this.httpClient.doPost(
                scheme,
                host,
                path,
                notificationPostRequest.buildBody()
        );

    }

    private void processResponse(StringResponse response) throws IOException {

        // Parse response
        NotificationPostResponse notificationPostResponse = new NotificationPostResponse.Builder().fromJson(response.getContent());

        if (notificationPostResponse.isResultOk()) {
            this.logger.error( "Samsung Pay notification OK: " + notificationPostResponse.getResultMessage() );
        } else {
            this.logger.error( "Samsung Pay notification KO: " + notificationPostResponse.getResultMessage() );
        }

    }

}
