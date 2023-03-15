package com.example.socialapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.socialapp.R;
import com.example.socialapp.dao.PostDao;
import com.example.socialapp.databinding.ActivityPostDetailsBinding;
import com.example.socialapp.model.Post;
import com.example.socialapp.model.User;
import com.example.socialapp.util.Constants;
import com.example.socialapp.util.DateTimeParser;
import com.example.socialapp.util.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class PostDetailsActivity extends AppCompatActivity {

    private ActivityPostDetailsBinding binding;
    private int adapter_position;
    private String currentUserId;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_post_details);
        preferenceManager = new PreferenceManager(getApplicationContext());
        Post current_post = getIntent().getParcelableExtra("current_post");
        User creator_user = getIntent().getParcelableExtra("creator_user");
        adapter_position = getIntent().getIntExtra("adapter_position" , -1);
        currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);

        setPostDetails(current_post,creator_user);
        binding.likeImage.setOnClickListener(v-> {
            likeImageClicker(current_post,binding.likeImage,binding.likeCount);
        });
        binding.postImage.setOnClickListener(v->{
            Intent intent = new Intent(this , ImageActivity.class);
            intent.putExtra(Constants.KEY_IMAGE,current_post.getImageUrl());
            startActivity(intent);
        });
        binding.backImage.setOnClickListener(v->{
            preferenceManager.putInt(Constants.KEY_ADAPTER_POSITION,adapter_position);
            onBackPressed();
        });
    }

    private void likeImageClicker(Post model , AppCompatImageView likeImage , TextView likeCount){
        int like = model.getLikedBy().size();
        if(likeImage.getTag().equals("liked")){
            likeImage.setImageResource(R.drawable.ic_unliked);
            model.getLikedBy().remove(currentUserId);
            like--;
            likeCount.setText(like + "");
            likeImage.setTag("unliked");
        }else {
            likeImage.setImageResource(R.drawable.ic_liked);
            model.getLikedBy().add(currentUserId);
            like++;
            likeCount.setText(like + "");
            likeImage.setTag("liked");
        }
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_POSTS)
                .document(model.getPostId()).set(model);
    }

    private void setPostDetails(Post current_post , User creator_user){
        binding.likeCount.setText(String.valueOf(current_post.getLikedBy().size()));
        if(!creator_user.getImage().equals("")){
            Glide.with(this).load(creator_user.getImage()).placeholder(R.drawable.loading).into(binding.userImage);
        }
        binding.userName.setText(creator_user.getName());

        binding.postingTime.setText(DateTimeParser.Companion.getTimeAgo(current_post.getCreatedAt()));
        binding.postText.setText(current_post.getText());
        binding.likeCount.setText(String.valueOf(current_post.getLikedBy().size()));
        PostDao dao = new PostDao(getApplicationContext());
        dao.loadLikeData(current_post.getPostId() , binding.likeImage);

        if(!current_post.getImageUrl().equals("")){
            binding.postImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(current_post.getImageUrl()).placeholder(R.drawable.loading).into(binding.postImage);
        }

        if (currentUserId.equals(creator_user.getUserId())){
            binding.deletePostBtn.setVisibility(View.VISIBLE);
            binding.deletePostBtn.setOnClickListener(v->deletePost(current_post.getPostId()));
        }
    }

    private void deletePost(String postId){
        ProgressDialog pd = new ProgressDialog(this);
        pd.show();
        PostDao dao = new PostDao(this);
        dao.deletePost(postId).addOnSuccessListener(unused ->{
                    pd.dismiss();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PostDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                });
    }

    @Override
    public void onBackPressed() {
        preferenceManager.putInt(Constants.KEY_ADAPTER_POSITION,adapter_position);
        super.onBackPressed();
    }
}