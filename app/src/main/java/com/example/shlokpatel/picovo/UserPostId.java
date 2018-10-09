package com.example.shlokpatel.picovo;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class UserPostId {
    @Exclude
    public String UserPostId;

    public <T extends UserPostId> T withId(@NonNull final String id) {
        this.UserPostId = id;
        return (T) this;
    }
}
