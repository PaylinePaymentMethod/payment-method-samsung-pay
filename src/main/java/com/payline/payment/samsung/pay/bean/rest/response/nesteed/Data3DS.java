package com.payline.payment.samsung.pay.bean.rest.response.nesteed;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Thales on 21/08/2018.
 */
public class Data3DS {

    @SerializedName("type")
    private String type;

    @SerializedName("version")
    private String version;

    @SerializedName("data")
    private String data;

    /**
     * Default public constructor
     */
    public Data3DS() {
       // Do nothing Not instantiated.
    }

    public String getType() {
        return this.type;
    }

    public String getVersion() {
        return this.version;
    }

    public String getData() {
        return this.data;
    }

}