package com.example.thebechain;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallState;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.sinch.android.rtc.video.VideoScalingType.ASPECT_FILL;

public class CallScreenActivity extends BaseActivity {

    static final String TAG = CallScreenActivity.class.getSimpleName();
    static final String CALL_START_TIME = "callStartTime";
    static final String ADDED_LISTENER = "addedListener";
    private  static final long COUNT_DOWN_IN_MILLIS = 5000;

    private RelativeLayout mQuestionScreen;
    private CircleImageView mMatch1, mMatch2;
    private Button mNextQuestnButton;
    private TextView mCallDuration, mNextQuesText, mCountDownTimer,mQuestnCount;
    private ImageView mSwitchCamera;

    private AudioPlayer mAudioPlayer;
    private Timer mTimer;
    private String mCallId;
    private long mCallStart = 0;
    private boolean mAddedListener = false;
    private boolean mVideoViewsAdded = false;
    private String sQuestions;
    private String[] arrayOfQuestns;
    private Set<Integer> set;
    private int iQuestionNmbr = -1;
    private boolean bProceedToNext = false;

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private VideoController vc;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private UpdateCallDurationTask mDurationTask;

    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            CallScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCallDuration();
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putLong(CALL_START_TIME, mCallStart);
        savedInstanceState.putBoolean(ADDED_LISTENER, mAddedListener);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mCallStart = savedInstanceState.getLong(CALL_START_TIME);
        mAddedListener = savedInstanceState.getBoolean(ADDED_LISTENER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.callscreen);
        getApplicationContext().bindService(new Intent(this, SinchService.class), this,
                BIND_AUTO_CREATE);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mCallId = getIntent().getStringExtra(SinchService.CALL_ID);
        getQuestionList();

        mCountDownTimer = findViewById(R.id.countDownTimerTV);

        //init UI elements
        mAudioPlayer = new AudioPlayer(this);
        mCallDuration = findViewById(R.id.callDuration);

        //questions fragment
        mNextQuesText = findViewById(R.id.questionTV);
        mQuestionScreen = findViewById(R.id.questionRLayout);
        mSwitchCamera = findViewById(R.id.switchCameraButton);
        mQuestnCount = findViewById(R.id.questnCount);
        mNextQuestnButton = findViewById(R.id.nextQuestion);

