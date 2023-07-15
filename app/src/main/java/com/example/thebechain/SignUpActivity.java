package com.example.thebechain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private Button mSignUpButton;
    private TextInputEditText mLoginName,mPassword;
    private RadioGroup mRadioGroup;
    private ProgressDialog mSpinner;
    private ImageView mBackImageView;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();

        //initializing UI elements
        mLoginName = findViewById(R.id.etUsername);
        mPassword = findViewById(R.id.etPassword);
        mRadioGroup = findViewById(R.id.radioGroup);
        mBackImageView = findViewById(R.id.login_back_button);
        mSignUpButton = findViewById(R.id.btSignUp);

        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               signupClicked();
            }
        });
    }

    private void signupClicked() {
        final String sUsername, sPassword;
        final int selectId = mRadioGroup.getCheckedRadioButtonId();

        sUsername = mLoginName.getText().toString();
        sPassword = mPassword.getText().toString();

        if (sUsername.isEmpty() || sPassword.isEmpty() || selectId <= 0) {
            Toast.makeText(this, "Please enter the credentials", Toast.LENGTH_LONG).show();
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(sUsername,sPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    String userId = firebaseAuth.getCurrentUser().getUid();
                    RadioButton gender = findViewById(selectId);

                    Map<String,Object> userInfo = new HashMap<>();
                    userInfo.put("email",sUsername);
                    userInfo.put("profileImageUrl","default");
                    userInfo.put("userSex",gender.getText().toString());

                    mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                    mDatabaseRef.updateChildren(userInfo);

                    startActivity(new Intent(SignUpActivity.this,LoginActivity.class));
                    finish();
                }
            }
        });
    }
}