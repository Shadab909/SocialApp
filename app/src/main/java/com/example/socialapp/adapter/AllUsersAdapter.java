package com.example.socialapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialapp.R;
import com.example.socialapp.dao.UserDao;
import com.example.socialapp.databinding.AllUsersItemLayoutBinding;
import com.example.socialapp.interfaces.FollowButtonClickListener;
import com.example.socialapp.model.User;

import java.util.List;

public class AllUsersAdapter extends RecyclerView.Adapter<AllUsersAdapter.AllUsersViewHolder> {

    private static Context context;
    private List<User> allUsersList;
    private FollowButtonClickListener listener;

    public AllUsersAdapter(Context context, List<User> allUsersList, FollowButtonClickListener listener) {
        AllUsersAdapter.context = context;
        this.allUsersList = allUsersList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AllUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AllUsersItemLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.all_users_item_layout, parent, false);
        return new AllUsersAdapter.AllUsersViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AllUsersViewHolder holder, int position) {
        User model = allUsersList.get(position);
        holder.bind(model,listener);
    }

    @Override
    public int getItemCount() {
        return allUsersList.size();
    }

    public static class AllUsersViewHolder extends RecyclerView.ViewHolder {
        private AllUsersItemLayoutBinding binding;
        public AllUsersViewHolder(AllUsersItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(User model , FollowButtonClickListener listener) {
            binding.userName.setText(model.getName());
            if(!model.getImage().equals("")){
                Glide.with(binding.userImage).load(model.getImage()).placeholder(R.drawable.loading).into(binding.userImage);
            }
            binding.userEmail.setText(model.getEmail());
            UserDao dao = new UserDao(context);
            dao.loadFollowBtnText(model,binding.btnFollow,binding.progressBar);
            binding.btnFollow.setOnClickListener(v->{
                listener.onFollowBtnClicked(model.getUserId(),getAdapterPosition(),binding.btnFollow);
            });
        }
    }
}
