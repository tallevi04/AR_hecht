package com.Hecht.ar;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.Hecht.ar.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Dialog extends AppCompatDialogFragment {

    private EditText etName;
    private EditText etUrl;
    private EditText etDesc;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    public String cloudAnchorId="";

    public Dialog(String cloudAnchorId) {
        this.cloudAnchorId=cloudAnchorId;
    }

    /**
     * Dialog box will open, it will ask for 3d model name
     * And it will also give you ability to launch url after it is Augmented in the real world
     */
    public android.app.Dialog onCreateDialog(Bundle saveInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Inflating layout for Dialog Box
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.example_dialog,null);
        etName = view.findViewById(R.id.ed_name);
        etUrl = view.findViewById(R.id.ed_url);
        etDesc=view.findViewById(R.id.ed_desc);
        builder.setView(view)
                .setTitle("Enter Name for 3D Model:")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    //onClick function on Dialog interface to open url
                    public void onClick(DialogInterface dialog, int which) {}
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = etName.getText().toString();
                        String url=etUrl.getText().toString();
                        // URL Logic for both Http and Https
                        if(url.substring(0,7)!="https://" && url.substring(0,6)!="http://") {
                            url="https://"+url;
                        }

                        String desc=etDesc.getText().toString();
                        rootNode = FirebaseDatabase.getInstance();
                        //All shared experience will be stored there
                        //change path name in case you want to name it something else
                        reference = rootNode.getReference("shared_cloud_anchors");
                        String finalUrl = url;
                        reference.child("next_short_code").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                String next_short_code = String.valueOf(task.getResult().getValue());
                                rootNode = FirebaseDatabase.getInstance();
                                reference = rootNode.getReference("user");
                                object3d object= new object3d(name,next_short_code,cloudAnchorId, finalUrl,desc);
                                reference.child("object_labeled").child(object.object_id).setValue(object);
                             }
                        });
                    }
                });
        return builder.create();
    }
}