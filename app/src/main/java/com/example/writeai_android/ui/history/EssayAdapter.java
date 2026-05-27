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
import java.util.Locale;

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
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_essay, parent, false);

        return new EssayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EssayViewHolder holder, int position) {
        Essay essay = essays.get(position);

        holder.tvItemTopic.setText(formatTopic(essay.getTopic()));
        holder.tvItemScore.setText(formatScore(essay.getScore()) + "/10");
        holder.tvItemContent.setText(DateTimeFormatter.shortenContent(
                safeText(essay.getContent()),
                90
        ));
        holder.tvItemDate.setText(DateTimeFormatter.formatReadableDate(essay.getCreatedAt()));

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

        TextView tvItemTopic, tvItemDate, tvItemScore, tvItemContent;

        public EssayViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItemTopic = itemView.findViewById(R.id.tvItemTopic);
            tvItemDate = itemView.findViewById(R.id.tvItemDate);
            tvItemScore = itemView.findViewById(R.id.tvItemScore);
            tvItemContent = itemView.findViewById(R.id.tvItemContent);
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String formatScore(double score) {
        DecimalFormat df = new DecimalFormat("0.0");
        return df.format(score);
    }

    private String formatTopic(String topic) {
        if (topic == null || topic.trim().isEmpty()) {
            return "KHÁC";
        }

        String result = topic.toUpperCase(Locale.ROOT);

        if (result.length() > 22) {
            result = result.substring(0, 22) + "...";
        }

        return result;
    }
}