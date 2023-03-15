package com.example.socialapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.socialapp.R;
import com.example.socialapp.databinding.ActivityImageBinding;
import com.example.socialapp.util.Constants;

public class ImageActivity extends AppCompatActivity {
    private ActivityImageBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_image);
        String post_image = getIntent().getStringExtra(Constants.KEY_IMAGE);
        if(!post_image.equals("")){
            Glide.with(this).load(post_image).placeholder(R.drawable.loading).into(binding.postImage);
        }
    }
}