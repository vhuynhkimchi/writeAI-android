package com.example.writeai_android.ui.writing;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.writeai_android.R;
import com.example.writeai_android.data.model.Essay;
import com.example.writeai_android.data.repository.EssayRepository;
import com.example.writeai_android.data.repository.GeminiRepository;
import com.example.writeai_android.data.repository.UserRepository;
import com.example.writeai_android.ui.main.MainActivity;
import com.example.writeai_android.utils.RepositoryCallback;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WritingActivity extends AppCompatActivity {

    private final String[] topics = new String[]{
            "Travel",
            "Food",
            "Career",
            "Education",
            "Technology",
            "Environment",
            "Health",
            "Family",
            "My favorite hobby",
            "The importance of learning English"
    };

    private TextView tvTopic;
    private EditText etEssayContent;
    private ProgressBar progressBar;
    private String currentTopic;
    private GeminiRepository geminiRepository;
    private final EssayRepository essayRepository = new EssayRepository();
    private final UserRepository userRepository = new UserRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, com.example.writeai_android.ui.auth.LoginActivity.class));
            finish();
            return;
        }

        geminiRepository = new GeminiRepository(getApplicationContext());
        tvTopic = findViewById(R.id.tvTopic);
        etEssayContent = findViewById(R.id.etEssayContent);
        progressBar = findViewById(R.id.progressBar);
        Button btnChangeTopic = findViewById(R.id.btnChangeTopic);
        Button btnGradeEssay = findViewById(R.id.btnGradeEssay);

        pickRandomTopic();

        btnChangeTopic.setOnClickListener(v -> pickRandomTopic());
        btnGradeEssay.setOnClickListener(v -> gradeEssay());
    }

    private void pickRandomTopic() {
        Random random = new Random();
        currentTopic = topics[random.nextInt(topics.length)];
        tvTopic.setText("Topic: " + currentTopic);
    }

    private void gradeEssay() {
        String content = etEssayContent.getText().toString().trim();

        if (TextUtils.isEmpty(content)) {
            etEssayContent.setError("Bài viết không được rỗng");
            etEssayContent.requestFocus();
            return;
        }

        int wordCount = content.split("\\s+").length;
        if (wordCount < 30) {
            etEssayContent.setError("Bài viết tối thiểu 30 từ");
            etEssayContent.requestFocus();
            return;
        }

        showLoading(true);
        geminiRepository.evaluateEssay(currentTopic, content, new RepositoryCallback<String>() {
            @Override
            public void onSuccess(String aiResponse) {
                double score = extractScore(aiResponse);

                Essay essay = new Essay();
                essay.setEssayId(null);
                essay.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                essay.setTopic(currentTopic);
                essay.setContent(content);
                essay.setAiFeedback(aiResponse);
                essay.setScore(score);
                essay.setCreatedAt(Timestamp.now());

                essayRepository.saveEssay(essay, new RepositoryCallback<Essay>() {
                    @Override
                    public void onSuccess(Essay savedEssay) {
                        userRepository.updateUserAfterEssayCompleted(score, new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                showLoading(false);
                                Intent intent = new Intent(WritingActivity.this, ResultActivity.class);
                                intent.putExtra(ResultActivity.EXTRA_ESSAY, savedEssay);
                                startActivity(intent);
                            }

                            @Override
                            public void onError(String message) {
                                showLoading(false);
                                Toast.makeText(WritingActivity.this, message, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        showLoading(false);
                        Toast.makeText(WritingActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                Toast.makeText(WritingActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private double extractScore(String aiResponse) {
        if (aiResponse == null) {
            return 0;
        }
        Pattern pattern = Pattern.compile("(?i)Điểm số\\s*:\\s*([0-9]+(?:[.,][0-9]+)?)\\s*/\\s*10");
        Matcher matcher = pattern.matcher(aiResponse);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1).replace(",", "."));
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}
