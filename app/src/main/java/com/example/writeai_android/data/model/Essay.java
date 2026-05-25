package com.example.writeai_android.data.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class Essay implements Serializable {
    private String essayId;
    private String userId;
    private String topic;
    private String content;
    private String aiFeedback;
    private double score;
    private Timestamp createdAt;

    public Essay() {
    }

    public Essay(String essayId, String userId, String topic, String content, String aiFeedback, double score, Timestamp createdAt) {
        this.essayId = essayId;
        this.userId = userId;
        this.topic = topic;
        this.content = content;
        this.aiFeedback = aiFeedback;
        this.score = score;
        this.createdAt = createdAt;
    }

    public String getEssayId() {
        return essayId;
    }

    public void setEssayId(String essayId) {
        this.essayId = essayId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAiFeedback() {
        return aiFeedback;
    }

    public void setAiFeedback(String aiFeedback) {
        this.aiFeedback = aiFeedback;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
