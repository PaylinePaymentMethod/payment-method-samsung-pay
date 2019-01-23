package com.payline.payment.samsung.pay.service;

import com.payline.payment.samsung.pay.utils.i18n.I18nService;
import com.payline.pmapi.bean.paymentform.bean.form.NoFieldForm;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.*;

/**
 * Created by Thales on 27/08/2018.
 */
public class PaymentFormConfigurationServiceImpl implements ThalesPaymentFormConfigurationService {
    private static final Logger LOGGER = LogManager.getLogger(PaymentFormConfigurationServiceImpl.class);

    private I18nService i18n;

    public PaymentFormConfigurationServiceImpl() {
        i18n = I18nService.getInstance();
    }


    @Override
    public PaymentFormConfigurationResponse getPaymentFormConfiguration(PaymentFormConfigurationRequest request) {
        NoFieldForm noFieldForm = NoFieldForm.NoFieldFormBuilder
                .aNoFieldForm()
                .withDisplayButton(NOFIELDFORM_DISPLAY_PAYMENT_BUTTON)
                .withButtonText(i18n.getMessage(NOFIELDFORM_BUTTON_TEXT, request.getLocale()))
                .withDescription(i18n.getMessage(NOFIELDFORM_BUTTON_DESCRIPTION, request.getLocale()))
                .build();

        return PaymentFormConfigurationResponseSpecific.PaymentFormConfigurationResponseSpecificBuilder
                .aPaymentFormConfigurationResponseSpecific()
                .withPaymentForm(noFieldForm)
                .build();
    }

}