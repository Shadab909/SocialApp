package com.example.socialapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.socialapp.R;
import com.example.socialapp.adapter.AllUsersAdapter;
import com.example.socialapp.adapter.MyUsersAdapter;
import com.example.socialapp.dao.UserDao;
import com.example.socialapp.databinding.ActivityMyUsersBinding;
import com.example.socialapp.interfaces.MyUsersItemClickListener;
import com.example.socialapp.model.MyUsers;
import com.example.socialapp.model.User;
import com.example.socialapp.util.Constants;
import com.example.socialapp.util.PreferenceManager;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyUsersActivity extends AppCompatActivity implements MyUsersItemClickListener {
    private ActivityMyUsersBinding binding;
    private List<String> myUsersIdList;
    private MyUsersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_my_users);
        myUsersIdList = new ArrayList<>();
        adapter = new MyUsersAdapter(this,myUsersIdList,this);

        initRecyclerView();

        binding.usersTitle.setOnClickListener(v->startActivity(new Intent(this,ChatActivity.class)));
        binding.backImage.setOnClickListener(v->onBackPressed());
        binding.allUsersFab.setOnClickListener(v->startActivity(new Intent(this,AllUsersActivity.class)));
    }

    private void initRecyclerView(){
        isLoading(true);
        UserDao dao = new UserDao(getApplicationContext());
        dao.getMyUsers().addOnCompleteListener(task -> {
            isLoading(false);
            if (task.isSuccessful() && task.getResult() != null) {
                MyUsers doc = task.getResult().toObject(MyUsers.class);
                List<String> userIds = doc.getUserIdList();
                myUsersIdList.clear();
                myUsersIdList.addAll(userIds);
                if (myUsersIdList.size() > 0){
                    binding.myUsersRv.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    binding.myUsersRv.scrollToPosition(0);
                }
            }
        });
    }

    private void isLoading(boolean loading){
        if (loading){
            binding.myUsersRv.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.myUsersRv.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(User model , int position) {
        Intent intent = new Intent(this,ChatActivity.class);
        intent.putExtra(Constants.KEY_NAME , model.getName());
        intent.putExtra(Constants.KEY_USER_ID,model.getUserId());
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initRecyclerView();
    }
}