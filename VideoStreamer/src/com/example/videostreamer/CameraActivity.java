package com.example.videostreamer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

import android.R.id;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class CameraActivity extends Activity {

    protected static final String TAG = "No file found";
	private Camera mCamera;
    private CameraPreview mPreview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        initCameraForPics();
    }
    
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
    
    private void initCameraForPics(){
    		final PictureCallback mPicture = new PictureCallback() {

    	    @Override
    	    public void onPictureTaken(byte[] data, Camera camera) {

    	        File pictureFile = getOutputMediaFile(1);
    	        if (pictureFile == null){
    	            Log.d(TAG, "Error creating media file, check storage permissions: ");
    	            return;
    	        }

    	        try {
    	            FileOutputStream fos = new FileOutputStream(pictureFile);
    	            fos.write(data);
    	            fos.close();
    	        } catch (FileNotFoundException e) {
    	            Log.d(TAG, "File not found: " + e.getMessage());
    	        } catch (IOException e) {
    	            Log.d(TAG, "Error accessing file: " + e.getMessage());
    	        }
    	    }
    	};
    	
    	
    	// Add a listener to the Capture button
    	Button captureButton = (Button) findViewById(R.id.button_capture);
    	captureButton.setOnClickListener(
    	    new View.OnClickListener() {
    	        @Override
    	        public void onClick(View v) {
    	            // get an image from the camera
    	            mCamera.takePicture(null, null, mPicture);
    	        }
    	    }
    	);
    	
    }

    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
    	
    	if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
    		Log.d("MyCameraApp", "Card is mounted!!!!!");
    	}else{
    		Log.d("MyCameraApp", "Card not mounted");
    	}

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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(0));
        File mediaFile;
        if (type == 1){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "IMG_"+ timeStamp + ".jpg");
        } else if(type == 0) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
}
