package com.example.socialapp.interfaces;

import android.widget.Button;

import com.example.socialapp.model.User;

public interface FollowButtonClickListener {
    void onFollowBtnClicked(String userId , int position , Button followBtn);
}
