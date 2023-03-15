package com.example.socialapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

import com.example.socialapp.R;
import com.example.socialapp.databinding.ActivityLoginSignUpBinding;
import com.example.socialapp.fragments.LoginFragment;
import com.example.socialapp.fragments.SignUpFragment;
import com.example.socialapp.util.Constants;
import com.example.socialapp.util.PreferenceManager;
import com.google.android.material.tabs.TabLayout;

public class LoginSignUpActivity extends AppCompatActivity {

    private ActivityLoginSignUpBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_login_sign_up);
        preferenceManager = new PreferenceManager(this);
        LoginFragment fragment = new LoginFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container ,fragment).commit();

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                replaceTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void replaceTab(int position){
        Fragment fragment;
        if (position == 0){
            fragment = new LoginFragment();
        }else{
            fragment = new SignUpFragment();
        }
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container,fragment);
        ft.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (preferenceManager.getBoolean(Constants.KEY_IS_LOGGED_IN)){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }
    }
}