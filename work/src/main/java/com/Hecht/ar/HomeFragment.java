package com.Hecht.ar;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.Hecht.ar.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//Fragment subclass for HomeFragment
public class HomeFragment extends Fragment {

    CardView userCard, adminCard;
    ProgressDialog progressDialog;
    CloudAnchorFragment cloudAnchorFragment = new CloudAnchorFragment();
    UserFragment userFragment = new UserFragment();
    // the fragment initialization parameters,
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Empty constructor
    }

    /**
     * Constructor is called
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     * At the end it will return a new instance of fragment HomeFragment.
     */

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        userCard = v.findViewById(R.id.userCard);
        adminCard = v.findViewById(R.id.adminCard);

        userCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Begin the transaction
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                // Replace the contents of the container with the new fragment
                transaction.replace(R.id.fragment_container, userFragment);
                // Transaction will be remembered after it is committed
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        adminCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog();
            }
        });
        return v;
    }

    /**
     * CustomDialog window is used to take user credentials User/Admin.
     * when Button is pressed it will check the Database for credentials.
     * */

    private void customDialog() {

        Dialog dialog = new Dialog(getActivity());
        dialog.setCancelable(true);
        dialog.getWindow().setGravity(Gravity.CENTER);

        View view = getActivity().getLayoutInflater().inflate(R.layout.layout_admin_password, null);
        dialog.setContentView(view);

        EditText et_password = view.findViewById(R.id.et_password);
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        Button btn_confirm = view.findViewById(R.id.btn_confirm);

        dialog.show();


        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new ProgressDialog(getContext());
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Loading....");
                progressDialog.show();
                String pass = et_password.getText().toString().trim();

                if (pass.isEmpty()) {
                    et_password.setError("Password Required");
                    progressDialog.dismiss();
                    return;
                } else {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("Admin");

                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String saved_pass = snapshot.child("password").getValue().toString();
                            if (pass.equals(saved_pass)) {
                                dialog.dismiss();
                                progressDialog.dismiss();
                                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, cloudAnchorFragment);
                                transaction.addToBackStack(null);
                                transaction.commit();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(getContext(), "Wrong Password", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }

}