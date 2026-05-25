package com.example.writeai_android.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseHelper {
    private static FirebaseAuth auth;
    private static FirebaseFirestore firestore;

    private FirebaseHelper() {
    }

    public static FirebaseAuth getAuth() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    public static FirebaseFirestore getFirestore() {
        if (firestore == null) {
            firestore = FirebaseFirestore.getInstance();
        }
        return firestore;
    }

    public static CollectionReference usersRef() {
        return getFirestore().collection("users");
    }

    public static CollectionReference essaysRef() {
        return getFirestore().collection("essays");
    }

    public static CollectionReference attendanceRef() {
        return getFirestore().collection("attendance");
    }
}
