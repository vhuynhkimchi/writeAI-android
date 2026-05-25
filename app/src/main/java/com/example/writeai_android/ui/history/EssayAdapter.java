package com.example.writeai_android.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.writeai_android.R;
import com.example.writeai_android.data.model.Essay;
import com.example.writeai_android.utils.DateTimeFormatter;

import java.text.DecimalFormat;
import java.util.List;

public class EssayAdapter extends RecyclerView.Adapter<EssayAdapter.EssayViewHolder> {

    public interface OnEssayClickListener {
        void onClick(Essay essay);
    }

    private List<Essay> essays;
    private final OnEssayClickListener listener;

    public EssayAdapter(List<Essay> essays, OnEssayClickListener listener) {
        this.essays = essays;
        this.listener = listener;
    }

    public void setEssays(List<Essay> essays) {
        this.essays = essays;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EssayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_essay, parent, false);
        return new EssayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EssayViewHolder holder, int position) {
        Essay essay = essays.get(position);
        holder.tvTopic.setText(safeText(essay.getTopic()));
        holder.tvDate.setText(DateTimeFormatter.formatReadableDate(essay.getCreatedAt()));
        holder.tvScore.setText("Score: " + formatScore(essay.getScore()) + "/10");
        holder.tvShortContent.setText(DateTimeFormatter.shortenContent(essay.getContent(), 80));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(essay);
            }
        });
    }

    @Override
    public int getItemCount() {
        return essays == null ? 0 : essays.size();
    }

    static class EssayViewHolder extends RecyclerView.ViewHolder {
        TextView tvTopic, tvDate, tvScore, tvShortContent;

        public EssayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTopic = itemView.findViewById(R.id.tvTopic);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvShortContent = itemView.findViewById(R.id.tvShortContent);
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String formatScore(double score) {
        DecimalFormat df = new DecimalFormat("0.0");
        return df.format(score);
    }
}
