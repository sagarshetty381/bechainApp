package com.example.thebechain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.thebechain.Adapters.ImageAdapter;
import com.example.thebechain.ViewHolders.ImageModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShowProfileActivity extends BaseActivity {
    private ImageView mToggleBottomSheet;
    private TextView mName,mProfession,mHobbies,mBio;
    private DatabaseReference mDatabaseRef;
    private FirebaseUser mAuth;
    ViewPager viewPager;
    private List<ImageModel> imageModel;
    ImageAdapter imageAdapter;
    private ImageView mBackButton;
    private BottomSheetBehavior mBottomSheetBehavior;

    private String sProfession = "",sHobbies = "",sBio = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile);

        mAuth = FirebaseAuth.getInstance().getCurrentUser();
        String userID = getIntent().getStringExtra("userID");
        String sName = getIntent().getStringExtra("name");
        String sAge = getIntent().getStringExtra("age");

        mBackButton = findViewById(R.id.backButtonIV);
        mName = findViewById(R.id.userNameTV);
        mProfession = findViewById(R.id.professionTV);
        mBio = findViewById(R.id.bioTV);
        mHobbies = findViewById(R.id.hobbiesTV);
        mToggleBottomSheet = findViewById(R.id.closeBottomSheetIV);
        ImageView mSwipeLeftBt = findViewById(R.id.swipeLeftIV);
        ImageView mSwipeRightBt = findViewById(R.id.swipeRightIV);

        mName.setText((sName + ", " + sAge));

        View bottomSheet = findViewById(R.id.bottomSheet);

        assert sName != null;
        if (sName.equals("self")){
            bottomSheet.setVisibility(View.GONE);
        }

        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState){
                    case BottomSheetBehavior.STATE_EXPANDED:
                        mToggleBottomSheet.setImageResource(R.drawable.cancel);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        mToggleBottomSheet.setImageResource(R.drawable.ic_arrow_circle_up);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        imageModel = new ArrayList<>();

        viewPager = findViewById(R.id.viewPager);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        imageAdapter = new ImageAdapter(this, imageModel);
        viewPager.setAdapter(imageAdapter);

        getImageUrls();

        mToggleBottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }else if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mSwipeLeftBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.arrowScroll(View.FOCUS_LEFT);
            }
        });

        mSwipeRightBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.arrowScroll(View.FOCUS_RIGHT);
            }
        });
    }

    private void getImageUrls() {
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("Bio"))
                    sBio = snapshot.child("Bio").getValue().toString();
                if (snapshot.hasChild("Profession"))
                    sProfession = snapshot.child("Profession").getValue().toString();
                if (snapshot.hasChild("Hobbies"))
                    sHobbies = snapshot.child("Hobbies").getValue().toString();
                setUserInfo();
                for (DataSnapshot postSnapshot : snapshot.child("FeedImages").getChildren()) {
                    ImageModel imageObj = postSnapshot.getValue(ImageModel.class);
                    imageModel.add(imageObj);
                }
                imageAdapter.notifyDataSetChanged();

//                imageModel = (ArrayList<ImageModel>) snapshot.getValue(ImageModel.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setUserInfo() {
        LinearLayout profContainer,bioContainer,hobbiesContainer;
        profContainer = findViewById(R.id.professionContainer);
        bioContainer = findViewById(R.id.bioContainer);
        hobbiesContainer = findViewById(R.id.hobbiesContainer);

        if (!sBio.equals(""))
            mBio.setText(sBio);
        else
            bioContainer.setVisibility(View.GONE);

        if (!sProfession.equals(""))
            mProfession.setText(sProfession);
        else
            profContainer.setVisibility(View.GONE);

        if (!sHobbies.equals(""))
            mHobbies.setText(sHobbies);
        else
            hobbiesContainer.setVisibility(View.GONE);
    }
}