# WriteAI Android - Ứng dụng luyện viết Tiếng Anh thông minh với AI
### Link Drive chạy chương trình: https://drive.google.com/file/d/1KNKc_6cEFWuvhrZwgckqzuB6PI9wrGfz/view?usp=drive_link 

**WriteAI Android** là ứng dụng Android hỗ trợ người dùng luyện viết Tiếng Anh theo chủ đề và nhận phản hồi tự động từ AI. Ứng dụng sử dụng **Gemini AI** để chấm điểm bài viết, phát hiện lỗi ngữ pháp, đưa ra gợi ý cải thiện và tạo bản sửa hoàn chỉnh.

Dự án được xây dựng bằng **Java Android**, kết hợp với **Firebase Authentication**, **Cloud Firestore** và **Gemini API**.

---

## Giới thiệu

Ứng dụng được phát triển nhằm hỗ trợ người học cải thiện kỹ năng viết Tiếng Anh thông qua việc luyện viết hằng ngày. Người dùng có thể chọn chủ đề, nhập bài viết, gửi bài đến AI để được chấm điểm và lưu lại kết quả để theo dõi quá trình học tập.

Các dữ liệu như thông tin người dùng, lịch sử bài viết, điểm số, streak và ngày luyện gần nhất được lưu trữ trên **Cloud Firestore**.

---

## Chức năng chính

### Quản lý tài khoản

* Đăng ký tài khoản bằng email và mật khẩu.
* Đăng nhập bằng email và mật khẩu.
* Đăng nhập bằng tài khoản Google.
* Quên mật khẩu thông qua email khôi phục.
* Đổi mật khẩu đối với tài khoản email/mật khẩu.
* Đăng xuất khỏi hệ thống.
* Hiển thị tên, email và ảnh đại diện người dùng.

### Luyện viết và chấm điểm bằng AI

* Chọn một hoặc nhiều chủ đề luyện viết.
* Nhập bài viết Tiếng Anh.
* Đếm số từ và số ký tự trong bài viết.
* Gửi bài viết đến Gemini AI để phân tích.
* Nhận kết quả gồm:

  * Điểm số.
  * Nhận xét chung.
  * Lỗi ngữ pháp.
  * Gợi ý cải thiện.
  * Bản sửa hoàn chỉnh.

### Quản lý bài viết

* Lưu bài viết sau khi AI chấm điểm.
* Xem danh sách bài viết đã lưu.
* Xem điểm số, chủ đề, thời gian và nội dung rút gọn.
* Xem chi tiết bài viết, bản sửa và phản hồi từ AI.

### Theo dõi tiến độ học tập

* Thống kê tổng số bài viết.
* Hiển thị điểm trung bình.
* Theo dõi streak luyện tập.
* Điểm danh hằng ngày bằng icon trạng thái.
* Nhắc nhở học tập hằng ngày bằng thông báo Android.

---

## Công nghệ sử dụng

| Công nghệ               | Mục đích                                                      |
| ----------------------- | ------------------------------------------------------------- |
| Java Android            | Xây dựng ứng dụng Android                                     |
| Firebase Authentication | Đăng ký, đăng nhập, Google Login, quên mật khẩu, đổi mật khẩu |
| Cloud Firestore         | Lưu thông tin người dùng và lịch sử bài viết                  |
| Gemini API              | Chấm điểm và phản hồi bài viết Tiếng Anh                      |
| Google Sign-In          | Đăng nhập bằng tài khoản Google                               |
| Glide                   | Hiển thị ảnh đại diện từ Google                               |
| RecyclerView            | Hiển thị danh sách lịch sử bài viết                           |
| AlarmManager            | Đặt lịch nhắc nhở học tập                                     |
| SharedPreferences       | Lưu trạng thái bật/tắt nhắc nhở                               |
| FlexboxLayout           | Hiển thị danh sách chủ đề luyện viết linh hoạt                |

---

## Cấu trúc dữ liệu Firestore

Ứng dụng sử dụng hai collection chính:

### Collection `users`

Lưu thông tin người dùng và thống kê học tập.

```text
users
 └── {uid}
     ├── uid
     ├── fullName
     ├── email
     ├── photoUrl
     ├── loginProvider
     ├── createdAt
     ├── streakCount
     ├── totalEssay
     ├── averageScore
     └── lastPracticeDate
```

### Collection `essays`

Lưu bài viết và kết quả chấm điểm từ AI.

```text
essays
 └── {essayId}
     ├── essayId
     ├── userId
     ├── topic
     ├── content
     ├── aiFeedback
     ├── score
     └── createdAt
```

---

## Luồng hoạt động chính

### Đăng ký và đăng nhập

Người dùng có thể đăng ký bằng email/mật khẩu hoặc đăng nhập bằng Google. Sau khi đăng nhập thành công, thông tin người dùng được lấy từ Firebase Authentication và lưu vào Cloud Firestore.

