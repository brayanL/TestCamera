package com.example.brayan.testcamera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.example.brayan.testcamera.cameraModule.CameraPreview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class MainActivity extends AppCompatActivity implements Camera.PictureCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG="MENSAJE";
    private CameraPreview mPreview;
    private Camera mCamera;
    private Camera.PictureCallback mPicture;
    private final static int GRANTED_PERMISSION_CAMERA=1;
    private File imageFile;
    private String mCurrentPhotoPath;
    private Uri photoURI;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_preview);
        if(checkPermissions()==0){
            mCamera = getCameraInstance();


            mPreview = new CameraPreview(this, mCamera, this);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);

            final Button button = (Button) findViewById(R.id.button_capture_photo);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPictureTaken(null, mCamera);
                }
            });
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},GRANTED_PERMISSION_CAMERA);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

    }


    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        try{
            imageFile=createImageFile();
            if (imageFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.brayan.testcamera.fileprovider",
                        imageFile);
                Log.w(TAG, "onPictureTaken: "+photoURI);
                //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                //startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }catch (Exception e){
            Log.w(TAG, "onPictureTaken: ", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case GRANTED_PERMISSION_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Log.w(TAG, "onRequestPermissionsResult: DENIED");
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        try{
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES); //Path complete where are stored images
            //Long freeBytesInternal = new File(getContext().getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
            //Log.w(TAG, "Total Calculado: "+freeBytesInternal);

            imageFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpeg",         /* suffix */
                    storageDir      /* directory */
            );

            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = imageFile.getAbsolutePath();
            return imageFile;
        }
        catch (Exception ex){
            Log.e(TAG, "createImageFile: No se creo bien la imagen ",ex);
            return imageFile=null;
        }
    }

    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        }
        else {
            return null;
        }

        return mediaFile;
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            Log.e(TAG, "getCameraInstance: ",e);
            e.printStackTrace();
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public int checkPermissions(){
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);

    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            Log.w(TAG, "checkCameraHardware: Si");
            return true;
        } else {
            Log.w(TAG, "checkCameraHardware: No");
            return false;
        }
    }

}
