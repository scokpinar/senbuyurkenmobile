package com.sam.senbuyurkenmobile;

/**
 * Created by SametCokpinar on 21/12/14.
 */
public enum UserType {

    FREE("0"), PAID("1");

    private String typeCode;

    private UserType(String s) {
        typeCode = s;
    }

    public String getTypeCode() {
        return typeCode;
    }


}
