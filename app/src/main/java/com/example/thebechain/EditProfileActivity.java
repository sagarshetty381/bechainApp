package com.example.thebechain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView mConfirmIV,mCancelIV,mChangeProfilePhoto;
    private TextInputEditText mBio,mProfession,mHobbies,mName;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mUserRef;
    private String sName,sHobbies,sProfession,sBio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mConfirmIV = findViewById(R.id.confirm_form);
        mCancelIV = findViewById(R.id.cancel_form);
        mBio = findViewById(R.id.enterBioTEL);
        mName = findViewById(R.id.nameTEL);
        mProfession = findViewById(R.id.professionTEL);
        mHobbies = findViewById(R.id.hobbiesTEL);
        RangeSlider mAgeRangeSlider = findViewById(R.id.agePrefRangeRS);
        TextView mAgeRefText = findViewById(R.id.ageRangeTV);

        mUserRef = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mUserRef.getUid());

        mCancelIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mConfirmIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
            }
        });

        mAgeRangeSlider.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull @NotNull RangeSlider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull @NotNull RangeSlider slider) {
                int minValue = (slider.getValues().get(0)).intValue();
                int maxValue = (slider.getValues().get(1)).intValue();
                String sAgeRange  = minValue + " yrs - " + maxValue + " yrs";
                mAgeRefText.setText(sAgeRange);
                setAgePreferenceOfUser(minValue,maxValue);
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        int minAgePref = sharedPreferences.getInt("minAgePref",18);
        int maxAgePref = sharedPreferences.getInt("maxAgePref",80);
        Log.d("Age min", String.valueOf(minAgePref));
        Log.d("Age max", String.valueOf(maxAgePref));

        List<Float> values = new ArrayList<Float>();
        values.add(Float.valueOf(String.valueOf(minAgePref)));
        values.add(Float.valueOf(String.valueOf(maxAgePref)));
        mAgeRangeSlider.setValues(values);
        String sAgeRange  = minAgePref + " yrs - " + maxAgePref + " yrs";
        mAgeRefText.setText(sAgeRange);


        GetUserInfo();
    }

    private void saveUserInformation() {
        if (mName.getText() == null)
            mName.setError("");
        if (mBio.getText() == null)
            mBio.setError("");
        if (mProfession.getText() == null)
            mProfession.setError("");

        if (!mName.getText().toString().isEmpty() && !mBio.getText().toString().isEmpty() && !mProfession.getText().toString().isEmpty() &&
             !mHobbies.getText().toString().isEmpty()) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("Bio", mBio.getText().toString());
            userInfo.put("Profession", mProfession.getText().toString());
            userInfo.put("Hobbies", mHobbies.getText().toString());

            mDatabaseReference.updateChildren(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    finish();
                }
            });
        }
    }

    private void setAgePreferenceOfUser(int minAgePref,int maxAgePref) {

        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        myEdit.putInt("minAgePref",minAgePref);
        myEdit.putInt("maxAgePref",maxAgePref);
        myEdit.commit();
    }

    private void GetUserInfo() {
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sName = snapshot.child("Name").getValue().toString();
                mName.setText(sName);

                if (snapshot.hasChild("Bio")){
                    sBio = snapshot.child("Bio").getValue().toString();
                    mBio.setText(sBio);
                }
                if (snapshot.hasChild("Profession")){
                    sProfession = snapshot.child("Profession").getValue().toString();
                    mProfession.setText(sProfession);
                }
                if (snapshot.hasChild("Hobbies")){
                    sHobbies = snapshot.child("Hobbies").getValue().toString();
                    mHobbies.setText(sHobbies);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}