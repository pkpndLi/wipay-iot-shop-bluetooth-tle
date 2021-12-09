package com.example.wipay_iot_shop.cypto;

import java.nio.charset.StandardCharsets;

public class DataConverter {

    public String HexByteToHexString(byte[] hexByte){

        String hex;
        hex = "";
        for (int i = 0; i < hexByte.length;i++){
            hex += Integer.toString((hexByte[i]&0xff)+0x100,16).substring(1);
        }

        return hex.toUpperCase();
    }

    public byte[] HexString2HexByte(String hexString)
    {
        String str = ((hexString.length()%2)!=0)?("0".concat(hexString)):hexString;
        byte[] hex=null;
        byte[] tmp=null;
        byte c = 0x00;

        str = str.toUpperCase();
        if(str.matches("[0-9A-F]+"))
        {
            hex = new byte[str.length()/2];
            tmp = str.getBytes();
            for(int i=0; i<str.length(); i++)
            {
                if((tmp[i]>='0')&&(tmp[i]<='9'))
                    c = 0x30;
                else
                {
                    if(((byte)tmp[i]>='A')&&((byte)tmp[i]<='F'))
                    {
                        c = 0x37;
                    }
                    else
                    {
                        hex = null;
                        break;
                    }
                }
                if((i%2)==0)
                    hex[i/2] |= (byte)((tmp[i]-c)<<4);
                else
                    hex[i/2] |= (byte)(tmp[i]-c);
            }
        }
        return hex;
    }

    public String hexByte2String(byte[] bytes){
        String s = new String(bytes, StandardCharsets.UTF_8);
        return s;
    }

}
