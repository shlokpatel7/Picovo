package com.example.shlokpatel.picovo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    Toolbar toolbarMain;
    FloatingActionButton addPhotoFab;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    String current_user_id;
    BottomNavigationView bottomNavigationView;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbarMain = findViewById(R.id.toolbar);
        setSupportActionBar(toolbarMain);
        getSupportActionBar().setTitle("Picovo");
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            bottomNavigationView = findViewById(R.id.navigation_view);
            bottomNavigationView.setOnNavigationItemSelectedListener(this);
            mAuth = FirebaseAuth.getInstance();
            firebaseFirestore = FirebaseFirestore.getInstance();
            addPhotoFab = findViewById(R.id.add_photo_fab);
            addPhotoFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, NewPostActivity.class);
                    startActivity(intent);
                }
            });
            getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.frame_layout, new HomeFragment()).commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // No user is signed in
            sendToLogin();
        } else {
            //user signed in
            current_user_id = mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            Intent loginIntent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(loginIntent);
                            finish();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }

    }

    private void sendToLogin() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
          /*  case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(intent);
                break;*/
            case R.id.action_logout:
                mAuth.signOut();
                sendToLogin();
                break;
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.frame_layout, new HomeFragment()).commit();
                break;
            case R.id.account:
                getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.frame_layout, new AccountFragment()).commit();
                break;
        }
        item.setCheckable(true);
        return true;
    }

}
