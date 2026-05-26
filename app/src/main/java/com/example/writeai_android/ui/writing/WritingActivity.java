package com.example.writeai_android.ui.writing;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
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
import com.example.writeai_android.ui.auth.LoginActivity;
import com.example.writeai_android.ui.main.MainActivity;
import com.example.writeai_android.utils.RepositoryCallback;
import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WritingActivity extends AppCompatActivity {

    private final String[] topics = new String[]{
            "Work",
            "Office",
            "Travel",
            "Health",
            "Finance",
            "Shopping",
            "Dining",
            "Orders",
            "Housing",
            "Meetings",
            "Personnel",
            "News",
            "Weather",
            "Events",
            "Other"
    };

    private FlexboxLayout layoutTopics;
    private EditText etEssayContent;
    private ProgressBar progressBar;

    private TextView tvWordCount, tvCharCount, tvScore, tvCorrectedText;
    private LinearLayoutCompatFix layoutAiResultFix;
    private View layoutAiResult;
    private android.widget.LinearLayout layoutAnalysis;
    private Button btnGradeEssay, btnSaveEssay;

    private final List<String> selectedTopics = new ArrayList<>();

    private GeminiRepository geminiRepository;
    private final EssayRepository essayRepository = new EssayRepository();
    private final UserRepository userRepository = new UserRepository();

    private String lastAiResponse = "";
    private String lastEssayContent = "";
    private double lastScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        geminiRepository = new GeminiRepository(getApplicationContext());

        initViews();
        setupTopics();
        setupTextCounter();
        handleEvents();
    }

    private void initViews() {
        layoutTopics = findViewById(R.id.layoutTopics);
        etEssayContent = findViewById(R.id.etEssayContent);
        progressBar = findViewById(R.id.progressBar);

        tvWordCount = findViewById(R.id.tvWordCount);
        tvCharCount = findViewById(R.id.tvCharCount);
        tvScore = findViewById(R.id.tvScore);
        tvCorrectedText = findViewById(R.id.tvCorrectedText);

        layoutAiResult = findViewById(R.id.layoutAiResult);
        layoutAnalysis = findViewById(R.id.layoutAnalysis);

        btnGradeEssay = findViewById(R.id.btnGradeEssay);
        btnSaveEssay = findViewById(R.id.btnSaveEssay);

        View btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupTopics() {
        layoutTopics.removeAllViews();

        for (String topic : topics) {
            TextView topicView = createTopicView(topic);
            layoutTopics.addView(topicView);
        }

        // Mặc định chọn Work
        selectedTopics.add("Work");
        refreshTopicViews();
    }

    private TextView createTopicView(String topic) {
        TextView textView = new TextView(this);
        textView.setText(topic);
        textView.setTextSize(13);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(dp(16), dp(9), dp(16), dp(9));
        textView.setSingleLine(false);
        textView.setMinWidth(dp(58));
        textView.setMinHeight(dp(42));

        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, dp(8), dp(8));
        textView.setLayoutParams(params);

        textView.setOnClickListener(v -> {
            if (selectedTopics.contains(topic)) {
                selectedTopics.remove(topic);
            } else {
                selectedTopics.add(topic);
            }

            refreshTopicViews();
        });

        return textView;
    }

    private void refreshTopicViews() {
        for (int i = 0; i < layoutTopics.getChildCount(); i++) {
            View child = layoutTopics.getChildAt(i);

            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                String topic = tv.getText().toString();

                if (selectedTopics.contains(topic)) {
                    tv.setBackgroundResource(R.drawable.bg_topic_selected);
                    tv.setTextColor(Color.WHITE);
                } else {
                    tv.setBackgroundResource(R.drawable.bg_topic_unselected);
                    tv.setTextColor(Color.parseColor("#5F636D"));
                }
            }
        }
    }

    private void setupTextCounter() {
        etEssayContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCounter();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        updateCounter();
    }

    private void updateCounter() {
        String content = etEssayContent.getText().toString().trim();

        int charCount = etEssayContent.getText().toString().length();
        int wordCount = 0;

        if (!TextUtils.isEmpty(content)) {
            wordCount = content.split("\\s+").length;
        }

        tvWordCount.setText(wordCount + " words");
        tvCharCount.setText(charCount + " chars");
    }

    private void handleEvents() {
        btnGradeEssay.setOnClickListener(v -> gradeEssay());

        btnSaveEssay.setOnClickListener(v -> saveEssayAfterAiGraded());
    }

    private void gradeEssay() {
        String content = etEssayContent.getText().toString().trim();

        if (selectedTopics.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 chủ đề", Toast.LENGTH_SHORT).show();
            return;
        }

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
        layoutAiResult.setVisibility(View.GONE);

        String topicText = TextUtils.join(", ", selectedTopics);

        geminiRepository.evaluateEssay(topicText, content, new RepositoryCallback<String>() {
            @Override
            public void onSuccess(String aiResponse) {
                showLoading(false);

                lastAiResponse = aiResponse;
                lastEssayContent = content;
                lastScore = extractScore(aiResponse);

                showAiResult(aiResponse, lastScore);
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                Toast.makeText(WritingActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showAiResult(String aiResponse, double score) {
        layoutAiResult.setVisibility(View.VISIBLE);

        tvScore.setText(formatScore(score));
        tvCorrectedText.setText(extractCorrectedVersion(aiResponse));

        layoutAnalysis.removeAllViews();

        List<String> analysisItems = extractAnalysisItems(aiResponse);

        if (analysisItems.isEmpty()) {
            analysisItems.add(aiResponse);
        }

        for (int i = 0; i < analysisItems.size(); i++) {
            addAnalysisCard(analysisItems.get(i), i);
        }
    }

    private void addAnalysisCard(String content, int index) {
        TextView card = new TextView(this);
        card.setText(content);
        card.setTextColor(Color.parseColor("#071B33"));
        card.setTextSize(15);
        card.setLineSpacing(4, 1.0f);
        card.setPadding(dp(18), dp(16), dp(16), dp(16));

        if (index % 2 == 0) {
            card.setBackgroundResource(R.drawable.bg_analysis_card_blue);
        } else {
            card.setBackgroundResource(R.drawable.bg_analysis_card_red);
        }

        android.widget.LinearLayout.LayoutParams params =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(params);

        layoutAnalysis.addView(card);
    }

    private void saveEssayAfterAiGraded() {
        if (TextUtils.isEmpty(lastAiResponse) || TextUtils.isEmpty(lastEssayContent)) {
            Toast.makeText(this, "Bạn cần chấm điểm với AI trước khi lưu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveEssay.setEnabled(false);
        btnSaveEssay.setText("Đang lưu...");

        String topicText = TextUtils.join(", ", selectedTopics);

        Essay essay = new Essay();
        essay.setEssayId(null);
        essay.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        essay.setTopic(topicText);
        essay.setContent(lastEssayContent);
        essay.setAiFeedback(lastAiResponse);
        essay.setScore(lastScore);
        essay.setCreatedAt(Timestamp.now());

        essayRepository.saveEssay(essay, new RepositoryCallback<Essay>() {
            @Override
            public void onSuccess(Essay savedEssay) {
                userRepository.updateUserAfterEssayCompleted(lastScore, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(WritingActivity.this, "Đã lưu bài viết", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(WritingActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        btnSaveEssay.setEnabled(true);
                        btnSaveEssay.setText("Lưu lại");
                        Toast.makeText(WritingActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                btnSaveEssay.setEnabled(true);
                btnSaveEssay.setText("Lưu lại");
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

    private String extractCorrectedVersion(String aiResponse) {
        if (aiResponse == null) {
            return "";
        }

        Pattern pattern = Pattern.compile(
                "(?i)Bản sửa hoàn chỉnh\\s*:\\s*([\\s\\S]*)"
        );

        Matcher matcher = pattern.matcher(aiResponse);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return aiResponse;
    }

    private List<String> extractAnalysisItems(String aiResponse) {
        List<String> items = new ArrayList<>();

        if (aiResponse == null) {
            return items;
        }

        String mistakes = extractSection(aiResponse, "Lỗi ngữ pháp", "Gợi ý cải thiện");
        String suggestions = extractSection(aiResponse, "Gợi ý cải thiện", "Bản sửa hoàn chỉnh");
        String feedback = extractSection(aiResponse, "Nhận xét chung", "Lỗi ngữ pháp");

        if (!TextUtils.isEmpty(feedback)) {
            items.add("Nhận xét chung\n" + feedback.trim());
        }

        if (!TextUtils.isEmpty(mistakes)) {
            items.add("Lỗi ngữ pháp\n" + mistakes.trim());
        }

        if (!TextUtils.isEmpty(suggestions)) {
            items.add("Gợi ý cải thiện\n" + suggestions.trim());
        }

        return items;
    }

    private String extractSection(String text, String startTitle, String endTitle) {
        Pattern pattern = Pattern.compile(
                "(?i)" + Pattern.quote(startTitle) + "\\s*:\\s*([\\s\\S]*?)" + Pattern.quote(endTitle) + "\\s*:"
        );

        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return "";
    }

    private String formatScore(double score) {
        return String.format(java.util.Locale.US, "%.1f", score);
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnGradeEssay.setEnabled(!isLoading);
        btnGradeEssay.setText(isLoading ? "AI đang chấm..." : "Chấm điểm với AI");
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    // Class rỗng này không dùng logic, chỉ tránh nhầm import nếu Android Studio tự thêm sai.
    private static class LinearLayoutCompatFix {
    }
}