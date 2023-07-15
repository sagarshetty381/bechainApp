package com.example.thebechain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.Cache;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.thebechain.Adapters.MessageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.SinchError;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends BaseActivity implements SinchService.StartFailedListener{

    CircleImageView mUserProfile;
    TextView mUserName;
    EditText mSendMsg;
    ImageView mSendButton, mVideoCallButton, mBackIV;
    String userid;
    FirebaseUser currentUser;
    DatabaseReference reference;

    MessageAdapter messageAdapter;
    List<Chat> mChat;

    RecyclerView recyclerView;

    ValueEventListener seenListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getWindow().setBackgroundDrawableResource(R.drawable.app_wallpaper);

        recyclerView = findViewById(R.id.chatsRV);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        mUserProfile = findViewById(R.id.chatImageIV);
        mUserName = findViewById(R.id.userNameTV);
        mSendMsg = findViewById(R.id.msgEditText);
        mSendButton = findViewById(R.id.sendBt);
//        mVideoCallButton = findViewById(R.id.videoCallIV);
        mBackIV = findViewById(R.id.backButtonIV);

        userid = getIntent().getStringExtra("userId");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

//        mVideoCallButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(ChatActivity.this,PlaceCallActivity.class);
//                intent.putExtra("userId",userid);
//                startActivity(intent);
//            }
//        });

        mBackIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = mSendMsg.getText().toString();
                if (!msg.equals("")){
                    sendMessage(currentUser.getUid(),userid,msg);
                }else{
                    Toast.makeText(ChatActivity.this,"type a message",Toast.LENGTH_LONG).show();
                }
                mSendMsg.setText("");
            }
        });

        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUserName.setText(snapshot.child("Name").getValue().toString());
                String profileUrl = snapshot.child("profileImageUrl").getValue().toString();
                if (!(ChatActivity.this).isDestroyed() && mUserProfile != null && profileUrl != null) {
                    if (!profileUrl.equals("default")) {
                        Glide.with(getApplicationContext()).load(profileUrl).into(mUserProfile);
                    } else {
                        Glide.with(getApplicationContext()).load(R.mipmap.ic_launcher).into(mUserProfile);
                    }
                }
                readMessages(currentUser.getUid(),userid,profileUrl);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_screen_options, menu);
        return true;
    }

    public void reportUser(MenuItem item){
        DialogInterface.OnClickListener dialogueClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        DatabaseReference deleteDataRef = FirebaseDatabase.getInstance().getReference();
                        deleteDataRef.child("Reported").child(userid).setValue(true);
                        Toast.makeText(ChatActivity.this, "User Reported", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                    default:
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setMessage(getResources().getString(R.string.reportConfirm))
                .setPositiveButton("Yes",dialogueClickListener)
                .setNegativeButton("No",dialogueClickListener).show();
    }

    public void deleteConnection(MenuItem item) {
        DatabaseReference deleteDataRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(currentUser.getUid());
        deleteDataRef.child("Connections").child("Matches").child(userid).removeValue();
        finish();
    }

    private void seenMessage(final String userId) {
        reference = FirebaseDatabase.getInstance().getReference().child("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snaps : snapshot.getChildren()) {
                    Chat chat = snaps.getValue(Chat.class);
                    if (chat.getReceiver().equals(currentUser.getUid()) || chat.getSender().equals(userId)) {
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("isseen",true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String sender, String receiver, String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        databaseReference.child("Chats").push().setValue(hashMap);
    }

    private void readMessages(final String myId, final String userId, final String imageUrl) {
        mChat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference().child("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mChat.clear();
                for (DataSnapshot snaps : snapshot.getChildren()) {
                    Chat chat = snaps.getValue(Chat.class);
                    if (chat.getReceiver().equals(myId) && chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) && chat.getSender().equals(myId)) {
                        mChat.add(chat);
                    }

                    messageAdapter = new MessageAdapter(ChatActivity.this,mChat,imageUrl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onStartFailed(SinchError error) {
        Log.e("Sinch error",error.toString());
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        getSinchServiceInterface().setStartListener(this);
        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(currentUser.getUid());
        }
    }

    @Override
    public void onStarted() {
    }
}