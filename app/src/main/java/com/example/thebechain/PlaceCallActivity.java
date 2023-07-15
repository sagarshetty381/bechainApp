package com.example.thebechain;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class PlaceCallActivity extends BaseActivity implements SinchService.StartFailedListener{

    private int count = 0, random;
    private final Map<String, Object> questnMap = new HashMap<>();
    private String userId;
    private String[] arrayOfQuestns;
    private Set<Integer> set;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mAuth = FirebaseAuth.getInstance();
        userId = getIntent().getStringExtra("userId");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        ImageView mRemoteUserImage = findViewById(R.id.remoteUserImage);

        ImageView mEndCall = findViewById(R.id.stopButton);
        mEndCall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                stopButtonClicked();
            }
        });
    }

    // invoked when the connection with SinchServer is established
    @Override
    protected void onServiceConnected() {
        getSinchServiceInterface().setStartListener(this);
        callFirebaseQuestions();
    }

    @Override
    public void onDestroy() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        super.onDestroy();
    }

    //to kill the current session of SinchService
    private void stopButtonClicked() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        finish();
    }

    //to place the call to the entered name
    private void processCall(String userName) {

        if (userName.isEmpty()) {
            Toast.makeText(this, "No user to call", Toast.LENGTH_LONG).show();
            return;
        }

        Call call = getSinchServiceInterface().callUserVideo(userName);
        final String callId = call.getCallId();

        mDatabaseRef.child(callId).updateChildren(questnMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                Intent callScreen = new Intent(PlaceCallActivity.this, CallScreenActivity.class);
                callScreen.putExtra(SinchService.CALL_ID, callId);
                startActivity(callScreen);
            }
        });
    }

    private void callFirebaseQuestions() {
        mDatabaseRef.child("Questions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, String> questions = (Map<String, String>) dataSnapshot.getValue();

                assert questions != null;
                Set keys = questions.keySet();
                arrayOfQuestns = new String[questions.size()];
                for (Object key : keys) {
                    arrayOfQuestns[count] = (String) key;
                    count++;
                }

                set = new HashSet<>();
                while (set.size() != 5) {
                    random = new Random().nextInt(arrayOfQuestns.length);
                    set.add(random);
                }
                Log.e("Array", String.valueOf(arrayOfQuestns.length));

                for (int item : set) {
                    questnMap.put(arrayOfQuestns[item], "pending");
                }
                processCall(userId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStartFailed(SinchError error) {

    }

    @Override
    public void onStarted() {
        if (!getSinchServiceInterface().isStarted() || (getSinchServiceInterface() == null)) {
            getSinchServiceInterface().startClient(mAuth.getUid());
        }
    }
}