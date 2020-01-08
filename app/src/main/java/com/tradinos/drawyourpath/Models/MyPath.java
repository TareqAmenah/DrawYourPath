package com.tradinos.drawyourpath.Models;

import java.text.DecimalFormat;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "path_table")
public class MyPath {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "from")
    private String from;
    @ColumnInfo(name = "to")
    private String to;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "distance")
    private double distance;
    @ColumnInfo(name = "duration")
    private int duration;
    @ColumnInfo(name = "image")
    private String imageBase64;

    public MyPath() {
    }

    @Ignore
    public MyPath(String from, String to, String title, double distance, int duration, String imageBase64) {
        this.from = from;
        this.to = to;
        this.title = title;
        this.distance = distance;
        this.duration = duration;
        this.imageBase64 = imageBase64;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(@NonNull String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(@NonNull String to) {
        this.to = to;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @NonNull
    @Override
    public String toString() {
        String s = "From: " + from + ", To: " + to
                + "\nDistance: " + distance + " km"
                + "\nDuration: " + duration;

        return s;
    }

    @Ignore
    public String getDistanceAsString(){
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(distance) + " km";
    }

    @Ignore
    public String getDurationAsString(){
        DecimalFormat df = new DecimalFormat("0.00");
        return duration + " m";
    }
}