        ImageView endCallButton = findViewById(R.id.hangupButton);
        endCallButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                endCall();
            }
        });

        ImageView pauseButton = findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseVideo();
            }
        });

        mCallId = getIntent().getStringExtra(SinchService.CALL_ID);
        if (savedInstanceState == null) {
            mCallStart = System.currentTimeMillis();
        }

        mSwitchCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                vc.toggleCaptureDevicePosition();
            }
        });
    }

    private void getQuestionList() {
        mDatabaseRef.child(mCallId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, String> questions = (Map<String, String>) dataSnapshot.getValue();

                assert questions != null;
                arrayOfQuestns = questions.keySet().toArray(new String[0]);
//                startQuestionSession();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void startQuestionSession() {
        if (countDownTimer != null){
            countDownTimer.cancel();
        }

        iQuestionNmbr++;
        mQuestnCount.setText(((iQuestionNmbr+1) + "/5"));
        if (iQuestionNmbr > 4) {
            Log.e("Calling",TAG);
            mNextQuestnButton.setEnabled(false);
            mQuestionScreen.setVisibility(View.GONE);
        }else {
            Toast.makeText(CallScreenActivity.this, String.valueOf(iQuestionNmbr), Toast.LENGTH_SHORT).show();
            mNextQuesText.setText(arrayOfQuestns[iQuestionNmbr]);
            timeLeftInMillis = COUNT_DOWN_IN_MILLIS;
            startCountDown();
        }
    }

    public void removeQuestionsEntry(){
        mDatabaseRef.child(mCallId).removeValue();
    }

    private void startCountDown() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long l) {
                timeLeftInMillis = l;
                updateCountdownText();
            }

            @Override
            public void onFinish() {
                startQuestionSession();
            }
        }.start();
    }

    private void updateCountdownText() {
        int minutes = (int) (timeLeftInMillis/1000) / 60;
        int seconds = (int) (timeLeftInMillis/1000) % 60;

        String timeFormatted = String.format(Locale.getDefault(),"%02d",seconds);
        mCountDownTimer.setText(timeFormatted);
    }


    private void pauseVideo() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.pauseVideo();
        }
    }

    @Override
    public void onServiceConnected() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            if (!mAddedListener) {
                call.addCallListener(new SinchCallListener());
                mAddedListener = true;
            }
        } else {
            Log.e(TAG, "Started with invalid callId, aborting.");
            finish();
        }
        updateUI();
    }

    //method to update video feeds in the UI
    private void updateUI() {
        if (getSinchServiceInterface() == null) {
            return; // early
        }

        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
//            mCallerName.setText(call.getRemoteUserId());
//            mCallState.setText(call.getState().toString());
            if (arrayOfQuestns != null)
                startQuestionSession();
            else
                getQuestionList();

            if (call.getState() == CallState.ESTABLISHED) {
                //when the call is established, addVideoViews configures the video to  be shown
                addVideoViews();
            }
        }
    }

    //stop the timer when call is ended
    @Override
    public void onStop() {
        super.onStop();
        mDurationTask.cancel();
        mTimer.cancel();
        removeVideoViews();
    }

    //start the timer for the call duration here
    @Override
    public void onStart() {
        super.onStart();
        mTimer = new Timer();
        mDurationTask = new UpdateCallDurationTask();
        mTimer.schedule(mDurationTask, 0, 500);
        updateUI();
    }

    @Override
    public void onBackPressed() {
        // User should exit activity by ending call, not by going back.
    }

    //method to end the call
    private void endCall() {
        mAudioPlayer.stopProgressTone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        removeQuestionsEntry();
        startActivity(new Intent(CallScreenActivity.this,ChatActivity.class));
        finish();
    }

    private String formatTimespan(long timespan) {
        long totalSeconds = timespan / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    //method to update live duration of the call
    private void updateCallDuration() {
        if (mCallStart > 0) {
            mCallDuration.setText(formatTimespan(System.currentTimeMillis() - mCallStart));
        }
    }

    //method which sets up the video feeds from the server to the UI of the activity
    private void addVideoViews() {
        if (mVideoViewsAdded || getSinchServiceInterface() == null) {
            return; //early
        }

        vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            RelativeLayout localView = (RelativeLayout) findViewById(R.id.localVideo);
            localView.addView(vc.getLocalView());
            vc.setResizeBehaviour(ASPECT_FILL);
            localView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //this toggles the front camera to rear camera and vice versa
//                    vc.toggleCaptureDevicePosition();
                }
            });

            LinearLayout view = findViewById(R.id.remoteVideo);
            view.addView(vc.getRemoteView());
            mVideoViewsAdded = true;
        }
    }

    //removes video feeds from the app once the call is terminated
    private void removeVideoViews() {
        if (getSinchServiceInterface() == null) {
            return; // early
        }

        VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            LinearLayout view = (LinearLayout) findViewById(R.id.remoteVideo);
            view.removeView(vc.getRemoteView());
            vc.setResizeBehaviour(ASPECT_FILL);
            RelativeLayout localView = (RelativeLayout) findViewById(R.id.localVideo);
            localView.removeView(vc.getLocalView());
            mVideoViewsAdded = false;
        }
    }

    private class SinchCallListener implements VideoCallListener {

        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended. Reason: " + cause.toString());
            mAudioPlayer.stopProgressTone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            String endMsg = "Call ended: " + call.getDetails().toString();
            Toast.makeText(CallScreenActivity.this, endMsg, Toast.LENGTH_LONG).show();

            endCall();
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
            mAudioPlayer.stopProgressTone();
//            mCallState.setText(call.getState().toString());
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            AudioController audioController = getSinchServiceInterface().getAudioController();
            audioController.enableSpeaker();
            mCallStart = System.currentTimeMillis();
            Log.d(TAG, "Call offered video: " + call.getDetails().isVideoOffered());
            startQuestionSession();
        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
            mAudioPlayer.playProgressTone();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            // Send a push through your push provider here, e.g. GCM
        }

        @Override
        public void onVideoTrackAdded(Call call) {
            Log.d(TAG, "Video track added");
            addVideoViews();
        }

        @Override
        public void onVideoTrackPaused(Call call) {
//            call.pauseVideo();
        }

        @Override
        public void onVideoTrackResumed(Call call) {

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null){
            countDownTimer.cancel();
        }
    }
}
