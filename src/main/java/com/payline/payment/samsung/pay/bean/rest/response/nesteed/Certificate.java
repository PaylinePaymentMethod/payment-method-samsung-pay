package com.payline.payment.samsung.pay.bean.rest.response.nesteed;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Thales on 21/08/2018.
 */
public class Certificate {

    @SerializedName("usage")
    private String usage;

    @SerializedName("alias")
    private String alias;

    @SerializedName("content")
    private String content;

    /**
     * Public default constructor
     */
    public Certificate() {
        // Do Do nothing Not instantiated.
    }

    public String getUsage() {
        return this.usage;
    }

    public String getAlias() {
        return this.alias;
    }

    public String getContent() {
        return this.content;
    }

}