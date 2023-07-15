package com.example.thebechain.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.thebechain.CallBackMatchCount;
import com.example.thebechain.EditProfileActivity;
import com.example.thebechain.ImagePickerActivity;
import com.example.thebechain.R;
import com.example.thebechain.SettingsActivity;
import com.example.thebechain.ShowProfileActivity;
import com.example.thebechain.ViewHolders.ImageModel;
import com.example.thebechain.ViewHolders.ProfileViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {
    private TextView mUserName,mBioTV;
    private CircleImageView mProfileImage;
    private ImageView mEditProfile, editImageBt, mSettingsIV;
    private ProgressBar mProfilePicLoad;
    private LinearLayout mProgressLayout, mProgressContainer,mCompleteProfileErrorText, mAddPhotosLL;

    private String userId, profileImageUrl;
    private int EDIT_PROFILE = 1;
    private boolean bAddPost = true;

    private DatabaseReference mUserDb;
    private RecyclerView mPhotosRV;
    private RecyclerView.LayoutManager mLayoutManager;
    private FirebaseRecyclerAdapter adapter;
    private Uri uri;
    View layoutView;
    LocalDate today;
    SharedPreferences sharedPreferences;

    public static final int REQUEST_IMAGE = 100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        layoutView = inflater.inflate(R.layout.fragment_profile, container, false);

        mUserName = layoutView.findViewById(R.id.tvName);
        mAddPhotosLL = layoutView.findViewById(R.id.editPhotosLayout);
        mProfileImage = layoutView.findViewById(R.id.profileImage);
        editImageBt = layoutView.findViewById(R.id.editImage);
        mEditProfile = layoutView.findViewById(R.id.editProfileIV);
        mProfilePicLoad = layoutView.findViewById(R.id.profileLoadPB);
        mSettingsIV = layoutView.findViewById(R.id.settingsIV);
        mCompleteProfileErrorText = layoutView.findViewById(R.id.complete_profile_layout);
        mBioTV = layoutView.findViewById(R.id.bioText);
        mProgressLayout = layoutView.findViewById(R.id.white_progressbar);
        mProgressContainer = layoutView.findViewById(R.id.progressContainer);

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        }

        //showProgressBar(true);

        mUserDb = FirebaseDatabase.getInstance().getReference();
        getUserInfo();

        mPhotosRV = layoutView.findViewById(R.id.photosRV);

        mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mPhotosRV.setLayoutManager(mLayoutManager);
        mPhotosRV.setHasFixedSize(true);
        fetch();

        mSettingsIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 startActivity(new Intent(getActivity(), SettingsActivity.class));
            }
        });

        CheckProfileCompletion();
        mCompleteProfileErrorText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), EditProfileActivity.class));
            }
        });

        mEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), EditProfileActivity.class));
            }
        });

        mAddPhotosLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bAddPost = true;
                showImagePickerOptions();
            }
        });

        editImageBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bAddPost = false;
                Dexter.withActivity(getActivity())
                        .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                if (report.areAllPermissionsGranted()) {
                                    showImagePickerOptions();
                                }

                                if (report.isAnyPermissionPermanentlyDenied()) {
                                    showSettingsDialog();
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                            }
                        }).check();
            }
        });
        return layoutView;
    }

    private void CheckProfileCompletion() {
        mUserDb.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("Profession") && snapshot.hasChild("Bio") && snapshot.hasChild("Hobbies"))
                    mCompleteProfileErrorText.setVisibility(View.GONE);
                else
                    mCompleteProfileErrorText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetch() {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(userId)
                .child("FeedImages");

        FirebaseRecyclerOptions<ImageModel> options =
                new FirebaseRecyclerOptions.Builder<ImageModel>()
                        .setQuery(query, new SnapshotParser<ImageModel>() {
                            @NonNull
                            @Override
                            public ImageModel parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new ImageModel(snapshot.child("id").getValue().toString(),
                                        snapshot.child("imgUrl").getValue().toString());
                            }
                        })
                        .build();

        adapter = new FirebaseRecyclerAdapter<ImageModel, ProfileViewHolder>(options) {
            @Override
            public ProfileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.profile_image_item, parent, false);

                return new ProfileViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(ProfileViewHolder holder, final int position, final ImageModel model) {
                holder.setFeedImage(model.getImgUrl(), getActivity());

                holder.mRemovePostIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (getItemCount() > 1)
                            mUserDb.child("Users").child(userId).child("FeedImages").child(model.getKeyId()).removeValue();
                        else
                            Toast.makeText(getContext(),"Need to have atleast one photo",Toast.LENGTH_SHORT).show();
                    }
                });

                holder.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(getActivity(), ShowProfileActivity.class).putExtra("userID",userId)
                        .putExtra("name","self"));

                    }
                });
            }

        };
        mPhotosRV.setAdapter(adapter);
    }

    public void showProgressBar(Boolean opened){
        if (opened) {
            mProgressLayout.setVisibility(View.VISIBLE);
            TranslateAnimation animate = new TranslateAnimation(
                    0,
                    0,
                    500,
                    0);
            animate.setDuration(500);
            animate.setFillAfter(true);
            mProgressLayout.startAnimation(animate);
        }else{
            
            TranslateAnimation animate = new TranslateAnimation(
                    0,
                    0,
                    0,
                    500);
            animate.setDuration(500);
            animate.setFillAfter(true);
            animate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mProgressLayout.clearAnimation();
                    mProgressContainer.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mProgressLayout.startAnimation(animate);
        }
    }

    private void getUserInfo() {
        mUserDb.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    assert map != null;
                    if (map.get("Name") != null && map.get("Age") != null) {
                        mUserName.setText((Objects.requireNonNull(map.get("Name")).toString()
                                + ", " + map.get("Age")));
                    }

                    if (map.get("Bio") != null)
                        mBioTV.setText(map.get("Bio").toString());
                    else
                        mBioTV.setText(R.string.openToConnection);

                    if (map.get("profileImageUrl") != null) {
                        profileImageUrl = map.get("profileImageUrl").toString();
                        switch (profileImageUrl) {
                            case "default":
                                Glide.with(getActivity()).load(R.mipmap.ic_launcher).into(mProfileImage);
                                break;
                            default:
                                Glide.with(getActivity()).load(profileImageUrl).into(mProfileImage);
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveUserInformation() {
        if (uri != null) {
            showProgressBar(true);
            StorageReference filePath;
            if (bAddPost) {
                filePath = FirebaseStorage.getInstance().getReference().child("profileImage").child(UUID.randomUUID().toString());
            } else {
                filePath = FirebaseStorage.getInstance().getReference().child("profileImage").child(userId);
            }
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            assert bitmap != null;
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getActivity(), "success1", Toast.LENGTH_SHORT).show();
                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("ProfileUrl", uri.toString());
                            Map<String, Object> map = new HashMap<>();
                            if (bAddPost) {
                                DatabaseReference mAddPostDb = mUserDb.child("Users").child(userId).child("FeedImages").push();
                                map.put("id", mAddPostDb.getKey());
                                map.put("imgUrl", uri.toString());
                                mAddPostDb.setValue(map);
                            } else {
                                map.put("profileImageUrl", uri.toString());
                                mUserDb.child("Users").child(userId).updateChildren(map);
                                mProfilePicLoad.setVisibility(View.GONE);
                                loadProfile(uri.toString());
                            }
                        }
                    });

                    Toast.makeText(getActivity(), "success2", Toast.LENGTH_SHORT).show();
                    showProgressBar(false);
                    return;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), "fail1", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(getActivity(), "uri nullllll", Toast.LENGTH_LONG).show();
        }
    }

    private void loadProfile(String url) {
        Glide.with(this).load(url)
                .into(mProfileImage);
    }

    private void showImagePickerOptions() {
        ImagePickerActivity.showImagePickerOptions(getActivity(), new ImagePickerActivity.PickerOptionListener() {
            @Override
            public void onTakeCameraSelected() {
                launchCameraIntent();
            }

            @Override
            public void onChooseGallerySelected() {
                launchGalleryIntent();
            }
        });
    }

    private void launchCameraIntent() {
        Intent intent = new Intent(getActivity(), ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);

        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void launchGalleryIntent() {
        Intent intent = new Intent(getActivity(), ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                uri = data.getParcelableExtra("path");
                try {
                    // You can update this bitmap to your server
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);

                    // loading profile image from local cache
                    if (!bAddPost) {
                        mProfilePicLoad.setVisibility(View.VISIBLE);
                    }
                    saveUserInformation();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_permission_title));
        builder.setMessage(getString(R.string.dialog_permission_message));
        builder.setPositiveButton(getString(R.string.go_to_settings),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        openSettings();
                    }
                });
        builder.setNegativeButton(getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        openSettings();
                    }
                });
        builder.show();

    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
