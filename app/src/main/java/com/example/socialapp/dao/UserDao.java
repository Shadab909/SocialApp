package com.example.socialapp.dao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.socialapp.R;
import com.example.socialapp.model.MyUsers;
import com.example.socialapp.model.User;
import com.example.socialapp.util.Constants;
import com.example.socialapp.util.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserDao {

    private final FirebaseFirestore db;
    private final Context context;
    private final PreferenceManager preferenceManager;

    public UserDao(Context context) {
        db = FirebaseFirestore.getInstance();
        this.context = context;
        preferenceManager = new PreferenceManager(context);
    }

    public Task<Void> addUserToFirebase(User user, String userId) {
        return db.collection(Constants.KEY_COLLECTION_USERS).document(userId).set(user);
    }

    public void addUserToPreferenceManager(boolean isSignedIn, String userId, String name, String email, String image) {
        preferenceManager.putBoolean(Constants.KEY_IS_LOGGED_IN, isSignedIn);
        preferenceManager.putString(Constants.KEY_USER_ID, userId);
        preferenceManager.putString(Constants.KEY_NAME, name);
        preferenceManager.putString(Constants.KEY_EMAIL, email);
        preferenceManager.putString(Constants.KEY_IMAGE, image);
        preferenceManager.putString(Constants.KEY_BANNER, "");
    }

    public Task<QuerySnapshot> getUserByEmailPassword(String email, String password) {
        return db.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, email)
                .whereEqualTo(Constants.KEY_PASSWORD, password)
                .get();
    }

    public Task<DocumentSnapshot> getUserById(String userId) {
        return db.collection(Constants.KEY_COLLECTION_USERS)
                .document(userId).get();
    }

    public Task<Void> updateUserToken(String token) {
        DocumentReference documentReference = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        return documentReference.update(Constants.KEY_FCM_TOKEN, token);
    }

    public Task<Void> logOut() {
        DocumentReference documentReference = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        return documentReference.update(Constants.KEY_FCM_TOKEN, FieldValue.delete());
    }

    public Task<Void> UpdateImage(String imageUrl, String imageType) {
        DocumentReference documentReference = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        if (imageType.equals("banner")) {
            return documentReference.update(Constants.KEY_BANNER, imageUrl);
        } else {
            return documentReference.update(Constants.KEY_IMAGE, imageUrl);
        }
    }

    public Task<Void> updateName(String name){
        DocumentReference documentReference = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        return documentReference.update(Constants.KEY_NAME, name);
    }

    public Task<QuerySnapshot> getAllUsers() {
        return db.collection(Constants.KEY_COLLECTION_USERS).get();
    }

    public Task<DocumentSnapshot> getMyUsers() {
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        return db.collection(Constants.KEY_COLLECTION_MY_USERS).document(userId).get();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void loadFollowBtnText(User model, Button followBtn, ProgressBar pb) {
        isLoading(true, followBtn, pb);
        getMyUsers().addOnCompleteListener(task -> {
            isLoading(false, followBtn, pb);
            boolean modelFound = false;
            if (task.isSuccessful() && task.getResult() != null) {
                MyUsers doc = task.getResult().toObject(MyUsers.class);
                List<String> userIdList = doc.getUserIdList();
                for(String id : userIdList){
                    if(id.equals(model.getUserId())){
                        modelFound = true;
                        break;
                    }
                }
            }
            if (modelFound) {
                followBtn.setText("Following");
                followBtn.setBackground(context.getResources().getDrawable(R.drawable.background_following_btn));
                followBtn.setTextColor(context.getResources().getColor(R.color.primary));
            } else {
                followBtn.setText("Follow");
                followBtn.setBackground(context.getResources().getDrawable(R.drawable.background_follow_btn));
                followBtn.setTextColor(context.getResources().getColor(R.color.white));
            }
        });
    }

    private void isLoading(boolean loading, Button followBtn, ProgressBar pb) {
        if (loading) {
            followBtn.setVisibility(View.GONE);
            pb.setVisibility(View.VISIBLE);
        } else {
            followBtn.setVisibility(View.VISIBLE);
            pb.setVisibility(View.GONE);
        }
    }

    public void addToMyUsers(String userId) {
        String myUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        db.collection(Constants.KEY_COLLECTION_MY_USERS).document(myUserId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {

                MyUsers users = task.getResult().toObject(MyUsers.class);
                users.getUserIdList().add(userId);
                db.collection(Constants.KEY_COLLECTION_MY_USERS).document(myUserId).set(users);
                Toast.makeText(context, "user added", Toast.LENGTH_SHORT).show();

            }else {
                Toast.makeText(context, "user not added", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void removeFromMyUser(String userId){
        String myUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        getMyUsers().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                MyUsers doc = task.getResult().toObject(MyUsers.class);
                List<String> userIdList = doc.getUserIdList();
                for(String id : userIdList){
                    if(id.equals(userId)){
                        userIdList.remove(userId);
                        break;
                    }
                }
                db.collection(Constants.KEY_COLLECTION_MY_USERS).document(myUserId).set(new MyUsers(myUserId,userIdList));
            }
        });
    }

    public Task<Void> initializeMyUsers(String userId){
        MyUsers temp = new MyUsers(userId,new ArrayList<>());
        return db.collection(Constants.KEY_COLLECTION_MY_USERS).document(userId).set(temp);
    }

    public void setTypingFalse(){
        DocumentReference documentReference = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update("typing", false);
    }

    public void setTypingTrue(){
        DocumentReference documentReference = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update("typing", true);
    }

    public void setIsOnlineLastSeen(){
        DocumentReference documentReference = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update("online", System.currentTimeMillis());
    }

    public void setIsOnlineTrue(){
        DocumentReference documentReference = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update("online", "true");
    }


}
