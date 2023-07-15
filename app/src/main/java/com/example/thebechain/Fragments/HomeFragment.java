package com.example.thebechain.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.thebechain.Adapters.ArrayAdapter;
import com.example.thebechain.CallBackMatchCount;
import com.example.thebechain.Cards;
import com.example.thebechain.ChatActivity;
import com.example.thebechain.R;
import com.example.thebechain.ShowProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.skyfishjy.library.RippleBackground;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment implements Animation.AnimationListener {
    private ImageView mLikeButton, mDislikeButton, mCloseMatchLayout;
    LinearLayout mSearchingLL, mReportUserLL;
    Button mGoToChatButton;
    RelativeLayout mRootLayout;
    BlurView mMatchScreenLayout, mPauseSessionBg;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor myEdit;

    private FirebaseAuth mAuth;
    private DatabaseReference userDb, databaseReference;
    CallBackMatchCount callBackMatchCount;

    private ArrayAdapter arrayAdapter;
    private String currentUid;
    public String matchUserId;
    int minAgePref, maxAgePref;
    List<Cards> rowItems;
    SwipeFlingAdapterView flingContainer;
    RippleBackground rippleBackground;

    View layoutView;

    Animation animFadeIn, animSlideInRight, animSlideOutRight;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        layoutView = inflater.inflate(R.layout.fragment_home, container, false);

        userDb = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();

        rippleBackground = layoutView.findViewById(R.id.content);
        mLikeButton = layoutView.findViewById(R.id.like_post);
        mDislikeButton = layoutView.findViewById(R.id.dislike_post);
        mSearchingLL = layoutView.findViewById(R.id.searchingLayout);
        mMatchScreenLayout = layoutView.findViewById(R.id.blur_match_layout);
        mGoToChatButton = layoutView.findViewById(R.id.sayHiButton);
        mCloseMatchLayout = layoutView.findViewById(R.id.closeMatchLayout);
        mPauseSessionBg = layoutView.findViewById(R.id.blur_pause_layout);
        mRootLayout = layoutView.findViewById(R.id.fragid);
        mReportUserLL = layoutView.findViewById(R.id.reportUser);

        sharedPreferences = getActivity().getSharedPreferences("MySharedPref", MODE_PRIVATE);
        myEdit = sharedPreferences.edit();

        minAgePref = sharedPreferences.getInt("minAgePref", 18);
        maxAgePref = sharedPreferences.getInt("maxAgePref", 80);
        Log.d("Age min", String.valueOf(minAgePref));
        Log.d("Age max", String.valueOf(maxAgePref));

        animFadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_bottom);
        animFadeIn.setAnimationListener(this);

        animSlideInRight = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
        animSlideInRight.setAnimationListener(this);

        animSlideOutRight = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_right);
        animSlideOutRight.setAnimationListener(this);

        final float radius = 25f;
        final Drawable windowBackground = getActivity().getWindow().getDecorView().getBackground();

        mMatchScreenLayout.setupWith(mRootLayout)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(getContext()))
                .setBlurRadius(radius)
                .setHasFixedTransformationMatrix(true);

        mPauseSessionBg.setupWith(mRootLayout)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(getContext()))
                .setBlurRadius(radius)
                .setHasFixedTransformationMatrix(true);

