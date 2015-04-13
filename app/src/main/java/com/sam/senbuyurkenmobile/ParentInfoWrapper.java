package com.sam.senbuyurkenmobile;

import java.io.Serializable;

/**
 * Created by SametCokpinar on 01/03/15.
 */
public class ParentInfoWrapper implements Serializable {

    private Integer parentInfoId;

    private Integer userId;

    private String mother_name;

    private String mother_surname;

    private String father_name;

    private String father_surname;

    private String wedding_anniversary;

    public Integer getParentInfoId() {
        return parentInfoId;
    }

    public void setParentInfoId(Integer parentInfoId) {
        this.parentInfoId = parentInfoId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getMother_name() {
        return mother_name;
    }

    public void setMother_name(String mother_name) {
        this.mother_name = mother_name;
    }

    public String getMother_surname() {
        return mother_surname;
    }

    public void setMother_surname(String mother_surname) {
        this.mother_surname = mother_surname;
    }

    public String getFather_name() {
        return father_name;
    }

    public void setFather_name(String father_name) {
        this.father_name = father_name;
    }

    public String getFather_surname() {
        return father_surname;
    }

    public void setFather_surname(String father_surname) {
        this.father_surname = father_surname;
    }

    public String getWedding_anniversary() {
        return wedding_anniversary;
    }

    public void setWedding_anniversary(String wedding_anniversary) {
        this.wedding_anniversary = wedding_anniversary;
    }
}
