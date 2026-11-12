package com.example.firebaseimageupload;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    Button btnCapture, btnUpload;
    ImageView imageView;
    Bitmap capturedBitmap;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        btnCapture = findViewById(R.id.capture);
        imageView = findViewById(R.id.image);
        btnUpload = findViewById(R.id.upload);
        storage = FirebaseStorage.getInstance();
        btnUpload.setEnabled(false);

        btnCapture.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(intent);
        });

        btnUpload.setOnClickListener(v -> {
            btnUpload.setEnabled(false);
            uploadImage();
        });
    }

    ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                Bundle extras = data.getExtras();
                                if (extras != null) {
                                    capturedBitmap = (Bitmap) extras.get("data");
                                    if (capturedBitmap != null) {
                                        imageView.setImageBitmap(capturedBitmap);
                                        btnUpload.setEnabled(true);
                                    }
                                }
                            }
                        } else {
                            btnUpload.setEnabled(capturedBitmap != null);
                        }
                    });

    void uploadImage() {
        if (capturedBitmap == null) {
            Toast.makeText(this, "Capture an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);

        byte[] imageData = baos.toByteArray();
        StorageReference imageRef = storage.getReference().child("images/myimage.jpg");

        imageRef.putBytes(imageData)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(this, "Image uploaded successfully!", Toast.LENGTH_LONG).show();
                    Log.d("MainActivity", "Image uploaded successfully");
                    btnUpload.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("MainActivity", "Upload failed", e);
                    btnUpload.setEnabled(true);
                });
    }
}