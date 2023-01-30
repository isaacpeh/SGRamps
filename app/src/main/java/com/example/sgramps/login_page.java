package com.example.sgramps;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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
import com.google.common.escape.Escaper;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class login_page extends Fragment {

    TextInputEditText email, password;
    Button loginButton, fingerprintButton;
    TextView registerButton;
    FirebaseAuth fAuth;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private final String ALIAS_KEY = "sgramplogin";
    byte[] iv;

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

        if (hasBiometricCapability(getContext()) == 1) {
            executor = ContextCompat.getMainExecutor(getContext());
            biometricPrompt = new BiometricPrompt(getActivity(), executor, new BiometricPrompt.AuthenticationCallback() {

                @Override
                public void onAuthenticationError(int errorCode,
                                                  @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Toast.makeText(getContext(),
                            "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationSucceeded(
                        @NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Toast.makeText(getContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show();

                    SecretKey key = getSecretKey();
                    Log.d("test", "" + key);

                    if (key == null) {
                        // create key
                        generateSecretKey(new KeyGenParameterSpec.Builder(
                                "sgramplogin",
                                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                                .build());

                        key = getSecretKey();
                        Log.d("test", "registered: " + key);
                    }

                    try {
                        byte[] response = storeCredentials(key);
                        Log.d("test", "Encrypted information: " +
                                Arrays.toString(response));
                        getCredentials(response, key);

                    } catch (Exception ex) {
                        Log.d("test", "Failed to store credentials", ex);
                    }
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(getContext(), "Authentication failed",
                            Toast.LENGTH_SHORT).show();
                }
            });

            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Verify it's you")
                    .setSubtitle("Use your fingerprint to continue")
                    .setNegativeButtonText("Use password")
                    .build();
        }

        fAuth = FirebaseAuth.getInstance();

        fingerprintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                biometricPrompt.authenticate(promptInfo);
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
                try {
                    KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                    keyStore.load(null);

                    keyStore.deleteEntry(ALIAS_KEY);
                    Log.d("test", "deleted");
                } catch (Exception ex) {
                    Log.d("test", "error deleteing", ex);
                }

                /*closeKeyboard();
                view.clearFocus();
                Fragment fragment = new register_page();
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, fragment)
                        .commit();
                StartUpActivity.active = StartUpActivity.fragmentRegister;*/
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
                    getActivity().finish();
                }
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);
        return view;
    }

    // 1. CHECK IF THERE'S BIOMETRIC SCANNER
    public int hasBiometricCapability(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG |
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
                case BiometricManager.BIOMETRIC_SUCCESS:
                    Log.d("test", "App can authenticate using biometrics.");
                    result = 1;
                    break;
                case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                    Log.e("test", "No biometric features available on this device.");
                    result = 2;
                    break;
                case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                    Log.e("test", "No biometric features available on this device.");
                    result = 3;
                    break;
                case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                    Log.e("test", "create credentials that your app accepts.");
                    result = 4;
                    break;
            }
        }
        return result;
    }

    // 2. GET KEYSTORE "sgramplogin"
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

    // 3. IF KEYSTORE "sgramplogin" !exist. Create one here
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

    // 3a. STORE CREDENTIALS IN KEYSTORE FOR FIRST TIME USE
    public byte[] storeCredentials(SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            Log.d("test", "ciphering: " + key);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            ByteArrayOutputStream rawText = new ByteArrayOutputStream();
            rawText.write(email.getText().toString().getBytes());
            rawText.write(password.getText().toString().getBytes());
            byte[] encryptedText = cipher.doFinal(rawText.toByteArray());
            return encryptedText;

        } catch (Exception ex) {
            Toast.makeText(getActivity(), "Error registering fingerprint. Please try again.", Toast.LENGTH_SHORT).show();
            Log.e("test", "Error encrypting credentials", ex);
            return null;
        }
    }

    // 4. GET CREDENTIALS FROM KEYSTORE
    public void getCredentials(byte[] encryptedCredentials, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            cipher.init(Cipher.DECRYPT_MODE, key);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    cipher.doFinal(encryptedCredentials));

            int emailLength = inputStream.read();
            byte[] emailBytes = new byte[emailLength];
            inputStream.read(emailBytes, 0, emailLength);
            int passwordLength = inputStream.available();
            byte[] passwordBytes = new byte[passwordLength];
            inputStream.read(passwordBytes, 0, passwordLength);
            Log.d("test", "deciphered: " + emailBytes + "  |  " + passwordBytes);
        } catch (Exception ex) {
            Log.d("test", "Failed to decipher", ex);
        }
    }

    // hide soft keyboard
    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}