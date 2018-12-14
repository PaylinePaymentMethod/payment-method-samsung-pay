package com.payline.payment.samsung.pay.service;

import com.payline.payment.samsung.pay.bean.rest.request.CreateTransactionPostRequest;
import com.payline.payment.samsung.pay.bean.rest.response.CreateTransactionPostResponse;
import com.payline.payment.samsung.pay.exception.ExternalCommunicationException;
import com.payline.payment.samsung.pay.exception.InvalidRequestException;
import com.payline.payment.samsung.pay.utils.SamsungPayConstants;
import com.payline.payment.samsung.pay.utils.http.StringResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFormUpdated;
import com.payline.pmapi.bean.paymentform.bean.form.PartnerWidgetForm;
import com.payline.pmapi.bean.paymentform.bean.form.partnerwidget.PartnerWidgetContainer;
import com.payline.pmapi.bean.paymentform.bean.form.partnerwidget.PartnerWidgetContainerTargetDivId;
import com.payline.pmapi.bean.paymentform.bean.form.partnerwidget.PartnerWidgetOnPay;
import com.payline.pmapi.bean.paymentform.bean.form.partnerwidget.PartnerWidgetOnPayCallBack;
import com.payline.pmapi.bean.paymentform.bean.form.partnerwidget.PartnerWidgetScriptImport;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import com.payline.pmapi.service.PaymentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.CREATE_TRANSACTION_PATH;
import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.DEV_HOST;
import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.PARTNER_CONFIG_SERVICE_ID;
import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.PROD_HOST;
import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.SCHEME;

/**
 * Created by Thales on 16/08/2018.
 */
public class PaymentServiceImpl extends AbstractPaymentHttpService<PaymentRequest> implements PaymentService {

    private static final Logger LOGGER = LogManager.getLogger(PaymentServiceImpl.class);

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
        return this.processRequest(paymentRequest);
    }

    @Override
    public StringResponse createSendRequest(PaymentRequest paymentRequest) throws IOException, InvalidRequestException, URISyntaxException, ExternalCommunicationException {

        // Create CreateTransaction request from Payline request
        CreateTransactionPostRequest createTransactionPostRequest = this.requestBuilder.fromPaymentRequest(paymentRequest);

        // Send CreateTransaction request
        String host = paymentRequest.getEnvironment().isSandbox() ? DEV_HOST : PROD_HOST;
        return this.httpClient.doPost(SCHEME, host, CREATE_TRANSACTION_PATH, createTransactionPostRequest.buildBody(), paymentRequest.getTransactionId());

    }

    @Override
    public PaymentResponse processResponse(StringResponse response) throws MalformedURLException {

        // Parse response
        CreateTransactionPostResponse createTransactionPostResponse = new CreateTransactionPostResponse.Builder().fromJson(response.getContent());

        if (createTransactionPostResponse.isResultOk()) {
            // create the response object

            PartnerWidgetScriptImport scriptImport = PartnerWidgetScriptImport.WidgetPartnerScriptImportBuilder
                    .aWidgetPartnerScriptImport()
                    .withUrl(new URL(SamsungPayConstants.JAVASCRIPT_URL))
                    .withCache(true)
                    .withAsync(true)
                    .build();

            // this object is not used because the SamsungPay widget is shown with an overlay and not in a specific div
            PartnerWidgetContainer container = PartnerWidgetContainerTargetDivId.WidgetPartnerContainerTargetDivIdBuilder
                    .aWidgetPartnerContainerTargetDivId()
                    .withId("notUsedButMandatory")
                    .build();

            // this object is not used because the SansungPay widget does automatically the redirect when transaction is done
            PartnerWidgetOnPay onPay = PartnerWidgetOnPayCallBack.WidgetContainerOnPayCallBackBuilder
                    .aWidgetContainerOnPayCallBack()
                    .withName("notUsedButMandatory")
                    .build();

            // temporary load the samsung.js file
            String script = "";
            try{
                script = loadFile();
            }catch (Exception e){
                LOGGER.error("Unable to load the script", e);
                return buildPaymentResponseFailure("Unable to load samsungPay script", FailureCause.INTERNAL_ERROR);
            }

            PartnerWidgetForm paymentForm = PartnerWidgetForm.WidgetPartnerFormBuilder.aWidgetPartnerForm()
                    //.withDisplayButton(false)    // the "pay" button is embedded in SamsungPay.js
                    .withDescription("")
                    .withScriptImport(scriptImport)
                    .withLoadingScriptBeforeImport(script)
                    .withLoadingScriptAfterImport(createConnectCall(paymentRequest, createTransactionPostResponse))
                    .withContainer(container)
                    .withOnPay(onPay)
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


    public String loadFile() throws IOException {
        String file;
        try(InputStream in = getClass().getResourceAsStream("/samsung.js");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        Stream<String> lines = reader.lines()) {
            file = lines.collect(Collectors.joining("\n"));
        }
        return file;
    }
}