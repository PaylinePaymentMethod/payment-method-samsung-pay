package com.payline.payment.samsung.pay.bean.rest.request;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.samsung.pay.bean.rest.request.nesteed.Payment;
import com.payline.payment.samsung.pay.bean.rest.request.nesteed.Service;
import com.payline.payment.samsung.pay.exception.InvalidRequestException;
import com.payline.payment.samsung.pay.utils.type.PaymentStatusEnum;
import com.payline.pmapi.bean.payment.request.NotifyTransactionStatusRequest;

import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.*;
import static com.payline.payment.samsung.pay.utils.SamsungPayStringUtils.isEmpty;

/**
 * Created by Thales on 16/08/2018.
 */
public class NotificationPostRequest implements AbstractJsonRequest {

    @SerializedName("payment")
    private Payment payment;

    @SerializedName("timestamp")
    private Long timestamp;

    /**
     * Constructor
     */
    protected NotificationPostRequest(Payment payment,
                                      Long timestamp) {

        this.payment    = payment;
        this.timestamp  = timestamp;

    }

    public static final class Builder {

        public NotificationPostRequest fromNotifyTransactionStatusRequest(NotifyTransactionStatusRequest paylineRequest) throws InvalidRequestException {

            // Check the input request for NPEs and mandatory fields
            this.checkInputRequest(paylineRequest);

            // Instantiate the CreateTransactionPostRequest from input request
            return new NotificationPostRequest(
                    this.getPaymentFromPaymentRequest(paylineRequest),
                    System.currentTimeMillis()
            );
        }

        /**
         * Verifies that the input request contains all the required fields.
         *
         * @param paylineRequest The input request
         * @throws InvalidRequestException If recovering the field value would result in a NPE or if the value is null or empty.
         */
        private void checkInputRequest(NotifyTransactionStatusRequest paylineRequest) throws InvalidRequestException {
            if ( paylineRequest == null ) {
                throw new InvalidRequestException( "Request must not be null" );
            }

            // Check attributes from PaymentRequest
            if ( isEmpty( paylineRequest.getPartnerTransactionId() ) ) {
                throw new InvalidRequestException( "Missing NotifyTransactionStatusRequest property: partner transaction id" );
            }
            if ( paylineRequest.getTransactionSatus() == null ) {
                throw new InvalidRequestException( "Missing NotifyTransactionStatusRequest property: partner status" );
            }

            // Check attributes from PaymentRequest.PartnerConfiguration
            if ( paylineRequest.getPartnerConfiguration() == null ) {
                throw new InvalidRequestException( "PartnerConfiguration properties object must not be null" );
            }
            if ( isEmpty( paylineRequest.getPartnerConfiguration().getProperty(paylineRequest.getEnvironment().isSandbox()? PARTNER_SERVICE_ID_SANDBOX: PARTNER_SERVICE_ID_PROD) ) ) {
                throw new InvalidRequestException( "Missing PartnerConfiguration property: service id" );
            }

        }

        /**
         * Construit la structure de données Payment à partir de la requête PaymentRequest
         *
         * @param paylineRequest
         * @return
         */
        private Payment getPaymentFromPaymentRequest(NotifyTransactionStatusRequest paylineRequest) {
            return new Payment()
                    .service(new Service(paylineRequest.getPartnerConfiguration().getProperty(paylineRequest.getEnvironment().isSandbox()? PARTNER_SERVICE_ID_SANDBOX: PARTNER_SERVICE_ID_PROD)))
                    .reference(
                            paylineRequest
                                .getPartnerTransactionId()
                    )
                    .status(
                            this.convertTransactionStatusToPaymentStatus(
                                    paylineRequest.getTransactionSatus()
                            )
                    )
                    .provider(PAYMENT_PROVIDER);
        }

        /**
         *
         * @param status
         * @return
         */
        private String convertTransactionStatusToPaymentStatus(NotifyTransactionStatusRequest.TransactionStatus status) {
            String paymentStatus = "";
            if (NotifyTransactionStatusRequest.TransactionStatus.SUCCESS.equals(status)) {
                paymentStatus = PaymentStatusEnum.CHARGED.name();
            }
            if (NotifyTransactionStatusRequest.TransactionStatus.FAIL.equals(status)) {
                paymentStatus = PaymentStatusEnum.ERRED.name();
            }
            if (NotifyTransactionStatusRequest.TransactionStatus.CANCELLED.equals(status)) {
                paymentStatus = PaymentStatusEnum.CANCELLED.name();
            }
            return paymentStatus;
        }

    }

}