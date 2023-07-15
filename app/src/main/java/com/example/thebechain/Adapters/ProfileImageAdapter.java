package com.example.thebechain.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.thebechain.Fragments.ProfileFragment;
import com.example.thebechain.R;

import java.util.List;

public class ProfileImageAdapter extends RecyclerView.Adapter<ProfileImageAdapter.MyViewHolder> {
    private List<String> imageUrlList;
    private Context context;
    public boolean editIndc;
    private View view;
    private MyViewHolder myViewHolder;


    public ProfileImageAdapter(List<String> imageUrlList, Context context) {
        this.imageUrlList = imageUrlList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_image_item,parent,false);
        myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        Glide.with(context).load(imageUrlList.get(position)).into(holder.mFeedImage);

        holder.mRemovePostIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Clicked",String.valueOf(position));
                ProfileFragment profileFragment = new ProfileFragment();
            }
        });
//        RemoveItem(editIndc);
//        if (editIndc){
//            myViewHolder.mRemovePostIV.setVisibility(View.VISIBLE);
//        }else{
//            myViewHolder.mRemovePostIV.setVisibility(View.GONE);
//        }
    }

    public void RemoveItem(Boolean Indc) {
        this.editIndc = Indc;
//        if (Indc){
//            myViewHolder.mRemovePostIV.setVisibility(View.VISIBLE);
//        }else{
//            myViewHolder.mRemovePostIV.setVisibility(View.GONE);
//        }
    }

    @Override
    public int getItemCount() {
        return this.imageUrlList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView mFeedImage, mRemovePostIV;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mRemovePostIV = itemView.findViewById(R.id.removeImageIV);
            mFeedImage = itemView.findViewById(R.id.profileImageView);
        }
    }
}
