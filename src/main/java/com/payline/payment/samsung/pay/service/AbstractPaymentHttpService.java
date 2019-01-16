package com.payline.payment.samsung.pay.service;

import com.payline.payment.samsung.pay.bean.rest.request.PaymentCredentialGetRequest;
import com.payline.payment.samsung.pay.bean.rest.response.AbstractJsonResponse;
import com.payline.payment.samsung.pay.bean.rest.response.PaymentCredentialGetResponse;
import com.payline.payment.samsung.pay.bean.rest.response.nesteed.DecryptedCard;
import com.payline.payment.samsung.pay.exception.DecryptException;
import com.payline.payment.samsung.pay.exception.ExternalCommunicationException;
import com.payline.payment.samsung.pay.exception.InvalidRequestException;
import com.payline.payment.samsung.pay.utils.JweDecrypt;
import com.payline.payment.samsung.pay.utils.http.SamsungPayHttpClient;
import com.payline.payment.samsung.pay.utils.http.StringResponse;
import com.payline.payment.samsung.pay.utils.type.WSRequestResultEnum;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentModeCard;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.Card;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseDoPayment;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.*;

/**
 * This abstract service handles the common issues encountered when sending, receiving and processing a {@link PaymentRequest} (or subclass)
 * It delegates the specific parts to the classes that will extends it, through the abstract methods.
 * This way, most of the exception handling can be done here, once.
 */
public abstract class AbstractPaymentHttpService<T extends PaymentRequest> {

    private static final Logger LOGGER = LogManager.getLogger(AbstractPaymentHttpService.class);

    protected SamsungPayHttpClient httpClient;
    protected JweDecrypt jweDecrypt;

    protected AbstractPaymentHttpService() {
        this.httpClient = SamsungPayHttpClient.getInstance();
        this.jweDecrypt = JweDecrypt.getInstance();
    }

    /**
     * Builds the request, sends it through HTTP using the httpClient and recovers the response.
     *
     * @param paymentRequest The input request provided by Payline
     * @return The {@link StringResponse} from the HTTP call
     * @throws IOException              Can be thrown while sending the HTTP request
     * @throws InvalidRequestException  Thrown if the input request in not valid
     * @throws NoSuchAlgorithmException Thrown if the HMAC algorithm is not available
     */
    public abstract StringResponse createSendRequest(T paymentRequest) throws IOException, InvalidRequestException, URISyntaxException, ExternalCommunicationException;

    /**
     * Process the response from the HTTP call.
     * It focuses on business aspect of the processing : the technical part has already been done by {@link #processRequest(PaymentRequest)} .
     *
     * @param response The {@link StringResponse} from the HTTP call, which HTTP code is 200 and which body is not null.
     * @return The {@link PaymentResponse}
     * @throws IOException Can be thrown while reading the response body
     */
    public abstract PaymentResponse processResponse(StringResponse response) throws IOException;

