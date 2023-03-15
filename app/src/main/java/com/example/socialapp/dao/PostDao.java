package com.example.socialapp.dao;

import android.content.Context;
import android.util.Log;

import androidx.appcompat.widget.AppCompatImageView;

import com.example.socialapp.R;
import com.example.socialapp.adapter.PostAdapter;
import com.example.socialapp.model.Post;
import com.example.socialapp.model.User;
import com.example.socialapp.util.Constants;
import com.example.socialapp.util.PreferenceManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PostDao {

    private final FirebaseFirestore db;
    private Context context;
    private final PreferenceManager preferenceManager;

    public PostDao(Context context) {
        db = FirebaseFirestore.getInstance();
        this.context = context;
        preferenceManager = new PreferenceManager(context);
    }


    public Task<Void> addPost(String text , String imageUrl){
        String creatorId = preferenceManager.getString(Constants.KEY_USER_ID);
        long createdAt = System.currentTimeMillis();
        String postId = UUID.randomUUID().toString();
        Post post = new Post(postId ,text , imageUrl,createdAt,creatorId,new ArrayList<>());
        return db.collection(Constants.KEY_COLLECTION_POSTS).document(postId).set(post);
    }

    public Task<QuerySnapshot> getPosts(){
        return db.collection(Constants.KEY_COLLECTION_POSTS).orderBy("createdAt", Query.Direction.DESCENDING).get();
    }

    public Task<DocumentSnapshot> getPostById(String Id){
        return db.collection(Constants.KEY_COLLECTION_POSTS).document(Id).get();
    }

    public void loadLikeData(String postId, AppCompatImageView likeImage) {
        getPostById(postId).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                Post post = task.getResult().toObject(Post.class);
                String userId = preferenceManager.getString(Constants.KEY_USER_ID);
                boolean isLiked = post.getLikedBy().contains(userId);
                if (isLiked){
                    likeImage.setImageResource(R.drawable.ic_liked);
                    likeImage.setTag("liked");
                }else{
                    likeImage.setImageResource(R.drawable.ic_unliked);
                    likeImage.setTag("unliked");
                }
            }
        });
    }

    public Task<Void> deletePost(String postId){
        return db.collection(Constants.KEY_COLLECTION_POSTS).document(postId).delete();
    }



}
