package com.Hecht.ar;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.Hecht.ar.R;
import com.Hecht.ar.helpers.FullScreenHelper;

/**
 * Main Fragment for Cloud Anchors
 * This is where the AR Session and the Cloud Anchors are managed.
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        Fragment frag = fm.findFragmentById(R.id.fragment_container);
        if (frag == null) {
            frag = new SplashFragment();

            fm.beginTransaction().add(R.id.fragment_container, frag).commit();
        }
    }

    //If the window gains/losses focus, it will be triggered
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }
}
