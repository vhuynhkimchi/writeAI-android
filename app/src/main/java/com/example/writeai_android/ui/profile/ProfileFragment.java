package com.example.writeai_android.ui.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.writeai_android.R;
import com.example.writeai_android.data.model.User;
import com.example.writeai_android.data.repository.UserRepository;
import com.example.writeai_android.ui.auth.LoginActivity;
import com.example.writeai_android.ui.auth.ChangePasswordActivity;
import com.example.writeai_android.utils.NotificationHelper;
import com.example.writeai_android.utils.RepositoryCallback;
import com.google.firebase.auth.FirebaseAuth;

import android.widget.ImageView;
import com.bumptech.glide.Glide;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.firebase.auth.FirebaseUser;
import android.net.Uri;

public class ProfileFragment extends Fragment {

    private static final int REQ_NOTIFICATION_PERMISSION = 778;

    private TextView tvFullName, tvEmail, btnLogout;
    private Switch switchReminder;

    private final UserRepository userRepository = new UserRepository();
    private ImageView imgAvatar;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        handleEvents(view);
        loadUserInfo();
    }

    private void initViews(View view) {
        tvFullName = view.findViewById(R.id.tvFullName);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnLogout = view.findViewById(R.id.btnLogout);
        switchReminder = view.findViewById(R.id.switchReminder);
        imgAvatar = view.findViewById(R.id.imgAvatar);
    }

    private void handleEvents(View view) {
        View btnBack = view.findViewById(R.id.btnBack);
        View layoutChangePassword = view.findViewById(R.id.layoutChangePassword);
        View layoutFaqScore = view.findViewById(R.id.layoutFaqScore);
        View layoutFaqSave = view.findViewById(R.id.layoutFaqSave);
        TextView tvFaqScoreDetail = view.findViewById(R.id.tvFaqScoreDetail);

        btnBack.setOnClickListener(v -> requireActivity().finish());

        btnLogout.setOnClickListener(v -> logout());

        layoutChangePassword.setOnClickListener(v -> {
            if (isGoogleUser()) {
                Toast.makeText(
                        getContext(),
                        "Không thể đổi mật khẩu khi đăng nhập bằng Google",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Intent intent = new Intent(getContext(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                enableReminder();
            } else {
                Toast.makeText(
                        getContext(),
                        "Đã tắt nhắc nhở học tập",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        layoutFaqScore.setOnClickListener(v -> {
            if (tvFaqScoreDetail.getVisibility() == View.VISIBLE) {
                tvFaqScoreDetail.setVisibility(View.GONE);
            } else {
                tvFaqScoreDetail.setVisibility(View.VISIBLE);
            }
        });

        layoutFaqSave.setOnClickListener(v -> {
            Toast.makeText(
                    getContext(),
                    "Sau khi AI chấm xong, bấm nút Lưu lại để lưu bài viết.",
                    Toast.LENGTH_LONG
            ).show();
        });
    }

    private boolean isGoogleUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            return false;
        }

        for (com.google.firebase.auth.UserInfo profile : user.getProviderData()) {
            if ("google.com".equals(profile.getProviderId())) {
                return true;
            }
        }

        return false;
    }

    private void loadUserInfo() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            return;
        }

        userRepository.getCurrentUserInfo(new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (getContext() == null) {
                    return;
                }

                String fullName = "";
                String email = "";

                if (user != null) {
                    fullName = user.getFullName();
                    email = user.getEmail();
                }

                if (fullName == null || fullName.trim().isEmpty()) {
                    fullName = firebaseUser.getDisplayName();
                }

                if (email == null || email.trim().isEmpty()) {
                    email = firebaseUser.getEmail();
                }

                tvFullName.setText(
                        fullName == null || fullName.trim().isEmpty()
                                ? "Chưa cập nhật"
                                : fullName
                );

                tvEmail.setText(
                        email == null || email.trim().isEmpty()
                                ? "Chưa có email"
                                : email
                );

                Uri photoUri = firebaseUser.getPhotoUrl();

                if (photoUri != null && imgAvatar != null) {
                    Glide.with(ProfileFragment.this)
                            .load(photoUri)
                            .placeholder(R.drawable.logo)
                            .error(R.drawable.logo)
                            .circleCrop()
                            .into(imgAvatar);
                } else if (imgAvatar != null) {
                    imgAvatar.setImageResource(R.drawable.logo);
                }
            }

            @Override
            public void onError(String message) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void enableReminder() {
        if (getContext() == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQ_NOTIFICATION_PERMISSION
            );
            return;
        }

        NotificationHelper.scheduleDailyReminder(requireContext());

        Toast.makeText(
                getContext(),
                "Đã bật nhắc nhở lúc 20:00 mỗi ngày",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        googleSignInClient.signOut().addOnCompleteListener(task -> {
            if (getContext() == null) {
                return;
            }

            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            requireActivity().finish();
        });
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                NotificationHelper.scheduleDailyReminder(requireContext());

                Toast.makeText(
                        getContext(),
                        "Đã bật nhắc nhở lúc 20:00 mỗi ngày",
                        Toast.LENGTH_SHORT
                ).show();

                switchReminder.setChecked(true);
            } else {
                Toast.makeText(
                        getContext(),
                        "Bạn cần cấp quyền thông báo để nhận nhắc nhở.",
                        Toast.LENGTH_LONG
                ).show();

                switchReminder.setChecked(false);
            }
        }
    }

    private String safeText(String value) {
        return value == null || value.trim().isEmpty() ? "Chưa cập nhật" : value;
    }
}