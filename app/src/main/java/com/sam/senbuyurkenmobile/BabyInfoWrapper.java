package com.sam.senbuyurkenmobile;

import java.io.Serializable;

/**
 * Created by SametCokpinar on 23/02/15.
 */
public class BabyInfoWrapper implements Serializable {

    private Integer babyInfoId;

    private Integer userId;

    private String name;

    private String surname;

    private String gender;

    private String birthDate;

    private String birthHour;

    private String birthPlace;

    private String hospital;

    private String gynecologyDoctor;

    private String pediatricianDoctor;

    private Integer birthWeight = 0;

    private Integer birthLength = 0;

    private String photoURL;

    public Integer getBabyInfoId() {
        return babyInfoId;
    }

    public void setBabyInfoId(Integer babyInfoId) {
        this.babyInfoId = babyInfoId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getBirthHour() {
        return birthHour;
    }

    public void setBirthHour(String birthHour) {
        this.birthHour = birthHour;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public String getGynecologyDoctor() {
        return gynecologyDoctor;
    }

    public void setGynecologyDoctor(String gynecologyDoctor) {
        this.gynecologyDoctor = gynecologyDoctor;
    }

    public String getPediatricianDoctor() {
        return pediatricianDoctor;
    }

    public void setPediatricianDoctor(String pediatricianDoctor) {
        this.pediatricianDoctor = pediatricianDoctor;
    }

    public Integer getBirthWeight() {
        return birthWeight;
    }

    public void setBirthWeight(Integer birthWeight) {
        this.birthWeight = birthWeight;
    }

    public Integer getBirthLength() {
        return birthLength;
    }

    public void setBirthLength(Integer birthLength) {
        this.birthLength = birthLength;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }
}
