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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeminiRepository {

    private static final String TAG = "GeminiRepository";

    private final Context context;
    private final String modelName;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

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
            callback.onError("Thiếu Gemini API key. Hãy kiểm tra BuildConfig.GEMINI_API_KEY.");
            return;
        }

        if (topic == null || topic.trim().isEmpty()) {
            callback.onError("Vui lòng chọn ít nhất một chủ đề.");
            return;
        }

        if (content == null || content.trim().isEmpty()) {
            callback.onError("Bài viết không được rỗng.");
            return;
        }

        try {
            GenerativeModel generativeModel = new GenerativeModel(modelName, apiKey);
            GenerativeModelFutures model = GenerativeModelFutures.from(generativeModel);

            String prompt = buildPrompt(topic.trim(), content.trim());

            Content requestContent = new Content.Builder()
                    .addText(prompt)
                    .build();

            ListenableFuture<GenerateContentResponse> response = model.generateContent(requestContent);

            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    mainHandler.post(() -> {
                        String text = result != null ? result.getText() : null;

                        if (text == null || text.trim().isEmpty()) {
                            callback.onError("AI trả về phản hồi rỗng.");
                            return;
                        }

                        Log.d(TAG, "AI response: " + text);
                        callback.onSuccess(text.trim());
                    });
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e(TAG, "Gemini error", t);

                    mainHandler.post(() -> {
                        String message = mapErrorMessage(t);
                        callback.onError(message);
                    });
                }
            }, executor);

        } catch (Exception e) {
            Log.e(TAG, "Failed to call Gemini", e);
            callback.onError("Lỗi khi gọi Gemini: " + e.getMessage());
        }
    }

    private String buildPrompt(String topic, String content) {
        return ""
                + "Bạn là giáo viên chấm bài viết tiếng Anh cho sinh viên Việt Nam.\n"
                + "Hãy chấm bài viết tiếng Anh bên dưới theo các chủ đề đã chọn.\n"
                + "Chủ đề có thể gồm một hoặc nhiều mục, cách nhau bằng dấu phẩy.\n\n"

                + "YÊU CẦU QUAN TRỌNG:\n"
                + "- Trả lời hoàn toàn bằng tiếng Việt.\n"
                + "- Không dùng Markdown table.\n"
                + "- Không thêm tiêu đề ngoài các tiêu đề được yêu cầu.\n"
                + "- Bắt buộc giữ đúng tên 5 mục sau để app Android tách dữ liệu.\n"
                + "- Điểm số chỉ ghi dạng số thập phân, ví dụ: 8.5/10.\n"
                + "- Phần Bản sửa hoàn chỉnh chỉ ghi lại đoạn văn đã sửa, không giải thích thêm.\n\n"

                + "FORMAT BẮT BUỘC:\n\n"

                + "Điểm số: [score]/10\n\n"

                + "Nhận xét chung:\n"
                + "[Viết 2-4 câu nhận xét tổng quan về nội dung, từ vựng, ngữ pháp và độ mạch lạc.]\n\n"

                + "Lỗi ngữ pháp:\n"
                + "- [Tên lỗi]: [giải thích ngắn gọn bằng tiếng Việt]. Ví dụ sửa: [cụm/câu đúng]\n"
                + "- [Tên lỗi]: [giải thích ngắn gọn bằng tiếng Việt]. Ví dụ sửa: [cụm/câu đúng]\n\n"

                + "Gợi ý cải thiện:\n"
                + "- [Gợi ý 1]\n"
                + "- [Gợi ý 2]\n"
                + "- [Gợi ý 3 nếu cần]\n\n"

                + "Bản sửa hoàn chỉnh:\n"
                + "[Viết lại toàn bộ bài viết bằng tiếng Anh đã được sửa đúng ngữ pháp, tự nhiên hơn nhưng vẫn giữ ý của người học.]\n\n"

                + "THÔNG TIN BÀI VIẾT:\n"
                + "Chủ đề đã chọn: " + topic + "\n\n"
                + "Bài viết của người học:\n"
                + content;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (manager == null) {
            return false;
        }

        Network network = manager.getActiveNetwork();

        if (network == null) {
            return false;
        }

        NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);

        return capabilities != null
                && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        );
    }

    private String mapErrorMessage(Throwable throwable) {
        if (throwable == null) {
            return "Đã xảy ra lỗi khi gọi AI.";
        }

        String rawMessage = throwable.getMessage();
        String message = rawMessage == null ? "" : rawMessage.toLowerCase();

        if (message.contains("404")
                || message.contains("not found")
                || message.contains("model")) {
            return "Lỗi model Gemini. Hãy kiểm tra lại tên model, ví dụ dùng gemini-2.5-flash.";
        }

        if (message.contains("api key")
                || message.contains("permission")
                || message.contains("unauthorized")
                || message.contains("403")
                || message.contains("401")) {
            return "Lỗi Gemini API key hoặc chưa được cấp quyền sử dụng API.";
        }

        if (message.contains("network")
                || message.contains("connect")
                || message.contains("timeout")
                || message.contains("unable to resolve host")) {
            return "Không có kết nối mạng hoặc kết nối quá chậm.";
        }

        if (message.contains("quota")
                || message.contains("rate")
                || message.contains("429")) {
            return "Gemini API đang bị giới hạn lượt gọi. Vui lòng thử lại sau.";
        }

        if (message.contains("candidate")
                || message.contains("safety")
                || message.contains("blocked")) {
            return "AI không thể phản hồi vì nội dung bị chặn bởi bộ lọc an toàn.";
        }

        if (rawMessage != null && !rawMessage.trim().isEmpty()) {
            return "Không thể chấm bài lúc này: " + rawMessage;
        }

        return "Không thể chấm bài lúc này.";
    }
}