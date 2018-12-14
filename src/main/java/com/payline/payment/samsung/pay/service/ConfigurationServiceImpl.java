package com.payline.payment.samsung.pay.service;

import com.payline.payment.samsung.pay.bean.rest.request.CreateTransactionPostRequest;
import com.payline.payment.samsung.pay.bean.rest.response.CreateTransactionPostResponse;
import com.payline.payment.samsung.pay.exception.ExternalCommunicationException;
import com.payline.payment.samsung.pay.exception.InvalidRequestException;
import com.payline.payment.samsung.pay.utils.http.StringResponse;
import com.payline.pmapi.bean.configuration.AvailableNetwork;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.InputParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.NetworkListBoxParameter;
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

    private static final Logger LOGGER = LogManager.getLogger(ConfigurationServiceImpl.class);
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
        merchantName.setKey(CONTRACT_CONFIG_MERCHANT_NAME);
        merchantName.setLabel(i18n.getMessage(CONTRACT_CONFIG_MERCHANT_NAME_PROPERTY_LABEL, locale));
        merchantName.setDescription(i18n.getMessage(CONTRACT_CONFIG_MERCHANT_NAME_PROPERTY_DESCRIPTION, locale));
        merchantName.setRequired(true);

        parameters.add(merchantName);

        // merchant url
        final InputParameter merchantUrl = new InputParameter();
        merchantUrl.setKey(CONTRACT_CONFIG_MERCHANT_URL);
        merchantUrl.setLabel(i18n.getMessage(CONTRACT_CONFIG_MERCHANT_URL_PROPERTY_LABEL, locale));
        merchantUrl.setDescription(i18n.getMessage(CONTRACT_CONFIG_MERCHANT_URL_PROPERTY_DESCRIPTION, locale));
        merchantUrl.setRequired(false);

        parameters.add(merchantUrl);

        // merchant reference
        final InputParameter merchantReference = new InputParameter();
        merchantReference.setKey(CONTRACT_CONFIG_MERCHANT_REFERENCE);
        merchantReference.setLabel(i18n.getMessage(CONTRACT_CONFIG_MERCHANT_REFERENCE_PROPERTY_LABEL, locale));
        merchantReference.setDescription(i18n.getMessage(CONTRACT_CONFIG_MERCHANT_REFERENCE_PROPERTY_DESCRIPTION, locale));
        merchantReference.setRequired(false);

        parameters.add(merchantReference);

        final NetworkListBoxParameter networkCb = new NetworkListBoxParameter();
        networkCb.setKey(AvailableNetwork.CB.getKey());
        networkCb.setLabel(i18n.getMessage(CONTRACT_CONFIG_CB_PROPERTY_LABEL, locale));
        networkCb.setDescription(i18n.getMessage(CONTRACT_CONFIG_CB_PROPERTY_DESCRIPTION, locale));
        networkCb.setNetwork(AvailableNetwork.CB);

        parameters.add(networkCb);

        final NetworkListBoxParameter networkVisa = new NetworkListBoxParameter();
        networkVisa.setKey(AvailableNetwork.VISA.getKey());
        networkVisa.setLabel(i18n.getMessage(CONTRACT_CONFIG_VISA_PROPERTY_LABEL, locale));
        networkVisa.setDescription(i18n.getMessage(CONTRACT_CONFIG_VISA_PROPERTY_DESCRIPTION, locale));
        networkVisa.setNetwork(AvailableNetwork.VISA);

        parameters.add(networkVisa);

        final NetworkListBoxParameter networkMastercard = new NetworkListBoxParameter();
        networkMastercard.setKey(AvailableNetwork.MASTERCARD.getKey());
        networkMastercard.setLabel(i18n.getMessage(CONTRACT_CONFIG_MASTERCARD_PROPERTY_LABEL, locale));
        networkMastercard.setDescription(i18n.getMessage(CONTRACT_CONFIG_MASTERCARD_PROPERTY_DESCRIPTION, locale));
        networkMastercard.setNetwork(AvailableNetwork.MASTERCARD);

        parameters.add(networkMastercard);

        final NetworkListBoxParameter networkAmex = new NetworkListBoxParameter();
        networkAmex.setKey(AvailableNetwork.AMEX.getKey());
        networkAmex.setLabel(i18n.getMessage(CONTRACT_CONFIG_AMEX_PROPERTY_LABEL, locale));
        networkAmex.setDescription(i18n.getMessage(CONTRACT_CONFIG_AMEX_PROPERTY_DESCRIPTION, locale));
        networkAmex.setNetwork(AvailableNetwork.AMEX);

        parameters.add(networkAmex);

        return parameters;
    }

    @Override
    public Map<String, String> check(ContractParametersCheckRequest contractParametersCheckRequest) {
        Map<String, String> errors = new HashMap<>();

        // check input fields
        if (contractParametersCheckRequest.getContractConfiguration().getProperty(CONTRACT_CONFIG_MERCHANT_NAME).getValue() == null) {
            errors.put(CONTRACT_CONFIG_MERCHANT_NAME, i18n.getMessage("error.noMerchantName", contractParametersCheckRequest.getLocale()));
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
            LOGGER.error("An error occurred reading the file: release.properties");
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
    public StringResponse createSendRequest(ContractParametersCheckRequest configRequest) throws IOException, InvalidRequestException, URISyntaxException, ExternalCommunicationException {

        // create Samsung request Object from Payline request Object
        CreateTransactionPostRequest samsungRequest = new CreateTransactionPostRequest.Builder().fromCheckRequest(configRequest);

        // get all variables needed to call Samsung API
        String host = configRequest.getEnvironment().isSandbox() ? DEV_HOST: PROD_HOST;
        return httpClient.doPost(SCHEME, host, CREATE_TRANSACTION_PATH, samsungRequest.buildBody(), DEFAULT_XREQUESTID);
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
