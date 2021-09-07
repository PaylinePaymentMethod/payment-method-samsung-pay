package com.payline.payment.samsung.pay.service;

import com.payline.payment.samsung.pay.exception.ExternalCommunicationException;
import com.payline.payment.samsung.pay.utils.Utils;
import com.payline.payment.samsung.pay.utils.http.SamsungPayHttpClient;
import com.payline.payment.samsung.pay.utils.http.StringResponse;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.CONTRACT_CONFIG_MERCHANT_NAME;
import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.HTTP_CREATED;
import static com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest.GENERIC_ERROR;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by Thales on 27/08/2018.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationServiceImplTest {
    private Locale locale = Locale.FRENCH;

    @Mock
    private SamsungPayHttpClient httpClient;

    @InjectMocks
    private ConfigurationServiceImpl service;
//
//    @Before
//    public void setup(){
//        httpClient = new SamsungPayHttpClient();
//    }


    @Test
    public void getParameters() {
        List<AbstractParameter> parameters = service.getParameters(locale);
        Assert.assertEquals(7, parameters.size());
    }

    @Test
    public void getReleaseInformation() {
        ReleaseInformation information = service.getReleaseInformation();
        Assert.assertFalse("01/01/1900".equals(information.getDate()));
        Assert.assertFalse("unknown".equals(information.getVersion()));
    }

    @Test
    public void checkOK() throws IOException, URISyntaxException, ExternalCommunicationException {
        String goodResponse = "{" +
                "   resultCode: 0," +
                "   resultMessage: SUCCESS," +
                "   id: 25ed71116be042d38fd09a," +
                "   href: 'https://us-online.stg.mpay.samsung.com/onlinepay'," +
                "   encInfo: { mod: XXXX, exp: XXXX, keyId: XXXX }" +
                "}";
        StringResponse mockedResponse = new StringResponse();
        mockedResponse.setContent(goodResponse);
        mockedResponse.setCode(HTTP_CREATED);
        when(httpClient.doPost(anyString(), anyString(), anyString(), anyString())).thenReturn(mockedResponse);

        ContractParametersCheckRequest request = Utils.createContractParametersCheckRequest(Utils.MERCHANT_ID);
        Map<String, String> errors = service.check(request);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void checkEmptyResponse() throws IOException, URISyntaxException, ExternalCommunicationException {
        StringResponse mockedResponse = new StringResponse();
        mockedResponse.setContent(null);
        mockedResponse.setCode(400);
        when(httpClient.doPost(anyString(), anyString(), anyString(), anyString())).thenReturn(mockedResponse);

        ContractParametersCheckRequest request = Utils.createContractParametersCheckRequest(Utils.MERCHANT_ID);
        Map<String, String> errors = service.check(request);
        Assert.assertEquals(1, errors.size());
    }

    @Test
    public void checksNoMerchantName() {
        ContractParametersCheckRequest request = Utils.createContractParametersCheckRequest(null);
        Map<String, String> errors = service.check(request);

        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.containsKey(CONTRACT_CONFIG_MERCHANT_NAME));
    }

    @Test
    public void checkIOExceptionResponse() throws IOException, URISyntaxException, ExternalCommunicationException {
        when(httpClient.doPost(anyString(), anyString(), anyString(), anyString())).thenThrow(IOException.class);

        ContractParametersCheckRequest request = Utils.createContractParametersCheckRequest(Utils.MERCHANT_ID);
        Map<String, String> errors = service.check(request);
        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.containsKey(GENERIC_ERROR));
    }
}