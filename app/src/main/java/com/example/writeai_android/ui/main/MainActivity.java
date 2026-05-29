package com.example.writeai_android.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_NOTIFICATION_PERMISSION = 991;

    private TextView tvGreeting, tvStreak, tvTotalEssay, tvAverageScore, tvPracticeStatus;
    private ImageView imgAttendanceIcon;
    private View btnPracticeNow;

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

        BottomNavigationHelper.setup(this, BottomNavigationHelper.TAB_HOME);

        requestNotificationPermissionIfNeeded();
        NotificationHelper.createNotificationChannel(this);
    }

    private void initViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        tvStreak = findViewById(R.id.tvStreak);
        tvTotalEssay = findViewById(R.id.tvTotalEssay);
        tvAverageScore = findViewById(R.id.tvAverageScore);
        tvPracticeStatus = findViewById(R.id.tvPracticeStatus);

        imgAttendanceIcon = findViewById(R.id.imgAttendanceIcon);

        btnPracticeNow = findViewById(R.id.btnPracticeNow);
    }

    private void handleEvents() {
        btnPracticeNow.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WritingActivity.class);
            startActivity(intent);
        });
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
                String lastPracticeDate = user.getLastPracticeDate();

                tvGreeting.setText("Ứng dụng luyện viết với AI");
                tvStreak.setText(streakCount + " ngày liên tiếp");
                tvTotalEssay.setText(String.valueOf(totalEssay));
                tvAverageScore.setText(formatScoreOneDigit(averageScore));

                updateAttendanceIcon(lastPracticeDate);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateAttendanceIcon(String lastPracticeDate) {
        boolean practicedToday = isPracticedToday(lastPracticeDate);

        if (practicedToday) {
            imgAttendanceIcon.setImageResource(R.drawable.ic_fire_on);
            tvPracticeStatus.setText("Hôm nay bạn đã điểm danh rồi!");
        } else {
            imgAttendanceIcon.setImageResource(R.drawable.ic_fire_off);
            tvPracticeStatus.setText("Luyện tập mỗi ngày bạn nhé!");
        }
    }

    private boolean isPracticedToday(String lastPracticeDate) {
        if (lastPracticeDate == null || lastPracticeDate.trim().isEmpty()) {
            return false;
        }

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        return today.equals(lastPracticeDate.trim());
    }

    private String formatScoreOneDigit(double score) {
        DecimalFormat df = new DecimalFormat("0.0");
        return df.format(score);
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