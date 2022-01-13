package com.payline.payment.samsung.pay.service;

import com.payline.payment.samsung.pay.exception.PluginException;
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

import static com.payline.payment.samsung.pay.utils.constants.LogoConstants.*;


public abstract class LogoPaymentFormConfigurationService implements PaymentFormConfigurationService {

    private static final  Logger LOGGER = LogManager.getLogger(LogoPaymentFormConfigurationService.class);
    private I18nService i18nservice = I18nService.getInstance();

    @Override
    public PaymentFormLogoResponse getPaymentFormLogo(PaymentFormLogoRequest paymentFormLogoRequest) {
        Properties props = new Properties();
        try {
            props = getProprities(props);
            return PaymentFormLogoResponseFile.PaymentFormLogoResponseFileBuilder.aPaymentFormLogoResponseFile()
                    .withHeight(Integer.valueOf(props.getProperty(LOGO_HEIGHT)))
                    .withWidth(Integer.valueOf(props.getProperty(LOGO_WIDTH)))
                    .withTitle(i18nservice.getMessage(props.getProperty(LOGO_TITLE), paymentFormLogoRequest.getLocale()))
                    .withAlt(i18nservice.getMessage(props.getProperty(LOGO_ALT), paymentFormLogoRequest.getLocale()))
                    .build();
        } catch (IOException e) {
            LOGGER.error("An error occurred reading the file logo.properties", e);
            throw new PluginException("Failed to reading file logo.properties: ", e);

        }
    }

    @Override
    public PaymentFormLogo getLogo(String s, Locale locale) {
        Properties props = new Properties();
        try {
            props = getProprities(props);
        } catch (IOException e) {
            LOGGER.error("Fail reading the file logo.properties", e);
            throw new PluginException("Failed to reading file logo.properties: ", e);

        }
        final String fileName = props.getProperty(LOGO_FILE_NAME);
        try {
            // Read logo file
            final BufferedImage logo = getBufferedImage(fileName);

            // Recover byte array from image
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(logo, props.getProperty(LOGO_FORMAT), baos);

            return PaymentFormLogo.PaymentFormLogoBuilder.aPaymentFormLogo()
                    .withFile(baos.toByteArray())
                    .withContentType(props.getProperty(LOGO_CONTENT_TYPE))
                    .build();
        } catch (IOException e) {
            LOGGER.error("Unable to load the logo", e);
            throw new PluginException("Unable to load the logo: ", e);
        }
    }

    protected Properties getProprities(Properties props) throws IOException {
        props.load(ConfigurationServiceImpl.class.getClassLoader().getResourceAsStream(LOGO_PROPERTIES));
        return props;
    }

    protected BufferedImage getBufferedImage(String fileName) throws IOException {
        final InputStream input = PaymentFormConfigurationService.class.getClassLoader().getResourceAsStream(fileName);
        return ImageIO.read(input);
    }
}
