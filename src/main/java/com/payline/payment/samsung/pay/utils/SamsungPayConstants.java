package com.payline.payment.samsung.pay.utils;

/**
 * Created by Thales on 16/08/2018.
 */
public class SamsungPayConstants {
    public static final String DEFAULT_ERROR_CODE = "no code transmitted";

    // Data used in ConfigurationServiceImpl
    public static final String CONTRACT_CONFIG_MERCHANT_NAME = "merchantName";
    public static final String CONTRACT_CONFIG_MERCHANT_NAME_PROPERTY_LABEL = "contractConfiguration.merchantName.label";
    public static final String CONTRACT_CONFIG_MERCHANT_NAME_PROPERTY_DESCRIPTION = "contractConfiguration.merchantName.description";

    public static final String CONTRACT_CONFIG_MERCHANT_URL = "merchantUrl";
    public static final String CONTRACT_CONFIG_MERCHANT_URL_PROPERTY_LABEL = "contractConfiguration.merchantUrl.label";
    public static final String CONTRACT_CONFIG_MERCHANT_URL_PROPERTY_DESCRIPTION = "contractConfiguration.merchantUrl.description";

    public static final String CONTRACT_CONFIG_MERCHANT_REFERENCE = "merchantReference";
    public static final String CONTRACT_CONFIG_MERCHANT_REFERENCE_PROPERTY_LABEL = "contractConfiguration.merchantReference.label";
    public static final String CONTRACT_CONFIG_MERCHANT_REFERENCE_PROPERTY_DESCRIPTION = "contractConfiguration.merchantReference.description";

    public static final String CONTRACT_CONFIG_CB_PROPERTY_LABEL = "contractConfiguration.cb.label";
    public static final String CONTRACT_CONFIG_CB_PROPERTY_DESCRIPTION = "contractConfiguration.cb.description";
    public static final String CONTRACT_CONFIG_VISA_PROPERTY_LABEL = "contractConfiguration.visa.label";
    public static final String CONTRACT_CONFIG_VISA_PROPERTY_DESCRIPTION = "contractConfiguration.visa.description";
    public static final String CONTRACT_CONFIG_MASTERCARD_PROPERTY_LABEL = "contractConfiguration.mastercard.label";
    public static final String CONTRACT_CONFIG_MASTERCARD_PROPERTY_DESCRIPTION = "contractConfiguration.mastercard.description";
    public static final String CONTRACT_CONFIG_AMEX_PROPERTY_LABEL = "contractConfiguration.amex.label";
    public static final String CONTRACT_CONFIG_AMEX_PROPERTY_DESCRIPTION = "contractConfiguration.amex.description";

    public static final String DEFAULT_XREQUESTID = "123456789";
    public static final String DEFAULT_CALLBACK = "http://default.callback.url";

    // Data used in PaymentFormConfigurationServiceImpl
    public static final boolean NOFIELDFORM_DISPLAY_PAYMENT_BUTTON = true;

    public static final String NOFIELDFORM_BUTTON_TEXT = "form.button.samsungPay.text";
    public static final String NOFIELDFORM_BUTTON_DESCRIPTION = "form.button.samsungPay.description";

    // Data used in PaymentService
    public static final String EMPTY_STRING ="";
    public static final String SCRIPT_BEFORE_IMPORT ="var $ = Payline.jQuery;";
    public static final String NOT_USED_BUT_MANDATORY ="notUsedButMandatory";
    public static final String PAYMENTDATA_TOKENDATA ="PAYMENTDATA_TOKENDATA";
    public static final String REFERENCE_ID ="REFERENCE_ID";

    // Data used in https calls
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String X_REQUEST_ID = "X-Request-Id";

    public static final String REF_ID = "ref_id";

    // data used in request body creation
    public static final String PAYMENT_DETAILS_PROTOCOL_TYPE = "3DS";
    public static final String PAYMENT_DETAILS_PROTOCOL_VERSION = "80";

    public static final String PAYMENT_PROVIDER = "PAYLINE";

