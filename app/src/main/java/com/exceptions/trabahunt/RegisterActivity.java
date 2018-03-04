package com.exceptions.trabahunt;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {
    private Button mCreateBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference mRootRef;
    private RadioButton mSeekerRb;
    private RadioButton mGiverRb;
    private String type;
    private EditText mPhone;
    private ImageView mPhoto;
    private StorageReference mImageStorage;
    private static final int GALLERY_PICK = 1;
    private FirebaseUser current_user;
    private String uid;
    private String download_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mCreateBtn = findViewById(R.id.reg_create_btn);
        mSeekerRb = findViewById(R.id.SeekerRb);
        mGiverRb = findViewById(R.id.GiverRb);
        mPhone = findViewById(R.id.mobileET);
        mPhoto = findViewById(R.id.photoView);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        current_user = FirebaseAuth.getInstance().getCurrentUser();
        uid = current_user.getUid();
        mImageStorage = FirebaseStorage.getInstance().getReference();
        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSeekerRb.isChecked())
                    type = "Seeker";
                if (mGiverRb.isChecked())
                    type = "Giver";

                if (type == "Seeker") {
                    HashMap<String, String> seekMap = new HashMap<>();
                    seekMap.put(uid, String.valueOf(mPhone.getText()));
                    mRootRef.child("Seekers").setValue(seekMap);
                }
                if (type == "Giver") {
                    HashMap<String, String> giveMap = new HashMap<>();
                    giveMap.put(uid, String.valueOf(mPhone.getText()));
                    mRootRef.child("Givers").setValue(giveMap);
                }

                register_user(type);


            }
        });
    }

    private void register_user(String type) {

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        HashMap<String, String> photoMap = new HashMap<>();
        photoMap.put("Photo", download_url);


        photoMap.put("Photo", download_url);

        mRootRef.child("Images").child(uid).setValue(photoMap);

        mDatabase.child("Type").setValue(type).addOnCompleteListener(new OnCompleteListener<Void>(){
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Intent mainIntent = new Intent(RegisterActivity.this, JobPostingActivity.class);
                    startActivity(mainIntent);
                }

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            Uri imageUri = data.getData();

            DatabaseReference user_pic = mRootRef.child("Users")
                    .child(uid).child("Photo").push();

            final String push_id = user_pic.getKey();

            StorageReference filepath = mImageStorage.child("profile_images").child(push_id + ".jpg");
            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        download_url = task.getResult().getDownloadUrl().toString();
                        Picasso.with(mPhoto.getContext()).load(download_url).into(mPhoto);
                    }

                }
            });

        }

    }


}



