package com.example.writeai_android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

import com.example.writeai_android.R;
import com.example.writeai_android.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN_TEST";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoRegister, tvForgotPassword;
    private ImageView imgTogglePassword;
    private LinearLayout btnGoogleLogin;

    private FirebaseAuth auth;
    private boolean isPasswordVisible = false;

    private GoogleSignInClient googleSignInClient;
    private FirebaseFirestore firestore;

    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        firestore = FirebaseFirestore.getInstance();
        setupGoogleSignIn();
        if (getIntent().getBooleanExtra("open_google_login", false)) {
            signInWithGoogle();
        }

        if (auth.getCurrentUser() != null) {
            goToMain();
            return;
        }

        initViews();
        handleEvents();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
    }

    private void handleEvents() {
        btnLogin.setOnClickListener(v -> {
            Log.d(TAG, "Đã bấm nút đăng nhập");
            Toast.makeText(this, "Đang xử lý đăng nhập...", Toast.LENGTH_SHORT).show();
            login();
        });

        tvGoRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        imgTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();

                    try {
                        GoogleSignInAccount account = GoogleSignIn
                                .getSignedInAccountFromIntent(data)
                                .getResult(ApiException.class);

                        firebaseAuthWithGoogle(account.getIdToken());

                    } catch (ApiException e) {
                        Toast.makeText(
                                LoginActivity.this,
                                "Đăng nhập Google thất bại: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void signInWithGoogle() {
        if (googleSignInClient == null) {
            Toast.makeText(this, "Google Sign-In chưa được khởi tạo", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null || idToken.trim().isEmpty()) {
            Toast.makeText(this, "Không lấy được Google ID Token", Toast.LENGTH_LONG).show();
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    if (firebaseUser == null) {
                        Toast.makeText(this, "Không lấy được thông tin Google user", Toast.LENGTH_LONG).show();
                        return;
                    }

                    saveGoogleUserToFirestore(firebaseUser);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            LoginActivity.this,
                            "Firebase Google Auth lỗi: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void saveGoogleUserToFirestore(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();

        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        goToMain();
                        return;
                    }

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("uid", uid);
                    userData.put("fullName", firebaseUser.getDisplayName() == null ? "" : firebaseUser.getDisplayName());
                    userData.put("email", firebaseUser.getEmail() == null ? "" : firebaseUser.getEmail());
                    userData.put("photoUrl", firebaseUser.getPhotoUrl() == null ? "" : firebaseUser.getPhotoUrl().toString());
                    userData.put("loginProvider", "google");
                    userData.put("createdAt", Timestamp.now());
                    userData.put("streakCount", 0);
                    userData.put("totalEssay", 0);
                    userData.put("averageScore", 0.0);
                    userData.put("lastPracticeDate", "");

                    firestore.collection("users")
                            .document(uid)
                            .set(userData)
                            .addOnSuccessListener(unused -> goToMain())
                            .addOnFailureListener(e -> {
                                Toast.makeText(
                                        LoginActivity.this,
                                        "Lưu thông tin Google user thất bại: " + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            LoginActivity.this,
                            "Không kiểm tra được user: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        Log.d(TAG, "Email nhập: " + email);

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email không được rỗng");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Mật khẩu không được rỗng");
            etPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(
                            LoginActivity.this,
                            "Đăng nhập thành công",
                            Toast.LENGTH_SHORT
                    ).show();

                    goToMain();
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Đăng Nhập");

                    String message = "Đăng nhập thất bại.";

                    if (e instanceof FirebaseAuthInvalidUserException) {
                        message = "Email chưa được đăng ký hoặc tài khoản không tồn tại.";
                    } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        message = "Email hoặc mật khẩu không đúng.";
                    } else if (e.getMessage() != null) {
                        message = e.getMessage();
                    }

                    Log.e(TAG, "Lỗi đăng nhập: ", e);
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                });
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Nhập email để khôi phục mật khẩu");
            etEmail.requestFocus();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(
                            LoginActivity.this,
                            "Đã gửi email khôi phục mật khẩu",
                            Toast.LENGTH_LONG
                    ).show();
                })
                .addOnFailureListener(e -> {
                    String message = "Không thể gửi email khôi phục mật khẩu";

                    if (e.getMessage() != null) {
                        message = e.getMessage();
                    }

                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            isPasswordVisible = false;
        } else {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            isPasswordVisible = true;
        }

        etPassword.setSelection(etPassword.getText().length());
    }

    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}