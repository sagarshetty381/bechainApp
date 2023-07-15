package com.example.thebechain;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.thebechain.Fragments.HomeFragment;
import com.example.thebechain.Fragments.MessageFragment;
import com.example.thebechain.Fragments.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.SinchError;

import java.util.Calendar;
import java.util.HashMap;

public class HomeActivity extends BaseActivity implements SinchService.StartFailedListener,CallBackMatchCount{
    FirebaseAuth mAuth;
    ImageView mProfileImg, mHomeImg, mChatImg;
    LinearLayout mExitSession;
    DatabaseReference mDatabaseRef;
    private TextView mNotificationCount;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor myEdit;
    String status;
    Boolean isSessionOpen = false;
    RelativeLayout bottomoNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        mProfileImg = findViewById(R.id.ivProfileFrag);
        mHomeImg = findViewById(R.id.ivHomeFrag);
        mChatImg = findViewById(R.id.ivChatsFrag);
        mExitSession = findViewById(R.id.exitSession);
        mNotificationCount = findViewById(R.id.notificationCountTV);
        bottomoNavigation = findViewById(R.id.bottom_navigation);
        
        mProfileImg.setOnClickListener(mClickListener);
        mHomeImg.setOnClickListener(mClickListener);
        mChatImg.setOnClickListener(mClickListener);
        mExitSession.setOnClickListener(mClickListener);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
        myEdit = sharedPreferences.edit();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new ProfileFragment(),"proileFrag").commit();
        mProfileImg.setImageResource(R.drawable.home_solid);

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("HOMEACTY","count");
                if (snapshot.child("Connections").hasChild("Matches")){

                    int iCount = (int) snapshot.child("Connections").child("Matches").getChildrenCount();
                    int currentCount = sharedPreferences.getInt("notificationCount",0);

                    if (iCount > currentCount){
                        mNotificationCount.setText(String.valueOf(iCount - currentCount));
                        if (mNotificationCount.getVisibility() == View.GONE) {
                            mNotificationCount.setVisibility(View.VISIBLE);
                        }
                        SharedPreferences.Editor myEdit = sharedPreferences.edit();
                        myEdit.putInt("notificationCount",iCount);
                        myEdit.commit();
                    }
                }else {
                    myEdit.putInt("notificationCount",0);
                    myEdit.commit();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ivProfileFrag:
                    setUserAvailability("offline");
                    mProfileImg.setImageResource(R.drawable.home_solid);
                    mChatImg.setImageResource(R.drawable.chat_trans);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new ProfileFragment(),"profileFrag").commit();
                    break;
                case R.id.ivHomeFrag:
                    setUserAvailability("online");
                    isSessionOpen = true;
                    mProfileImg.setImageResource(R.drawable.home_trans);
                    mChatImg.setImageResource(R.drawable.chat_trans);
                    mExitSession.setVisibility(View.VISIBLE);
                    Fragment homeFragment = new HomeFragment();
                    ((HomeFragment) homeFragment).setCallbackFragment(HomeActivity.this);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            homeFragment,"homeFrag").commit();
                    break;
                case R.id.ivChatsFrag:
                    setUserAvailability("offline");
                    mProfileImg.setImageResource(R.drawable.home_trans);
                    mChatImg.setImageResource(R.drawable.chat_solid);

                    Fragment chatFragment = new MessageFragment();
                    ((MessageFragment) chatFragment).setCallbackFragment(HomeActivity.this);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            chatFragment,"msgFrag").commit();
                    break;
                case R.id.exitSession:
                    showNavigation();
            }
        }
    };

    @Override
    public void onStartFailed(SinchError error) {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        getSinchServiceInterface().setStartListener(this);
        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(mAuth.getUid());
        }
    }

    @Override
    public void onStarted() {

    }

    @Override
    public void notificationReadIndc() {
        if (mNotificationCount.getVisibility() == View.VISIBLE) {
            mNotificationCount.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideNavigation() {
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("homeFrag");
        if (homeFragment != null) {
            homeFragment.stopSession(false);
        }
        TranslateAnimation animate = new TranslateAnimation(
                0,
                0,
                0,
                100);
        animate.setDuration(1000);
        animate.setFillAfter(true);
        bottomoNavigation.startAnimation(animate);

        TranslateAnimation animate2 = new TranslateAnimation(
                0,
                0,
                0,
                500);
        animate2.setDuration(1500);
        animate2.setFillAfter(true);
        mHomeImg.startAnimation(animate2);
    }

    private void showNavigation() {
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("homeFrag");
        if (homeFragment != null) {
            homeFragment.stopSession(true);
        }
        mExitSession.setVisibility(View.GONE);
        isSessionOpen = false;
        TranslateAnimation animate = new TranslateAnimation(
                0,
                0,
                100,
                0);
        animate.setDuration(1000);
        animate.setFillAfter(true);
        bottomoNavigation.startAnimation(animate);

        TranslateAnimation animate2 = new TranslateAnimation(
                0,
                0,
                500,
                0);
        animate2.setDuration(1500);
        animate2.setFillAfter(true);
        mHomeImg.startAnimation(animate2);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if (isSessionOpen){
            showNavigation();
        }else{
            showDialogueBox(this,getResources().getString(R.string.areYouSure));
        }
    }
}