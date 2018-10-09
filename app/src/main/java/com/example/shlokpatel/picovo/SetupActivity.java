package com.example.shlokpatel.picovo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    Toolbar toolbarMain;
    CircleImageView circleImageView;
    TextInputEditText name_edit_text;
    Button submitBtn;
    Uri resultUri = null;
    private boolean isChanged;
    private StorageReference mStorageRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        firebaseAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        toolbarMain = findViewById(R.id.toolbar_setup);
        setSupportActionBar(toolbarMain);
        circleImageView = findViewById(R.id.user_image);
        name_edit_text = findViewById(R.id.name_edit);
        submitBtn = findViewById(R.id.submit_setup);
        submitBtn.setEnabled(false);
        userId = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        resultUri = Uri.parse((String) task.getResult().get("image"));
                        name_edit_text.setText(task.getResult().getString("name"));
                        Glide.with(SetupActivity.this).load(task.getResult().getString("image")).into(circleImageView);
                    }

                } else {
                    Toast.makeText(SetupActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                submitBtn.setEnabled(true);
            }
        });

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(SetupActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        imagePicker();
                    }
                } else {
                    imagePicker();
                }
            }

            private void imagePicker() {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(SetupActivity.this);
            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userName = name_edit_text.getText().toString();
                if (!TextUtils.isEmpty(userName) && resultUri != null) {
                    if (isChanged) {
                        userId = firebaseAuth.getCurrentUser().getUid();
                        StorageReference imageStoragePath = mStorageRef.child("user_profile_images").child(userId + ".jpeg");
                        imageStoragePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    storeFirestore(task, userName);
                                } else {
                                    Toast.makeText(SetupActivity.this, "" + task.getException()
                                            .getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else{
                        storeFirestore(null,userName);
                    }
                }
            }
        });
    }

    private void storeFirestore(Task<UploadTask.TaskSnapshot> task, String userName) {
        Uri downloadUri;
        if (task != null) {
            downloadUri = task.getResult().getDownloadUrl();
        } else {
            downloadUri = resultUri;
        }
        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", userName);
        userMap.put("image", downloadUri.toString());
        firebaseFirestore.collection("Users").document(userId)
                .set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SetupActivity.this,
                            "User settings are updated", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SetupActivity.this,
                            "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                circleImageView.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


}
