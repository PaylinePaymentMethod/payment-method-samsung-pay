package com.payline.payment.samsung.pay.bean.rest.response.nesteed;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Thales on 20/08/2018.
 */
public class EncryptionInfo {

    @SerializedName("mod")
    private String mod;

    @SerializedName("exp")
    private String exp;

    @SerializedName("keyId")
    private String keyId;

    /**
     * Public default constructor
     */
    public EncryptionInfo() {
        // Do nothing Not instantiated.
    }

    public String getMod() {
        return this.mod;
    }

    public String getExp() {
        return this.exp;
    }

    public String getKeyId() {
        return this.keyId;
    }

}