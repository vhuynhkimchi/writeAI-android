package com.example.writeai_android.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
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
import com.example.writeai_android.ui.writing.WritingActivity;
import com.example.writeai_android.utils.BottomNavigationHelper;
import com.example.writeai_android.utils.NotificationHelper;
import com.example.writeai_android.utils.RepositoryCallback;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_NOTIFICATION_PERMISSION = 991;

    private TextView tvGreeting, tvStreak, tvTotalEssay, tvAverageScore;
    private View btnPracticeNow, btnLogout;

    private final UserRepository userRepository = new UserRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            goToLogin();
            return;
        }

        initViews();
        handleEvents();

        // Gọi helper điều hướng dưới cùng
        BottomNavigationHelper.setup(this, BottomNavigationHelper.TAB_HOME);

        requestNotificationPermissionIfNeeded();
        NotificationHelper.createNotificationChannel(this);
    }

    private void initViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        tvStreak = findViewById(R.id.tvStreak);
        tvTotalEssay = findViewById(R.id.tvTotalEssay);
        tvAverageScore = findViewById(R.id.tvAverageScore);

        btnPracticeNow = findViewById(R.id.btnPracticeNow);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void handleEvents() {
        btnPracticeNow.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WritingActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> logout());
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

                int streakCount = user.getStreakCount();
                int totalEssay = user.getTotalEssay();
                double averageScore = user.getAverageScore();

                tvGreeting.setText("Ứng dụng luyện viết với AI");
                tvStreak.setText(streakCount + " ngày liên tiếp");
                tvTotalEssay.setText(String.valueOf(totalEssay));
                tvAverageScore.setText(formatScoreOneDigit(averageScore));
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String formatScoreOneDigit(double score) {
        DecimalFormat df = new DecimalFormat("0.0");
        return df.format(score);
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
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(
                        this,
                        "Bạn có thể bật lại quyền thông báo trong Cài đặt để nhận nhắc nhở.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }
}