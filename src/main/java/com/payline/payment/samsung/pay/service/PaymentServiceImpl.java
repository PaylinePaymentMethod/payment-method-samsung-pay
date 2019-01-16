package com.payline.payment.samsung.pay.service;

import com.google.gson.JsonParser;
import com.payline.payment.samsung.pay.bean.rest.request.CreateTransactionPostRequest;
import com.payline.payment.samsung.pay.bean.rest.response.CreateTransactionPostResponse;
import com.payline.payment.samsung.pay.exception.ExternalCommunicationException;
import com.payline.payment.samsung.pay.exception.InvalidRequestException;
import com.payline.payment.samsung.pay.utils.SamsungPayConstants;
import com.payline.payment.samsung.pay.utils.http.StringResponse;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFormUpdated;
import com.payline.pmapi.bean.paymentform.bean.form.PartnerWidgetForm;
import com.payline.pmapi.bean.paymentform.bean.form.partnerwidget.*;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import com.payline.pmapi.service.PaymentService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.*;

/**
 * Created by Thales on 16/08/2018.
 */
public class PaymentServiceImpl extends AbstractPaymentHttpService<PaymentRequest> implements PaymentService {

    private CreateTransactionPostRequest.Builder requestBuilder;

    private PaymentRequest paymentRequest;

    /**
     * Default public constructor
     */
    public PaymentServiceImpl() {
        super();
        this.requestBuilder = new CreateTransactionPostRequest.Builder();
    }

    @Override
    public PaymentResponse paymentRequest(PaymentRequest paymentRequest) {
        this.paymentRequest = paymentRequest;

        // check if it is a direct mode
        if (paymentRequest.getPaymentFormContext().getPaymentFormParameter().containsKey(PAYMENTDATA_TOKENDATA)) {
            // we need to do the same thing as PaymentWithRedirectionService
            String refId = getRefIdFromRequest(paymentRequest);
            return this.processDirectRequest(paymentRequest, refId);

        } else {
            return this.processRequest(paymentRequest);
        }
    }

    @Override
    public StringResponse createSendRequest(PaymentRequest paymentRequest) throws IOException, InvalidRequestException, URISyntaxException, ExternalCommunicationException {

        // Create CreateTransaction request from Payline request
        CreateTransactionPostRequest createTransactionPostRequest = this.requestBuilder.fromPaymentRequest(paymentRequest);

        // Send CreateTransaction request
        String hostKey = paymentRequest.getEnvironment().isSandbox() ? PARTNER_URL_API_SANDBOX : PARTNER_URL_API_PROD;
        String host = paymentRequest.getPartnerConfiguration().getProperty(hostKey);
        return this.httpClient.doPost(host, CREATE_TRANSACTION_PATH, createTransactionPostRequest.buildBody(), paymentRequest.getTransactionId());

    }

    @Override
    public PaymentResponse processResponse(StringResponse response) throws MalformedURLException {

        // Parse response
        CreateTransactionPostResponse createTransactionPostResponse = new CreateTransactionPostResponse.Builder().fromJson(response.getContent());

        if (createTransactionPostResponse.isResultOk()) {
            // create the response object
            String samsungJsUrlKey = paymentRequest.getEnvironment().isSandbox() ? SamsungPayConstants.PARTNER_URL_JS_SANDBOX : SamsungPayConstants.PARTNER_URL_JS_PROD;
            String samsungJsUrl = paymentRequest.getPartnerConfiguration().getProperty(samsungJsUrlKey);

            PartnerWidgetScriptImport scriptImport = PartnerWidgetScriptImport.WidgetPartnerScriptImportBuilder
                    .aWidgetPartnerScriptImport()
                    .withUrl(new URL(samsungJsUrl))
                    .withCache(true)
                    .withAsync(true)
                    .build();

            // this object is not used because the SamsungPay widget is shown with an overlay and not in a specific div
            PartnerWidgetContainer container = PartnerWidgetContainerTargetDivId.WidgetPartnerContainerTargetDivIdBuilder
                    .aWidgetPartnerContainerTargetDivId()
                    .withId(NOT_USED_BUT_MANDATORY)
                    .build();

            // this object is not used because the SansungPay widget does automatically the redirect when transaction is done
            PartnerWidgetOnPay onPay = PartnerWidgetOnPayCallBack.WidgetContainerOnPayCallBackBuilder
                    .aWidgetContainerOnPayCallBack()
                    .withName(NOT_USED_BUT_MANDATORY)
                    .build();

            PartnerWidgetForm paymentForm = PartnerWidgetForm.WidgetPartnerFormBuilder.aWidgetPartnerForm()
                    .withDescription(EMPTY_STRING)
                    .withScriptImport(scriptImport)
                    .withLoadingScriptBeforeImport(SCRIPT_BEFORE_IMPORT)
                    .withLoadingScriptAfterImport(createConnectCall(paymentRequest, createTransactionPostResponse))
                    .withContainer(container)
                    .withOnPay(onPay)
                    .withPerformsAutomaticRedirection(true)
                    .build();

            PaymentFormConfigurationResponse paymentFormConfigurationResponse = PaymentFormConfigurationResponseSpecific.PaymentFormConfigurationResponseSpecificBuilder
                    .aPaymentFormConfigurationResponseSpecific()
                    .withPaymentForm(paymentForm)
                    .build();


            return PaymentResponseFormUpdated.PaymentResponseFormUpdatedBuilder
                    .aPaymentResponseFormUpdated()
                    .withPaymentFormConfigurationResponse(paymentFormConfigurationResponse)
                    .build();

        } else {
            return this.processGenericErrorResponse(createTransactionPostResponse);
        }
    }

    public void setPaymentRequest(PaymentRequest paymentRequest) {
        this.paymentRequest = paymentRequest;
    }

    public String createConnectCall(PaymentRequest request, CreateTransactionPostResponse response) {
        String functionToCall = "(function(){\n" +
                "    SamsungPay.connect('transactionId' ,'href' ,'serviceId' ,'callbackUrl' ,'cancelUrl' ,'countryCode' ,'mod' ,'exp' ,'keyId' );\n" +
                "})()";

        return functionToCall.replace("transactionId", response.getId())
                .replace("href", response.getHref())
                .replace("serviceId", request.getPartnerConfiguration().getProperty(PARTNER_CONFIG_SERVICE_ID))
                .replace("callbackUrl", request.getEnvironment().getRedirectionReturnURL())
                .replace("cancelUrl", request.getEnvironment().getRedirectionCancelURL())
                .replace("countryCode", request.getLocale().getCountry())
                .replace("mod", response.getEncryptionInfo().getMod())
                .replace("exp", response.getEncryptionInfo().getExp())
                .replace("keyId", response.getEncryptionInfo().getKeyId());
    }

    public String getRefIdFromRequest(PaymentRequest request) {
        String data = request.getPaymentFormContext().getPaymentFormParameter().get(PAYMENTDATA_TOKENDATA);
        JsonParser p = new JsonParser();
        return p.parse(data).getAsJsonObject().get(REFERENCE_ID).getAsString();
    }
}