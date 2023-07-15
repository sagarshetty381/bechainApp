package com.example.thebechain.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.example.thebechain.ViewHolders.ImageModel;

import java.util.List;

public class ImageAdapter extends PagerAdapter {
    private Context mContext;
    private List<ImageModel> mImageModel;

    public ImageAdapter(Context mContext, List<ImageModel> mImageModel) {
        this.mContext = mContext;
        this.mImageModel = mImageModel;
    }

    @Override
    public int getCount() {
        return mImageModel.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageView = new ImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        Glide.with(mContext).load(mImageModel.get(position).getImgUrl()).into(imageView);
        container.addView(imageView,0);
        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView) object);
    }
}
