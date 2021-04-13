package com.example.android.messenger.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.example.android.messenger.Model.User;
import com.example.android.messenger.databinding.ActivitySetupProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SetupProfileActivity extends AppCompatActivity {

    ProgressDialog dialog;

    ActivitySetupProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Setup your profile...");
        dialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        binding.imageView.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 45);
        });

        binding.continueBtn.setOnClickListener(v -> {
            String name = binding.nameBox.getText().toString();

            if (name.isEmpty()) {
                binding.nameBox.setError("Please input your name first.");
                return;
            }

            dialog.show();
            if (selectedImage != null) {
                StorageReference reference = storage.getReference().child("Profiles").child(auth.getUid());
                reference.putFile(selectedImage).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        reference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            String uid = auth.getUid();
                            String phoneNumber = auth.getCurrentUser().getPhoneNumber();
                            String username = binding.nameBox.getText().toString();
                            dbSave(uid, username, phoneNumber, imageUrl);
                        });
                    }
                });
            }
            else {
                String uid = auth.getUid();
                String phoneNumber = auth.getCurrentUser().getPhoneNumber();
                dbSave(uid, name, phoneNumber, "No image");
            }
        });
    }

    public void dbSave(String uid, String username, String phoneNumber, String imageUrl) {
        User user = new User(uid, username, phoneNumber, imageUrl);
        database.getReference().child("users").child(uid).setValue(user).addOnSuccessListener(aVoid -> {
            dialog.dismiss();
            Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (data.getData() != null){
                binding.imageView.setImageURI(data.getData());
                selectedImage = data.getData();
            }
        }
    }
}