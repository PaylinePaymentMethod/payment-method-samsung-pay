package com.payline.payment.samsung.pay.service;

import com.payline.payment.samsung.pay.bean.rest.request.CreateTransactionPostRequest;
import com.payline.payment.samsung.pay.bean.rest.response.CreateTransactionPostResponse;
import com.payline.payment.samsung.pay.exception.InvalidRequestException;
import com.payline.payment.samsung.pay.utils.SamsungPayConstants;
import com.payline.payment.samsung.pay.utils.config.ConfigEnvironment;
import com.payline.payment.samsung.pay.utils.config.ConfigProperties;
import com.payline.payment.samsung.pay.utils.http.StringResponse;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.InputParameter;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.service.ConfigurationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.*;
import static com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest.GENERIC_ERROR;

/**
 * Created by Thales on 16/08/2018.
 */
public class ConfigurationServiceImpl extends AbstractConfigurationHttpService implements ConfigurationService {

    private static final Logger logger = LogManager.getLogger(ConfigurationServiceImpl.class);
    private static final String RELEASE_DATE_FORMAT = "dd/MM/yyyy";

    /**
     * Default public constructor
     */
    public ConfigurationServiceImpl() {
    }

    @Override
    public List<AbstractParameter> getParameters(Locale locale) {
        List<AbstractParameter> parameters = new ArrayList<>();

        // Merchant name
        final InputParameter merchantName = new InputParameter();
        merchantName.setKey(CONTRACT_CONFIG__MERCHANT_NAME);
        merchantName.setLabel(CONTRACT_CONFIG__MERCHANT_NAME_PROPERTY_LABEL);
        merchantName.setDescription(CONTRACT_CONFIG__MERCHANT_NAME_PROPERTY_DESCRIPTION);
        merchantName.setRequired(true);

        parameters.add(merchantName);

        // todo ajouter le merchant reference et le merchant url

        return parameters;
    }

    @Override
    public Map<String, String> check(ContractParametersCheckRequest contractParametersCheckRequest) {
        Map<String, String> errors = new HashMap<>();

        // check input fields
        if (contractParametersCheckRequest.getContractConfiguration().getProperty(CONTRACT_CONFIG__MERCHANT_NAME).getValue() == null) {
            errors.put(CONTRACT_CONFIG__MERCHANT_NAME, i18n.getMessage("error.noMerchantName", contractParametersCheckRequest.getLocale()));
        }

        // stop the process if error exists
        if (errors.size() > 0) {
            return errors;
        }

        return this.processRequest(contractParametersCheckRequest);
    }

    @Override
    public ReleaseInformation getReleaseInformation() {
        Properties props = new Properties();
        try {
            props.load(ConfigurationServiceImpl.class.getClassLoader().getResourceAsStream("release.properties"));
        } catch (IOException e) {
            logger.error("An error occurred reading the file: release.properties");
            props.setProperty("release.version", "unknown");
            props.setProperty("release.date", "01/01/1900");
        }

        LocalDate date = LocalDate.parse(props.getProperty("release.date"), DateTimeFormatter.ofPattern(RELEASE_DATE_FORMAT));
        return ReleaseInformation.ReleaseBuilder.aRelease()
                .withDate(date)
                .withVersion(props.getProperty("release.version"))
                .build();
    }

    @Override
    public String getName(Locale locale) {
        return i18n.getMessage("paymentMethod.name", locale);
    }

    @Override
    public StringResponse createSendRequest(ContractParametersCheckRequest configRequest) throws IOException, InvalidRequestException, URISyntaxException {

        // create Samsung request Object from Payline request Object
        CreateTransactionPostRequest samsungRequest = new CreateTransactionPostRequest.Builder().fromCheckRequest(configRequest);

        // get all variables needed to call Samsung API
        ConfigEnvironment environment = configRequest.getEnvironment().isSandbox() ? ConfigEnvironment.DEV : ConfigEnvironment.PROD;
        String scheme = ConfigProperties.get(SamsungPayConstants.CONFIG__SHEME, environment);
        String host = ConfigProperties.get(SamsungPayConstants.CONFIG__HOST, environment);
        String path = ConfigProperties.get(SamsungPayConstants.CONFIG__PATH_TRANSACTION);

        return httpClient.doPost(scheme, host, path, samsungRequest.buildBody());
    }

    @Override
    public Map<String, String> processResponse(StringResponse response) {
        Map<String, String> errors = new HashMap<>();

        // Parse response
        CreateTransactionPostResponse createTransactionPostResponse = new CreateTransactionPostResponse.Builder().fromJson(response.getContent());

        if (!createTransactionPostResponse.isResultOk()) {
            errors.put(GENERIC_ERROR, createTransactionPostResponse.getResultMessage());
        }
        return errors;
    }
}
