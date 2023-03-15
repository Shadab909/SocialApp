package com.example.socialapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class Post implements Serializable , Parcelable {
    private String postId;
    private String text;
    private String imageUrl;
    private long createdAt;
    private String creatorId;
    private ArrayList<String> likedBy;

    public Post(String postId ,String text, String imageUrl, long createdAt, String creatorId, ArrayList<String> likedBy ) {
        this.postId = postId;
        this.text = text;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.creatorId = creatorId;
        this.likedBy = likedBy;
    }

    public Post() {
    }

    protected Post(Parcel in) {
        postId = in.readString();
        text = in.readString();
        imageUrl = in.readString();
        createdAt = in.readLong();
        creatorId = in.readString();
        likedBy = in.createStringArrayList();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public ArrayList<String> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(ArrayList<String> likedBy) {
        this.likedBy = likedBy;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(postId);
        parcel.writeString(text);
        parcel.writeString(imageUrl);
        parcel.writeLong(createdAt);
        parcel.writeString(creatorId);
        parcel.writeStringList(likedBy);
    }
}
