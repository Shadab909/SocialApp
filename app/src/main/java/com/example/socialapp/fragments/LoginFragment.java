package com.example.socialapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.socialapp.R;
import com.example.socialapp.activities.MainActivity;
import com.example.socialapp.dao.UserDao;
import com.example.socialapp.databinding.FragmentLoginBinding;
import com.example.socialapp.util.Constants;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Objects;


public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_login,container,false);
        binding.btnLogin.setOnClickListener(v->{if (isLogInValid()){logIn();}});
        return binding.getRoot();
    }

    private void logIn(){
        isLoading(true);
        UserDao dao = new UserDao(getActivity());
        dao.getUserByEmailPassword(binding.etLoginEmail.getText().toString().trim(),
                binding.etLoginPassword.getText().toString().trim())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null
                    && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);
                        dao.addUserToPreferenceManager(true, snapshot.getString(Constants.KEY_USER_ID),
                                snapshot.getString(Constants.KEY_NAME) ,
                                snapshot.getString(Constants.KEY_EMAIL),
                                snapshot.getString(Constants.KEY_IMAGE));
                        isLoading(false);
                        Intent intent = new Intent(getActivity() , MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        requireActivity().startActivity(intent);
                        requireActivity().finish();
                    }else {
                        isLoading(false);
                        showToast("log in credential incorrect");
                    }
                });
    }

    private boolean isLogInValid(){
        if (Objects.requireNonNull(binding.etLoginEmail.getText()).toString().trim().isEmpty()){
            binding.etLoginEmail.setError("Email address required");
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.etLoginEmail.getText()).matches()){
            binding.etLoginEmail.setError("Invalid email address");
            return false;
        }else if (Objects.requireNonNull(binding.etLoginPassword.getText()).toString().trim().length() < 6){
            binding.etLoginPassword.setError("");
            showToast("more than 6 characters password required");
            return false;
        }
        return true;
    }

    private void isLoading(boolean loading){
        if (loading){
            binding.btnLogin.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.btnLogin.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    private void showToast(String message){
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }


}