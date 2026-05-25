package com.example.writeai_android.ui.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.writeai_android.R;
import com.example.writeai_android.data.model.User;
import com.example.writeai_android.data.repository.UserRepository;
import com.example.writeai_android.ui.auth.LoginActivity;
import com.example.writeai_android.utils.NotificationHelper;
import com.example.writeai_android.utils.RepositoryCallback;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DecimalFormat;

public class ProfileFragment extends Fragment {

    private static final int REQ_NOTIFICATION_PERMISSION = 778;

    private TextView tvFullName, tvEmail, tvStreak, tvTotalEssay, tvAverageScore, tvLastPracticeDate;
    private final UserRepository userRepository = new UserRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvFullName = view.findViewById(R.id.tvFullName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvStreak = view.findViewById(R.id.tvStreak);
        tvTotalEssay = view.findViewById(R.id.tvTotalEssay);
        tvAverageScore = view.findViewById(R.id.tvAverageScore);
        tvLastPracticeDate = view.findViewById(R.id.tvLastPracticeDate);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        Button btnReminder = view.findViewById(R.id.btnReminder);

        btnLogout.setOnClickListener(v -> logout());
        btnReminder.setOnClickListener(v -> enableReminder());

        loadUserInfo();
    }

    private void loadUserInfo() {
        userRepository.getCurrentUserInfo(new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null || getContext() == null) {
                    return;
                }
                tvFullName.setText("Họ tên: " + safeText(user.getFullName()));
                tvEmail.setText("Email: " + safeText(user.getEmail()));
                tvStreak.setText("Streak hiện tại: " + user.getStreakCount());
                tvTotalEssay.setText("Tổng bài viết: " + user.getTotalEssay());
                tvAverageScore.setText("Điểm trung bình: " + formatScore(user.getAverageScore()));
                tvLastPracticeDate.setText("Ngày luyện gần nhất: " + safeText(user.getLastPracticeDate()));
            }

            @Override
            public void onError(String message) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        if (getContext() == null) {
            return;
        }
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void enableReminder() {
        if (getContext() == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIFICATION_PERMISSION);
            return;
        }

        NotificationHelper.scheduleDailyReminder(requireContext());
        Toast.makeText(getContext(), "Đã bật nhắc nhở lúc 20:00 mỗi ngày", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                NotificationHelper.scheduleDailyReminder(requireContext());
                Toast.makeText(getContext(), "Đã bật nhắc nhở lúc 20:00 mỗi ngày", Toast.LENGTH_SHORT).show();
            } else if (getContext() != null) {
                Toast.makeText(getContext(),
                        "Bạn cần cấp quyền thông báo để nhận nhắc nhở.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String formatScore(double score) {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(score);
    }
}
