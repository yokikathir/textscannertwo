package com.ppms.textscannertwo;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;
import com.ppms.textscannertwo.Camera.FileCompressor;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    public static final int MULTIPLE_PERMISSIONS = 10;
    private static final int REQUEST_CONTACTS = 1;
    static String ServerKey = null;
    private static String[] PERMISSIONS_CONTACT = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,


    };
    private Bitmap myBitmap;
    private ImageView myImageView;
    private TextView myTextView;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    File mPhotoFile;
    FileCompressor mCompressor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!checkPermissions()) {
            isRuntimePermission();
        }
        mCompressor = new FileCompressor(this);
        myTextView = findViewById(R.id.textView);
        myImageView = findViewById(R.id.imageView);
        findViewById(R.id.checkText).setOnClickListener(this);
        findViewById(R.id.camera).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.checkText:
                if (myBitmap != null) {
                    runTextRecog();
                }
                break;
            case R.id.camera:
                dispatchTakePictureIntent();
               /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!isRuntimePermission()) {
                        requestCameraPermission();
                    } else {
                        dispatchTakePictureIntent();

                    }

                } else {
                    dispatchTakePictureIntent();
                }*/

                break;
        }
    }
/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case WRITE_STORAGE:
                    checkPermission(requestCode);
                    break;
                case SELECT_PHOTO:
                    Uri dataUri = data.getData();
                    String path = MyHelper.getPath(this, dataUri);
                    if (path == null) {
                        myBitmap = MyHelper.resizePhoto(photo, this, dataUri, myImageView);
                    } else {
                        myBitmap = MyHelper.resizePhoto(photo, path, myImageView);
                    }
                    if (myBitmap != null) {
                        myTextView.setText(null);
                        myImageView.setImageBitmap(myBitmap);
                    }
                    break;

            }
        }
    }*/
private void dispatchTakePictureIntent() {
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            ex.printStackTrace();
            // Error occurred while creating the File
        }
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);

            mPhotoFile = photoFile;
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }
}


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                try {
                    mPhotoFile = mCompressor.compressToFile(mPhotoFile);
                    previewCapturedemployee(mPhotoFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }
    private void previewCapturedemployee(final File file) {

        try {

            String filePath = file.getPath();
            myBitmap = BitmapFactory.decodeFile(filePath);
            Log.e("mybitmap", ":" + myBitmap);


            try {

                    if (myBitmap != null) {
                        myTextView.setText(null);
                        myImageView.setImageBitmap(myBitmap);
                    }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG)
                        .show();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runTextRecog() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(myBitmap);
        FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();
        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText texts) {
                processExtractedText(texts);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure
                    (@NonNull Exception exception) {
                Toast.makeText(MainActivity.this,"Exception", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void processExtractedText(FirebaseVisionText firebaseVisionText) {
        myTextView.setText(null);
        if (firebaseVisionText.getBlocks().size() == 0) {
            myTextView.setText(R.string.no_text);
            return;

        }
        for (FirebaseVisionText.Block block : firebaseVisionText.getBlocks()) {
            myTextView.append(block.getText());

        }
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new java.util.Date());
        String mFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File mFile = File.createTempFile(mFileName, ".jpg", storageDir);
        return mFile;
    }
    private boolean isRuntimePermission() {

        boolean value = true;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {

            return true;
        }
    }

    private void requestCameraPermission() {
        Log.i("TAG", "CAMERA permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(camera_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i("TAG",
                    "Displaying camera permission rationale to provide additional context.");

            ActivityCompat
                    .requestPermissions(MainActivity.this, PERMISSIONS_CONTACT,
                            REQUEST_CONTACTS);

        } else {

            // Camera permission has not been granted yet. Request it directly.


            ActivityCompat.requestPermissions(this, PERMISSIONS_CONTACT, REQUEST_CONTACTS);
        }
        // END_INCLUDE(camera_permission_request)
    }
    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

}