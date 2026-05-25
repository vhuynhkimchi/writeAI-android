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
import com.example.writeai_android.utils.RepositoryCallback;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView emptyView;
    private EssayAdapter adapter;
    private final EssayRepository essayRepository = new EssayRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        adapter = new EssayAdapter(new ArrayList<>(), essay -> {
            Intent intent = new Intent(HistoryActivity.this, ResultActivity.class);
            intent.putExtra(ResultActivity.EXTRA_ESSAY, essay);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadHistory();
    }

    private void loadHistory() {
        progressBar.setVisibility(View.VISIBLE);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        essayRepository.getEssaysByUser(uid, new RepositoryCallback<List<Essay>>() {
            @Override
            public void onSuccess(List<Essay> essays) {
                progressBar.setVisibility(View.GONE);
                if (essays == null || essays.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    return;
                }
                emptyView.setVisibility(View.GONE);
                adapter.setEssays(essays);
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                Toast.makeText(HistoryActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
