package com.payline.payment.samsung.pay.bean.rest.request;

import com.payline.payment.samsung.pay.exception.InvalidRequestException;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;

import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.PARTNER_CONFIG_SERVICE_ID;
import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.REF_ID;
import static com.payline.payment.samsung.pay.utils.SamsungPayStringUtils.isEmpty;

/**
 * Created by Thales on 16/08/2018.
 */
public class PaymentCredentialGetRequest {

    private String id;

    private String serviceId;

    /**
     * Constructor
     */
    public PaymentCredentialGetRequest(String id, String serviceId) {
        this.id = id;
        this.serviceId = serviceId;
    }

    public String getId() {
        return this.id;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public static final class Builder {

        public PaymentCredentialGetRequest fromRedirectionPaymentRequest(RedirectionPaymentRequest paylineRequest) throws InvalidRequestException {

            // Check the input request for NPEs and mandatory fields
            this.checkInputRequest(paylineRequest);

            // Instantiate the PaymentCredentialGetRequest from input request
            return new PaymentCredentialGetRequest(
                    paylineRequest.getHttpRequestParametersMap().get(REF_ID)[0],
                    paylineRequest.getPartnerConfiguration().getProperty(PARTNER_CONFIG_SERVICE_ID)
            );
        }

        /**
         * Verifies that the input request contains all the required fields.
         *
         * @param paymentRequest The input request
         * @throws InvalidRequestException If recovering the field value would result in a NPE or if the value is null or empty.
         */
        private void checkInputRequest(RedirectionPaymentRequest paymentRequest) throws InvalidRequestException {
            if (paymentRequest == null) {
                throw new InvalidRequestException("Request must not be null");
            }

            // Check attributes from PaymentRequest
            if (paymentRequest.getTransactionId() == null) {
                throw new InvalidRequestException("Missing PaymentRequest property: transaction id");
            }

            // Check attributes from PaymentRequest.PartnerConfiguration
            if (paymentRequest.getPartnerConfiguration() == null) {
                throw new InvalidRequestException("PartnerConfiguration properties object must not be null");
            }
            if ( isEmpty( paymentRequest.getPartnerConfiguration().getProperty(PARTNER_CONFIG_SERVICE_ID))) {
                throw new InvalidRequestException("Missing PartnerConfiguration property: service id");
            }

            if(paymentRequest.getHttpRequestParametersMap() == null){
                throw new InvalidRequestException("Missing HttpRequestParameters");
            }

            if(paymentRequest.getHttpRequestParametersMap().get(REF_ID) == null){
                throw new InvalidRequestException("Missing HttpRequestParameters property: ref_id");
            }

            if(paymentRequest.getHttpRequestParametersMap().get(REF_ID)[0] == null){
                throw new InvalidRequestException("Missing HttpRequestParameters property: ref_id");
            }
        }
    }

}