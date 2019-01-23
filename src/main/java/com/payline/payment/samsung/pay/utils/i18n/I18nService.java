package com.payline.payment.samsung.pay.utils.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

public class I18nService {

    private static final Logger logger = LogManager.getLogger( I18nService.class );

    private static final String RESOURCE_BUNDLE_BASE_NAME = "messages";

    /**
     * Private constructor
     */
    private I18nService(){
        Locale.setDefault( new Locale( "en" ) );
    }

    /**
     * Holder
     */
    private static class SingletonHolder {
        /**
         * Unique instance, not preinitializes
         */
        private static final I18nService instance = new I18nService();
    }

    /**
     * Unique access point for the singleton instance
     */
    public static I18nService getInstance() {
        return SingletonHolder.instance;
    }

    public String getMessage( final String key, final Locale locale ){
        ResourceBundle messages = ResourceBundle.getBundle( RESOURCE_BUNDLE_BASE_NAME, locale );
        try {
            return messages.getString( key );
        }
        catch( MissingResourceException e ){
            logger.error( "Trying to get a message with a key that does not exist: " + key + " (language: " + locale.getLanguage() + ")" );
            return "???" + locale + "." + key + "???";
        }
    }

    // If ever needed, implement getMessage( String, Locale, String... ) to insert values into the translation messages
}
