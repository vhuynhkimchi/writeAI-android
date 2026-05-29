package com.example.writeai_android.ui.profile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.writeai_android.R;
import com.example.writeai_android.data.model.User;
import com.example.writeai_android.data.repository.UserRepository;
import com.example.writeai_android.ui.auth.ChangePasswordActivity;
import com.example.writeai_android.ui.auth.LoginActivity;
import com.example.writeai_android.utils.NotificationHelper;
import com.example.writeai_android.utils.RepositoryCallback;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private static final int REQ_NOTIFICATION_PERMISSION = 778;

    private static final String PREF_NAME = "app_settings";
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";

    private TextView tvFullName, tvEmail, btnLogout;
    private Switch switchReminder;
    private ImageView imgAvatar;

    private final UserRepository userRepository = new UserRepository();

    private boolean isBindingSwitch = false;

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
        setupReminderSwitch();
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

    private void setupReminderSwitch() {
        boolean reminderEnabled = getReminderEnabled();

        isBindingSwitch = true;
        switchReminder.setChecked(reminderEnabled);
        isBindingSwitch = false;

        if (reminderEnabled) {
            enableReminder(false);
        }
    }

    private void handleEvents(View view) {
        View layoutChangePassword = view.findViewById(R.id.layoutChangePassword);

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
            if (isBindingSwitch) {
                return;
            }

            saveReminderEnabled(isChecked);

            if (isChecked) {
                enableReminder(true);
            } else {
                disableReminder();
            }
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
                            .placeholder(R.drawable.ic_avatar)
                            .error(R.drawable.ic_avatar)
                            .circleCrop()
                            .into(imgAvatar);
                } else if (imgAvatar != null) {
                    imgAvatar.setImageResource(R.drawable.ic_avatar);
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

    private void enableReminder(boolean showToast) {
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
        saveReminderEnabled(true);

        if (showToast) {
            Toast.makeText(
                    getContext(),
                    "Đã bật nhắc nhở lúc 20:00 mỗi ngày",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void disableReminder() {
        if (getContext() == null) {
            return;
        }

        /*
         * Nếu NotificationHelper của bạn đã có hàm cancelDailyReminder(Context context),
         * hãy mở dòng bên dưới.
         */
        // NotificationHelper.cancelDailyReminder(requireContext());

        Toast.makeText(
                getContext(),
                "Đã tắt nhắc nhở học tập",
                Toast.LENGTH_SHORT
        ).show();
    }

    private boolean getReminderEnabled() {
        if (getContext() == null) {
            return true;
        }

        SharedPreferences preferences = requireContext().getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );

        /*
         * Mặc định true:
         * Người dùng mới vào app thì nhắc nhở luôn bật.
         * Chỉ khi người dùng tự tắt thì mới lưu false.
         */
        return preferences.getBoolean(KEY_REMINDER_ENABLED, true);
    }

    private void saveReminderEnabled(boolean enabled) {
        if (getContext() == null) {
            return;
        }

        SharedPreferences preferences = requireContext().getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );

        preferences.edit()
                .putBoolean(KEY_REMINDER_ENABLED, enabled)
                .apply();
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
                saveReminderEnabled(true);

                isBindingSwitch = true;
                switchReminder.setChecked(true);
                isBindingSwitch = false;

                Toast.makeText(
                        getContext(),
                        "Đã bật nhắc nhở lúc 20:00 mỗi ngày",
                        Toast.LENGTH_SHORT
                ).show();

            } else {
                saveReminderEnabled(false);

                isBindingSwitch = true;
                switchReminder.setChecked(false);
                isBindingSwitch = false;

                Toast.makeText(
                        getContext(),
                        "Bạn cần cấp quyền thông báo để nhận nhắc nhở.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }
}