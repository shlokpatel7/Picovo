package com.example.shlokpatel.picovo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    TextInputEditText reg_emailEdit,reg_passEdit,pass_confirm;
    Button reg_loginBtn,reg_newAccntBtn;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        reg_emailEdit=findViewById(R.id.email_edit_register);
        reg_passEdit=findViewById(R.id.password_edit_register);
        reg_loginBtn=findViewById(R.id.already_accnt_btn);
        reg_newAccntBtn=findViewById(R.id.new_accnt_confirm);
        pass_confirm=findViewById(R.id.password_edit_register_confirm);
        reg_newAccntBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=reg_emailEdit.getText().toString();
                String password=reg_passEdit.getText().toString();
                String passwordConfirm=pass_confirm.getText().toString();
                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(passwordConfirm)){
                    if(password.equals(passwordConfirm)){
                        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Intent intent=new Intent(RegisterActivity.this,SetupActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    Toast.makeText(RegisterActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else{
                        Toast.makeText(RegisterActivity.this, "Password doesn't match", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        reg_loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // User is signed in
            sendToMain();
        } else {
            // No user is signed in
        }
    }

    private void sendToMain() {
        Intent loginIntent=new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(loginIntent);
        finish();
    }
}
