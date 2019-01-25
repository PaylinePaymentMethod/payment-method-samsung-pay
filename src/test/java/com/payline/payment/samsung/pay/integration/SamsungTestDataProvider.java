package com.payline.payment.samsung.pay.integration;

import com.payline.payment.samsung.pay.utils.JweDecrypt;
import com.payline.payment.samsung.pay.utils.SamsungPayConstants;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.PaymentFormContext;
import com.payline.pmapi.bean.payment.RequestContext;
import com.payline.pmapi.integration.bean.ExpectedConfigurationError;
import com.payline.pmapi.integration.bean.PaymentMethodTestDataProvider;
import com.payline.pmapi.integration.bean.PaymentMethodType;
import com.payline.pmapi.integration.helper.IntegrationTestHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.CONTRACT_CONFIG_MERCHANT_NAME;

public class SamsungTestDataProvider implements PaymentMethodTestDataProvider {

    public static final String MERCHANT_ID = "virtual shop";
    private static final String SERVICE_ID = "db1294c3c8bc42fe9ce762";

    private static final String SANDBOX_URL_API = "https://api-ops.stg.mpay.samsung.com/";
    private static final String SANDBOX_URL_JS = "https://d35p4vvdul393k.cloudfront.net/sdk_library/us/stg/ops/pc_gsmpi_web_sdk.js";

    private static final String EMAIL = "payline.pilote@monext.net";

    @Override
    public PartnerConfiguration getPartnerConfiguration() {
        String PRIVATE_KEY_STG = null;
        try {
            PRIVATE_KEY_STG = new String(Files.readAllBytes(Paths.get(JweDecrypt.class.getClassLoader().getResource("keystore/encodedPrivateKey.txt").toURI())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, String> partnerConfigMap = new HashMap<>();
        partnerConfigMap.put(SamsungPayConstants.PARTNER_CONFIG_SERVICE_ID, SERVICE_ID);
        partnerConfigMap.put(SamsungPayConstants.PARTNER_URL_API_SANDBOX, SANDBOX_URL_API);
        partnerConfigMap.put(SamsungPayConstants.PARTNER_URL_JS_SANDBOX, SANDBOX_URL_JS);

        Map<String, String> partnerConfigMapSensitive = new HashMap<>();
        partnerConfigMapSensitive.put(SamsungPayConstants.PARTNER_PRIVATE_KEY_SANDBOX, PRIVATE_KEY_STG);

        return new PartnerConfiguration(partnerConfigMap, partnerConfigMapSensitive);
    }

    @Override
    public Map<String, String> getValidAccountInfo() {
        return Collections.singletonMap(CONTRACT_CONFIG_MERCHANT_NAME, MERCHANT_ID);
    }

    @Override
    public List<ExpectedConfigurationError> getExpectedConfigurationError() {
        return null;
    }

    @Override
    public PaymentFormContext getPaymentFormContext(final RequestContext previousRequestContext) {
        return null;
    }

    @Override
    public EnumSet<PaymentMethodType> getPaymentMethodType() {
        return EnumSet.of(PaymentMethodType.EXT_WALLET, PaymentMethodType.REDIRECT);
    }

    @Override
    public String payOnPartnerWebsite(final WebDriver webDriver) {
        webDriver.findElement(By.id("email")).sendKeys(EMAIL);
        ((ChromeDriver) webDriver).findElementByXPath("/html/body/div/div[2]/div[1]/div[3]/div/button[2]").click();

        // Wait for redirection to success or cancel url
        WebDriverWait wait = new WebDriverWait(webDriver, 60 * 5);
        wait.until(ExpectedConditions.or(ExpectedConditions.urlContains(IntegrationTestHelper.SUCCESS_URL), ExpectedConditions.urlToBe(IntegrationTestHelper.CANCEL_URL)));

        return webDriver.getCurrentUrl();
    }
}
