package com.tradinos.drawyourpath;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "path_table")
public class MyPath {

    public MyPath() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "from")
    private String from;

    @NonNull
    @ColumnInfo(name = "to")
    private String to;

    @ColumnInfo(name = "distance")
    private String distance;

    @ColumnInfo(name = "duration")
    private String duration;

    @Ignore
    public MyPath(String from, String to, String distance, String duration) {
        this.from = from;
        this.to = to;
        this.distance = distance;
        this.duration = duration;
    }

    public void setFrom(@NonNull String from) {
        this.from = from;
    }

    public void setTo(@NonNull String to) {
        this.to = to;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getDistance() {
        return distance;
    }

    public String getDuration() {
        return duration;
    }

    @NonNull
    @Override
    public String toString() {
        String s = "From: " + from + ", To: " + to
                + "\nDistance: " + distance
                + "\nDuration: " + duration;

        return s;
    }
}
