package com.example.socialapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialapp.R;
import com.example.socialapp.dao.UserDao;
import com.example.socialapp.databinding.AllUsersItemLayoutBinding;
import com.example.socialapp.databinding.MyUsersItemLayoutBinding;
import com.example.socialapp.interfaces.FollowButtonClickListener;
import com.example.socialapp.interfaces.MyUsersItemClickListener;
import com.example.socialapp.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class MyUsersAdapter extends RecyclerView.Adapter<MyUsersAdapter.MyUsersViewHolder> {
    private static Context context;
    private List<String> myUsersIdList;
    private MyUsersItemClickListener listener;

    public MyUsersAdapter(Context context, List<String> myUsersIdList, MyUsersItemClickListener listener) {
        MyUsersAdapter.context = context;
        this.myUsersIdList = myUsersIdList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyUsersAdapter.MyUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MyUsersItemLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.my_users_item_layout, parent, false);
        return new MyUsersAdapter.MyUsersViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyUsersAdapter.MyUsersViewHolder holder, int position) {
        String userId = myUsersIdList.get(position);
        holder.bind(userId ,listener , holder.itemView);

    }

    @Override
    public int getItemCount() {
        return myUsersIdList.size();
    }

    public static class MyUsersViewHolder extends RecyclerView.ViewHolder {
        private MyUsersItemLayoutBinding binding;
        public MyUsersViewHolder(MyUsersItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String userId , MyUsersItemClickListener listener , View itemView) {
            UserDao dao = new UserDao(context);
            dao.getUserById(userId).addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null){
                    User model = task.getResult().toObject(User.class);
                    binding.userName.setText(model.getName());

                    if (model.getOnline().equals("true")){
                        binding.onlineStatus.setVisibility(View.VISIBLE);
                    }else {
                        binding.onlineStatus.setVisibility(View.GONE);
                    }
                    if(!model.getImage().equals("")){
                        Glide.with(binding.userImage).load(model.getImage()).placeholder(R.drawable.loading).into(binding.userImage);
                    }
                    itemView.setOnClickListener(v-> listener.onItemClick(model,getAdapterPosition()));
                }
            });

        }
    }
}
