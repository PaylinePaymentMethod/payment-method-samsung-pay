package com.payline.payment.samsung.pay.utils.i18n;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class I18nServiceTest {

    private I18nService service;

    @BeforeEach
    public void setup(){
        this.service = I18nService.getInstance();
    }

    @Test
    public void testGetMessage_default(){
        // when: recovering the same message in FR, EN and a likely-not-implemented language (co: Corsican)
        String coMessage = service.getMessage( "contractConfiguration.keyId.error", new Locale( "co" ) );
        String enMessage = service.getMessage( "contractConfiguration.keyId.error", new Locale( "en" ) );
        String frMessage = service.getMessage( "contractConfiguration.keyId.error", new Locale( "fr" ) );

        // then: all 3 messages are not null, FR is different then EN, CO equals EN.
        assertNotNull( coMessage );
        assertNotNull( enMessage );
        assertNotNull( frMessage );
        assertNotEquals( frMessage, enMessage );
        assertNotEquals( enMessage, coMessage );
    }

    @Test
    public void testGetMessage_notNull(){
        // when: recovering a message with a key that does not exist
        String message = service.getMessage( "this.key.surely.does.not.exist", Locale.getDefault() );

        // then: resulting message is not null
        assertNotNull( message );
    }

}
