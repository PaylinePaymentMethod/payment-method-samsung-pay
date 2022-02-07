package com.payline.payment.samsung.pay.service;

import com.payline.payment.samsung.pay.exception.PluginException;
import com.payline.pmapi.bean.paymentform.bean.PaymentFormLogo;
import com.payline.pmapi.bean.paymentform.request.PaymentFormLogoRequest;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import com.payline.pmapi.bean.paymentform.response.logo.PaymentFormLogoResponse;
import com.payline.pmapi.bean.paymentform.response.logo.impl.PaymentFormLogoResponseFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import static com.payline.payment.samsung.pay.utils.Utils.*;
import static com.payline.payment.samsung.pay.utils.constants.LogoConstants.LOGO_FILE_NAME;
import static com.payline.payment.samsung.pay.utils.constants.LogoConstants.LOGO_PROPERTIES;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

/**
 * Created by Thales on 27/08/2018.
 */
@ExtendWith(MockitoExtension.class)
public class PaymentFormConfigurationServiceImplTest {

    @Spy
    @InjectMocks
    private PaymentFormConfigurationServiceImpl service;

    @Mock
    Properties properties;

    public static final String FAILURE_TRANSACTION_ID = "NO TRANSACTION YET";

    @Test
    public void testGetPaymentFormConfiguration() {
        // when: getPaymentFormConfiguration is called
        PaymentFormConfigurationResponse response = service.getPaymentFormConfiguration(createDefaultPaymentFormConfigurationRequest());

        // then: returned object is an instance of PaymentFormConfigurationResponseSpecific
        assertTrue(response instanceof PaymentFormConfigurationResponseSpecific);
    }

    @Test
    public void testGetLogo() {
        // when: getLogo is called
        PaymentFormLogo paymentFormLogo = service.getLogo("", Locale.getDefault());

        // then: returned elements are not null
        assertNotNull(paymentFormLogo);
        assertNotNull(paymentFormLogo.getFile());
        assertNotNull(paymentFormLogo.getContentType());
    }

    @Test
    public void testGetLogoShouldThrowException() throws IOException {
        Properties props =new Properties();
        doThrow(IOException.class).when(service).getProprities(props);
        Assertions.assertThrows(PluginException.class, () -> service.getLogo("", Locale.getDefault()));
    }

    @Test
    public void testGetLogoShouldThrowIOException() throws IOException {
        Properties props = new Properties();
        final String fileName = "fileName";
        doReturn(properties).when(service).getProprities(props);
        doReturn(fileName).when(properties).getProperty(LOGO_FILE_NAME);
        doThrow(IOException.class).when(service).getBufferedImage(fileName);
        Assertions.assertThrows(PluginException.class, () -> service.getLogo("", Locale.getDefault()));
    }

    @Test
    public void testGetPaymentFormLogo() throws IOException {
        Properties props = new Properties();
        props.load(ConfigurationServiceImpl.class.getClassLoader().getResourceAsStream(LOGO_PROPERTIES));

        String fileName = props.getProperty(LOGO_FILE_NAME);
        // given: the logo image read from resources
        InputStream input = PaymentFormConfigurationServiceImpl.class.getClassLoader().getResourceAsStream(fileName);
        BufferedImage image = ImageIO.read(input);

        // when: getPaymentFormLogo is called
        PaymentFormLogoRequest request = PaymentFormLogoRequest.PaymentFormLogoRequestBuilder.aPaymentFormLogoRequest()
                .withLocale(Locale.getDefault())
                .withEnvironment(createDefaultPaylineEnvironment())
                .withContractConfiguration(createDefaultContractConfiguration())
                .withPartnerConfiguration(createDefaultPartnerConfiguration())
                .build();
        PaymentFormLogoResponse paymentFormLogoResponse = service.getPaymentFormLogo(request);

        // then: returned elements match the image file data
        assertTrue(paymentFormLogoResponse instanceof PaymentFormLogoResponseFile);
        PaymentFormLogoResponseFile casted = (PaymentFormLogoResponseFile) paymentFormLogoResponse;
        assertEquals(image.getHeight(), casted.getHeight());
        assertEquals(image.getWidth(), casted.getWidth());
        assertNotNull(casted.getTitle());
        assertNotNull(casted.getAlt());
    }

    @Test
    public void testGetPaymentFormLogoShouldThrowInvalidDataException() throws IOException {
        Properties props =new Properties();
        PaymentFormLogoRequest request = PaymentFormLogoRequest.PaymentFormLogoRequestBuilder.aPaymentFormLogoRequest()
                .withLocale(Locale.getDefault())
                .withEnvironment(createDefaultPaylineEnvironment())
                .withContractConfiguration(createDefaultContractConfiguration())
                .withPartnerConfiguration(createDefaultPartnerConfiguration())
                .build();

        doThrow(IOException.class).when(service).getProprities(props);
        assertThrows(PluginException.class, () -> service.getPaymentFormLogo(request));
    }

    @Test
    public void getBufferedImageOK() throws IOException {
        assertNotNull(service.getBufferedImage("samsung-pay-logo.png"));
    }

    @Test
    public void getPropritiesShouldThrowException() throws IOException {
        Properties props =new Properties();
        doThrow(IOException.class).when(service).getProprities(props);
        Assertions.assertThrows(IOException.class, () -> service.getProprities(props));
    }

    @Test
    public void getPropritiesOK() throws IOException {
        Properties props =new Properties();
        assertEquals(props, service.getProprities(props));
    }
}