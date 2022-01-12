package com.payline.payment.samsung.pay.utils;

import java.math.BigInteger;
import java.util.Currency;

public class SamsungPayStringUtils {

    /* Static utility class : no need to instantiate it (to Sonar) */
    private SamsungPayStringUtils() {
        // Do nothing Not instantiated.
    }

    public static boolean isEmpty(String str){
        if( str == null || str.length() == 0){
            return true;
        }else{
            for(int i=0; i<str.length(); i++){
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    public static String createStringAmount(BigInteger amount, Currency currency) {
        //récupérer le nombre de digits dans currency
        int nbDigits = currency.getDefaultFractionDigits();

        StringBuilder sb = new StringBuilder();
        sb.append(amount);

        for (int i = sb.length(); i < 3; i++) {
            sb.insert(0, "0");
        }

        sb.insert(sb.length() - nbDigits, ".");
        return sb.toString();
    }
}
