package com.thertx.telertx;

import android.content.Context;
import android.util.Log;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

public class TelegramClient {
    
    private static final String TAG = "TelegramClient";
    private static TelegramClient instance;
    private Client client;
    private Context context;
    
    // Telegram API credentials (these should be obtained from https://my.telegram.org)
    // For demo purposes, using placeholder values
    private static final int API_ID = 94575; // Replace with your API ID
    private static final String API_HASH = "a3406de8d171bb422bb6ddf3bbd800e2"; // Replace with your API Hash
    
    private String currentPhoneNumber;
    private AuthCallback currentAuthCallback;
    
    public interface AuthCallback {
        void onSuccess();
        void onError(String error);
    }
    
    private TelegramClient(Context context) {
        this.context = context.getApplicationContext();
        initializeTelegramClient();
    }
    
    public static synchronized TelegramClient getInstance(Context context) {
        if (instance == null) {
            instance = new TelegramClient(context);
        }
        return instance;
    }
    
    private void initializeTelegramClient() {
        try {
            // Set log verbosity
            Client.execute(new TdApi.SetLogVerbosityLevel(1));
            
            // Create Telegram client
            client = Client.create(new UpdatesHandler(), null, null);
            
            // Set TDLib parameters
            TdApi.SetTdlibParameters parameters = new TdApi.SetTdlibParameters();
            parameters.databaseDirectory = context.getFilesDir().getAbsolutePath() + "/tdlib";
            parameters.useMessageDatabase = true;
            parameters.useSecretChats = true;
            parameters.apiId = API_ID;
            parameters.apiHash = API_HASH;
            parameters.systemLanguageCode = "en";
            parameters.deviceModel = android.os.Build.MODEL;
            parameters.systemVersion = android.os.Build.VERSION.RELEASE;
            parameters.applicationVersion = "1.0";
            parameters.enableStorageOptimizer = true;
            
            client.send(parameters, new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {
                    Log.d(TAG, "SetTdlibParameters result: " + object.toString());
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Telegram client", e);
        }
    }
    
    public void sendAuthenticationCode(String phoneNumber, AuthCallback callback) {
        this.currentPhoneNumber = phoneNumber;
        this.currentAuthCallback = callback;
        
        try {
            TdApi.SetAuthenticationPhoneNumber authPhone = new TdApi.SetAuthenticationPhoneNumber();
            authPhone.phoneNumber = phoneNumber;
            authPhone.settings = new TdApi.PhoneNumberAuthenticationSettings(
                false, false, false, false, null, null
            );
            
            client.send(authPhone, new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {
                    if (object.getConstructor() == TdApi.Ok.CONSTRUCTOR) {
                        Log.d(TAG, "Authentication code sent successfully");
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    } else if (object.getConstructor() == TdApi.Error.CONSTRUCTOR) {
                        TdApi.Error error = (TdApi.Error) object;
                        Log.e(TAG, "Error sending code: " + error.message);
                        if (callback != null) {
                            callback.onError(error.message);
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception sending authentication code", e);
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        }
    }
    
    public void checkAuthenticationCode(String code, AuthCallback callback) {
        this.currentAuthCallback = callback;
        
        try {
            TdApi.CheckAuthenticationCode checkCode = new TdApi.CheckAuthenticationCode();
            checkCode.code = code;
            
            client.send(checkCode, new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {
                    if (object.getConstructor() == TdApi.Ok.CONSTRUCTOR) {
                        Log.d(TAG, "Authentication successful");
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    } else if (object.getConstructor() == TdApi.Error.CONSTRUCTOR) {
                        TdApi.Error error = (TdApi.Error) object;
                        Log.e(TAG, "Error checking code: " + error.message);
                        if (callback != null) {
                            callback.onError(error.message);
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception checking authentication code", e);
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        }
    }
    
    public void logout() {
        try {
            if (client != null) {
                client.send(new TdApi.LogOut(), new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object) {
                        Log.d(TAG, "Logout result: " + object.toString());
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during logout", e);
        }
    }
    
    private class UpdatesHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                    onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
                    break;
                default:
                    Log.d(TAG, "Received update: " + object.toString());
            }
        }
        
        private void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {
            switch (authorizationState.getConstructor()) {
                case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                    Log.d(TAG, "Waiting for TDLib parameters");
                    break;
                case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR:
                    Log.d(TAG, "Waiting for phone number");
                    break;
                case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR:
                    Log.d(TAG, "Waiting for authentication code");
                    break;
                case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                    Log.d(TAG, "Authorization ready");
                    break;
                case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                    Log.d(TAG, "Logging out");
                    break;
                case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                    Log.d(TAG, "Closing");
                    break;
                case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                    Log.d(TAG, "Closed");
                    break;
                default:
                    Log.d(TAG, "Unknown authorization state: " + authorizationState.toString());
            }
        }
    }
}