### Luyện viết với AI

Người dùng chọn chủ đề, nhập bài viết và bấm **Chấm điểm với AI**. Ứng dụng gửi nội dung bài viết đến Gemini API. AI trả về điểm số, nhận xét, lỗi sai, gợi ý cải thiện và bản sửa hoàn chỉnh.

### Lưu bài viết

Sau khi AI chấm xong, người dùng có thể bấm **Lưu lại**. Bài viết, điểm số và phản hồi AI được lưu vào collection `essays`. Đồng thời, hệ thống cập nhật tổng số bài viết, điểm trung bình, streak và ngày luyện gần nhất trong collection `users`.

### Lịch sử bài viết

Người dùng có thể xem danh sách bài viết đã lưu. Khi bấm vào một bài viết, ứng dụng mở màn hình chi tiết để hiển thị nội dung bài viết gốc, bản sửa từ AI và phần phân tích chi tiết.

---

## Cài đặt và chạy dự án

### 1. Clone dự án

```bash
git clone https://github.com/your-username/writeAI-android.git
cd writeAI-android
```

### 2. Mở bằng Android Studio

Mở thư mục dự án bằng **Android Studio**.

### 3. Cấu hình Firebase

Tạo project trên Firebase Console và bật các dịch vụ:

* Firebase Authentication.
* Google Sign-In.
* Cloud Firestore.

Sau đó tải file:

```text
google-services.json
```

và đặt vào thư mục:

```text
app/google-services.json
```

### 4. Thêm SHA-1 cho Google Sign-In

Chạy lệnh:

```bash
./gradlew signingReport
```

Copy SHA-1 debug và thêm vào Firebase Console:

```text
Project settings → Your apps → Android app → SHA certificate fingerprints
```

Sau đó tải lại file `google-services.json` nếu cần.

### 5. Cấu hình Gemini API Key

Trong file `local.properties`, thêm:

```properties
GEMINI_API_KEY=your_gemini_api_key_here
```

Trong `build.gradle`, cần đảm bảo API key được đưa vào `BuildConfig`.

Ví dụ:

```gradle
buildConfigField "String", "GEMINI_API_KEY", "\"${project.properties['GEMINI_API_KEY']}\""
```

### 6. Build và chạy ứng dụng

```bash
./gradlew clean
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Hoặc chạy trực tiếp bằng Android Studio.

---

## Một số thư viện cần có

Trong file `app/build.gradle`, dự án sử dụng các thư viện chính:

```gradle
implementation "com.google.firebase:firebase-auth"
implementation "com.google.firebase:firebase-firestore"
implementation "com.google.android.gms:play-services-auth"
implementation "com.google.ai.client.generativeai:generativeai"
implementation "com.github.bumptech.glide:glide:4.16.0"
implementation "com.google.android.flexbox:flexbox:3.0.0"
```

---

## Giao diện chính

Ứng dụng gồm các màn hình chính:

* Màn hình đăng nhập.
* Màn hình đăng ký.
* Màn hình trang chủ.
* Màn hình luyện viết.
* Màn hình lịch sử bài viết.
* Màn hình chi tiết bài viết.
* Màn hình tài khoản.
* Màn hình đổi mật khẩu.
* Màn hình quên mật khẩu.

Thanh điều hướng dưới cùng giúp người dùng chuyển nhanh giữa:

```text
Trang chủ → Lịch sử → Tài khoản
```

---

## Kết quả đạt được

Dự án đã xây dựng được một ứng dụng Android hỗ trợ luyện viết Tiếng Anh với AI. Ứng dụng có thể xác thực tài khoản, đăng nhập bằng Google, chấm điểm bài viết bằng Gemini AI, lưu lịch sử bài viết, xem chi tiết phản hồi AI, thống kê tiến độ học tập và nhắc nhở người dùng luyện viết hằng ngày.

Ứng dụng đáp ứng được mục tiêu hỗ trợ người học cải thiện kỹ năng viết Tiếng Anh thông qua việc luyện tập thường xuyên và nhận phản hồi tự động từ AI.

---

## Hướng phát triển

Trong tương lai, ứng dụng có thể được mở rộng thêm các chức năng:

* Biểu đồ tiến độ học tập theo tuần/tháng.
* Lịch điểm danh chi tiết.
* Gợi ý chủ đề luyện viết hằng ngày.
* Phân loại trình độ người học.
* Hệ thống huy hiệu hoặc phần thưởng học tập.
* Đồng bộ nhắc nhở học tập trên nhiều thiết bị.

---

## Tác giả

Dự án được phát triển phục vụ học tập và báo cáo môn lập trình Android.

```text
WriteAI Android
Ứng dụng luyện viết Tiếng Anh thông minh với AI chấm bài tự động
```
