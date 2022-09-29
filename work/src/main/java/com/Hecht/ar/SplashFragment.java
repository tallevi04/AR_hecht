package com.Hecht.ar;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.Hecht.ar.R;

//SplashFragment Subclass of Fragment
public class SplashFragment extends Fragment {

    ImageView logo;
    HomeFragment homeFragment=new HomeFragment();

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public SplashFragment() {
        // constructor
    }

    //Calling Constructor method SplashFragment and passing parameters
    public static SplashFragment newInstance(String param1, String param2) {
        SplashFragment fragment = new SplashFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    //The onCreate method is called when the Fragment instance is being created, or re-created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    // Inflate the layout for this fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_splash, container, false);

        logo=v.findViewById(R.id.logo);

        Animation fadeIn=new AlphaAnimation(0,1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(2000);

        logo.startAnimation(fadeIn);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //allows us to add fragments to the FrameLayout at runtime.
                FragmentTransaction transaction=getActivity().getSupportFragmentManager().beginTransaction();

                transaction.addToBackStack(null); //transaction can be rolled back.

                transaction.replace(R.id.fragment_container,homeFragment);
                transaction.commit();
            }
        },3000);
        return v;
    }
}