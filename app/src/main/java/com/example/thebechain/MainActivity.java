package com.example.thebechain;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.text.Html;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thebechain.Fragments.GetDetailsFragment;
import com.example.thebechain.Fragments.GetPhotoFragment;

import java.util.ArrayList;
import java.util.List;

import ivb.com.materialstepper.progressMobileStepper;

public class MainActivity extends AppCompatActivity implements CallbackFragment {

    Fragment mFragment;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    public LinearLayout dotsLayout;
    public TextView[] dots;
    public int position = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

        dotsLayout = findViewById(R.id.layoutDots1);
        addBottomDots(1);
        addFragment();
    }

    private void addBottomDots(int currentPage) {
        position = position + currentPage;
        dots = new TextView[2];

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(55);
            dots[i].setTextColor(getResources().getColor(R.color.colorBlue));
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0) {
            for (int i = 0; i <= position; i++) {
                dots[i].setTextColor(getResources().getColor(R.color.colorOrange));
            }
        }
    }

    private void addFragment() {
        Fragment mFragment = new GetDetailsFragment();
        ((GetDetailsFragment) mFragment).setCallbackFragment(this);
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragmentContainer,mFragment);
        fragmentTransaction.commit();
    }

    public void replaceFragment() {
        mFragment = new GetPhotoFragment();
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.fragmentContainer,mFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void changeFragment() {
        replaceFragment();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        addBottomDots(-1);
    }

    @Override
    public void addProgressDots() {
        addBottomDots(1);
    }
}