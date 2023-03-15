package com.example.socialapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import com.example.socialapp.R;
import com.example.socialapp.dao.UserDao;
import com.example.socialapp.databinding.ActivityChatBinding;
import com.example.socialapp.util.Constants;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private UserDao dao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_chat);
        dao = new UserDao(getApplicationContext());
        binding.backImage.setOnClickListener(v->onBackPressed());
        String username = getIntent().getStringExtra(Constants.KEY_NAME);
        binding.profileName.setText(username);

        String senderId = getIntent().getStringExtra(Constants.KEY_USER_ID);

        updateTypingStatus();
        setTypingStatus();

        setOnlineStatus(senderId);
    }

    private void setOnlineStatus(String senderId) {
        
    }

    private void setTypingStatus() {
    }

    private void updateTypingStatus() {
        binding.chatEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                dao.setTypingTrue();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                dao.setTypingFalse();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        dao.setIsOnlineTrue();
    }

    @Override
    protected void onPause() {
        super.onPause();
        dao.setIsOnlineLastSeen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dao.setIsOnlineTrue();
    }
}