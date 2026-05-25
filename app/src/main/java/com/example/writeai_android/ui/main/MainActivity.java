package com.example.writeai_android.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.writeai_android.R;
import com.example.writeai_android.data.model.User;
import com.example.writeai_android.data.repository.UserRepository;
import com.example.writeai_android.ui.auth.LoginActivity;
import com.example.writeai_android.ui.history.HistoryActivity;
import com.example.writeai_android.ui.profile.ProfileActivity;
import com.example.writeai_android.ui.writing.WritingActivity;
import com.example.writeai_android.utils.NotificationHelper;
import com.example.writeai_android.utils.RepositoryCallback;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DecimalFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_NOTIFICATION_PERMISSION = 991;

    private TextView tvGreeting, tvStreak, tvTotalEssay, tvAverageScore;
    private final UserRepository userRepository = new UserRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            goToLogin();
            return;
        }

        tvGreeting = findViewById(R.id.tvGreeting);
        tvStreak = findViewById(R.id.tvStreak);
        tvTotalEssay = findViewById(R.id.tvTotalEssay);
        tvAverageScore = findViewById(R.id.tvAverageScore);

        Button btnPracticeNow = findViewById(R.id.btnPracticeNow);
        Button btnHistory = findViewById(R.id.btnHistory);
        Button btnAccount = findViewById(R.id.btnAccount);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnPracticeNow.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, WritingActivity.class)));
        btnHistory.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, HistoryActivity.class)));
        btnAccount.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
        btnLogout.setOnClickListener(v -> logout());

        requestNotificationPermissionIfNeeded();
        NotificationHelper.createNotificationChannel(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            goToLogin();
            return;
        }
        loadUserInfo();
    }

    private void loadUserInfo() {
        userRepository.getCurrentUserInfo(new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    return;
                }
                tvGreeting.setText("Xin chào, " + safeText(user.getFullName()));
                tvStreak.setText("Streak hiện tại: " + user.getStreakCount());
                tvTotalEssay.setText("Tổng số bài viết: " + user.getTotalEssay());
                tvAverageScore.setText("Điểm trung bình: " + formatScore(user.getAverageScore()));
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String formatScore(double score) {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(score);
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIFICATION_PERMISSION
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,
                        "Bạn có thể bật lại quyền thông báo trong Cài đặt để nhận nhắc nhở.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
