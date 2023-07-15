package com.example.thebechain.Fragments;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.thebechain.CallbackFragment;
import com.example.thebechain.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class GetDetailsFragment extends Fragment {

    CallbackFragment callbackFragment;
    public EditText mFullName, mProfession;
    public TextView mGender;
    public TextView mBirthDate,mPreferenceTv;
    public ImageView mGenderMalePref, mGenderFemalePref;
    public String sGender = "";
    public DatePickerDialog.OnDateSetListener mDateSetListener;
    Button proceedBt;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private FirebaseUser currentUser;
    private Date birthDate;
    private int year,month, day;
    public static int iAge = 0;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor myEdit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.registration_slide1, container,false);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            currentUser = mAuth.getCurrentUser();
        }

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());

        proceedBt = view.findViewById(R.id.proceed);
        mFullName = view.findViewById(R.id.firstName);
        mBirthDate = view.findViewById(R.id.birth_date);
        mPreferenceTv = view.findViewById(R.id.preference);
        mProfession = view.findViewById(R.id.profession);
        mGenderMalePref = view.findViewById(R.id.malePref);
        mGenderFemalePref = view.findViewById(R.id.femalePref);

        mBirthDate.setOnClickListener(mClickListener);
        mGenderMalePref.setOnClickListener(mClickListener);
        mGenderFemalePref.setOnClickListener(mClickListener);

        sharedPreferences = getActivity().getSharedPreferences("MySharedPref", MODE_PRIVATE);
        myEdit = sharedPreferences.edit();


        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                String sMaxAgePref="",sMinAgePref="";
                mBirthDate.setText((day + "/" + (month+1) + "/" + year));

                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                myEdit.putString("birthDay",(day + "/" + (month+1) + "/" + (year+1)));
                myEdit.commit();

                iAge = getAge(year,month,day);
                if ((iAge - 6) < 18){
                    sMinAgePref = 18+"";
                }else{
                    sMinAgePref = (iAge-6)+"";
                }
                sMaxAgePref = iAge+6+"";

                myEdit.putString("minAgePref",sMinAgePref);
                myEdit.putString("maxAgePref",sMaxAgePref);
            }
        };

        proceedBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("Age1",(iAge + ""));
                validateFields(iAge);
            }
        });
        return view;
    }

    public void validateFields(int iAge) {
        String sName = mFullName.getText().toString();
        String sBirthDate = mBirthDate.getText().toString();
        String sProfession = mProfession.getText().toString();
        Log.e("Age2",(iAge + ""));
        if (sName.isEmpty() || sBirthDate.isEmpty() || sProfession.isEmpty() || sGender.isEmpty() || iAge == 0) {
            if (sName.isEmpty())
                mFullName.setError("Enter your details");
            if (sBirthDate.isEmpty())
                mBirthDate.setError("Enter your details");
            if (sProfession.isEmpty())
                mProfession.setError("Enter your details");
            if (sGender.isEmpty())
                mPreferenceTv.setError("Enter your preference");
            if (iAge == 0)
                Log.e("AGEEEE BHAIII","set Age");
            return;
        }

        Map<String,Object> userInfo = new HashMap<>();
        userInfo.put("Gender",sGender);
        userInfo.put("Name",sName);
        userInfo.put("EmailId",currentUser.getEmail());
        userInfo.put("MobileNum",currentUser.getPhoneNumber());
        userInfo.put("BirthDate",sBirthDate);
        userInfo.put("Age",iAge);
        userInfo.put("Profession",sProfession);
        userInfo.put("profileImageUrl","default");

        mDatabaseRef.updateChildren(userInfo).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (callbackFragment != null) {
                    callbackFragment.changeFragment();
                    callbackFragment.addProgressDots();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(),"check your internet connection",Toast.LENGTH_LONG).show();
            }
        });
    }

    public void setCallbackFragment(CallbackFragment callbackFragment) {
        this.callbackFragment = callbackFragment;
    }

    private OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.birth_date:
                    setBirthDate();
                    break;
                case R.id.malePref:
                    mGenderMalePref.setImageResource(R.drawable.male_sign_fill);
                    mGenderFemalePref.setImageResource(R.drawable.female_sign_trans);
                    sGender = "Male";
                    break;
                case R.id.femalePref:
                    mGenderFemalePref.setImageResource(R.drawable.female_sign_fill);
                    mGenderMalePref.setImageResource(R.drawable.male_sign_trans);
                    sGender = "Female";
                    break;
            }
        }
    };

    private void setBirthDate() {
        Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH) + 1;
        day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialogue = new DatePickerDialog(
                getActivity(),
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                mDateSetListener,
                year,month,day);
        dialogue.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogue.show();
    }

    private int getAge(int year, int month, int day){
        Calendar cal = Calendar.getInstance();
        int curYear = cal.get(Calendar.YEAR);
        int curMonth = cal.get(Calendar.MONTH) + 1;
        int curDay = cal.get(Calendar.DAY_OF_MONTH);
        int age = 0;

        age = curYear - year;
        if ((month > curMonth) ||
                ((month == curMonth) && (day > curDay))){
            age --;
        }
        Log.e("Age",(age + ""));
        return age;
    }
}
