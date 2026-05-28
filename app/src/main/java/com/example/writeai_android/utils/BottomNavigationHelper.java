package com.example.writeai_android.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.ImageView;

import com.example.writeai_android.R;
import com.example.writeai_android.ui.history.HistoryActivity;
import com.example.writeai_android.ui.main.MainActivity;
import com.example.writeai_android.ui.profile.ProfileActivity;

public class BottomNavigationHelper {

    public static final int TAB_HOME = 1;
    public static final int TAB_HISTORY = 2;
    public static final int TAB_PROFILE = 3;

    private static final int COLOR_SELECTED = Color.parseColor("#06285D");
    private static final int COLOR_UNSELECTED = Color.parseColor("#4B5563");

    public static void setup(Activity activity, int selectedTab) {
        View navHome = activity.findViewById(R.id.navHome);
        View navHistory = activity.findViewById(R.id.navHistory);
        View navProfile = activity.findViewById(R.id.navProfile);

        ImageView iconHome = activity.findViewById(R.id.iconHome);
        ImageView iconHistory = activity.findViewById(R.id.iconHistory);
        ImageView iconProfile = activity.findViewById(R.id.iconProfile);

        applySelectedState(selectedTab, iconHome, iconHistory, iconProfile);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                if (selectedTab != TAB_HOME) {
                    Intent intent = new Intent(activity, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
                }
            });
        }

        if (navHistory != null) {
            navHistory.setOnClickListener(v -> {
                if (selectedTab != TAB_HISTORY) {
                    Intent intent = new Intent(activity, HistoryActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
                }
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                if (selectedTab != TAB_PROFILE) {
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
                }
            });
        }
    }

    private static void applySelectedState(
            int selectedTab,
            ImageView iconHome,
            ImageView iconHistory,
            ImageView iconProfile
    ) {
        resetIcon(iconHome);
        resetIcon(iconHistory);
        resetIcon(iconProfile);

        if (selectedTab == TAB_HOME) {
            selectIcon(iconHome);
        } else if (selectedTab == TAB_HISTORY) {
            selectIcon(iconHistory);
        } else if (selectedTab == TAB_PROFILE) {
            selectIcon(iconProfile);
        }
    }

    private static void resetIcon(ImageView icon) {
        if (icon == null) {
            return;
        }

        icon.setBackground(null);
        icon.setColorFilter(COLOR_UNSELECTED, PorterDuff.Mode.SRC_IN);
    }

    private static void selectIcon(ImageView icon) {
        if (icon == null) {
            return;
        }

        icon.setBackgroundResource(R.drawable.bg_nav_selected);
        icon.setColorFilter(COLOR_SELECTED, PorterDuff.Mode.SRC_IN);
    }
}