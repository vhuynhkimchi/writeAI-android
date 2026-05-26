package com.example.writeai_android.data.repository;

import android.util.Log;

import com.example.writeai_android.data.model.Essay;
import com.example.writeai_android.utils.FirebaseHelper;
import com.example.writeai_android.utils.RepositoryCallback;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.Collections;
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

        if (essay.getUserId() == null || essay.getUserId().trim().isEmpty()) {
            callback.onError("Thiếu userId, không thể lưu bài viết.");
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
        if (userId == null || userId.trim().isEmpty()) {
            callback.onError("userId rỗng, không thể tải lịch sử bài viết.");
            return;
        }

        FirebaseHelper.essaysRef()
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Essay> essays = new ArrayList<>();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Essay essay = doc.toObject(Essay.class);

                        if (essay != null) {
                            if (essay.getEssayId() == null || essay.getEssayId().trim().isEmpty()) {
                                essay.setEssayId(doc.getId());
                            }

                            essays.add(essay);
                        }
                    }

                    // Sắp xếp bài viết mới nhất lên trước ở phía Android
                    Collections.sort(essays, (e1, e2) -> {
                        Timestamp t1 = e1.getCreatedAt();
                        Timestamp t2 = e2.getCreatedAt();

                        if (t1 == null && t2 == null) {
                            return 0;
                        }

                        if (t1 == null) {
                            return 1;
                        }

                        if (t2 == null) {
                            return -1;
                        }

                        return t2.compareTo(t1);
                    });

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

        FirebaseHelper.essaysRef()
                .document(essayId)
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

                    if (essay.getEssayId() == null || essay.getEssayId().trim().isEmpty()) {
                        essay.setEssayId(documentSnapshot.getId());
                    }

                    callback.onSuccess(essay);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Get essay detail failed", e);
                    callback.onError("Không thể tải chi tiết bài viết: " + e.getMessage());
                });
    }
}