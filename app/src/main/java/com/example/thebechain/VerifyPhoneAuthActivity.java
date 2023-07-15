package com.example.thebechain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyPhoneAuthActivity extends AppCompatActivity {

    private String verificationId;
    private Button verifyButton;
    private EditText etOTP;
    private String userEnteredOTP;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone_auth);

        mAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.progressBar);
        String phoneNum = getIntent().getStringExtra("phoneNumber");
        sendVerificationCode(phoneNum);

        etOTP = findViewById(R.id.otp);
        verifyButton = findViewById(R.id.verifyOTPButton);
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userEnteredOTP = etOTP.getText().toString();
                if (userEnteredOTP.isEmpty() || userEnteredOTP.length() < 6) {
                    etOTP.setError("Enter valid code");
                    etOTP.requestFocus();
                    return;
                }
                verifyCode(userEnteredOTP);
            }
        });
    }

    private void sendVerificationCode(String number) {
        progressBar.setVisibility(View.VISIBLE);
//        PhoneAuthProvider.getInstance().verifyPhoneNumber(
//                number,
//                60,
//                TimeUnit.SECONDS,
//                 TaskExecutors.MAIN_THREAD,
//                mCallBack
//        );
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder()
                .setPhoneNumber(number)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallBack)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId,code);
        signInWithCredentials(credential);
    }

    private void signInWithCredentials(PhoneAuthCredential credential) {
        mAuth.getCurrentUser().linkWithCredential(credential).
                addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    startActivity(new Intent(VerifyPhoneAuthActivity.this,MainActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                }else {
//                    startActivity(new Intent(VerifyPhoneAuthActivity.this,MainActivity.class)
//                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(VerifyPhoneAuthActivity.this, task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        startActivity(new Intent(VerifyPhoneAuthActivity.this,PhoneAuthActivity.class));
                        finish();
                        //                        startActivity(new Intent(VerifyPhoneAuthActivity.this,MainActivity.class)
//                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }else {
                        Toast.makeText(VerifyPhoneAuthActivity.this, "task", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId  = s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null){
                etOTP.setText(code);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            if (e instanceof FirebaseAuthUserCollisionException) {
                Toast.makeText(VerifyPhoneAuthActivity.this,"e.getMessage()",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(VerifyPhoneAuthActivity.this,MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        }
    };
}