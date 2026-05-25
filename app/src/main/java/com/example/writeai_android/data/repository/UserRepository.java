package com.example.writeai_android.data.repository;

import android.util.Log;

import com.example.writeai_android.data.model.Attendance;
import com.example.writeai_android.data.model.User;
import com.example.writeai_android.utils.DateTimeFormatter;
import com.example.writeai_android.utils.FirebaseHelper;
import com.example.writeai_android.utils.RepositoryCallback;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {

    private static final String TAG = "UserRepository";

    public void getCurrentUserInfo(RepositoryCallback<User> callback) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            callback.onError("Người dùng chưa đăng nhập.");
            return;
        }

        FirebaseHelper.usersRef().document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onError("Không tìm thấy thông tin tài khoản.");
                        return;
                    }
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        user.setUid(firebaseUser.getUid());
                    }
                    callback.onSuccess(user);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Get current user failed", e);
                    callback.onError("Không thể tải thông tin người dùng: " + e.getMessage());
                });
    }

    public void updateUserAfterEssayCompleted(double newScore, RepositoryCallback<Void> callback) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            callback.onError("Người dùng chưa đăng nhập.");
            return;
        }

        String uid = firebaseUser.getUid();
        String today = DateTimeFormatter.getTodayDate();

        FirebaseHelper.usersRef().document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onError("Không tìm thấy document người dùng.");
                        return;
                    }

                    int oldTotalEssay = safeInt(documentSnapshot.getLong("totalEssay"));
                    double oldAverageScore = safeDouble(documentSnapshot.getDouble("averageScore"));
                    int currentStreak = safeInt(documentSnapshot.getLong("streakCount"));
                    String lastPracticeDate = documentSnapshot.getString("lastPracticeDate");
                    if (lastPracticeDate == null) {
                        lastPracticeDate = "";
                    }

                    int newTotalEssay = oldTotalEssay + 1;
                    double newAverageScore = calculateAverageScore(oldAverageScore, oldTotalEssay, newScore);
                    int newStreak = calculateStreak(currentStreak, lastPracticeDate, today);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("totalEssay", newTotalEssay);
                    updates.put("averageScore", newAverageScore);
                    updates.put("streakCount", newStreak);
                    updates.put("lastPracticeDate", today);

                    FirebaseHelper.usersRef().document(uid)
                            .update(updates)
                            .addOnSuccessListener(unused -> createAttendanceIfNeeded(uid, today, callback))
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Update user stats failed", e);
                                callback.onError("Không thể cập nhật thông tin người dùng: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Load user for update failed", e);
                    callback.onError("Không thể cập nhật người dùng: " + e.getMessage());
                });
    }

    public void updateStreak(RepositoryCallback<Integer> callback) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            callback.onError("Người dùng chưa đăng nhập.");
            return;
        }

        String uid = firebaseUser.getUid();
        String today = DateTimeFormatter.getTodayDate();

        FirebaseHelper.usersRef().document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onError("Không tìm thấy document người dùng.");
                        return;
                    }

                    int currentStreak = safeInt(documentSnapshot.getLong("streakCount"));
                    String lastPracticeDate = documentSnapshot.getString("lastPracticeDate");
                    if (lastPracticeDate == null) {
                        lastPracticeDate = "";
                    }

                    int newStreak = calculateStreak(currentStreak, lastPracticeDate, today);
                    if (today.equals(lastPracticeDate)) {
                        callback.onSuccess(currentStreak);
                        return;
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("streakCount", newStreak);
                    updates.put("lastPracticeDate", today);

                    FirebaseHelper.usersRef().document(uid)
                            .update(updates)
                            .addOnSuccessListener(unused -> createAttendanceIfNeeded(uid, today, new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    callback.onSuccess(newStreak);
                                }

                                @Override
                                public void onError(String message) {
                                    callback.onError(message);
                                }
                            }))
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Update streak failed", e);
                                callback.onError("Không thể cập nhật streak: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Get streak failed", e);
                    callback.onError("Không thể tải streak: " + e.getMessage());
                });
    }

    public double calculateAverageScore(double oldAverageScore, int oldTotalEssay, double newScore) {
        if (oldTotalEssay <= 0) {
            return newScore;
        }
        return ((oldAverageScore * oldTotalEssay) + newScore) / (oldTotalEssay + 1);
    }

    private int calculateStreak(int currentStreak, String lastPracticeDate, String today) {
        if (lastPracticeDate == null || lastPracticeDate.trim().isEmpty()) {
            return 1;
        }

        if (today.equals(lastPracticeDate)) {
            return currentStreak;
        }

        long diffDays = DateTimeFormatter.daysBetween(lastPracticeDate, today);
        if (diffDays == 1) {
            return currentStreak + 1;
        }
        return 1;
    }

    private void createAttendanceIfNeeded(String uid, String today, RepositoryCallback<Void> callback) {
        FirebaseHelper.attendanceRef()
                .whereEqualTo("userId", uid)
                .whereEqualTo("date", today)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        callback.onSuccess(null);
                        return;
                    }

                    Attendance attendance = new Attendance();
                    attendance.setAttendanceId(FirebaseHelper.attendanceRef().document().getId());
                    attendance.setUserId(uid);
                    attendance.setDate(today);
                    attendance.setCreatedAt(Timestamp.now());

                    Map<String, Object> data = new HashMap<>();
                    data.put("attendanceId", attendance.getAttendanceId());
                    data.put("userId", attendance.getUserId());
                    data.put("date", attendance.getDate());
                    data.put("createdAt", attendance.getCreatedAt());

                    FirebaseHelper.attendanceRef().document(attendance.getAttendanceId())
                            .set(data)
                            .addOnSuccessListener(unused -> callback.onSuccess(null))
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Create attendance failed", e);
                                callback.onSuccess(null);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Check attendance failed", e);
                    callback.onSuccess(null);
                });
    }

    private int safeInt(Long value) {
        return value == null ? 0 : value.intValue();
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }
}