    /**
     * Process a {@link PaymentRequest} (or subclass), handling all the generic error cases
     *
     * @param paymentRequest The input request from Payline
     * @return The corresponding {@link PaymentResponse}
     */
    public PaymentResponse processRequest(T paymentRequest) {
        try {
            // Mandate the child class to create and send the request (which is specific to each implementation)
            StringResponse response = this.createSendRequest(paymentRequest);
            if (response != null && (response.getCode() == HTTP_OK || response.getCode() == HTTP_CREATED) && response.getContent() != null) {
                // Mandate the child class to process the request when it's OK (which is specific to each implementation)
                return this.processResponse(response);
            } else if (response != null && response.getCode() != HTTP_OK) {
                LOGGER.error("An HTTP error occurred while sending the request: " + response.getContent());
                return buildPaymentResponseFailure(Integer.toString(response.getCode()), FailureCause.COMMUNICATION_ERROR);
            } else {
                LOGGER.error("The HTTP response or its body is null and should not be");
                return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);
            }

        } catch (InvalidRequestException e) {
            LOGGER.error("The input payment request is invalid", e);
            return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INVALID_DATA);
        } catch (IOException e) {
            LOGGER.error("An IOException occurred while sending the HTTP request or receiving the response", e);
            return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.COMMUNICATION_ERROR);
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred", e);
            return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);
        }

    }

    public PaymentResponse processDirectRequest(T paymentRequest, String refId) {
        try {
            // Mandate the child class to create and send the request (which is specific to each implementation)
            StringResponse response = this.createGetCredentialRequest(paymentRequest, refId);
            if (response != null && (response.getCode() == HTTP_OK || response.getCode() == HTTP_CREATED) && response.getContent() != null) {
                // Mandate the child class to process the request when it's OK (which is specific to each implementation)
                return this.processDirectResponse(paymentRequest, response);
            } else if (response != null && response.getCode() != HTTP_OK) {
                LOGGER.error("An HTTP error occurred while sending the request: " + response.getContent());
                return buildPaymentResponseFailure(Integer.toString(response.getCode()), FailureCause.COMMUNICATION_ERROR);
            } else {
                LOGGER.error("The HTTP response or its body is null and should not be");
                return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);
            }

        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred", e);
            return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR);
        }

    }


    /**
     * @param response
     * @return
     */
    protected PaymentResponse processGenericErrorResponse(AbstractJsonResponse response) {
        WSRequestResultEnum resultEnum = WSRequestResultEnum.fromResultCodeValue(response.getResultCode());
        if (resultEnum != null) {
            return buildPaymentResponseFailure(response.getResultMessage(), resultEnum.getPaylineResult());
        } else {
            return buildPaymentResponseFailure(response.getResultMessage(), FailureCause.INVALID_DATA);
        }
    }

    /**
     * Utility method to instantiate {@link PaymentResponseFailure} objects, using the class' builder.
     *
     * @param errorCode    The error code
     * @param failureCause The failure cause
     * @return The instantiated object
     */
    protected PaymentResponseFailure buildPaymentResponseFailure(String errorCode, FailureCause failureCause) {

        return PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                .withFailureCause(failureCause)
                .withErrorCode(errorCode)
                .build();

    }

    public StringResponse createGetCredentialRequest(PaymentRequest paymentRequest, String referenceId) throws URISyntaxException, ExternalCommunicationException {

        // Create PaymentCredential request form Payline request
        PaymentCredentialGetRequest paymentCredentialGetRequest = new PaymentCredentialGetRequest(referenceId, paymentRequest.getPartnerConfiguration().getProperty(PARTNER_CONFIG_SERVICE_ID));

        // Send PaymentCredential request
        String hostKey = paymentRequest.getEnvironment().isSandbox() ? PARTNER_URL_API_SANDBOX : PARTNER_URL_API_PROD;
        String host = paymentRequest.getPartnerConfiguration().getProperty(hostKey);
        String path = GET_PAYMENT_CREDENTIALS_PATH + "/" + paymentCredentialGetRequest.getId();

        // Build the request query attributes map
        Map<String, String> queryAttributes = new HashMap<>();
        queryAttributes.put(SERVICE_ID, paymentCredentialGetRequest.getServiceId());

        return this.httpClient.doGet(host, path, queryAttributes, paymentRequest.getTransactionId());
    }


    public PaymentResponse processDirectResponse(T paymentRequest, StringResponse response) {

        // Parse response
        PaymentCredentialGetResponse paymentCredentialGetResponse = new PaymentCredentialGetResponse.Builder().fromJson(response.getContent());

        if (paymentCredentialGetResponse.isResultOk()) {
            try {
                // get data for decryption step
                String cipheredData = paymentCredentialGetResponse.getData3DS().getData();
                String property = paymentRequest.getEnvironment().isSandbox() ? PARTNER_PRIVATE_KEY_SANDBOX : PARTNER_PRIVATE_KEY_PROD;
                byte[] privateKey = paymentRequest.getPartnerConfiguration().getProperty(property).getBytes();

                // Decrypt 3DS data to retrieve Payment Mode info
                String cardData = jweDecrypt.getDecryptedData(cipheredData, privateKey);
                DecryptedCard decryptedCard = new DecryptedCard.Builder().fromJson(cardData);

                Card card = Card.CardBuilder.aCard()
                        .withPan(decryptedCard.getTokenPan())
                        .withExpirationDate(YearMonth.of(decryptedCard.getExpiryYear(), decryptedCard.getExpiryMonth()))
                        .build();

                PaymentModeCard paymentModeCard = PaymentModeCard.PaymentModeCardBuilder.aPaymentModeCard()
                        .withCard(card)
                        .build();

                return PaymentResponseDoPayment.PaymentResponseDoPaymentBuilder.aPaymentResponseDoPayment()
                        .withPaymentMode(paymentModeCard)
                        .withPartnerTransactionId(paymentRequest.getTransactionId())
                        .build();
            } catch (DecryptException e) {
                LOGGER.error("Unable to decrypt data", e);
                return PaymentResponseFailure.PaymentResponseFailureBuilder
                        .aPaymentResponseFailure()
                        .withFailureCause(FailureCause.INVALID_FIELD_FORMAT)
                        .withPartnerTransactionId(paymentRequest.getTransactionId())
                        .build();
            }

        } else {
            return this.processGenericErrorResponse(paymentCredentialGetResponse);
        }
    }

}
