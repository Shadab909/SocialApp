package com.example.socialapp.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.socialapp.R;
import com.example.socialapp.adapter.PostAdapter;
import com.example.socialapp.dao.PostDao;
import com.example.socialapp.dao.UserDao;
import com.example.socialapp.databinding.ActivityProfileBinding;
import com.example.socialapp.model.Post;
import com.example.socialapp.model.User;
import com.example.socialapp.util.Constants;
import com.example.socialapp.interfaces.PostAdapterClickListener;
import com.example.socialapp.util.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements PostAdapterClickListener {

    private ActivityProfileBinding binding;
    private List<Post> postList ;
    private PostAdapter adapter;
    String[] cameraPermissions;
    String[] storagePermissions;
    private Uri cam_uri;
    private String imageType;
    private ProgressDialog pd;
    private StorageReference storageReference;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_profile);
        cameraPermissions = new String[]{Manifest.permission.CAMERA , Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        pd = new ProgressDialog(this);
        storageReference = FirebaseStorage.getInstance().getReference();
        preferenceManager = new PreferenceManager(this);
        postList = new ArrayList<>();
        adapter = new PostAdapter(postList,this , this);


        initRecyclerView();
        setUserData();
        binding.changeBannerImage.setOnClickListener(v->{
                    imageType = "banner";
                    showImageBannerPickDialog();
        } );
        binding.changeProfileImage.setOnClickListener(v-> {
            imageType = "profile";
            showImageBannerPickDialog();
        });
        binding.editUserName.setOnClickListener(v->showEditNameDialog());
        binding.backImage.setOnClickListener(v->onBackPressed());
        binding.createPostBtn.setOnClickListener(v->startActivity(new Intent(this,CreatePostActivity.class)));

    }

    private void setUserData(){
        String image = preferenceManager.getString(Constants.KEY_IMAGE);
        String name = preferenceManager.getString(Constants.KEY_NAME);
        String banner =preferenceManager.getString(Constants.KEY_BANNER);
        binding.userName.setText(name);
        if(!image.equals("")){
            Glide.with(binding.userImage).load(image).placeholder(R.drawable.loading).into(binding.userImage);
        }
        if(!banner.equals("")){
            Glide.with(binding.bannerImage).load(banner).placeholder(R.drawable.loading).into(binding.bannerImage);
        }
    }

    private void initRecyclerView(){
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        isLoading(true);
        PostDao dao = new PostDao(getApplicationContext());
        dao.getPosts().addOnCompleteListener(task -> {
            isLoading(false);
            if (task.isSuccessful() && task.getResult() != null){
                postList.clear();
                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                    Post post = queryDocumentSnapshot.toObject(Post.class);
                    if (userId.equals(post.getCreatorId())){
                        postList.add(post);
                    }
                }
                if (postList.size() > 0){
                    adapter = new PostAdapter(postList,this , this);
                    binding.postRv.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    binding.postRv.scrollToPosition(0);
                }
            }
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

    private boolean checkStoragePermission(){
        return ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermissions,Constants.STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean isCameraPermitted = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        boolean isStoragePermitted = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        return isCameraPermitted && isStoragePermitted;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions,Constants.CAMERA_REQUEST_CODE);
    }

    private void showImageBannerPickDialog(){
        String[] options = new String[]{"Camera","Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Pick from ?");
        builder.setItems(options, (dialogInterface, i) -> {
            if(i==0){
                if (!checkCameraPermission()){
                    requestCameraPermission();
                }else{
                    pickImageBannerFromCamera();
                }
            }else{
                if (!checkStoragePermission()){
                    requestStoragePermission();
                }else{
                    pickImageBannerFromGallery();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_user_name_dialog_layout, null);

        final EditText user_name_et = dialogView.findViewById(R.id.user_name);

        builder.setView(dialogView).setPositiveButton("Ok", (dialogInterface, i) -> {
            if (!user_name_et.getText().toString().trim().equals("")){
                editUserName(pd,user_name_et.getText().toString().trim(),dialogInterface,binding.userName,preferenceManager);
                initRecyclerView();
            }else{
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Cancel",((dialogInterface, i) -> dialogInterface.dismiss()));

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void editUserName(ProgressDialog pd , String userName , DialogInterface dialogInterface , TextView userNameTv , PreferenceManager preferenceManager){
        pd.show();
        UserDao dao = new UserDao(ProfileActivity.this);
        dao.updateName(userName)
                .addOnSuccessListener(unused -> {
                    pd.dismiss();
                    binding.userName.setText(userName);
                    preferenceManager.putString(Constants.KEY_NAME,userName);
                    dialogInterface.dismiss();

                }).addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(ProfileActivity.this, "Error Occurred name not edited", Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                });
    }

    private void pickImageBannerFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        cam_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cam_uri);

        //startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE); // OLD WAY
        startCamera.launch(cameraIntent); // VERY NEW WAY
    }

    private void pickImageBannerFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        openGallery.launch(galleryIntent);
    }

    ActivityResultLauncher<Intent> startCamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        // There are no request codes
                        if (imageType.equals("banner")){
                            binding.bannerImage.setImageURI(cam_uri);
                        }else {
                            binding.userImage.setImageURI(cam_uri);
                        }
                        uploadProfileBannerImage(cam_uri);
                    }
                }
            }
    );

    ActivityResultLauncher<Intent> openGallery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri galleryUri = result.getData().getData();
                if (imageType.equals("banner")){
                    binding.bannerImage.setImageURI(galleryUri);
                }else {
                    binding.userImage.setImageURI(galleryUri);
                }
                uploadProfileBannerImage(galleryUri);
            }
        }
    }
    );

    private void uploadProfileBannerImage(Uri imageUri) {
        pd.setMessage("Uploading " + imageType + " Image ....");
        pd.show();
        String storagePath = "Users_Profile_Banner_Images/" + imageType + "_" + preferenceManager.getString(Constants.KEY_USER_ID);
        storageReference.child(storagePath).putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    Uri downloadUri = uriTask.getResult();

                    if(uriTask.isSuccessful()){
                        //image uploaded
                        UserDao dao = new UserDao(this);
                        dao.UpdateImage(downloadUri.toString() , imageType)
                                .addOnSuccessListener(unused -> {
                                    if(imageType.equals("banner")){
                                        preferenceManager.putString(Constants.KEY_BANNER,downloadUri.toString());
                                    }else{
                                        preferenceManager.putString(Constants.KEY_IMAGE,downloadUri.toString());
                                    }
                                    setUserData();
                                    initRecyclerView();
                                    pd.dismiss();
                                    Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show();
                                }).addOnFailureListener(e -> {
                                    pd.dismiss();
                                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
                                });
                    }else{
                        pd.dismiss();
                        Toast.makeText(this, "some error occurred", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }



    @Override
    public void onLikeImageClicked(Post model, int position , AppCompatImageView likeImage , TextView likeCount) {
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        int like = model.getLikedBy().size();
        if(likeImage.getTag().equals("liked")){
            likeImage.setImageResource(R.drawable.ic_unliked);
            model.getLikedBy().remove(userId);
            like--;
            likeCount.setText(like + "");
            likeImage.setTag("unliked");
        }else {
            likeImage.setImageResource(R.drawable.ic_liked);
            model.getLikedBy().add(userId);
            like++;
            likeCount.setText(like + "");
            likeImage.setTag("liked");
        }
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_POSTS)
                .document(model.getPostId()).set(model);
    }

    @Override
    public void onUsernameClicked(User user) {
        Toast.makeText(this, user.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPostClickListener(Post post , User user , int position) {
        Intent intent = new Intent(this,PostDetailsActivity.class);
        intent.putExtra("current_post", (Parcelable) post);
        intent.putExtra("creator_user",(Parcelable) user);
        intent.putExtra("adapter_position" , position);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initRecyclerView();
        setUserData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initRecyclerView();
        setUserData();
    }
}