package com.example.writeai_android.data.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class User implements Serializable {
    private String uid;
    private String fullName;
    private String email;
    private Timestamp createdAt;
    private int streakCount;
    private int totalEssay;
    private double averageScore;
    private String lastPracticeDate;

    public User() {
    }

    public User(String uid, String fullName, String email, Timestamp createdAt,
                int streakCount, int totalEssay, double averageScore, String lastPracticeDate) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.createdAt = createdAt;
        this.streakCount = streakCount;
        this.totalEssay = totalEssay;
        this.averageScore = averageScore;
        this.lastPracticeDate = lastPracticeDate;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public int getStreakCount() {
        return streakCount;
    }

    public void setStreakCount(int streakCount) {
        this.streakCount = streakCount;
    }

    public int getTotalEssay() {
        return totalEssay;
    }

    public void setTotalEssay(int totalEssay) {
        this.totalEssay = totalEssay;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public String getLastPracticeDate() {
        return lastPracticeDate;
    }

    public void setLastPracticeDate(String lastPracticeDate) {
        this.lastPracticeDate = lastPracticeDate;
    }
}
