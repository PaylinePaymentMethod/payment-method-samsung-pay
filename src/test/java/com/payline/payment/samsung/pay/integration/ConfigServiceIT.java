package com.payline.payment.samsung.pay.integration;

import com.payline.payment.samsung.pay.service.ConfigurationServiceImpl;
import com.payline.payment.samsung.pay.utils.JweDecrypt;
import com.payline.payment.samsung.pay.utils.SamsungPayConstants;
import com.payline.payment.samsung.pay.utils.Utils;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.CONTRACT_CONFIG_MERCHANT_NAME;
import static com.payline.payment.samsung.pay.utils.Utils.SANDBOX_URL_API;
import static com.payline.payment.samsung.pay.utils.Utils.SANDBOX_URL_JS;

public class ConfigServiceIT {

    @InjectMocks
    private ConfigurationServiceImpl service;

    @Test
    public void checkOk() {
        ContractParametersCheckRequest request = Utils.createContractParametersCheckRequest(Utils.MERCHANT_ID);
        Map<String, String> errors = service.check(request);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void checkNoMerchant() {
        ContractParametersCheckRequest request = Utils.createContractParametersCheckRequest(null);
        Map<String, String> errors = service.check(request);
        Assert.assertEquals(1, errors.size());
        Assert.assertNotNull(errors.get(CONTRACT_CONFIG_MERCHANT_NAME));
    }

    @Test
    public void checkWrongServiceId() {

        String PRIVATE_KEY_STG = null;
        try {
            PRIVATE_KEY_STG = new String(Files.readAllBytes(Paths.get(JweDecrypt.class.getClassLoader().getResource("keystore/encodedPrivateKey.txt").toURI())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, String> partnerConfigMap = new HashMap<>();
        partnerConfigMap.put(SamsungPayConstants.PARTNER_SERVICE_ID_SANDBOX, "foo");
        partnerConfigMap.put(SamsungPayConstants.PARTNER_URL_API_SANDBOX, SANDBOX_URL_API);
        partnerConfigMap.put(SamsungPayConstants.PARTNER_URL_JS_SANDBOX, SANDBOX_URL_JS);

        Map<String, String> partnerConfigMapSensitive = new HashMap<>();
        partnerConfigMapSensitive.put(SamsungPayConstants.PARTNER_PRIVATE_KEY_SANDBOX, PRIVATE_KEY_STG);


        ContractParametersCheckRequest request = Utils.createContractParametersCheckRequestBuilder(Utils.MERCHANT_ID)
                .withPartnerConfiguration(new PartnerConfiguration(partnerConfigMap, partnerConfigMapSensitive))
                .build();

        Map<String, String> errors = service.check(request);
        Assert.assertEquals(1, errors.size());
    }
}
