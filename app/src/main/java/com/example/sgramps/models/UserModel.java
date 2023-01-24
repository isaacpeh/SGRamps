package com.example.sgramps.models;

import com.google.firebase.firestore.auth.User;

public class UserModel {
    String email, password, img_url, gender, dob, name, number;

    public UserModel() {

    }

    public UserModel(String name, String email, String password, String img_url, String gender, String dob, String number) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.img_url = img_url;
        this.gender = gender;
        this.dob = dob;
        this.number = number;
    }

    public UserModel(String name, String email, String img_url, String gender, String dob, String number) {
        this.name = name;
        this.email = email;
        this.img_url = img_url;
        this.gender = gender;
        this.dob = dob;
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
