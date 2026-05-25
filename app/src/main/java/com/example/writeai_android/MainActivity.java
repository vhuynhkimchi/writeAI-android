package com.example.writeai_android;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

// Import thư viện Gemini AI
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AI_TEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ví dụ về chủ đề và nội dung bài luận
        String topic = "The Impact of Technology on Education";
        String essayContent = "Technology has revolutionized education by providing access to vast resources and enabling remote learning. However, it also poses challenges such as distractions and the digital divide. Overall, technology has the potential to enhance learning experiences if used effectively.";
        askGeminiToGradeEssay(topic, essayContent);
    }

    private void askGeminiToGradeEssay(String topic, String essayContent) {
        // 1. Khởi tạo Model Gemini (Sử dụng dòng 2.5 Flash cho tốc độ phản hồi nhanh)
        String apiKey = BuildConfig.GEMINI_API_KEY;
        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", apiKey);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        // 2. Tạo câu lệnh (Prompt) kỹ lưỡng để ép AI trả về đúng cấu trúc bạn muốn
        String prompt = "You are an English teacher. Grade this essay.\n" +
                "Topic: " + topic + "\n" +
                "Content: " + essayContent + "\n" +
                "Please provide the result strictly in this format:\n" +
                "Score: [Your Score/10]\n" +
                "Feedback: [Your short correction and comments]";

        Log.d(TAG, "Đang gửi bài lên AI chấm...");

        // 3. Thực hiện gọi AI chạy bất đồng bộ (Async) tránh đơ màn hình app
        ListenableFuture<GenerateContentResponse> response = model.generateContent(
                new Content.Builder().addText(prompt).build());

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                // Nhận kết quả chữ trả về từ AI
                String aiResponseText = result.getText();
                Log.d(TAG, "🎉 AI đã phản hồi:\n" + aiResponseText);

                // Tại đây bạn có thể viết thêm code để lưu cái aiResponseText này vào Firestore
                // Database!
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "❌ Lỗi khi gọi AI: ", t);
            }
        }, Executors.newSingleThreadExecutor());
    }
}