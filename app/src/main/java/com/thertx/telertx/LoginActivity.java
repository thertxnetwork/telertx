package com.thertx.telertx;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    
    private TextInputLayout tilPhoneNumber;
    private TextInputLayout tilVerificationCode;
    private TextInputEditText etPhoneNumber;
    private TextInputEditText etVerificationCode;
    private Button btnSendCode;
    private Button btnVerify;
    private ProgressBar progressBar;
    private TelegramClient telegramClient;
    private SharedPreferences prefs;
    
    private static final String PREFS_NAME = "TeleRTXPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_USER_NAME = "userName";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        telegramClient = TelegramClient.getInstance(this);
        
        initViews();
        setupListeners();
    }
    
    private void initViews() {
        tilPhoneNumber = findViewById(R.id.tilPhoneNumber);
        tilVerificationCode = findViewById(R.id.tilVerificationCode);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etVerificationCode = findViewById(R.id.etVerificationCode);
        btnSendCode = findViewById(R.id.btnSendCode);
        btnVerify = findViewById(R.id.btnVerify);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupListeners() {
        btnSendCode.setOnClickListener(v -> sendVerificationCode());
        btnVerify.setOnClickListener(v -> verifyCode());
    }
    
    private void sendVerificationCode() {
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        
        if (phoneNumber.isEmpty()) {
            tilPhoneNumber.setError(getString(R.string.invalid_phone));
            return;
        }
        
        tilPhoneNumber.setError(null);
        showProgress(true);
        
        // Send authentication code via Telegram API
        telegramClient.sendAuthenticationCode(phoneNumber, new TelegramClient.AuthCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    showProgress(false);
                    tilVerificationCode.setVisibility(View.VISIBLE);
                    btnVerify.setVisibility(View.VISIBLE);
                    btnSendCode.setEnabled(false);
                    Toast.makeText(LoginActivity.this, "Verification code sent!", Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(LoginActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void verifyCode() {
        String code = etVerificationCode.getText().toString().trim();
        
        if (code.isEmpty()) {
            tilVerificationCode.setError(getString(R.string.invalid_code));
            return;
        }
        
        tilVerificationCode.setError(null);
        showProgress(true);
        
        // Verify authentication code
        telegramClient.checkAuthenticationCode(code, new TelegramClient.AuthCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    showProgress(false);
                    
                    // Save login state
                    String phone = etPhoneNumber.getText().toString().trim();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(KEY_IS_LOGGED_IN, true);
                    editor.putString(KEY_USER_PHONE, phone);
                    editor.putString(KEY_USER_NAME, "Telegram User");
                    editor.apply();
                    
                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    
                    // Return to main activity
                    finish();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(LoginActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSendCode.setEnabled(!show);
        btnVerify.setEnabled(!show);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
