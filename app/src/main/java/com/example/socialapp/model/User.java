package com.example.socialapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class User implements Serializable , Parcelable {
    private String name;
    private String email;
    private String image;
    private String banner;
    private String password;
    private String userId;
    private boolean typing;
    private String online;

    public User(String userId , String name , String email , String password , String image , String banner , boolean typing , String online) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.image = image;
        this.banner = banner;
        this.password = password;
        this.online = online;
        this.typing = typing;
    }

    public User(String userId , String name , String email , String image) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.image = image;
    }

    public User() {
    }

    protected User(Parcel in) {
        name = in.readString();
        email = in.readString();
        image = in.readString();
        banner = in.readString();
        password = in.readString();
        userId = in.readString();
        typing = in.readByte() != 0;
        online = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(email);
        parcel.writeString(image);
        parcel.writeString(banner);
        parcel.writeString(password);
        parcel.writeString(userId);
        parcel.writeByte((byte) (typing ? 1 : 0));
        parcel.writeString(online);
    }
}
