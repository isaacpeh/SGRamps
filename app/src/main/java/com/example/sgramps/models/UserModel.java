package com.example.sgramps.models;

public class UserModel {
    String email, password, img_url, gender;

    public UserModel(String email, String password, String img_url, String gender) {
        this.email = email;
        this.password = password;
        this.img_url = img_url;
        this.gender = gender;
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
