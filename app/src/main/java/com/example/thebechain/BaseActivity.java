package com.example.thebechain;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public abstract class BaseActivity extends AppCompatActivity implements ServiceConnection {

    private SinchService.SinchServiceInterface mSinchServiceInterface;
    DatabaseReference mDatabaseRef;
    FirebaseAuth mAuth;
    String returnValue = "No";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationContext().bindService(new Intent(this, SinchService.class), this,
                BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (SinchService.class.getName().equals(componentName.getClassName())) {
            mSinchServiceInterface = (SinchService.SinchServiceInterface) iBinder;
            onServiceConnected();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        if (SinchService.class.getName().equals(componentName.getClassName())) {
            mSinchServiceInterface = null;
            onServiceDisconnected();
        }
    }

    protected void onServiceConnected() {
        // for subclasses
    }

    protected void onServiceDisconnected() {
        // for subclasses
    }

    protected SinchService.SinchServiceInterface getSinchServiceInterface() {
        return mSinchServiceInterface;
    }

    protected void setUserAvailability(String status) {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {

            mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", status);
            mDatabaseRef.updateChildren(hashMap);
        }
    }

    public String showDialogueBox(Context context, String title){

        DialogInterface.OnClickListener dialogueClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        String className = context.getClass().getSimpleName();
                        if (className.equals("HomeActivity")){
                            if (title.equals(getResources().getString(R.string.areYouSure))) {
                                finish();
                            }else if (title.equals(getResources().getString(R.string.reportConfirm))) {
                                returnValue = "Yes";
                            }
                        }else if(className.equals("ChatActivity")){
                            returnValue = "Yes";
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                    default:
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(title).setPositiveButton("Yes",dialogueClickListener)
                .setNegativeButton("No",dialogueClickListener).show();

        return returnValue;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setUserAvailability("offline");
    }

    @Override
    protected void onPause() {
        super.onPause();
        setUserAvailability("offline");
    }
}
