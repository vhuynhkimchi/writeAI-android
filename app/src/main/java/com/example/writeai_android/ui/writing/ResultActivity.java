package com.example.writeai_android.ui.writing;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.writeai_android.R;
import com.example.writeai_android.data.model.Essay;
import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.writeai_android.data.repository.EssayRepository;
import com.example.writeai_android.utils.RepositoryCallback;

public class ResultActivity extends AppCompatActivity {

    public static final String EXTRA_ESSAY = "extra_essay";

    private FlexboxLayout layoutTopics;
    private TextView tvCreatedAt, tvEssayContent, tvCorrectedText;
    private LinearLayout layoutAnalysis;

    private Essay essay;

    public static final String EXTRA_ESSAY_ID = "extra_essay_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        initViews();

        String essayId = getIntent().getStringExtra(EXTRA_ESSAY_ID);

        if (essayId == null || essayId.trim().isEmpty()) {
            Toast.makeText(this, "Không có ID bài viết", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadEssayDetail(essayId);
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView btnMore = findViewById(R.id.btnMore);

        layoutTopics = findViewById(R.id.layoutTopics);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        tvEssayContent = findViewById(R.id.tvEssayContent);
        tvCorrectedText = findViewById(R.id.tvCorrectedText);
        layoutAnalysis = findViewById(R.id.layoutAnalysis);

        btnBack.setOnClickListener(v -> finish());

        btnMore.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng mở rộng sẽ làm sau", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadEssayDetail(String essayId) {
    EssayRepository essayRepository = new EssayRepository();

    essayRepository.getEssayDetail(essayId, new RepositoryCallback<Essay>() {
        @Override
        public void onSuccess(Essay result) {
            if (result == null) {
                Toast.makeText(ResultActivity.this, "Không tìm thấy bài viết", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            essay = result;
            bindData();
        }

        @Override
        public void onError(String message) {
            Toast.makeText(ResultActivity.this, message, Toast.LENGTH_LONG).show();
            finish();
        }
    });
}

    private void bindData() {
        showTopics(essay.getTopic());

        tvCreatedAt.setText(formatDateTime(essay.getCreatedAt()));
        tvEssayContent.setText(safeText(essay.getContent()));

        String aiFeedback = safeText(essay.getAiFeedback());

        tvCorrectedText.setText(extractCorrectedVersion(aiFeedback));

        showAnalysis(aiFeedback);
    }

    private void showTopics(String topicText) {
        layoutTopics.removeAllViews();

        if (TextUtils.isEmpty(topicText)) {
            addTopicChip("Khác");
            return;
        }

        String[] topics = topicText.split(",");

        for (String topic : topics) {
            String value = topic.trim();

            if (!value.isEmpty()) {
                addTopicChip(value);
            }
        }
    }

    private void addTopicChip(String topic) {
        TextView chip = new TextView(this);

        chip.setText(topic);
        chip.setTextColor(getColorCompat("#0079B8"));
        chip.setTextSize(14);
        chip.setGravity(Gravity.CENTER);
        chip.setSingleLine(true);
        chip.setBackgroundResource(R.drawable.bg_topic_chip);
        chip.setPadding(dp(16), 0, dp(16), 0);

        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                dp(40)
        );
        params.setMargins(0, 0, dp(8), dp(8));
        chip.setLayoutParams(params);

        layoutTopics.addView(chip);
    }

    private void showAnalysis(String aiFeedback) {
        layoutAnalysis.removeAllViews();

        List<String> items = extractAnalysisItems(aiFeedback);

        if (items.isEmpty()) {
            addAnalysisCard("Phân tích từ AI", aiFeedback, 0);
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            String title = getFirstLine(item);
            String body = removeFirstLine(item);

            addAnalysisCard(title, body, i);
        }
    }

    private void addAnalysisCard(String title, String body, int index) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(dp(22), dp(18), dp(18), dp(18));

        if (index % 3 == 0) {
            card.setBackgroundResource(R.drawable.bg_analysis_red);
        } else if (index % 3 == 1) {
            card.setBackgroundResource(R.drawable.bg_analysis_blue);
        } else {
            card.setBackgroundResource(R.drawable.bg_analysis_dark);
        }

        TextView icon = new TextView(this);
        icon.setWidth(dp(48));
        icon.setHeight(dp(48));
        icon.setGravity(Gravity.CENTER);
        icon.setTextSize(22);
        icon.setTextColor(getColorCompat("#FFFFFF"));

        if (index % 3 == 0) {
            icon.setText("A");
            icon.setBackgroundColor(getColorCompat("#FFD7D5"));
            icon.setTextColor(getColorCompat("#D11A2A"));
        } else if (index % 3 == 1) {
            icon.setText("▧");
            icon.setBackgroundColor(getColorCompat("#4AA3FF"));
        } else {
            icon.setText("✎");
            icon.setBackgroundColor(getColorCompat("#123A78"));
        }

        LinearLayout textBox = new LinearLayout(this);
        textBox.setOrientation(LinearLayout.VERTICAL);
        textBox.setPadding(dp(18), 0, 0, 0);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(getColorCompat("#071B33"));
        tvTitle.setTextSize(17);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvBody = new TextView(this);
        tvBody.setText(body);
        tvBody.setTextColor(getColorCompat("#4B5563"));
        tvBody.setTextSize(15);
        tvBody.setLineSpacing(5, 1.0f);
        tvBody.setPadding(0, dp(8), 0, 0);

        textBox.addView(tvTitle);
        textBox.addView(tvBody);

        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dp(48),
                dp(48)
        );
        icon.setLayoutParams(iconParams);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        textBox.setLayoutParams(textParams);

        card.addView(icon);
        card.addView(textBox);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(16));
        card.setLayoutParams(cardParams);

        layoutAnalysis.addView(card);
    }

    private List<String> extractAnalysisItems(String aiResponse) {
        List<String> items = new ArrayList<>();

        if (TextUtils.isEmpty(aiResponse)) {
            return items;
        }

        String feedback = extractSection(aiResponse, "Nhận xét chung", "Lỗi ngữ pháp");
        String mistakes = extractSection(aiResponse, "Lỗi ngữ pháp", "Gợi ý cải thiện");
        String suggestions = extractSection(aiResponse, "Gợi ý cải thiện", "Bản sửa hoàn chỉnh");

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

    private String extractCorrectedVersion(String aiResponse) {
        if (TextUtils.isEmpty(aiResponse)) {
            return "Không có bản sửa.";
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

    private String getFirstLine(String text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }

        String[] lines = text.split("\\n", 2);
        return lines[0].trim();
    }

    private String removeFirstLine(String text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }

        String[] lines = text.split("\\n", 2);

        if (lines.length < 2) {
            return "";
        }

        return lines[1].trim();
    }

    private String formatDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private int getColorCompat(String color) {
        return android.graphics.Color.parseColor(color);
    }
}