//        showMatchLayout();

        checkUserSex();
        callBackMatchCount.hideNavigation();
        stopSession(false);
        mGoToChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!matchUserId.equals(""))
                    startActivity(new Intent(getActivity(), ChatActivity.class).putExtra("userId", getMatchUserId()));
            }
        });

        mCloseMatchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMatchScreenLayout.setVisibility(View.GONE);
                stopSession(true);
            }
        });

        mReportUserLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogueBox(getActivity(), getResources().getString(R.string.reportConfirm));
            }
        });

        rowItems = new ArrayList<>();
        arrayAdapter = new ArrayAdapter(getActivity(), R.layout.item, rowItems);

        flingContainer = layoutView.findViewById(R.id.frame);

        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                rowItems.remove(0);
                if (rowItems.size() == 0) {
                    layoutChange(false);
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                Cards obj = (Cards) dataObject;
                String userId = obj.getUserId();

                userDb.child(userId).child("Connections").child("nopes").child(currentUid).setValue(true);
                Toast.makeText(getActivity(), "Left!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                Cards obj = (Cards) dataObject;
                String userId = obj.getUserId();

                userDb.child(userId).child("Connections").child("yups").child(currentUid).setValue(true);
                isConnectionMatched(userId);
                Toast.makeText(getActivity(), "Right!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
                rippleBackground.startRippleAnimation();
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getOppositeSexUsers();
                    }
                }, 1000);
            }

            @Override
            public void onScroll(float scrollProgressPercent) {

            }
        });

        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                Cards cards = (Cards) dataObject;

                Intent intent = new Intent(getActivity(), ShowProfileActivity.class);
                intent.putExtra("userID", cards.getUserId());
                intent.putExtra("name", cards.getName());
                intent.putExtra("age", cards.getAge());
                startActivity(intent);
            }
        });

        mLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rowItems.size() > 0)
                    flingContainer.getTopCardListener().selectRight();
            }
        });

        mDislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rowItems.size() > 0)
                    flingContainer.getTopCardListener().selectLeft();
            }
        });

        return layoutView;
    }

    public void setCallbackFragment(CallBackMatchCount callBackMatchCount) {
        this.callBackMatchCount = callBackMatchCount;
    }

    public void isConnectionMatched(String userId) {
        DatabaseReference currentUserConnections = userDb.child(currentUid).child("Connections").child("yups").child(userId);
        currentUserConnections.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    setMatchUserId(userId);
                    rippleBackground.stopRippleAnimation();
                    showMatchLayout();
//                    mMatchScreenLayout.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(), "new connection", Toast.LENGTH_SHORT).show();
                    userDb.child(dataSnapshot.getKey()).child("Connections").child("Matches").child(currentUid).setValue(true);
                    userDb.child(currentUid).child("Connections").child("Matches").child(dataSnapshot.getKey()).setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String userSex, oppositeSex;

    public void checkUserSex() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mDatabase = userDb.child(user.getUid());
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("Gender").getValue() != null) {
                        userSex = snapshot.child("Gender").getValue().toString();
                    }
                    if (userSex.equals("Male"))
                        oppositeSex = "Female";
                    else
                        oppositeSex = "Male";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getOppositeSexUsers() {
        rowItems.clear();
        userDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("Name"))
                    if (!dataSnapshot.child("Connections").child("nopes").hasChild(currentUid) &&
                            !dataSnapshot.child("Connections").child("yups").hasChild(currentUid) &&
                            dataSnapshot.child("Gender").getValue().toString().equals(oppositeSex) &&
                            dataSnapshot.child("status").getValue().toString().equals("online") &&
                            (Integer.parseInt(dataSnapshot.child("Age").getValue().toString()) >= minAgePref) &&
                            (Integer.parseInt(dataSnapshot.child("Age").getValue().toString()) <= maxAgePref)) {
                        String profileImage = "default";
                        if (!dataSnapshot.child("profileImageUrl").getValue().equals("default")) {
                            profileImage = dataSnapshot.child("profileImageUrl").getValue().toString();
                        }
                        Cards item = new Cards(dataSnapshot.getKey(),
                                Objects.requireNonNull(dataSnapshot.child("Name").getValue()).toString(),
                                Objects.requireNonNull(dataSnapshot.child("Age").getValue()).toString(),
                                profileImage);
                        rowItems.add(item);
                        if (rowItems.size() > 0) {
                            rippleBackground.stopRippleAnimation();
                            layoutChange(true);
                        }
                        arrayAdapter.notifyDataSetChanged();
                    }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("Name"))
                    if (dataSnapshot.child("status").getValue().equals("offline")) {
                        String profileImage = "default";
                        if (!dataSnapshot.child("profileImageUrl").getValue().equals("default")) {
                            profileImage = dataSnapshot.child("profileImageUrl").getValue().toString();
                        }
                        String sDisplayName = dataSnapshot.child("Name").getValue().toString();
                        Cards item = new Cards(dataSnapshot.getKey(),
                                sDisplayName,
                                Objects.requireNonNull(dataSnapshot.child("Age").getValue()).toString(),
                                profileImage);

                        for (Iterator<Cards> iter = rowItems.iterator(); iter.hasNext(); ) {
                            Cards items = iter.next();
                            if (items.getUserId().equals(item.getUserId()) && !(rowItems.get(0).getUserId().equals(item.getUserId()))) {
                                iter.remove();
                            }
                        }
                    }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void stopSession(Boolean sessionIndc) {
        if (sessionIndc) {
            if (rippleBackground != null && mPauseSessionBg != null) {
                rippleBackground.stopRippleAnimation();
                mPauseSessionBg.setVisibility(View.VISIBLE);
                mPauseSessionBg.startAnimation(animSlideInRight);
            }
        } else {
            if (rippleBackground != null && mPauseSessionBg != null) {
                rippleBackground.startRippleAnimation();
                mPauseSessionBg.setVisibility(View.GONE);
                mPauseSessionBg.startAnimation(animSlideOutRight);
            }
        }
    }

    public String getMatchUserId() {
        return matchUserId;
    }

    public void setMatchUserId(String matchUserId) {
        this.matchUserId = matchUserId;
    }

    public void layoutChange(Boolean visibility) {
        if (visibility) {
            mLikeButton.setVisibility(View.VISIBLE);
            mDislikeButton.setVisibility(View.VISIBLE);
            mSearchingLL.setVisibility(View.GONE);
        } else {
            mLikeButton.setVisibility(View.GONE);
            mDislikeButton.setVisibility(View.GONE);
            mSearchingLL.setVisibility(View.VISIBLE);
        }
    }

    private void showMatchLayout() {
        mMatchScreenLayout.setVisibility(View.VISIBLE);
        mMatchScreenLayout.startAnimation(animFadeIn);
    }

    public void showDialogueBox(Context context, String title) {
        DialogInterface.OnClickListener dialogueClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (rowItems.size() > 0) {
                            Cards card = rowItems.get(0);
                            DatabaseReference mDataRef = FirebaseDatabase.getInstance().getReference();
                            mDataRef.child("Reported").child(card.getUserId()).setValue(true);
                            flingContainer.getTopCardListener().selectLeft();
                        Toast.makeText(getActivity(), "User Reported", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                    default:
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (rowItems.size() > 0)
        builder.setMessage(title).setPositiveButton("Yes", dialogueClickListener)
                .setNegativeButton("No", dialogueClickListener).show();

    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (animation == animFadeIn) {
            Log.d("ANIMATION FADEIN", "ANimation stopped");
        } else if (animation == animSlideInRight) {
            Log.d("ANIMATION SLIDE IN", "ANimation stopped");
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