    // data used in http Client
    public static final String CREATE_TRANSACTION_PATH = "ops/v1/transactions";
    public static final String GET_PAYMENT_CREDENTIALS_PATH = "ops/v1/transactions/paymentCredentials";
    public static final String NOTIFICATION_PATH = "ops/v1/notifications";

    public static final String SERVICE_ID = "serviceId";

    public static final int HTTP_OK = 200;
    public static final int HTTP_CREATED = 201;

    public static final String HTTP_CODE_200 = "200";
    public static final String HTTP_CODE_400 = "400";
    public static final String HTTP_CODE_404 = "404";
    public static final String HTTP_CODE_409 = "409";
    public static final String HTTP_CODE_500 = "500";

    public static final String SAMSUNG_PAY_CODE_0 = "0";
    public static final String SAMSUNG_PAY_CODE_OPM1N1001 = "OPM1N1001";
    public static final String SAMSUNG_PAY_CODE_OPM1N1002 = "OPM1N1002";
    public static final String SAMSUNG_PAY_CODE_OPM1N1003 = "OPM1N1003";
    public static final String SAMSUNG_PAY_CODE_OPM1N1026 = "OPM1N1026";
    public static final String SAMSUNG_PAY_CODE_OPM1N1033 = "OPM1N1033";
    public static final String SAMSUNG_PAY_CODE_OPM1N1035 = "OPM1N1035";
    public static final String SAMSUNG_PAY_CODE_OPM2N1103 = "OPM2N1103";
    public static final String SAMSUNG_PAY_CODE_OPM3N1002 = "OPM3N1002";
    public static final String SAMSUNG_PAY_CODE_OPM3N1003 = "OPM3N1003";
    public static final String SAMSUNG_PAY_CODE_OPM5N9001 = "OPM5N9001";

    public static final String SAMSUNG_PAY_CODE_0_DESCRIPTION = "SUCCESS";
    public static final String SAMSUNG_PAY_CODE_OPM1N1001_DESCRIPTION = "Mandatory parameter does not exist";
    public static final String SAMSUNG_PAY_CODE_OPM1N1002_DESCRIPTION = "Parameter value is invalid";
    public static final String SAMSUNG_PAY_CODE_OPM1N1003_DESCRIPTION = "Parameter format is invalid or Could not read document";
    public static final String SAMSUNG_PAY_CODE_OPM1N1026_DESCRIPTION = "Requested is inconsistent with Id";
    public static final String SAMSUNG_PAY_CODE_OPM1N1033_DESCRIPTION = "This partner is not approved status";
    public static final String SAMSUNG_PAY_CODE_OPM1N1035_DESCRIPTION = "Service does not exist";
    public static final String SAMSUNG_PAY_CODE_OPM2N1103_DESCRIPTION = "This transaction already bound to device";
    public static final String SAMSUNG_PAY_CODE_OPM3N1002_DESCRIPTION = "Data encryption error";
    public static final String SAMSUNG_PAY_CODE_OPM3N1003_DESCRIPTION = "Internal server processing error";
    public static final String SAMSUNG_PAY_CODE_OPM5N9001_DESCRIPTION = "DB data processing error";

    // data used in JWEDecrypt
    public static final String RSA = "RSA";
    public static final String AES = "AES";
    public static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
    public static final String RSA_ECB_PKCS_1_PADDING = "RSA/ECB/PKCS1Padding";
    public static final String DELIMS = "[.]";

    // partnerConfiguration data keys
    public static final String PARTNER_CONFIG_SERVICE_ID = "serviceId";
    public static final String PARTNER_PRIVATE_KEY_SANDBOX = "sandbox.privateKey";
    public static final String PARTNER_PRIVATE_KEY_PROD = "prod.privateKey";
    public static final String PARTNER_URL_API_SANDBOX = "sandbox.url.api";
    public static final String PARTNER_URL_JS_SANDBOX = "sandbox.url.js";
    public static final String PARTNER_URL_API_PROD = "prod.url.api";
    public static final String PARTNER_URL_JS_PROD = "prod.url.js";

}