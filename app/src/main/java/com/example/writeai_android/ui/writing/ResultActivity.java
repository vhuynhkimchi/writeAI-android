package com.example.writeai_android.ui.writing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.writeai_android.R;
import com.example.writeai_android.data.model.Essay;
import com.example.writeai_android.data.repository.EssayRepository;
import com.example.writeai_android.ui.history.HistoryActivity;
import com.example.writeai_android.ui.main.MainActivity;
import com.example.writeai_android.utils.DateTimeFormatter;
import com.example.writeai_android.utils.RepositoryCallback;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DecimalFormat;

public class ResultActivity extends AppCompatActivity {

    public static final String EXTRA_ESSAY = "extra_essay";
    public static final String EXTRA_ESSAY_ID = "extra_essay_id";

    private TextView tvTopic, tvEssayContent, tvAiFeedback, tvScore, tvDate;
    private final EssayRepository essayRepository = new EssayRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, com.example.writeai_android.ui.auth.LoginActivity.class));
            finish();
            return;
        }

        tvTopic = findViewById(R.id.tvTopic);
        tvEssayContent = findViewById(R.id.tvEssayContent);
        tvAiFeedback = findViewById(R.id.tvAiFeedback);
        tvScore = findViewById(R.id.tvScore);
        tvDate = findViewById(R.id.tvDate);
        Button btnBackHome = findViewById(R.id.btnBackHome);
        Button btnViewHistory = findViewById(R.id.btnViewHistory);

        btnBackHome.setOnClickListener(v -> goToMain());
        btnViewHistory.setOnClickListener(v ->
                startActivity(new Intent(ResultActivity.this, HistoryActivity.class)));

        loadEssay();
    }

    private void loadEssay() {
        Essay essay = (Essay) getIntent().getSerializableExtra(EXTRA_ESSAY);
        String essayId = getIntent().getStringExtra(EXTRA_ESSAY_ID);

        if (essay != null) {
            bindEssay(essay);
            return;
        }

        if (essayId != null) {
            essayRepository.getEssayDetail(essayId, new RepositoryCallback<Essay>() {
                @Override
                public void onSuccess(Essay result) {
                    bindEssay(result);
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(ResultActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        Toast.makeText(this, "Không có dữ liệu bài viết.", Toast.LENGTH_LONG).show();
    }

    private void bindEssay(Essay essay) {
        if (essay == null) {
            return;
        }
        tvTopic.setText("Topic: " + safeText(essay.getTopic()));
        tvEssayContent.setText("Your writing:\n" + safeText(essay.getContent()));
        tvAiFeedback.setText("AI feedback:\n" + safeText(essay.getAiFeedback()));
        tvScore.setText("Score: " + formatScore(essay.getScore()) + "/10");
        tvDate.setText("Date: " + DateTimeFormatter.formatReadableDate(essay.getCreatedAt()));
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String formatScore(double score) {
        DecimalFormat df = new DecimalFormat("0.0");
        return df.format(score);
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
