package com.example.thebechain.ViewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thebechain.R;

public class MatchesViewHolders extends RecyclerView.ViewHolder{
    public TextView mMatchName;
    public LinearLayout mChatItemLLayout;
    public ImageView mMatchImage;

    public MatchesViewHolders(@NonNull View itemView) {
        super(itemView);

        mMatchName = itemView.findViewById(R.id.matchName);
        mMatchImage = itemView.findViewById(R.id.matchesImage);
        mChatItemLLayout = itemView.findViewById(R.id.chatItemLL);
    }
}

