package com.cfd.asmi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import java.io.File;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startCamera(View view){
        startCameraWithPermissions();
    }

    private void startCameraWithPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }else{
            // open camera
            EasyImage.openChooserWithGallery(this, "Take/ Pick a photo", 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startCameraWithPermissions();
            }else{
                Toast.makeText(this, "Please allow camera for searching!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UCrop.REQUEST_CROP) {
            handleCropResult(data);
        }else {
            EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
                @Override
                public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                    //Some error handling
                }

                @Override
                public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                    //Handle the images
                    cropImage(Uri.fromFile(imageFile));
                    Log.d("image picked", imageFile.getAbsolutePath());
                }
            });
        }
    }

    private void cropImage(Uri uri){
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(80);
        options.setFreeStyleCropEnabled(true);
//        options.setHideBottomControls(true);
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), "croopedimage.jpg")));
        uCrop.start(this);
    }

    private void handleCropResult(Intent result){
        Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
        intent.setData(UCrop.getOutput(result));
        startActivity(intent);
    }
}
