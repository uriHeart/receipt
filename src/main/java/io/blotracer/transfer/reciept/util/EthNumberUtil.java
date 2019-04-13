package io.blotracer.transfer.reciept.util;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.BigInteger;

public class EthNumberUtil {


    public static int hexToNumber(@NotNull String hexDecimal){
        if(hexDecimal == null)
            return 3;

       return new BigInteger(hexDecimal.substring(2),16).intValue();
    }

    public static BigDecimal hexToRealNumber(@NotNull String hexDecimal){

        BigInteger hexValue =  new BigInteger( hexDecimal.substring(2),16 );
        BigDecimal value = new BigDecimal( hexValue,hexValue.compareTo(BigInteger.ZERO)==0 ? 0 : 18);
        return value;
    }

    public static Float hexToFlotNumber(@NotNull String hexDecimal){

        BigInteger hexValue =  new BigInteger( hexDecimal.substring(2),16 );
        BigDecimal value = new BigDecimal( hexValue,hexValue.compareTo(BigInteger.ZERO)==0 ? 0 : 18);
        return value.floatValue();
    }

    public static String hexToStringNumber(@NotNull String hexDecimal){

        BigInteger hexValue =  new BigInteger( hexDecimal.substring(2),16 );
        BigDecimal value = new BigDecimal( hexValue,hexValue.compareTo(BigInteger.ZERO)==0 ? 0 : 18);
        String val = value.toPlainString();
        int endIndex=1;

        for(int i=val.length(); i > 1; i--){
            if(!val.substring(i-1,i).equals("0")){
                endIndex=i;
                break;
            }
        }
        return val.substring(0,endIndex);
    }

    public static String hexToGasPrice(@NotNull String hexDecimal){

        BigInteger hexValue =  new BigInteger( hexDecimal.substring(2),16 );
        String value = new BigDecimal( hexValue,hexValue.compareTo(BigInteger.ZERO)==0 ? 0 : 18).toPlainString();
        int endIndex=0;

        for(int i=1; i < value.length(); i++){
             endIndex = value.length() - (i-1);
            if(!value.substring(value.length()-i,endIndex).equals("0")){
                break;
            }
        }

        return value.substring(0,endIndex);
    }

    public static Float hexToGasNumber(@NotNull String hexDecimal){

        BigInteger hexValue =  new BigInteger( hexDecimal.substring(2),16 );

        return new BigDecimal( hexValue,hexValue.compareTo(BigInteger.ZERO)==0 ? 0 : 9).floatValue();
    }
}
