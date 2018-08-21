package com.payline.payment.samsung.pay.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import com.payline.payment.samsung.pay.exception.InvalidRequestException;
import com.payline.payment.samsung.pay.utils.config.ConfigProperties;
import com.payline.payment.samsung.pay.utils.http.JsonHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.PaymentResponseFailure;

import okhttp3.Response;

import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.*;

/**
 * This abstract service handles the common issues encountered when sending, receiving and processing a {@link PaymentRequest} (or subclass)
 * It delegates the specific parts to the classes that will extends it, through the abstract methods.
 * This way, most of the exception handling can be done here, once.
 */
public abstract class AbstractPaymentHttpService<T extends PaymentRequest> {

    private static final Logger logger = LogManager.getLogger( AbstractPaymentHttpService.class );

    private static final String DEFAULT_ERROR_CODE = "no code transmitted";

    protected JsonHttpClient httpClient;

    protected AbstractPaymentHttpService() {

        this.httpClient = new JsonHttpClient(
                Integer.parseInt( ConfigProperties.get(CONFIG__HTTP_CONNECT_TIMEOUT) ),
                Integer.parseInt( ConfigProperties.get(CONFIG__HTTP_WRITE_TIMEOUT) ),
                Integer.parseInt( ConfigProperties.get(CONFIG__HTTP_READ_TIMEOUT) )
        );

    }

    /**
     * Builds the request, sends it through HTTP using the httpClient and recovers the response.
     *
     * @param paymentRequest The input request provided by Payline
     * @return The {@link Response} from the HTTP call
     * @throws IOException Can be thrown while sending the HTTP request
     * @throws InvalidRequestException Thrown if the input request in not valid
     * @throws NoSuchAlgorithmException Thrown if the HMAC algorithm is not available
     */
    public abstract Response createSendRequest(T paymentRequest ) throws IOException, InvalidRequestException, NoSuchAlgorithmException;

    /**
     * Process the response from the HTTP call.
     * It focuses on business aspect of the processing : the technical part has already been done by {@link #processRequest(PaymentRequest)} .
     *
     * @param response The {@link Response} from the HTTP call, which HTTP code is 200 and which body is not null.
     * @return The {@link PaymentResponse}
     * @throws IOException Can be thrown while reading the response body
     */
    public abstract PaymentResponse processResponse( Response response ) throws IOException;

    /**
     * Process a {@link PaymentRequest} (or subclass), handling all the generic error cases
     *
     * @param paymentRequest The input request from Payline
     * @return The corresponding {@link PaymentResponse}
     */
    protected PaymentResponse processRequest( T paymentRequest ) {

        try {

            // Mandate the child class to create and send the request (which is specific to each implementation)
            Response response = this.createSendRequest( paymentRequest );

            if ( response != null && response.code() == 200 && response.body() != null ) {
                // Mandate the child class to process the request when it's OK (which is specific to each implementation)
                return this.processResponse( response );
            } else if ( response != null && response.code() != 200 ) {
                logger.error( "An HTTP error occurred while sending the request: " + response.message() );
                return buildPaymentResponseFailure( Integer.toString( response.code() ), FailureCause.COMMUNICATION_ERROR );
            } else {
                logger.error( "The HTTP response or its body is null and should not be" );
                return buildPaymentResponseFailure( DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR );
            }

        } catch ( InvalidRequestException e ) {
            logger.error( "The input payment request is invalid: " + e.getMessage() );
            return buildPaymentResponseFailure( DEFAULT_ERROR_CODE, FailureCause.INVALID_DATA );
        } catch ( IOException e ) {
            logger.error( "An IOException occurred while sending the HTTP request or receiving the response: " + e.getMessage() );
            return buildPaymentResponseFailure( DEFAULT_ERROR_CODE, FailureCause.COMMUNICATION_ERROR );
        } catch ( Exception e ) {
            logger.error( "An unexpected error occurred: ", e );
            return buildPaymentResponseFailure( DEFAULT_ERROR_CODE, FailureCause.INTERNAL_ERROR );
        }

    }

    /**
     * Utility method to instantiate {@link PaymentResponseFailure} objects, using the class' builder.
     *
     * @param errorCode The error code
     * @param failureCause The failure cause
     * @return The instantiated object
     */
    protected PaymentResponseFailure buildPaymentResponseFailure( String errorCode, FailureCause failureCause ) {

        return PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                .withFailureCause( failureCause )
                .withErrorCode( errorCode )
                .build();

    }

}
