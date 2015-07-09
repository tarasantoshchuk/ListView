package com.example.tarasantoshchuk.listview;

import android.graphics.Bitmap;

public class Contact {
    private String mPhotoUrl;
    private String mName;
    private String mSurname;

    public Contact(String mPhotoUrl, String mName, String mSurname) {
        this.mPhotoUrl = mPhotoUrl;
        this.mName = mName;
        this.mSurname = mSurname;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public String getName() {
        return mName;
    }

    public String getSurname() {
        return mSurname;
    }
}
