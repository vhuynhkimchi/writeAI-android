package com.example.writeai_android.utils;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeFormatter {

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String DISPLAY_PATTERN = "dd/MM/yyyy HH:mm";

    private DateTimeFormatter() {
    }

    public static String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date());
    }

    public static String formatReadableDate(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DISPLAY_PATTERN, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(timestamp.toDate());
    }

    public static String formatDateOnly(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(timestamp.toDate());
    }

    public static long daysBetween(String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            if (start == null || end == null) {
                return 0;
            }
            long diff = end.getTime() - start.getTime();
            return diff / (24L * 60L * 60L * 1000L);
        } catch (ParseException e) {
            return 0;
        }
    }

    public static String shortenContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        String trimmed = content.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, Math.max(0, maxLength - 3)).trim() + "...";
    }
}
