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
import android.widget.FrameLayout;

public class FeatureTrackerActivity extends GLFrameViewActivity {
    static {
        System.loadLibrary("FeatureImageTracking");
    }

    private static final String TAG = "FeatureTrackerActivity";
    private static final String ASSET_URL_BASE = "https://storage.googleapis.com/avatar-system/test/assets/";

    private Handler handler = new Handler(Looper.getMainLooper());
    private VideoView videoView;
    private BoundingBoxView boundingBoxView;

    private Runnable logBoundingBoxEdgesTask = new Runnable() {
        @Override
        public void run() {
            String edges = boundingBoxEdges();
            if (!edges.isEmpty()) {
                float[] points = parseBoundingBoxEdges(edges);
                if (points != null) {
                    boundingBoxView.setPoints(points);
                    playVideo(getFilesDir().getAbsolutePath() + "/samsung-masked_kl4BCZJH.mp4");
                }
            } else {
                // stop the video
                videoView.stopPlayback();
                boundingBoxView.setPoints(null);
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
        boundingBoxView = new BoundingBoxView(this);

        addContentView(videoView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addContentView(boundingBoxView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
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
        Log.d(TAG, "Feature Tracker initialized" + result);
    }

    public static native boolean initializeFeatureTracker(String inputMedium, String pattern, String resolution);
    public static native String boundingBoxEdges();

    private void playVideo(String videoPath) {
        Uri videoUri = Uri.parse(videoPath);
        if (!videoView.isPlaying() && !videoPath.equals(videoView.getTag())) {
            videoView.setVideoURI(videoUri);
            videoView.setTag(videoPath); // Tag the VideoView with the current video path
            // videoView.start();
        } else if (!videoView.isPlaying()) {
            // videoView.resume();
        }
    }

    private float[] parseBoundingBoxEdges(String boundingBoxEdges) {
        try {
            Log.i(TAG, "Bounding box edges " + boundingBoxEdges);
            String[] parts = boundingBoxEdges.split("[^\\d.-]+");
            if (parts.length < 4) {
                Log.e(TAG, "Invalid bounding box edges: " + boundingBoxEdges);
                return null;
            }
            return new float[]{
                Float.parseFloat(parts[0].trim()),
                Float.parseFloat(parts[1].trim()),
                Float.parseFloat(parts[2].trim()),
                Float.parseFloat(parts[3].trim())
            };
        } catch (Exception e) {
            Log.e(TAG, "Error parsing bounding box edges: " + e.getMessage(), e);
            return null;
        }
    }
}
