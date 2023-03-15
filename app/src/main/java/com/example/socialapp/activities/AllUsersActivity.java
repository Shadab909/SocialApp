package com.example.socialapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.socialapp.R;
import com.example.socialapp.adapter.AllUsersAdapter;
import com.example.socialapp.dao.UserDao;

import com.example.socialapp.databinding.ActivityAllUsersBinding;
import com.example.socialapp.interfaces.FollowButtonClickListener;
import com.example.socialapp.model.User;
import com.example.socialapp.util.Constants;
import com.example.socialapp.util.PreferenceManager;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AllUsersActivity extends AppCompatActivity implements FollowButtonClickListener {

    private ActivityAllUsersBinding binding;
    private List<User> allUsersList;
    private AllUsersAdapter adapter;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_all_users);
        allUsersList = new ArrayList<>();
        adapter = new AllUsersAdapter(this,allUsersList,this);
        preferenceManager = new PreferenceManager(this);
        binding.backImage.setOnClickListener(v->onBackPressed());
        initRecyclerView();
    }

    private void initRecyclerView(){
        isLoading(true);
        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        UserDao dao = new UserDao(getApplicationContext());
        dao.getAllUsers().addOnCompleteListener(task -> {
            isLoading(false);
            if (task.isSuccessful() && task.getResult() != null){
                allUsersList.clear();
                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                    User user = queryDocumentSnapshot.toObject(User.class);
                    if(!user.getUserId().equals(currentUserId)) allUsersList.add(user);
                }
                if (allUsersList.size() > 0){
                    binding.allUsersRv.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    binding.allUsersRv.scrollToPosition(0);
                }
            }
        });
    }

    private void isLoading(boolean loading){
        if (loading){
            binding.allUsersRv.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.allUsersRv.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onFollowBtnClicked(String userId, int position, Button followBtn) {
        UserDao dao = new UserDao(getApplicationContext());
        if (followBtn.getText().equals("Follow")){
            followBtn.setText("Following");
            followBtn.setBackground(getResources().getDrawable(R.drawable.background_following_btn));
            followBtn.setTextColor(getResources().getColor(R.color.primary));
            dao.addToMyUsers(userId);
        }else{
            followBtn.setText("Follow");
            followBtn.setBackground(getResources().getDrawable(R.drawable.background_follow_btn));
            followBtn.setTextColor(getResources().getColor(R.color.white));
            dao.removeFromMyUser(userId);
        }
    }
}