package com.example.thebechain.ViewHolders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.thebechain.R;

public class ProfileViewHolder extends RecyclerView.ViewHolder {
    public ImageView mFeedImage, mRemovePostIV;
    public CardView root;

    public ProfileViewHolder(@NonNull View itemView) {
        super(itemView);
        mRemovePostIV = itemView.findViewById(R.id.removeImageIV);
        mFeedImage = itemView.findViewById(R.id.profileImageView);
        root = itemView.findViewById(R.id.profileImgCard);
    }

    public void setFeedImage(String imageUrl, Context context) {
        Glide.with(context).load(imageUrl).into(mFeedImage);
    }
}
