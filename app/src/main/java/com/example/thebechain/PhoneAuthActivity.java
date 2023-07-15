package com.example.thebechain;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sinch.android.rtc.SinchError;

public class PhoneAuthActivity extends BaseActivity implements SinchService.StartFailedListener{

    private Button mVerifyButton;
    private EditText phoneNum;
    private TextView countryCode;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        user = FirebaseAuth.getInstance().getCurrentUser();

        mVerifyButton = findViewById(R.id.verifyButton);
        phoneNum = findViewById(R.id.phoneNumber);
        countryCode = findViewById(R.id.country_code);

        mVerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = countryCode.getText().toString();
                String number = phoneNum.getText().toString().trim();

                if (number.isEmpty()) {
                    phoneNum.setError("Enter phone number");
                    phoneNum.requestFocus();
                    return;
                }
                String finalNum = code + number;

                startActivity(new Intent(PhoneAuthActivity.this,VerifyPhoneAuthActivity.class).
                        putExtra("phoneNumber",finalNum));
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onStartFailed(SinchError error) {

    }

    @Override
    public void onStarted() {
        if (!getSinchServiceInterface().isStarted()) {
            Log.e("currentUserId",user.getUid());
            getSinchServiceInterface().startClient(user.getUid());
        }
    }
}