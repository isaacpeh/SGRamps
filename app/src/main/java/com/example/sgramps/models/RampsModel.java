package com.example.sgramps.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class RampsModel {
    String ramp_name, ramp_description, uploader;
    List<String> img_url;
    boolean active;
    GeoPoint gpoint;
    Timestamp created_at;

    public RampsModel() {

    }

    public RampsModel(String ramp_name, String ramp_description, Timestamp created_at, String uploader, List<String> img_url, boolean active, GeoPoint gpoint) {
        this.ramp_name = ramp_name;
        this.ramp_description = ramp_description;
        this.created_at = created_at;
        this.uploader = uploader;
        this.img_url = img_url;
        this.active = active;
        this.gpoint = gpoint;
    }

    public RampsModel(String ramp_description, Timestamp created_at, String uploader, List<String> img_url, boolean active, GeoPoint gpoint) {
        this.ramp_description = ramp_description;
        this.created_at = created_at;
        this.uploader = uploader;
        this.img_url = img_url;
        this.active = active;
        this.gpoint = gpoint;
    }

    public String getRamp_name() {
        return ramp_name;
    }

    public void setRamp_name(String ramp_name) {
        this.ramp_name = ramp_name;
    }

    public String getRamp_description() {
        return ramp_description;
    }

    public void setRamp_description(String ramp_description) {
        this.ramp_description = ramp_description;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public List<String> getImg_url() {
        return img_url;
    }

    public void setImg_url(List<String> img_url) {
        this.img_url = img_url;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public GeoPoint getGpoint() {
        return gpoint;
    }

    public void setGpoint(GeoPoint gpoint) {
        this.gpoint = gpoint;
    }
}
