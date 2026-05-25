package com.example.writeai_android.data.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class Attendance implements Serializable {
    private String attendanceId;
    private String userId;
    private String date;
    private Timestamp createdAt;

    public Attendance() {
    }

    public Attendance(String attendanceId, String userId, String date, Timestamp createdAt) {
        this.attendanceId = attendanceId;
        this.userId = userId;
        this.date = date;
        this.createdAt = createdAt;
    }

    public String getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(String attendanceId) {
        this.attendanceId = attendanceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
