package com.example.videostreamer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

import android.R.id;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
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
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;

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
        //initCameraForPics();
        intCameraForVideo();
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
    
    private boolean prepareVideoRecorder(){

       // mCamera = getCameraInstance();
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(getOutputMediaFile(0).toString());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }
    
    
    
    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
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
    
    private void intCameraForVideo(){
    	

    	// Add a listener to the Capture button
    	final Button captureButton = (Button) findViewById(R.id.button_capture);
    	captureButton.setOnClickListener(
    	    new View.OnClickListener() {
    	        @Override
    	        public void onClick(View v) {
    	            if (isRecording) {
    	                // stop recording and release camera
    	                mMediaRecorder.stop();  // stop the recording
    	                releaseMediaRecorder(); // release the MediaRecorder object
    	                mCamera.lock();         // take camera access back from MediaRecorder

    	                // inform the user that recording has stopped
    	                captureButton.setText("Capture");
    	                isRecording = false;
    	            } else {
    	                // initialize video camera
    	                if (prepareVideoRecorder()) {
    	                    // Camera is available and unlocked, MediaRecorder is prepared,
    	                    // now you can start recording
    	                    mMediaRecorder.start();

    	                    // inform the user that recording has started
    	                    captureButton.setText("Stop!!");
    	                    isRecording = true;
    	                } else {
    	                    // prepare didn't work, release the camera
    	                    releaseMediaRecorder();
    	                    // inform user
    	                }
    	            }
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
