package com.payline.payment.samsung.pay.service;

import com.payline.payment.samsung.pay.exception.ExternalCommunicationException;
import com.payline.payment.samsung.pay.utils.http.StringResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.service.PaymentWithRedirectionService;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URISyntaxException;

import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.DEFAULT_ERROR_CODE;
import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.REF_ID;

/**
 * Created by Thales on 16/08/2018.
 */
public class PaymentWithRedirectionServiceImpl extends AbstractPaymentHttpService<RedirectionPaymentRequest> implements PaymentWithRedirectionService {

    private RedirectionPaymentRequest redirectionPaymentRequest;

    /**
     * Default public constructor
     */
    public PaymentWithRedirectionServiceImpl() {
        super();
    }

    @Override
    public PaymentResponse finalizeRedirectionPayment(RedirectionPaymentRequest redirectionPaymentRequest) {
        this.redirectionPaymentRequest = redirectionPaymentRequest;
        return this.processRequest(redirectionPaymentRequest);
    }

    @Override
    public PaymentResponse handleSessionExpired(TransactionStatusRequest transactionStatusRequest) {
        return buildPaymentResponseFailure(DEFAULT_ERROR_CODE, FailureCause.SESSION_EXPIRED);
    }

    @Override
    public StringResponse createSendRequest(RedirectionPaymentRequest paymentRequest) throws URISyntaxException, ExternalCommunicationException {
        return this.createGetCredentialRequest(paymentRequest, paymentRequest.getHttpRequestParametersMap().get(REF_ID)[0]);
    }

    @Override
    public PaymentResponse processResponse(StringResponse response) {
        return this.processDirectResponse(redirectionPaymentRequest, response);
    }

    public void setRedirectionPaymentRequest(RedirectionPaymentRequest redirectionPaymentRequest) {
        this.redirectionPaymentRequest = redirectionPaymentRequest;
    }
}
