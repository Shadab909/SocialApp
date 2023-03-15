package com.example.socialapp.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.socialapp.R;
import com.example.socialapp.adapter.PostAdapter;
import com.example.socialapp.dao.PostDao;
import com.example.socialapp.dao.UserDao;
import com.example.socialapp.databinding.ActivityMainBinding;
import com.example.socialapp.model.Post;
import com.example.socialapp.model.User;
import com.example.socialapp.util.Constants;
import com.example.socialapp.interfaces.PostAdapterClickListener;
import com.example.socialapp.util.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PostAdapterClickListener {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private PostAdapter adapter;
    private List<Post> postList;
    private String currentUserId;
    int adapter_position = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        preferenceManager = new PreferenceManager(getApplicationContext());
        postList = new ArrayList<>();
        adapter = new PostAdapter(postList,this , this);
        currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);

        setUserData();
        getToken();
        initRecyclerView();
        binding.profileImage.setOnClickListener(v->startActivity(new Intent(this,ProfileActivity.class)));
        binding.chatFab.setOnClickListener(v->startActivity(new Intent(this, MyUsersActivity.class)));
        binding.logOutImage.setOnClickListener(v->{
            UserDao dao = new UserDao(getApplicationContext());
            dao.logOut()
                    .addOnSuccessListener(unused -> logOut())
                    .addOnFailureListener(e->showToast(e.getLocalizedMessage()));
        });
    }

    private void initRecyclerView(){
        isLoading(true);
        PostDao dao = new PostDao(getApplicationContext());
        dao.getPosts().addOnCompleteListener(task -> {
            isLoading(false);
            if (task.isSuccessful() && task.getResult() != null){
                postList.clear();
                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                    Post post = queryDocumentSnapshot.toObject(Post.class);
                    postList.add(post);
                }
                if (postList.size() > 0){
                    binding.postRv.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    if (adapter_position == -1) binding.postRv.scrollToPosition(0);
                    else binding.postRv.scrollToPosition(adapter_position);
                }
            }
        });
    }

    private void setUserData(){
        binding.profileName.setText(preferenceManager.getString(Constants.KEY_NAME));
        String image = preferenceManager.getString(Constants.KEY_IMAGE);
        if(!image.equals("")){
            Glide.with(binding.profileImage).load(image).placeholder(R.drawable.loading).into(binding.profileImage);
        }
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token->{
            UserDao dao = new UserDao(getApplicationContext());
            dao.updateUserToken(token)
                    .addOnSuccessListener(unused -> {})
                    .addOnFailureListener(e-> showToast(e.getLocalizedMessage()));
        });
    }

    private void isLoading(boolean loading){
        if (loading){
            binding.postRv.setVisibility(View.GONE);
//            binding.progressBar.setVisibility(View.VISIBLE);
            binding.shimmerLayout.setVisibility(View.VISIBLE);
            binding.shimmerLayout.startShimmer();
        }else{
            binding.postRv.setVisibility(View.VISIBLE);
//            binding.progressBar.setVisibility(View.GONE);
            binding.shimmerLayout.stopShimmer();
            binding.shimmerLayout.setVisibility(View.GONE);
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setUserData();
        adapter_position = preferenceManager.getInt(Constants.KEY_ADAPTER_POSITION);
        initRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUserData();
        adapter_position = preferenceManager.getInt(Constants.KEY_ADAPTER_POSITION);
        initRecyclerView();
    }

    @Override
    protected void onStop() {
        super.onStop();
        preferenceManager.putInt(Constants.KEY_ADAPTER_POSITION,-1);
    }

    private void logOut(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Do you want to log out ?");
        builder.setTitle("Alert !");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            preferenceManager.clearPreference();
            startActivity(new Intent(getApplicationContext(),LoginSignUpActivity.class));
            dialogInterface.cancel();
            finish();
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onLikeImageClicked(Post model , int position , AppCompatImageView likeImage , TextView likeCount) {
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

    @Override
    public void onUsernameClicked(User model) {
        if(!model.getUserId().equals(currentUserId)){
            Intent intent = new Intent(this,ChatActivity.class);
            intent.putExtra(Constants.KEY_NAME , model.getName());
            intent.putExtra(Constants.KEY_USER_ID,model.getUserId());
            startActivity(intent);
        }
    }

    @Override
    public void onPostClickListener(Post post,  User user , int position) {
        Intent intent = new Intent(this,PostDetailsActivity.class);
        intent.putExtra("current_post", (Parcelable) post);
        intent.putExtra("creator_user",(Parcelable) user);
        intent.putExtra("adapter_position" , position);
        startActivity(intent);
    }
}