package com.example.writeai_android.data.repository;

import android.util.Log;

import com.example.writeai_android.data.model.Essay;
import com.example.writeai_android.utils.FirebaseHelper;
import com.example.writeai_android.utils.RepositoryCallback;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EssayRepository {

    private static final String TAG = "EssayRepository";

    public void saveEssay(Essay essay, RepositoryCallback<Essay> callback) {
        if (essay == null) {
            callback.onError("Essay không hợp lệ.");
            return;
        }

        DocumentReference docRef;
        if (essay.getEssayId() == null || essay.getEssayId().trim().isEmpty()) {
            docRef = FirebaseHelper.essaysRef().document();
            essay.setEssayId(docRef.getId());
        } else {
            docRef = FirebaseHelper.essaysRef().document(essay.getEssayId());
        }

        if (essay.getCreatedAt() == null) {
            essay.setCreatedAt(Timestamp.now());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("essayId", essay.getEssayId());
        data.put("userId", essay.getUserId());
        data.put("topic", essay.getTopic());
        data.put("content", essay.getContent());
        data.put("aiFeedback", essay.getAiFeedback());
        data.put("score", essay.getScore());
        data.put("createdAt", essay.getCreatedAt());

        docRef.set(data)
                .addOnSuccessListener(unused -> callback.onSuccess(essay))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Save essay failed", e);
                    callback.onError("Không thể lưu bài viết: " + e.getMessage());
                });
    }

    public void getEssaysByUser(String userId, RepositoryCallback<List<Essay>> callback) {
        FirebaseHelper.essaysRef()
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Essay> essays = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Essay essay = doc.toObject(Essay.class);
                        if (essay != null) {
                            essays.add(essay);
                        }
                    }
                    callback.onSuccess(essays);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Get essays failed", e);
                    callback.onError("Không thể tải lịch sử bài viết: " + e.getMessage());
                });
    }

    public void getEssayDetail(String essayId, RepositoryCallback<Essay> callback) {
        if (essayId == null || essayId.trim().isEmpty()) {
            callback.onError("essayId rỗng.");
            return;
        }

        FirebaseHelper.essaysRef().document(essayId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onError("Không tìm thấy bài viết.");
                        return;
                    }
                    Essay essay = documentSnapshot.toObject(Essay.class);
                    if (essay == null) {
                        callback.onError("Dữ liệu bài viết không hợp lệ.");
                        return;
                    }
                    callback.onSuccess(essay);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Get essay detail failed", e);
                    callback.onError("Không thể tải chi tiết bài viết: " + e.getMessage());
                });
    }
}
