package com.example.socialapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialapp.R;
import com.example.socialapp.dao.PostDao;
import com.example.socialapp.dao.UserDao;
import com.example.socialapp.databinding.PostItemLayoutBinding;
import com.example.socialapp.model.Post;
import com.example.socialapp.model.User;
import com.example.socialapp.util.DateTimeParser;
import com.example.socialapp.interfaces.PostAdapterClickListener;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList = null;
    private static Context context;
    private final PostAdapterClickListener listener;

    public PostAdapter(List<Post> postList , Context context , PostAdapterClickListener listener) {
        PostAdapter.context = context;
        this.postList = postList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PostItemLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.post_item_layout, parent, false);
        return new PostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post model = postList.get(position);
        holder.bind(model,listener,holder.itemView);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        private PostItemLayoutBinding binding;
        public PostViewHolder(PostItemLayoutBinding postItemLayoutBinding) {
            super(postItemLayoutBinding.getRoot());
            this.binding = postItemLayoutBinding;
        }

        public void bind(Post model , PostAdapterClickListener listener , View itemView) {
//            isLoading(true , binding);
            UserDao userDao = new UserDao(context);
            userDao.getUserById(model.getCreatorId()).addOnCompleteListener(task -> {
                if(task.isSuccessful() && task.getResult() != null){
                    User user = task.getResult().toObject(User.class);
                    if(!user.getImage().equals("")){
                        Glide.with(binding.userImage).load(user.getImage()).placeholder(R.drawable.loading).into(binding.userImage);
                    }
                    binding.userName.setText(user.getName());
                    binding.userName.setOnClickListener(v->listener.onUsernameClicked(user));
                    itemView.setOnClickListener(v->listener.onPostClickListener(model , user , getAdapterPosition()));
//                    isLoading(false,binding);
                    binding.postingTime.setText(DateTimeParser.Companion.getTimeAgo(model.getCreatedAt()));
                    binding.postText.setText(model.getText());
                    binding.likeCount.setText(String.valueOf(model.getLikedBy().size()));
                    binding.likeImage.setOnClickListener(v-> listener
                            .onLikeImageClicked(model, getAdapterPosition(), binding.likeImage, binding.likeCount));
                    PostDao dao = new PostDao(context);
                    dao.loadLikeData(model.getPostId() , binding.likeImage);

                    if(!model.getImageUrl().equals("")){
                        binding.postImage.setVisibility(View.VISIBLE);
                        Glide.with(binding.postImage).load(model.getImageUrl()).placeholder(R.drawable.loading).into(binding.postImage);
                    }else {
                        binding.postImage.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

//    private static void isLoading(boolean loading , PostItemLayoutBinding binding){
//        if (loading){
//            binding.post.setVisibility(View.GONE);
//            binding.shimmer.setVisibility(View.VISIBLE);
//            binding.shimmer.startShimmer();
//        }else{
//            binding.post.setVisibility(View.VISIBLE);
//            binding.shimmer.stopShimmer();
//            binding.shimmer.setVisibility(View.GONE);
//        }
//    }
}






