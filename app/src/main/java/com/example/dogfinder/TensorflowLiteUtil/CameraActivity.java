package com.example.dogfinder.TensorflowLiteUtil;


import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.dogfinder.Activity.IndexActivity;
import com.example.dogfinder.Activity.StrayFormActivity;
import com.example.dogfinder.R;
import com.example.dogfinder.env.ImageUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class CameraActivity extends FragmentActivity
        implements OnImageAvailableListener,Camera.PreviewCallback {

    static final int PICK_IMAGE = 100;
    private static final int PERMISSIONS_REQUEST = 1;
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_STORAGE_READ = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String PERMISSION_STORAGE_WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    static private final int[] CHART_COLORS = {Color.rgb(114, 147, 203),
            Color.rgb(225, 151, 76), Color.rgb(132, 186, 91), Color.TRANSPARENT};

    public static String cameraId;
    private static int cameraPermissionRequests = 0;
    protected ArrayList<String> currentRecognitions;
    protected int previewWidth = 0;
    protected int previewHeight = 0;
    protected ClassifierActivity.InferenceTask inferenceTask;
    TextView resultsView;

    AtomicBoolean snapShot = new AtomicBoolean(false);
    boolean continuousInference = false;
    boolean imageSet = false;
    ImageButton cameraButton, shareButton, closeButton, saveButton,exitButton,galleryButton;
    ToggleButton continuousInferenceButton;
    ImageView imageViewFromGallery;
    ProgressBar progressBar;
    private Handler handler;
    private HandlerThread handlerThread;
    private boolean isProcessingFrame = false;
    private byte[][] yuvBytes = new byte[3][];
    private int[] rgbBytes = null;
    private int yRowStride;
    private Runnable postInferenceCallback;
    private Runnable imageConverter;
    private boolean useCamera2API;
    private String fileUrl;
    boolean alreadyAdded;
    String result;
    Bitmap bitmap;
    public Intent galleryIntent;

    public static String preferredLanguageCode;

    abstract void handleSendImage(Intent intent);

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(null);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        preferredLanguageCode = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("lang", "en");

        setLocale();

        setContentView(R.layout.activity_camera);
        setupButtons();
        initClassifier();
        // Get intent, action and MIME type
        final Intent intent = getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();

        // Handle single image being sent from other application
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                if (inferenceTask != null)
                    inferenceTask.cancel(true);

                handleSendImage(intent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (permissions[0]) {
                case PERMISSION_CAMERA:
                    setFragment();
                    break;
                case PERMISSION_STORAGE_READ:
                    pickImage();
                    break;
                case PERMISSION_STORAGE_WRITE:
                    // user might have clicked on save or share button
                    shareButton.callOnClick();
                    break;
            }
        }
    }

    private void setupButtons() {
        imageViewFromGallery = findViewById(R.id.imageView);
        resultsView = findViewById(R.id.results);
        progressBar = findViewById(R.id.progressBar);

        continuousInferenceButton = findViewById(R.id.continuousInferenceButton);
        cameraButton = findViewById(R.id.cameraButton);
        shareButton = findViewById(R.id.shareButton);
        closeButton = findViewById(R.id.closeButton);
        saveButton = findViewById(R.id.saveButton);
        exitButton = findViewById(R.id.exitButton);
        galleryButton = findViewById(R.id.galleryButton);
        cameraButton.setEnabled(false);

        setButtonsVisibility(View.GONE);
        galleryButton.setOnClickListener(v -> {
            if (!hasPermission(PERMISSION_STORAGE_READ)) {
                requestPermission(PERMISSION_STORAGE_READ);
                return;
            }
            alreadyAdded = true;
            Toast.makeText(getApplicationContext(),alreadyAdded+"",Toast.LENGTH_SHORT).show();
            pickImage();
        });
        cameraButton.setOnClickListener(v -> {
            if (!hasPermission(PERMISSION_CAMERA)) {
                requestPermission(PERMISSION_CAMERA);
                return;
            }

            final View pnlFlash = findViewById(R.id.pnlFlash);
            alreadyAdded = false;
            cameraButton.setEnabled(false);
            snapShot.set(true);
            imageSet = false;
            updateResults(null);

            imageViewFromGallery.setEnabled(false);
            continuousInferenceButton.setChecked(false);

            // show flash animation
            pnlFlash.setVisibility(View.VISIBLE);
            AlphaAnimation fade = new AlphaAnimation(1, 0);
            fade.setDuration(500);
            fade.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation anim) {
                    pnlFlash.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            pnlFlash.startAnimation(fade);
        });
        exitButton.setVisibility(View.VISIBLE);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CameraActivity.this, IndexActivity.class);
                startActivity(intent);
                finish();
            }
        });
        continuousInferenceButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!hasPermission(PERMISSION_CAMERA)) requestPermission(PERMISSION_CAMERA);

            //imageViewFromGallery.setVisibility(View.GONE);
            continuousInference = isChecked;

            if (!continuousInference)
                if (inferenceTask != null)
                    inferenceTask.cancel(true);

            if (!isChecked)
                resultsView.setEnabled(false);

            cameraButton.setEnabled(true);

            imageSet = false;

            if (handler != null)
                handler.post(() -> updateResults(null));

            readyForNextImage();
        });

    }


    public void setLocale() {
        Locale locale;

        locale = new Locale(preferredLanguageCode);

        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());    //restart Activity
    }


    private void pickImage() {
        galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        continuousInferenceButton.setChecked(false);
        alreadyAdded = true;
        startActivityForResult(galleryIntent, PICK_IMAGE);
    }

    protected int[] getRgbBytes() {
        if (imageConverter != null)
            imageConverter.run();

        return rgbBytes;
    }

    /**
     * Callback for Camera2 API
     */
    @Override
    public void onImageAvailable(final ImageReader reader) {
        //We need to wait until we have some size from onPreviewSizeChosen
        if (previewWidth == 0 || previewHeight == 0) {
            return;
        }
        if (rgbBytes == null) {
            rgbBytes = new int[previewWidth * previewHeight];
        }
        try {
            final Image image = reader.acquireLatestImage();

            if (image == null) {
                return;
            }

            if (isProcessingFrame) {
                image.close();
                return;
            }
            isProcessingFrame = true;
            final Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);
            yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();

            imageConverter = () -> ImageUtils.convertYUV420ToARGB8888(
                    yuvBytes[0],
                    yuvBytes[1],
                    yuvBytes[2],
                    previewWidth,
                    previewHeight,
                    yRowStride,
                    uvRowStride,
                    uvPixelStride,
                    rgbBytes);

            postInferenceCallback = () -> {
                image.close();
                isProcessingFrame = false;
            };

            processImage();
        } catch (final Exception ignored) {
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        if (!hasPermission(PERMISSION_CAMERA)) {

            if (cameraPermissionRequests++ < 3) {
                requestPermission(PERMISSION_CAMERA);
            } else {
                Toast.makeText(getApplicationContext(), "Camera permission required.", Toast.LENGTH_LONG).show();
            }
        } else {
            setFragment();
        }

        snapShot.set(false);

        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        final ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
                    | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);

        if (!imageSet) cameraButton.setEnabled(true);
    }


    @Override
    public synchronized void onPause() {
        snapShot.set(false);
        cameraButton.setEnabled(false);
        isProcessingFrame = false;
        progressBar.setVisibility(View.GONE);

        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException ignored) {
        }

        super.onPause();
    }

    @Override
    public synchronized void onStop() {
        super.onStop();
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    private boolean hasPermission(final String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestPermission(final String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{permission}, PERMISSIONS_REQUEST);
        }
    }

    // Returns true if the device supports the required hardware level, or better.
    private boolean isHardwareLevelSupported(
            CameraCharacteristics characteristics, int requiredLevel) {
        int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            return requiredLevel == deviceLevel;
        }
        // deviceLevel is not LEGACY, can use numerical sort
        return requiredLevel <= deviceLevel;
    }

    protected String chooseCamera() {
        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (final String cameraId : manager.getCameraIdList()) {
                final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                final StreamConfigurationMap map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (map == null) {
                    continue;
                }

                // Fallback to camera1 API for internal cameras that don't have full support.
                // This should help with legacy situations where using the camera2 API causes
                // distorted or otherwise broken previews.
                useCamera2API = (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
                        || isHardwareLevelSupported(characteristics,
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);
                return cameraId;
            }
        } catch (CameraAccessException ignored) {
        }

        return null;
    }

    protected void setFragment() {
        cameraId = chooseCamera();
        if (cameraId == null) {
            Toast.makeText(getApplicationContext(), "No Camera Detected", Toast.LENGTH_SHORT).show();
            finish();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, useCamera2API ? new CameraConnectionFragment() : new LegacyCameraConnectionFragment())
                .commitAllowingStateLoss();
    }

    protected void fillBytes(final Plane[] planes, final byte[][] yuvBytes) {
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                //LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity());
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }

    protected void readyForNextImage() {
        // sometimes this will be uninitialized, for whatever reason
        if (postInferenceCallback != null) {
            postInferenceCallback.run();
        } else isProcessingFrame = false;

    }

    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }

    protected abstract void processImage();

    protected abstract void onPreviewSizeChosen(final Size size, final int rotation);

    protected abstract void initClassifier();

    void updateResults(List<Classifier.Recognition> results) {
        runOnUiThread(() -> {
            updateResultsView(results);

        });
    }

    void setButtonsVisibility(final int visibility) {
        final boolean enabled = visibility == View.VISIBLE;

        shareButton.setVisibility(visibility);
        shareButton.setEnabled(enabled);
        closeButton.setVisibility(visibility);
        closeButton.setEnabled(enabled);
        saveButton.setVisibility(visibility);
        saveButton.setEnabled(enabled);
    }

    // update results on our custom textview
    void updateResultsView(List<Classifier.Recognition> results) {
        final StringBuilder sb = new StringBuilder();
        currentRecognitions = new ArrayList<String>();

        if (results != null) {
            resultsView.setEnabled(true);

            if (!continuousInference) {
                setButtonsVisibility(View.VISIBLE);
            }

            if (results.size() > 0) {
                for (final Classifier.Recognition recog : results) {
                    final String text = String.format(Locale.getDefault(), "%s: %d %%\n",
                            recog.getTitle(), Math.round(recog.getConfidence() * 100));
                    sb.append(text);
                    currentRecognitions.add(recog.getTitle());
                }
            } else {
                sb.append(getString(R.string.no_detection));
            }
        } else {
            resultsView.setEnabled(false);
        }

        result = sb.toString();
        resultsView.setText(result);
    }


    protected void setImage(Bitmap image) {
        final int transitionTime = 1000;
        imageSet = true;
        alreadyAdded = false;

        cameraButton.setEnabled(false);
        imageViewFromGallery.setImageBitmap(image);
        imageViewFromGallery.setVisibility(View.VISIBLE);

        final TransitionDrawable transition = (TransitionDrawable) imageViewFromGallery.getBackground();
        transition.startTransition(transitionTime);
        setupShareButton();

        // fade out image on click
        final AlphaAnimation fade = new AlphaAnimation(1, 0);
        fade.setDuration(transitionTime);

        fade.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (inferenceTask != null)
                    inferenceTask.cancel(true);

                imageViewFromGallery.setClickable(false);
                runInBackground(() -> updateResults(null));
                transition.reverseTransition(transitionTime);
                imageViewFromGallery.setVisibility(View.GONE);
                setButtonsVisibility(View.GONE);
                //exitButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation anim) {
                progressBar.setVisibility(View.GONE);
                imageSet = false;
                snapShot.set(false);
                cameraButton.setEnabled(true);
                readyForNextImage();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        imageViewFromGallery.setVisibility(View.VISIBLE);
        closeButton.setOnClickListener(v -> imageViewFromGallery.startAnimation(fade));
        exitButton.setVisibility(View.VISIBLE);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImage();
            }
        });

    }


    private void saveImage() {
        if (!hasPermission(PERMISSION_STORAGE_WRITE)) {
            requestPermission(PERMISSION_STORAGE_WRITE);
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("breed",result);

        //save image to gallery
        bitmap = ((BitmapDrawable)imageViewFromGallery.getDrawable()).getBitmap();
        final String fileName = getString(R.string.app_name) + " " + System.currentTimeMillis() / 1000;
        fileUrl = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, fileName, currentRecognitions.toString());
        bundle.putString("image",fileUrl);
        saveButton.setVisibility(View.GONE);
        Intent intent = new Intent(CameraActivity.this,StrayFormActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();

    }

    protected void setupShareButton() {

        shareButton.setOnClickListener(v -> {
            if (!hasPermission(PERMISSION_STORAGE_WRITE)) {
                requestPermission(PERMISSION_STORAGE_WRITE);
                return;
            }

            saveImage();

            final Uri contentUri = Uri.parse(fileUrl);

            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with)));
            }
        });
    }
    /**
     * Callback for android.hardware.Camera API
     */
    @Override
    public void onPreviewFrame(final byte[] bytes, final Camera camera) {
        if (isProcessingFrame) {
            return;
        }

        try {
            // Initialize the storage bitmaps once when the resolution is known.
            if (rgbBytes == null) {
                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                previewHeight = previewSize.height;
                previewWidth = previewSize.width;
                rgbBytes = new int[previewWidth * previewHeight];
                onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
            }
        } catch (final Exception ignored) {
            return;
        }

        isProcessingFrame = true;
        yuvBytes[0] = bytes;
        yRowStride = previewWidth;

        imageConverter =
                () -> ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);

        postInferenceCallback =
                () -> {
                    camera.addCallbackBuffer(bytes);
                    isProcessingFrame = false;
                };
        processImage();
    }


}