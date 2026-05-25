package com.example.writeai_android.data.repository;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.writeai_android.BuildConfig;
import com.example.writeai_android.utils.RepositoryCallback;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executors;

public class GeminiRepository {

    private static final String TAG = "GeminiRepository";
    private final Context context;
    private final String modelName;

    public GeminiRepository(Context context) {
        this(context, "gemini-2.5-flash");
    }

    public GeminiRepository(Context context, String modelName) {
        this.context = context.getApplicationContext();
        this.modelName = modelName;
    }

    public void evaluateEssay(String topic, String content, RepositoryCallback<String> callback) {
        if (!isNetworkAvailable()) {
            callback.onError("Không có kết nối mạng. Vui lòng kiểm tra Internet và thử lại.");
            return;
        }

        String apiKey = BuildConfig.GEMINI_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            callback.onError("Thiếu Gemini API key.");
            return;
        }

        try {
            GenerativeModel generativeModel = new GenerativeModel(modelName, apiKey);
            GenerativeModelFutures model = GenerativeModelFutures.from(generativeModel);

            String prompt = buildPrompt(topic, content);
            ListenableFuture<GenerateContentResponse> response =
                    model.generateContent(new Content.Builder().addText(prompt).build());

            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        String text = result != null ? result.getText() : null;
                        if (text == null || text.trim().isEmpty()) {
                            callback.onError("AI trả về phản hồi rỗng.");
                            return;
                        }
                        callback.onSuccess(text.trim());
                    });
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e(TAG, "Gemini error", t);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        String message = mapErrorMessage(t);
                        callback.onError(message);
                    });
                }
            }, Executors.newSingleThreadExecutor());
        } catch (Exception e) {
            Log.e(TAG, "Failed to call Gemini", e);
            callback.onError("Lỗi model AI hoặc lỗi cấu hình: " + e.getMessage());
        }
    }

    private String buildPrompt(String topic, String content) {
        return "You are an English writing teacher.\n"
                + "Please evaluate the following English paragraph.\n"
                + "Return the result in Vietnamese using exactly this format:\n\n"
                + "Điểm số: [score]/10\n\n"
                + "Nhận xét chung:\n"
                + "[general feedback]\n\n"
                + "Lỗi ngữ pháp:\n"
                + "- [mistake 1]\n"
                + "- [mistake 2]\n\n"
                + "Gợi ý cải thiện:\n"
                + "- [suggestion 1]\n"
                + "- [suggestion 2]\n\n"
                + "Bản sửa hoàn chỉnh:\n"
                + "[corrected version]\n\n"
                + "Student topic:\n"
                + topic + "\n\n"
                + "Student writing:\n"
                + content;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        Network network = manager.getActiveNetwork();
        if (network == null) {
            return false;
        }
        NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
        return capabilities != null
                && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    private String mapErrorMessage(Throwable throwable) {
        if (throwable == null) {
            return "Đã xảy ra lỗi khi gọi AI.";
        }
        String message = throwable.getMessage() == null ? "" : throwable.getMessage().toLowerCase();
        if (message.contains("network") || message.contains("connect") || message.contains("timeout")) {
            return "Không có kết nối mạng hoặc kết nối quá chậm.";
        }
        if (message.contains("api") || message.contains("permission") || message.contains("unauthorized")) {
            return "Lỗi API Gemini hoặc key không hợp lệ.";
        }
        if (message.contains("model")) {
            return "Lỗi model AI hoặc model không khả dụng.";
        }
        return "Không thể chấm bài lúc này: " + throwable.getMessage();
    }
}
