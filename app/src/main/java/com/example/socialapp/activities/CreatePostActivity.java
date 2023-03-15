package com.example.socialapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import com.example.socialapp.R;
import com.example.socialapp.dao.PostDao;
import com.example.socialapp.databinding.ActivityCreatePostBinding;
import com.example.socialapp.util.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

public class CreatePostActivity extends AppCompatActivity {

    private ActivityCreatePostBinding binding;
    private PreferenceManager preferenceManager;
    private Uri imageUri = null;
    private String imageUrl;
    private StorageReference storageReference;
    private final String path = "post_images/";
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_create_post);
        preferenceManager = new PreferenceManager(getApplicationContext());
        storageReference = FirebaseStorage.getInstance().getReference();
        pd = new ProgressDialog(this);


        binding.backImage.setOnClickListener(v->onBackPressed());
        binding.imageLayout.setOnClickListener(v-> selectPostImage());
        binding.postBtn.setOnClickListener(v->{
            if(imageUri != null){
                uploadImageAndAddPost(imageUri);
            }else{
                PostDao dao = new PostDao(getApplicationContext());
                dao.addPost(binding.etPost.getText().toString().trim() , "")
                        .addOnSuccessListener(documentSnapshot -> finish());
            }

        });
    }

    private void selectPostImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        selectPostImage.launch(intent);
    }

    private final ActivityResultLauncher<Intent> selectPostImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult() , result ->{
        if (result.getResultCode() == RESULT_OK){
            if (result.getData() != null){
                imageUri = result.getData().getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream);
                    binding.postImage.setImageBitmap(imageBitmap);
                    binding.addPostText.setVisibility(View.GONE);
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }
            }
        }
    });
    private void uploadImageAndAddPost(Uri imageUri) {
        pd.setMessage("Uploading Post ....");
        pd.show();
        String storagePath = path + UUID.randomUUID().toString();
        storageReference.child(storagePath).putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    Uri downloadUri = uriTask.getResult();

                    if(uriTask.isSuccessful()){
                        //image uploaded
                        imageUrl = downloadUri.toString();
                        PostDao dao = new PostDao(getApplicationContext());
                        dao.addPost(binding.etPost.getText().toString().trim() , imageUrl)
                                .addOnSuccessListener(unused ->{
                                    pd.dismiss();
                                    finish();
                                }).addOnFailureListener(e -> {
                                    pd.dismiss();
                                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                });;
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

}