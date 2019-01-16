package com.payline.payment.samsung.pay.bean.rest.request;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.samsung.pay.bean.rest.request.nesteed.*;
import com.payline.payment.samsung.pay.exception.InvalidRequestException;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.Environment;
import com.payline.pmapi.bean.payment.Order;
import com.payline.pmapi.bean.payment.request.PaymentRequest;

import java.math.BigInteger;

import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.*;
import static com.payline.payment.samsung.pay.utils.SamsungPayStringUtils.isEmpty;

/**
 * Created by Thales on 16/08/2018.
 */
public class CreateTransactionPostRequest extends AbstractJsonRequest {

    @SerializedName("callback")
    private String callback;

    @SerializedName("paymentDetails")
    private PaymentDetails paymentDetails;

    /**
     * Constructor
     */
    protected CreateTransactionPostRequest(String callback,
                                           PaymentDetails paymentDetails) {

        this.callback = callback;
        this.paymentDetails = paymentDetails;

    }


    public static final class Builder {

        public CreateTransactionPostRequest fromPaymentRequest(PaymentRequest paylineRequest) throws InvalidRequestException {

            // Check the input request for NPEs and mandatory fields
            this.checkInputRequest(paylineRequest);

            // Instantiate the CreateTransactionPostRequest from input request
            return new CreateTransactionPostRequest(
                    paylineRequest.getEnvironment().getRedirectionReturnURL(),
                    this.getPaymentDetailsFromPaymentRequest(paylineRequest)
            );
        }

        public CreateTransactionPostRequest fromCheckRequest(ContractParametersCheckRequest paylineRequest) throws InvalidRequestException {
            this.checkInputRequest(paylineRequest);

            //
            return new CreateTransactionPostRequest(
                    DEFAULT_CALLBACK,
                    this.getPaymentDetailsFromPaymentRequest(paylineRequest));
        }

        /**
         * Verifies that the input request contains all the required fields.
         *
         * @param paylineRequest The input request
         * @throws InvalidRequestException If recovering the field value would result in a NPE or if the value is null or empty.
         */
        private void checkInputRequest(PaymentRequest paylineRequest) throws InvalidRequestException {
            if (paylineRequest == null) {
                throw new InvalidRequestException("Request must not be null");
            }

            // Check attributes from PaymentRequest.Amount
            if (paylineRequest.getAmount() == null) {
                throw new InvalidRequestException("Amount properties object must not be null");
            }
            com.payline.pmapi.bean.common.Amount amount = paylineRequest.getAmount();
            if (amount.getAmountInSmallestUnit() == null) {
                throw new InvalidRequestException("Missing Amount property: amount smallest unit");
            }
            if (amount.getCurrency() == null) {
                throw new InvalidRequestException("Missing Amount property: currency");
            }

            // Check attributes from PaymentRequest.Order
            if (paylineRequest.getOrder() == null) {
                throw new InvalidRequestException("Order properties object must not be null");
            }
            Order order = paylineRequest.getOrder();
            if (order.getReference() == null) {
                throw new InvalidRequestException("Missing Order property: reference");
            }

            // Check attributes from PaymentRequest.ContractConfiguration
            if (paylineRequest.getContractConfiguration() == null
                    || paylineRequest.getContractConfiguration().getContractProperties() == null) {
                throw new InvalidRequestException("ContractConfiguration properties object must not be null");
            }
            if ( paylineRequest.getContractConfiguration().getProperty(CONTRACT_CONFIG_MERCHANT_NAME) == null) {
                throw new InvalidRequestException("Missing ContractConfiguration property: merchant name");
            }

            // Check attributes from PaymentRequest.PaylineEnvironment
            if (paylineRequest.getEnvironment() == null) {
                throw new InvalidRequestException("PaylineEnvironment properties object must not be null");
            }
            Environment paylineEnvironment = paylineRequest.getEnvironment();
            if (paylineEnvironment.getRedirectionReturnURL() == null
                    || paylineEnvironment.getRedirectionReturnURL().isEmpty()) {
                throw new InvalidRequestException("Missing PaylineEnvironment property: redirection return URL");
            }

            // Check attributes from PaymentRequest.PartnerConfiguration
            if (paylineRequest.getPartnerConfiguration() == null) {
                throw new InvalidRequestException("PartnerConfiguration properties object must not be null");
            }
            if ( isEmpty( paylineRequest.getPartnerConfiguration().getProperty(PARTNER_CONFIG_SERVICE_ID) )) {
                throw new InvalidRequestException("Missing PartnerConfiguration property: service id");
            }

        }

