package com.meta.ocean.app.shark.android;

import com.meta.ocean.devices.android.DevicesAndroidJni;
import com.meta.ocean.devices.pattern.DevicesPatternJni;
import com.meta.ocean.media.openimagelibraries.MediaOpenImageLibrariesJni;
import com.meta.ocean.platform.android.*;
import com.meta.ocean.platform.android.application.*;
import com.meta.ocean.scenedescription.sdl.obj.SceneDescriptionSDLOBJJni;
import com.meta.ocean.scenedescription.sdx.x3d.SceneDescriptionSDXX3DJni;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class implements the main Activity object for the Shark viewer.
 * @ingroup sharkandroid
 */
public class SharkActivity extends GLFrameViewActivity
{
    private static final String TAG = "SharkActivity";

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
		Assets.copyFiles(getAssets(), assetDir, true);

        // URL of the asset to download
        String assetUrl = "https://drive.google.com/file/d/1UTt6P3gaQkRrIgsM8GaNz9dcDrkR-f33/view?usp=drive_link";
        new DownloadAndLoadSceneTask().execute(assetUrl, assetDir);
		// NativeInterfaceShark.loadScene(assetDir + "dinosaur.ox3dv", true);
	}

    private class DownloadAndLoadSceneTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String assetUrl = params[0];
            String assetDir = params[1];
            try {
                URL url = new URL(assetUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }
                InputStream input = connection.getInputStream();
                File file = new File(assetDir, "tropical-island-with-toucans.jpeg");
                FileOutputStream output = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                output.close();
                input.close();
                return file.getAbsolutePath();
            } catch (Exception e) {
                Log.e(TAG, "Error downloading asset", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String filePath) {
            if (filePath != null) {
                NativeInterfaceShark.loadScene(filePath, true);
            } else {
                Log.e(TAG, "Failed to download asset");
            }
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
