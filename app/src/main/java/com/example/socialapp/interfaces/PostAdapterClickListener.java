package com.example.socialapp.interfaces;

import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.example.socialapp.model.Post;
import com.example.socialapp.model.User;

public interface PostAdapterClickListener {
    void onLikeImageClicked(Post post , int position , AppCompatImageView likeImage , TextView likeCount);
    void onUsernameClicked(User user);
    void onPostClickListener(Post post , User user , int position);
}
