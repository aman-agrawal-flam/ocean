/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

 package com.flam.fit.app.demo.tracking.featuretracker.android;

 import com.meta.ocean.base.BaseJni;
 import com.meta.ocean.platform.android.*;
 import com.meta.ocean.platform.android.application.*;
 import android.os.Bundle;
 import android.view.ViewGroup;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import android.util.Log;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import android.os.Handler;
 import android.os.Looper;
 import java.util.Arrays;
 import java.util.List;
 import android.widget.VideoView;
 import android.net.Uri;
 
 /**
  * This class implements the main Activity object for the Feature Tracker (Android).
  * @ingroup applicationdemotrackingfeaturetrackerandroid
  */
 public class FeatureTrackerActivity extends GLFrameViewActivity {
	 static {
		 System.loadLibrary("FeatureImageTracking");
	 }
 
	 private static final String TAG = "FeatureTrackerActivity";
	 private static final String ASSET_URL_BASE = "https://storage.googleapis.com/avatar-system/test/assets/";
 
	 private Handler handler = new Handler(Looper.getMainLooper());
	 private VideoView videoView;
 
	 private Runnable logBoundingBoxEdgesTask = new Runnable() {
		 @Override
		 public void run() {
			 // Log the bounding box edges
			 Log.i(TAG, "Aman Bounding box edges are detected " + boundingBoxEdges());
			 if (boundingBoxEdges() != "Hello") {
			
				playVideo(getFilesDir().getAbsolutePath() + "/samsung-masked_kl4BCZJH.mp4");
 	
			 }
			 // Schedule this task again in the near future (e.g., 1000ms later)
			 handler.postDelayed(this, 1000);
		 }
	 };
 
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
		 messageOutput_ = BaseJni.MessageOutput.OUTPUT_QUEUED.value();
		 super.onCreate(savedInstanceState);
 
		 videoView = new VideoView(this);
		 addContentView(videoView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		 addContentView(new MessengerView(this, true), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200));
		 final String assetDir = getFilesDir().getAbsolutePath() + "/";
		 downloadAssets();
		 Assets.copyFiles(getAssets(), assetDir + "/", true);
	 }
 
	 @Override
	 public void onStart() {
		 super.onStart();
		 // Start the repeating task
		 handler.post(logBoundingBoxEdgesTask);
	 }
 
	 @Override
	 public void onStop() {
		 super.onStop();
		 // Stop the repeating task
		 handler.removeCallbacks(logBoundingBoxEdgesTask);
	 }
 
	 private void downloadAssets() {
		 final String assetDir = getFilesDir().getAbsolutePath() + "/";
		 List<String> files = Arrays.asList("testcinema.ox3dv", "cinema.jpeg", "trex-attribution.txt", "trex.mtl", "trex.obj", "trex.png", "samsung-masked_kl4BCZJH.mp4");
		 for (String file : files) {
			 String fileURL = ASSET_URL_BASE + file;
			 downloadFile(fileURL, assetDir, file);
		 }
		 Assets.copyFiles(getAssets(), assetDir, true);
	 }
 
	 private void downloadFile(final String fileURL, final String dirPath, final String fileName) {
		 ExecutorService executor = Executors.newSingleThreadExecutor();
		 Handler handler = new Handler(Looper.getMainLooper());
 
		 executor.execute(() -> {
			 String result = performDownload(fileURL, dirPath, fileName);
			 handler.post(() -> {
				 // Update UI with result, e.g., display a message or update a view
				 if (result != null) {
					 Log.d(TAG, "Download successful: " + result);
				 } else {
					 Log.e(TAG, "Download failed");
				 }
			 });
		 });
	 }
 
	 private String performDownload(String fileURL, String dirPath, String fileName) {
		 InputStream input = null;
		 OutputStream output = null;
		 HttpURLConnection urlConnection = null;
		 try {
			 File dir = new File(dirPath);
			 if (!dir.exists() && !dir.mkdirs()) {
				 Log.e(TAG, "Failed to create directory: " + dirPath);
				 return null;
			 }
 
			 URL url = new URL(fileURL);
			 urlConnection = (HttpURLConnection) url.openConnection();
			 urlConnection.setRequestMethod("GET");
			 urlConnection.connect();
 
			 File file = new File(dir, fileName);
			 output = new FileOutputStream(file);
 
			 input = urlConnection.getInputStream();
			 byte[] buffer = new byte[4096];
			 int byteCount;
			 while ((byteCount = input.read(buffer)) != -1) {
				 output.write(buffer, 0, byteCount);
			 }
 
			 return file.getAbsolutePath();
		 } catch (Exception e) {
			 Log.e(TAG, "Error downloading file: " + e.getMessage(), e);
			 return null;
		 } finally {
			 try {
				 if (input != null) input.close();
				 if (output != null) output.close();
			 } catch (Exception e) {
				 Log.e(TAG, "Error closing streams: " + e.getMessage(), e);
			 }
			 if (urlConnection != null) urlConnection.disconnect();
		 }
	 }
 
	 @Override
	 protected void onCameraPermissionGranted() {
		 final String assetDir = getFilesDir().getAbsolutePath() + "/";
		 boolean result = initializeFeatureTracker("LiveVideoId:0", assetDir + "cinema.jpeg", "960x540");
		 // Print bounding box edges in logs
		 Log.d(TAG, "Aman Feature Tracker initialized" + result);
	 }
 
	 /**
	  * Java native interface function to set or change the view's background media object.
	  * @param inputMedium The URL of the input medium (e.g., "LiveVideoId:0")
	  * @param pattern The filename of the pattern to be used for tracking
	  * @param resolution The resolution of the input medium (e.g., "640x480", "1280x720", "1920x1080")
	  * @return True, if succeeded
	  */
	 public static native boolean initializeFeatureTracker(String inputMedium, String pattern, String resolution);
	 public static native String boundingBoxEdges();
 
	 private void playVideo(String videoPath) {
		 Uri videoUri = Uri.parse(videoPath);
		 videoView.setVideoURI(videoUri);
		//  videoView.start();
	 }
 }
 