package com.example.writeai_android.ui.history;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.writeai_android.R;
import com.example.writeai_android.data.model.Essay;
import com.example.writeai_android.data.repository.EssayRepository;
import com.example.writeai_android.ui.auth.LoginActivity;
import com.example.writeai_android.ui.writing.ResultActivity;
import com.example.writeai_android.utils.BottomNavigationHelper;
import com.example.writeai_android.utils.RepositoryCallback;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView tvHistoryTotalEssay, tvHistoryAverageScore;

    private RecyclerView recyclerView;
    private EssayAdapter adapter;

    private final EssayRepository essayRepository = new EssayRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            goToLogin();
            return;
        }

        initViews();
        setupRecyclerView();
        handleEvents();

        BottomNavigationHelper.setup(this, BottomNavigationHelper.TAB_HISTORY);

        loadHistory();
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        recyclerView = findViewById(R.id.recyclerView);

        tvHistoryTotalEssay = findViewById(R.id.tvHistoryTotalEssay);
        tvHistoryAverageScore = findViewById(R.id.tvHistoryAverageScore);

        View btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new EssayAdapter(new ArrayList<>(), essay -> {
            Intent intent = new Intent(HistoryActivity.this, ResultActivity.class);
            intent.putExtra(ResultActivity.EXTRA_ESSAY, essay);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void handleEvents() {
        // Để trống nếu chưa cần xử lý thêm
    }

    private void loadHistory() {
        showLoading(true);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        essayRepository.getEssaysByUser(uid, new RepositoryCallback<List<Essay>>() {
            @Override
            public void onSuccess(List<Essay> essays) {
                showLoading(false);

                if (essays == null || essays.isEmpty()) {
                    showEmptyState();
                    return;
                }

                showDataState(essays);
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                showEmptyState();
                Toast.makeText(HistoryActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showEmptyState() {
        tvHistoryTotalEssay.setText("0");
        tvHistoryAverageScore.setText("0.0");

        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        adapter.setEssays(new ArrayList<>());
    }

    private void showDataState(List<Essay> essays) {
        int totalEssay = essays.size();
        double averageScore = calculateAverageScore(essays);

        tvHistoryTotalEssay.setText(String.valueOf(totalEssay));
        tvHistoryAverageScore.setText(formatScore(averageScore));

        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        adapter.setEssays(essays);
    }

    private double calculateAverageScore(List<Essay> essays) {
        if (essays == null || essays.isEmpty()) {
            return 0;
        }

        double totalScore = 0;

        for (Essay essay : essays) {
            totalScore += essay.getScore();
        }

        return totalScore / essays.size();
    }

    private String formatScore(double score) {
        DecimalFormat df = new DecimalFormat("0.0");
        return df.format(score);
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        if (isLoading) {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(HistoryActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}