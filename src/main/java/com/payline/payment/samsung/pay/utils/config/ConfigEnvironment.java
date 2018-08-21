package com.payline.payment.samsung.pay.utils.config;

public enum ConfigEnvironment {

    DEV("dev"),
    PROD("prod");

    private String prefix;

    ConfigEnvironment(String prefix ) {
        this.prefix = prefix;
    }

    public String getPrefix(){
        return this.prefix;
    }

}
