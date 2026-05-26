package com.example.writeai_android.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.example.writeai_android.R;
import com.example.writeai_android.ui.history.HistoryActivity;
import com.example.writeai_android.ui.main.MainActivity;
import com.example.writeai_android.ui.profile.ProfileActivity;

public class BottomNavigationHelper {

    public static final int TAB_HOME = 1;
    public static final int TAB_HISTORY = 2;
    public static final int TAB_PROFILE = 3;

    public static void setup(Activity activity, int selectedTab) {
        View navHome = activity.findViewById(R.id.navHome);
        View navHistory = activity.findViewById(R.id.btnHistory);
        View navProfile = activity.findViewById(R.id.btnAccount);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                if (selectedTab != TAB_HOME) {
                    Intent intent = new Intent(activity, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                }
            });
        }

        if (navHistory != null) {
            navHistory.setOnClickListener(v -> {
                if (selectedTab != TAB_HISTORY) {
                    Intent intent = new Intent(activity, HistoryActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                }
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                if (selectedTab != TAB_PROFILE) {
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                }
            });
        }
    }
}