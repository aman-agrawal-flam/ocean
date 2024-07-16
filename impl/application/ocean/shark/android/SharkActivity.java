/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.meta.ocean.app.shark.android;

import com.meta.ocean.devices.android.DevicesAndroidJni;
import com.meta.ocean.devices.pattern.DevicesPatternJni;
import com.meta.ocean.media.openimagelibraries.MediaOpenImageLibrariesJni;
import com.meta.ocean.platform.android.*;
import com.meta.ocean.platform.android.application.*;
import com.meta.ocean.scenedescription.sdl.obj.SceneDescriptionSDLOBJJni;
import com.meta.ocean.scenedescription.sdx.x3d.SceneDescriptionSDXX3DJni;
import android.os.Bundle;
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

/**
 * This class implements the main Activity object for the Shark viewer.
 * @ingroup sharkandroid
 */
public class SharkActivity extends GLFrameViewActivity
{
	static
	{
		System.loadLibrary("OceanShark");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		MediaOpenImageLibrariesJni.registerLibrary();

		DevicesAndroidJni.registerLibrary();
		DevicesPatternJni.registerLibrary();

		SceneDescriptionSDLOBJJni.registerLibrary();
		SceneDescriptionSDXX3DJni.registerLibrary();

		final String assetDir = getExternalFilesDir(null) + "/";
		String fileURL = "https://storage.googleapis.com/avatar-system/test/assets/dinosaur.ox3dv";
    	String fileName = "dinosaur.ox3dv";

    	// Download the file
    	downloadFile(fileURL, assetDir, fileName);
		Assets.copyFiles(getAssets(), assetDir, true);
		NativeInterfaceShark.loadScene(assetDir + "dinosaur.ox3dv", true);
	}
	
	/* private void downloadFile(final String fileURL, final String dirPath, final String fileName) {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... voids) {
				InputStream input = null;
				OutputStream output = null;
				HttpURLConnection urlConnection = null;
				try {
					// Create directory if it doesn't exist
					File dir = new File(dirPath);
					if (!dir.exists()) {
						if (!dir.mkdirs()) {
							Log.e("SharkActivity", "Failed to create directory: " + dirPath);
							return null;
						}
					}

					// Set up the URL connection
					URL url = new URL(fileURL);
					urlConnection = (HttpURLConnection) url.openConnection();
					urlConnection.setRequestMethod("GET");
					urlConnection.connect();

					// Check for successful response code or throw error
					if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
						Log.e("SharkActivity", "Server returned HTTP " + urlConnection.getResponseCode()
								+ " " + urlConnection.getResponseMessage());
						return null;
					}

					// Create the file output stream
					File file = new File(dir, fileName);
					output = new FileOutputStream(file);

					// Read data from the URL connection input stream
					input = urlConnection.getInputStream();
					byte[] buffer = new byte[4096];
					int byteCount;
					while ((byteCount = input.read(buffer)) != -1) {
						output.write(buffer, 0, byteCount);
					}

					// Log the path of the saved file
					Log.d("SharkActivity", "File saved to: " + file.getAbsolutePath());
					return file.getAbsolutePath();
				} catch (Exception e) {
					Log.e("SharkActivity", "Error downloading file: " + e.getMessage(), e);
					return null;
				} finally {
					try {
						if (input != null) input.close();
						if (output != null) output.close();
					} catch (Exception e) {
						Log.e("SharkActivity", "Error closing streams: " + e.getMessage(), e);
					}
					if (urlConnection != null) urlConnection.disconnect();
				}
			}

			@Override
			protected void onPostExecute(String path) {
				super.onPostExecute(path);
				if (path != null) {
					// Handle the completion of the download, e.g., update UI
					Log.d("SharkActivity", "Download completed: " + path);
				} else {
					// Handle error
					Log.e("SharkActivity", "Download failed");
				}
			}
		}.execute();
	}
	*/

	private void downloadFile(final String fileURL, final String dirPath, final String fileName) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Handler handler = new Handler(Looper.getMainLooper());

		executor.execute(() -> {
			String result = performDownload(fileURL, dirPath, fileName);
			handler.post(() -> {
				// Update UI with result, e.g., display a message or update a view
				if (result != null) {
					Log.d("SharkActivity", "Download successful: " + result);
				} else {
					Log.e("SharkActivity", "Download failed");
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
				Log.e("SharkActivity", "Failed to create directory: " + dirPath);
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
			Log.e("SharkActivity", "Error downloading file: " + e.getMessage(), e);
			return null;
		} finally {
			try {
				if (input != null) input.close();
				if (output != null) output.close();
			} catch (Exception e) {
				Log.e("SharkActivity", "Error closing streams: " + e.getMessage(), e);
			}
			if (urlConnection != null) urlConnection.disconnect();
		}
	}


	@Override
	protected void onCameraPermissionGranted()
	{
		GLFrameView.setFrameMedium("LiveVideoId:0", "LIVE_VIDEO", 1280, 720, true);
	}

	@Override
	protected void onDestroy()
	{
		SceneDescriptionSDXX3DJni.unregisterLibrary();
		SceneDescriptionSDLOBJJni.unregisterLibrary();

		DevicesPatternJni.unregisterLibrary();
		DevicesAndroidJni.unregisterLibrary();

		MediaOpenImageLibrariesJni.unregisterLibrary();

		super.onDestroy();
	}
}
