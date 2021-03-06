package com.payline.payment.samsung.pay.service;

import com.payline.payment.samsung.pay.utils.i18n.I18nService;
import com.payline.pmapi.bean.paymentform.bean.PaymentFormLogo;
import com.payline.pmapi.bean.paymentform.request.PaymentFormLogoRequest;
import com.payline.pmapi.bean.paymentform.response.logo.PaymentFormLogoResponse;
import com.payline.pmapi.bean.paymentform.response.logo.impl.PaymentFormLogoResponseFile;
import com.payline.pmapi.service.PaymentFormConfigurationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import static com.payline.payment.samsung.pay.utils.propertiesFilesConstants.LogoConstants.*;


public interface DefaultPaymentFormConfigurationService extends PaymentFormConfigurationService {

    Logger LOGGER = LogManager.getLogger(DefaultPaymentFormConfigurationService.class);
    I18nService i18n = I18nService.getInstance();

    @Override
    default PaymentFormLogoResponse getPaymentFormLogo(PaymentFormLogoRequest paymentFormLogoRequest) {
        Properties props = new Properties();
        try {
            props.load(ConfigurationServiceImpl.class.getClassLoader().getResourceAsStream(LOGO_PROPERTIES));
            return PaymentFormLogoResponseFile.PaymentFormLogoResponseFileBuilder.aPaymentFormLogoResponseFile()
                    .withHeight(Integer.valueOf(props.getProperty(LOGO_HEIGHT)))
                    .withWidth(Integer.valueOf(props.getProperty(LOGO_WIDTH)))
                    .withTitle(i18n.getMessage(props.getProperty(LOGO_TITLE), paymentFormLogoRequest.getLocale()))
                    .withAlt(i18n.getMessage(props.getProperty(LOGO_ALT), paymentFormLogoRequest.getLocale()))
                    .build();
        } catch (IOException e) {
            LOGGER.error("An error occurred reading the file logo.properties", e);
            throw new RuntimeException("Failed to reading file logo.properties: ", e);

        }
    }

    @Override
    default PaymentFormLogo getLogo(String s, Locale locale) {
        Properties props = new Properties();
        try {
            props.load(ConfigurationServiceImpl.class.getClassLoader().getResourceAsStream(LOGO_PROPERTIES));
        } catch (IOException e) {
            LOGGER.error("An error occurred reading the file logo.properties", e);
            throw new RuntimeException("Failed to reading file logo.properties: ", e);

        }
        String fileName = props.getProperty(LOGO_FILE_NAME);
        try {
            // Read logo file
            InputStream input = DefaultPaymentFormConfigurationService.class.getClassLoader().getResourceAsStream(fileName);
            BufferedImage logo = ImageIO.read(input);

            // Recover byte array from image
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(logo, props.getProperty(LOGO_FORMAT), baos);

            return PaymentFormLogo.PaymentFormLogoBuilder.aPaymentFormLogo()
                    .withFile(baos.toByteArray())
                    .withContentType(props.getProperty(LOGO_CONTENT_TYPE))
                    .build();
        } catch (IOException e) {
            LOGGER.error("Unable to load the logo", e);
            throw new RuntimeException(e);
        }
    }
}
