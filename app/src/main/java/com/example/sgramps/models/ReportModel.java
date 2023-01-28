package com.example.sgramps.models;

import com.google.firebase.Timestamp;

import java.util.List;

public class ReportModel {
    String reporter, description, ramp_name;
    Long reported_at;
    List<String> img_urls;

    public ReportModel() {

    }

    public ReportModel(String reporter, String description, String ramp_name, Long reported_at, List<String> img_urls) {
        this.reporter = reporter;
        this.description = description;
        this.ramp_name = ramp_name;
        this.reported_at = reported_at;
        this.img_urls = img_urls;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRamp_name() {
        return ramp_name;
    }

    public void setRamp_name(String ramp_name) {
        this.ramp_name = ramp_name;
    }

    public Long getReported_at() {
        return reported_at;
    }

    public void setReported_at(Long reported_at) {
        this.reported_at = reported_at;
    }

    public List<String> getImg_urls() {
        return img_urls;
    }

    public void setImg_urls(List<String> img_urls) {
        this.img_urls = img_urls;
    }
}
