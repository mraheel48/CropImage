package com.example.cropimage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    final int Camera_Capture = 1;
    final int PIC_CROP = 1010;
    private Uri picUri;

    final String fileProvider = "com.example.cropimage.filters.provider";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Toast.makeText(MainActivity.this, "Calling camera", Toast.LENGTH_SHORT).show();

                /*Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(captureImage, Camera_Capture);*/
                pickGalleryImage();

            }
        });
    }


    private void pickGalleryImage() {
        try {
            Intent filePicker = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (filePicker.resolveActivity(getPackageManager()) != null){
                startActivityForResult(filePicker,Camera_Capture);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Intent filePicker = new Intent();
            filePicker.setType("image/*");
            filePicker.setAction(Intent.ACTION_GET_CONTENT);
            this.startActivityForResult(filePicker,Camera_Capture);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == Camera_Capture) {
            Toast.makeText(this, "image is find", Toast.LENGTH_SHORT).show();
            if (data != null) {

                Uri uri = data.getData();

                String path = getPathFromUri(uri);
                Log.d("myTag",String.valueOf(path));

                File newFile = new File(path);
                if (Build.VERSION.SDK_INT >= 24) {
                    picUri = FileProvider.getUriForFile(MainActivity.this, fileProvider, newFile);
                    performCrop();
                }

            }
        }

    }

    private String getPathFromUri(Uri uri) {
        String[] filePathColumn = {MediaStore.Files.FileColumns.DATA};
        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String Path = cursor.getString(columnIndex);
        cursor.close();
        return Path;
    }

    private void performCrop() {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            // set crop properties here
            cropIntent.putExtra("crop", true);
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 128);
            cropIntent.putExtra("outputY", 128);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);

        } catch (ActivityNotFoundException ex) {
            ex.printStackTrace();
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}