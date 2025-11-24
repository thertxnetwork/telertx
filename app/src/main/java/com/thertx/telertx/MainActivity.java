package com.thertx.telertx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    private TextView tvLoginStatus;
    private TextView tvUserInfo;
    private Button btnLogin;
    private Button btnLogout;
    private SharedPreferences prefs;
    
    private static final String PREFS_NAME = "TeleRTXPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_USER_NAME = "userName";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        initViews();
        setupListeners();
        updateUI();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }
    
    private void initViews() {
        tvLoginStatus = findViewById(R.id.tvLoginStatus);
        tvUserInfo = findViewById(R.id.tvUserInfo);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogout = findViewById(R.id.btnLogout);
    }
    
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
        
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }
    
    private void updateUI() {
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        
        if (isLoggedIn) {
            String phone = prefs.getString(KEY_USER_PHONE, "");
            String name = prefs.getString(KEY_USER_NAME, "Unknown User");
            
            tvLoginStatus.setText(getString(R.string.logged_in_as));
            tvUserInfo.setText(name + "\n" + phone);
            tvUserInfo.setVisibility(View.VISIBLE);
            
            btnLogin.setVisibility(View.GONE);
            btnLogout.setVisibility(View.VISIBLE);
        } else {
            tvLoginStatus.setText(getString(R.string.login_status));
            tvUserInfo.setVisibility(View.GONE);
            
            btnLogin.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE);
        }
    }
    
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.logout_confirm))
            .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                performLogout();
            })
            .setNegativeButton(getString(R.string.no), null)
            .show();
    }
    
    private void performLogout() {
        // Clear session data
        TelegramClient.getInstance(this).logout();
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.remove(KEY_USER_PHONE);
        editor.remove(KEY_USER_NAME);
        editor.apply();
        
        updateUI();
        
        // Show logout success message
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.logout_success))
            .setPositiveButton(android.R.string.ok, null)
            .show();
    }
}