        private void checkInputRequest(ContractParametersCheckRequest paylineRequest) throws InvalidRequestException {
            if (paylineRequest == null) {
                throw new InvalidRequestException("Request must not be null");
            }

            // Check attributes from PaymentRequest.ContractConfiguration
            if (paylineRequest.getContractConfiguration() == null
                    || paylineRequest.getContractConfiguration().getContractProperties() == null) {
                throw new InvalidRequestException("ContractConfiguration properties object must not be null");
            }
            if ( paylineRequest.getContractConfiguration().getProperty(CONTRACT_CONFIG_MERCHANT_NAME) == null) {
                throw new InvalidRequestException("Missing ContractConfiguration property: merchant name");
            }

            // Check attributes from PaymentRequest.PartnerConfiguration
            if (paylineRequest.getPartnerConfiguration() == null) {
                throw new InvalidRequestException("PartnerConfiguration properties object must not be null");
            }
            if ( isEmpty( paylineRequest.getPartnerConfiguration().getProperty(PARTNER_CONFIG_SERVICE_ID) )) {
                throw new InvalidRequestException("Missing PartnerConfiguration property: service id");
            }
        }

        /**
         * Construit la structure de données PaymentDetails à partir de la requête PaymentRequest
         *
         * @param paylineRequest
         * @return
         */
        private PaymentDetails getPaymentDetailsFromPaymentRequest(PaymentRequest paylineRequest) {
            return new PaymentDetails()
                    .service(
                            new Service().id(
                                    paylineRequest
                                            .getPartnerConfiguration()
                                            .getProperty(PARTNER_CONFIG_SERVICE_ID)
                            )
                    )
                    .orderNumber(
                            paylineRequest
                                    .getOrder()
                                    .getReference()
                    )
                    .protocol(
                            new Protocol()
                                    .type(PAYMENT_DETAILS_PROTOCOL_TYPE)
                                    .version(PAYMENT_DETAILS_PROTOCOL_VERSION)
                    )
                    .amount(
                            new Amount()
                                    .currency(
                                            paylineRequest
                                                    .getAmount()
                                                    .getCurrency()
                                                    .getCurrencyCode()
                                    )
                                    .total(
                                            paylineRequest
                                                    .getAmount()
                                                    .getAmountInSmallestUnit()
                                    )
                    )
                    .merchant(
                            new Merchant()
                                    .name(getValue(paylineRequest, CONTRACT_CONFIG_MERCHANT_NAME))
                                    .url(getValue(paylineRequest, CONTRACT_CONFIG_MERCHANT_URL))
                                    .reference(getValue(paylineRequest, CONTRACT_CONFIG_MERCHANT_REFERENCE))
                    );
        }

        private String getValue(PaymentRequest request, String key) {
            ContractProperty property = request.getContractConfiguration().getProperty(key);
            if (property == null) {
                return null;
            } else {
                return property.getValue();
            }
        }

        private PaymentDetails getPaymentDetailsFromPaymentRequest(ContractParametersCheckRequest paylineRequest) {
            return new PaymentDetails()
                    .amount(new Amount().currency("USD").total(BigInteger.valueOf(1)))
                    .merchant(new Merchant()
                            .name( paylineRequest.getContractConfiguration()
                                    .getProperty(CONTRACT_CONFIG_MERCHANT_NAME)
                                    .getValue()))
                    .orderNumber("0001")
                    .protocol(new Protocol()
                            .type(PAYMENT_DETAILS_PROTOCOL_TYPE)
                            .version(PAYMENT_DETAILS_PROTOCOL_VERSION))
                    .service(new Service()
                            .id(paylineRequest.getPartnerConfiguration().getProperty(PARTNER_CONFIG_SERVICE_ID))
                    );
        }

    }

}