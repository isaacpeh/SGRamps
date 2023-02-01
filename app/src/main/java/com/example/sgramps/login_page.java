package com.example.sgramps;

import static android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import android.util.Base64;

import java.util.List;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class login_page extends Fragment {

    TextInputEditText email, password;
    Button loginButton;

    ImageButton fingerprintButton;

    TextView registerButton;
    FirebaseAuth fAuth;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private final String ALIAS_KEY = "sgramplogin";
    protected Activity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_page, container, false);
        loginButton = view.findViewById(R.id.loginButton);
        fingerprintButton = view.findViewById(R.id.biometricButton);
        registerButton = view.findViewById(R.id.registerButton);
        email = view.findViewById(R.id.emailInput);
        password = view.findViewById(R.id.passwordInput);

        // check for permissions
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.USE_BIOMETRIC,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 55);
        }

        fAuth = FirebaseAuth.getInstance();

        fingerprintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if biometrics are available
                if (hasBiometricCapability(getContext()) == 1) {
                    executor = ContextCompat.getMainExecutor(getContext());
                    biometricPrompt = new BiometricPrompt(getActivity(), executor, new BiometricPrompt.AuthenticationCallback() {

                        @Override
                        public void onAuthenticationError(int errorCode,
                                                          @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                        }

                        @Override
                        public void onAuthenticationSucceeded(
                                @NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);

                            // check if biometrics are registered
                            SecretKey key = getSecretKey();
                            if (key == null) {
                                // create key and store in shared pref
                                generateSecretKey(new KeyGenParameterSpec.Builder(
                                        "sgramplogin",
                                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                                        .build());
                                storeCredentials();
                            }
                            try {
                                getCredentials();
                            } catch (Exception ex) {
                                Log.d("LOG", "Failed to store credentials", ex);
                            }
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            Toast.makeText(getContext(), "Fingerprint not recognized",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    promptInfo = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Verify it's you")
                            .setSubtitle("Use your fingerprint to continue")
                            .setNegativeButtonText("Use password")
                            .build();

                    startBiometrics();
                }
            }

            public void startBiometrics() {
                SecretKey key = getSecretKey();

                if (key == null) {
                    dummyLogin(new dummyCallback() {
                        @Override
                        public void onCallBack(boolean result) {
                            if (result == true) {
                                new MaterialAlertDialogBuilder(getContext())
                                        .setTitle("Enable biometric authentication?")
                                        .setMessage("Use your biometric to sign in quicker in the future.")
                                        .setNegativeButton(getContext().getResources().getString(R.string.notnow), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                return;
                                            }
                                        })
                                        .setPositiveButton(getContext().getResources().getString(R.string.enable), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                biometricPrompt.authenticate(promptInfo);
                                            }
                                        })
                                        .show();
                            } else {
                                Toast.makeText(getContext(), "Incorrect email or password", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    });
                } else {
                    biometricPrompt.authenticate(promptInfo);
                }

            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard();
                view.clearFocus();
                String emailString = email.getText().toString();
                String passwordString = password.getText().toString();
                if (emailString.isEmpty()) {
                    email.setError("Email is required");
                    email.requestFocus();
                } else if (passwordString.isEmpty()) {
                    password.setError("Password is required");
                    password.requestFocus();
                } else {

                    fAuth.signInWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Logged in successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            } else {
                                if (task.getException().getMessage().contains("no user record") ||
                                        task.getException().getMessage().contains("password is invalid")) {
                                    Toast.makeText(getActivity(), "Email or Password is incorrect", Toast.LENGTH_SHORT).show();
                                } else if (task.getException().getMessage().contains("The email address is badly formatted")) {
                                    Toast.makeText(getActivity(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), "Error occurred logging in", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard();
                view.clearFocus();
                Fragment fragment = new register_page();
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, fragment)
                        .commit();
                StartUpActivity.active = StartUpActivity.fragmentRegister;
            }
        });

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                if (StartUpActivity.active == StartUpActivity.fragmentLogin) {
                    email.setText("");
                    password.setText("");
                    Fragment fragment = new start_up_page();
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_layout, fragment)
                            .commit();
                    StartUpActivity.active = StartUpActivity.fragmentStartup;
                } else {
                    mActivity.finish();
                }
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);
        return view;
    }

    public int hasBiometricCapability(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG |
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
                case BiometricManager.BIOMETRIC_SUCCESS:
                    Log.d("LOG", "App can authenticate using biometrics.");
                    result = 1;
                    break;
                case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                    Log.e("LOG", "No biometric features available on this device.");
                    Toast.makeText(context, "No biometric features available on this device.", Toast.LENGTH_SHORT).show();
                    result = 2;
                    break;
                case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                    Log.e("LOG", "No biometric features available on this device.");
                    result = 3;
                    break;
                case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                    Log.e("LOG", "create credentials that your app accepts.");
                    result = 4;
                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle("No biometrics registered on this device")
                            .setMessage("Register biometrics now?")
                            .setNegativeButton(getContext().getResources().getString(R.string.notnow), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    return;
                                }
                            })
                            .setPositiveButton(getContext().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                                    enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                            BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
                                    startActivityForResult(enrollIntent, 90);
                                }
                            })
                            .show();
                    break;
            }
        }
        return result;
    }

    private SecretKey getSecretKey() {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            // Before the keystore can be accessed, it must be loaded.
            keyStore.load(null);
            return ((SecretKey) keyStore.getKey(ALIAS_KEY, null));
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void generateSecretKey(KeyGenParameterSpec keyGenParameterSpec) {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(keyGenParameterSpec);
            keyGenerator.generateKey();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    public void storeCredentials() {
        try {
            SecretKey key = getSecretKey();
            Cipher cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            String credentials = email.getText().toString() + ":" + password.getText().toString();
            byte[] iv = cipher.getIV();
            byte[] encryptedCredentials = cipher.doFinal(credentials.getBytes());

            storeSharedPref(iv, encryptedCredentials);
        } catch (Exception ex) {
            Toast.makeText(getActivity(), "Error registering fingerprint. Please try again.", Toast.LENGTH_SHORT).show();
            Log.e("LOG", "Error storing credentials", ex);
        }
    }

    public void getCredentials() {
        try {
            List<byte[]> data = getSharedPref();
            byte[] ivdata = data.get(0);
            byte[] credentials = data.get(1);

            SecretKey key = getSecretKey();
            Cipher cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ivdata));

            byte[] credentialsEncrypted = credentials;
            byte[] credentialBytes = cipher.doFinal(credentialsEncrypted);
            String credential = new String(credentialBytes, "UTF-8");
            String[] credentialArr = credential.split(":");
            String emailData = credentialArr[0];
            String passwordData = credentialArr[1];
            email.setText(emailData);
            password.setText(passwordData);
            loginButton.performClick();

        } catch (Exception ex) {
            Log.d("LOG", "Failed to get credentials", ex);
        }
    }

    public void storeSharedPref(byte[] iv, byte[] encryptedCredentials) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("iv", Base64.encodeToString(iv, Base64.DEFAULT));
        editor.apply();
        editor.putString("credentials", Base64.encodeToString(encryptedCredentials, Base64.DEFAULT));
        editor.apply();
    }

    public List<byte[]> getSharedPref() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("data", Context.MODE_PRIVATE);

        String ivString = sharedPreferences.getString("iv", null);
        byte[] iv = Base64.decode(ivString, Base64.DEFAULT);

        String credentialEncryptedString = sharedPreferences.getString("credentials", null);
        byte[] credentialEncrypted = Base64.decode(credentialEncryptedString, Base64.DEFAULT);

        List<byte[]> data = new ArrayList<>();
        data.add(iv);
        data.add(credentialEncrypted);
        return data;
    }

    // hide soft keyboard
    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void dummyLogin(dummyCallback callback) {
        closeKeyboard();
        String emailString = email.getText().toString();
        String passwordString = password.getText().toString();
        if (emailString.isEmpty()) {
            email.setError("Email is required");
            email.requestFocus();
        } else if (passwordString.isEmpty()) {
            password.setError("Password is required");
            password.requestFocus();
        } else {
            fAuth.signInWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        callback.onCallBack(true);
                        fAuth.signOut();
                    } else {
                        callback.onCallBack(false);
                    }
                }
            });
        }
    }

    public interface dummyCallback {
        void onCallBack(boolean result);
    }
}