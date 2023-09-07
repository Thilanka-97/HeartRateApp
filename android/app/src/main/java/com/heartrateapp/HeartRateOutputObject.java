package com.heartrateapp;

import java.util.Date;

public class HeartRateOutputObject {
    private Float measurement;
    private Date timestamp;

    public HeartRateOutputObject(Float measurement, Date timestamp) {
        this.measurement = measurement;
        this.timestamp = timestamp;
    }

    public Float getMeasurement() {
        return measurement;
    }

    public Date getTimestamp() {
        return timestamp;
    }

}
