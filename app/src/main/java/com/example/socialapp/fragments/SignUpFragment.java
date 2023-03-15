package com.example.socialapp.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.socialapp.R;
import com.example.socialapp.activities.MainActivity;
import com.example.socialapp.activities.ProfileActivity;
import com.example.socialapp.dao.UserDao;
import com.example.socialapp.databinding.FragmentSignUpBinding;
import com.example.socialapp.model.User;
import com.example.socialapp.util.Constants;
import com.example.socialapp.util.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;


public class SignUpFragment extends Fragment {
    private FragmentSignUpBinding binding;
    private PreferenceManager preferenceManager;
    String[] cameraPermissions;
    String[] storagePermissions;
    private Uri cam_uri;
    private ProgressDialog pd;
    private StorageReference storageReference;
    private final String userId = UUID.randomUUID().toString();
    private Uri imageUri = null ;

    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_sign_up,container,false);
        cameraPermissions = new String[]{Manifest.permission.CAMERA , Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        preferenceManager = new PreferenceManager(requireActivity());
        storageReference = FirebaseStorage.getInstance().getReference();
        pd = new ProgressDialog(requireActivity());


        binding.profileImage.setOnClickListener(v->{
            showImageBannerPickDialog();
        });
        binding.signUpBtn.setOnClickListener(v->{if(isSignUpValid()){signUp();}});
        return binding.getRoot();
    }

    private void signUp(){
        String name = binding.etSignUpName.getText().toString().trim();
        String email = binding.etSignUpEmail.getText().toString().trim();
        String password = binding.etSignUpPassword.getText().toString().trim();
        pd.setMessage("Creating user ....");
        pd.show();
        pd.setCancelable(false);
        String storagePath = "Users_Profile_Banner_Images/" + "profile" + "_" + userId;
        storageReference.child(storagePath).putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    Uri downloadUri = uriTask.getResult();

                    if(uriTask.isSuccessful()){
                        //image uploaded
                        User user = new User(userId , name, email, password, downloadUri.toString() , "" , false , null);
                        UserDao dao = new UserDao(requireActivity());
                        dao.addUserToFirebase(user , userId)
                                .addOnSuccessListener(unused -> {
                                    dao.addUserToPreferenceManager(true,userId, name ,email, downloadUri.toString());
                                    dao.initializeMyUsers(userId).addOnSuccessListener(unused1 -> {
                                                Intent intent = new Intent(getActivity() , MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                pd.dismiss();
                                                requireActivity().startActivity(intent);
                                                requireActivity().finish();
                                            })
                                            .addOnFailureListener(e1->{
                                                pd.dismiss();
                                                showToast(e1.getMessage());
                                            });

                                })
                                .addOnFailureListener(e -> {
                                    pd.dismiss();
                                    showToast(e.getMessage());
                                });

                    }else{
                        pd.dismiss();
                        Toast.makeText(requireActivity(), "some error occurred", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    private void showToast(String message){
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private boolean isSignUpValid(){
        if (imageUri == null){
            showToast("Select profile image");
            return false;
        }else if (Objects.requireNonNull(binding.etSignUpName.getText()).toString().trim().isEmpty()){
            binding.etSignUpName.setError("Name required");
            return false;
        }else if (Objects.requireNonNull(binding.etSignUpEmail.getText()).toString().trim().isEmpty()){
            binding.etSignUpEmail.setError("Email address required");
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.etSignUpEmail.getText()).matches()){
            binding.etSignUpEmail.setError("Invalid email address");
            return false;
        }else if (Objects.requireNonNull(binding.etSignUpPassword.getText()).toString().trim().length() < 6){
            binding.etSignUpPassword.setError("");
            showToast("more than 6 characters password required");
            return false;
        }else if (Objects.requireNonNull(binding.etSignUpConfirmPassword.getText()).toString().trim().length() < 6){
            binding.etSignUpConfirmPassword.setError("");
            showToast("more than 6 characters confirm password required");
            return false;
        }else if (!binding.etSignUpPassword.getText().toString().equals(binding.etSignUpConfirmPassword.getText().toString().trim())){
            showToast("confirm password and password different");
            return false;
        }
        return true;
    }

    private boolean checkStoragePermission(){
        return ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission(){
        requestStoragePermission.launch(storagePermissions[0]);
    }
    private final ActivityResultLauncher<String> requestStoragePermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    pickImageBannerFromGallery();
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                    Toast.makeText(requireActivity(), "Please grant storage permission", Toast.LENGTH_SHORT).show();
                }
            });

    private boolean checkCameraPermission(){
        boolean isCameraPermitted = ContextCompat.checkSelfPermission(requireActivity(),Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        boolean isStoragePermitted = ContextCompat.checkSelfPermission(requireActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        return isCameraPermitted && isStoragePermitted;
    }

    private void requestCameraPermission(){
        requestCameraPermission.launch(cameraPermissions);
    }

    private final ActivityResultLauncher<String[]> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    if(Boolean.TRUE.equals(result.get(cameraPermissions[0])) && Boolean.TRUE.equals(result.get(cameraPermissions[1]))){
                        pickImageBannerFromCamera();
                    }else{
                        Toast.makeText(requireActivity(), "Please grant camera and storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            });


    private void showImageBannerPickDialog(){
        String[] options = new String[]{"Camera","Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
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

    private void pickImageBannerFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        cam_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
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
                        imageUri = cam_uri;
                        binding.profileImage.setImageURI(cam_uri);
                        binding.addImage.setVisibility(View.GONE);
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
                        imageUri = galleryUri;
                        binding.profileImage.setImageURI(galleryUri);
                        binding.addImage.setVisibility(View.GONE);
                    }
                }
            }
    );

}