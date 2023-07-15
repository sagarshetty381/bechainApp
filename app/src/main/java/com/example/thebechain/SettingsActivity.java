package com.example.thebechain;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private TextView mAccountSettings;
    private TextView mTitleText,mFeedback;
    private Button mSubmitFeedback;
    private TextInputEditText mEditFeedback;
    private ViewFlipper mViewFlipper;
    GoogleSignInClient mGoogleSignInClient;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TextView mRateUs = findViewById(R.id.rateUsTV);
        TextView mShare = findViewById(R.id.shareTV);
        mAccountSettings = findViewById(R.id.accountSettingTV);
        mViewFlipper = findViewById(R.id.viewFlipperLayout);
        mTitleText = findViewById(R.id.mainTitleTV);
        TextView mLogOut = findViewById(R.id.logOutTV);
        TextView mDeleteAccount = findViewById(R.id.deleteAccountTV);
        mFeedback = findViewById(R.id.feedbackTV);
        mEditFeedback = findViewById(R.id.enterFeedbackTEL);
        mSubmitFeedback = findViewById(R.id.submitFeedbackBT);

        mRateUs.setOnClickListener(onClickListener);
        mShare.setOnClickListener(onClickListener);
        mAccountSettings.setOnClickListener(onClickListener);
        mLogOut.setOnClickListener(onClickListener);
        mDeleteAccount.setOnClickListener(onClickListener);
        mFeedback.setOnClickListener(onClickListener);
        mSubmitFeedback.setOnClickListener(onClickListener);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mEditFeedback.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0){
                    mSubmitFeedback.setBackgroundResource(R.color.gray);
                    mSubmitFeedback.setEnabled(false);
                }else {
                    mSubmitFeedback.setBackgroundResource(R.color.colorOrange);
                    mSubmitFeedback.setEnabled(true);
                }
            }
        });
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.accountSettingTV:
                    mTitleText.setText(mAccountSettings.getText());
                    mViewFlipper.setDisplayedChild(1);
                    break;
                case R.id.rateUsTV:
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                    }catch (ActivityNotFoundException e){
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                    }
                    break;
                case R.id.shareTV:
                    shareApp();
                    break;
                case R.id.feedbackTV:
                    mTitleText.setText(mFeedback.getText());
                    mViewFlipper.setDisplayedChild(2);
                    break;
                case R.id.deleteAccountTV:
                    deleteBechainAccount();
                    break;
                case R.id.logOutTV:
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    mAuth.signOut();
                    startActivity(new Intent(SettingsActivity.this,LoginActivity.class));
                    finish();
                    break;
                case R.id.submitFeedbackBT:
                    String sFeedback = mEditFeedback.getText().toString();
                    sendFeedback(sFeedback);
                    break;
                default:
            }
        }
    };

    private void sendFeedback(String sFeedback) {
        DatabaseReference feedbackRef = FirebaseDatabase.getInstance().getReference().child("Feedbacks");
        String key = feedbackRef.push().getKey();
        feedbackRef.child(key).setValue(sFeedback).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(SettingsActivity.this,"Thank you for the feedback!!",Toast.LENGTH_LONG).show();
                    onBackPressed();
                }
            }
        });
    }

    private void createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void deleteBechainAccount() {
        startActivity(new Intent(SettingsActivity.this,DeleteUser.class));
        finish();
//        String token="",accessToken="";

//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        createRequest();
//        if (user != null){
//            mGoogleSignInClient.revokeAccess().addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull @NotNull Task<Void> task) {
//                    if (task.isSuccessful()){
//                        Toast.makeText(SettingsActivity.this,"Account deleted",Toast.LENGTH_SHORT).show();
//
//                    }else{
//                        Toast.makeText(SettingsActivity.this,"Error occurred while deleting your account",Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });

//            user.delete()
//                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull @NotNull Task<Void> task) {
//                            if (task.isSuccessful()){
//                                Toast.makeText(SettingsActivity.this,"Account deleted",Toast.LENGTH_SHORT).show();
//
//                            }else{
//                                Toast.makeText(SettingsActivity.this,"Error occurred while deleting your account",Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
//        }
    }

    private void shareApp() {
        final String appPackageName = getPackageName();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,"Check out the App at: https://play.google.com/store/apps/details?id=" + appPackageName);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    @Override
    public void onBackPressed() {
        switch (mViewFlipper.getDisplayedChild()){
            case 1:
            case 2:
                mTitleText.setText(R.string.settings);
                mViewFlipper.setDisplayedChild(0);
                break;
            case 0:
                finish();
        }
    }
}