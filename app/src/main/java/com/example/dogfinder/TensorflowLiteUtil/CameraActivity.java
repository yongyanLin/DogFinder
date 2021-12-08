package com.example.dogfinder.TensorflowLiteUtil;
/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.dogfinder.Activity.IndexActivity;
import com.example.dogfinder.Activity.StrayFormActivity;
import com.example.dogfinder.R;
import com.example.dogfinder.env.ImageUtils;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.hdodenhof.circleimageview.CircleImageView;

public abstract class CameraActivity extends FragmentActivity
        implements OnImageAvailableListener,Camera.PreviewCallback {

    static final int PICK_IMAGE_FROM_GALLERY = 99;
    private static final int PERMISSIONS_REQUEST = 1;
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_STORAGE_READ = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String PERMISSION_STORAGE_WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static String cameraId;
    protected int previewWidth = 0;
    protected int previewHeight = 0;
    protected ClassifierActivity.resultTask resultTask;
    TextView resultView;
    AtomicBoolean shortCut;
    boolean statusInference = false;
    boolean imageSet = false;
    ImageButton cameraBtn, closeBtn, saveBtn,exitBtn;
    CircleImageView galleryBtn;
    ToggleButton statusBtn;
    ImageView imageView;
    View flashLight;
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
    String result,predicted_breed;
    Bitmap bitmap;
    public Intent galleryIntent;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_camera);
        shortCut = new AtomicBoolean(false);
        imageView = findViewById(R.id.imageView);
        resultView = findViewById(R.id.results);

        statusBtn = findViewById(R.id.statusBtn);
        cameraBtn = findViewById(R.id.cameraBtn);
        closeBtn = findViewById(R.id.closeBtn);
        saveBtn = findViewById(R.id.saveBtn);
        exitBtn = findViewById(R.id.exitBtn);
        flashLight = findViewById(R.id.flashLight);
        galleryBtn = findViewById(R.id.galleryBtn);
        setButtonVisibility(View.GONE);
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasPermission(PERMISSION_STORAGE_READ)) {
                    requestPermission(PERMISSION_STORAGE_READ);
                    return;
                }
                pickImageFromGallery();
            }
        });
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasPermission(PERMISSION_CAMERA)) {
                    requestPermission(PERMISSION_CAMERA);
                    return;
                }
                cameraBtn.setEnabled(false);
                shortCut.set(true);
                imageSet = false;
                imageView.setEnabled(false);
                statusBtn.setChecked(false);
                //enable flash light
                flashLight.setVisibility(View.VISIBLE);
                AlphaAnimation gone = new AlphaAnimation(1, 0);
                gone.setDuration(300);
                gone.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation anim) {
                        flashLight.setVisibility(View.GONE);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                flashLight.startAnimation(gone);
            }
        });
        //exit to Index Activity
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CameraActivity.this, IndexActivity.class);
                startActivity(intent);
                finish();
            }
        });
        statusBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            statusInference = isChecked;
            if (!statusInference){
                if (resultTask != null){
                    resultTask.cancel(true);
                }
            }
            cameraBtn.setEnabled(true);
            imageSet = false;
            if (handler != null){
                handler.post(() -> getResults(null));
            }
            readyForNextImage();
        });

        createClassifier();

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
                    pickImageFromGallery();
                    break;
            }
        }
    }

    private void pickImageFromGallery() {
        galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_FROM_GALLERY);
    }

    protected int[] getRgbBytes() {
        if (imageConverter != null)
            imageConverter.run();
        return rgbBytes;
    }

    @Override
    public void onImageAvailable(final ImageReader reader) {
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
            requestPermission(PERMISSION_CAMERA);
        } else {
            setFragment();
        }

        shortCut.set(false);

        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

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

    private boolean isHardwareLevelSupported(
            CameraCharacteristics characteristics, int requiredLevel) {
        int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            return requiredLevel == deviceLevel;
        }
        return requiredLevel <= deviceLevel;
    }

    protected String chooseCamera() {
        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (final String cameraId : manager.getCameraIdList()) {
                final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                final StreamConfigurationMap map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (map == null) {
                    continue;
                }

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
            finish();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.camera_layout, useCamera2API ? new CameraConnectionFragment() : new LegacyCameraConnectionFragment())
                .commitAllowingStateLoss();
    }

    protected void fillBytes(final Plane[] planes, final byte[][] yuvBytes) {
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }

    protected void readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback.run();
        } else{ isProcessingFrame = false;}

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

    protected abstract void createClassifier();

    public void getResults(List<Classifier.Recognition> results) {
        runOnUiThread(() -> {
            setResult(results);
        });
    }

    public void setButtonVisibility(int visibility) {
        boolean enabled = visibility == View.VISIBLE;
        closeBtn.setVisibility(visibility);
        closeBtn.setEnabled(enabled);
        saveBtn.setVisibility(visibility);
        saveBtn.setEnabled(enabled);
        exitBtn.setEnabled(!enabled);
        if(visibility == View.VISIBLE){
            cameraBtn.setVisibility(View.INVISIBLE);
            galleryBtn.setVisibility(View.INVISIBLE);
            statusBtn.setVisibility(View.INVISIBLE);
            exitBtn.setVisibility(View.INVISIBLE);
        }else{
            galleryBtn.setVisibility(View.VISIBLE);
            exitBtn.setVisibility(View.VISIBLE);
            cameraBtn.setVisibility(View.VISIBLE);
            statusBtn.setVisibility(View.VISIBLE);
        }
    }

    // update results
    public void setResult(List<Classifier.Recognition> results) {
        StringBuilder sb = new StringBuilder();
        resultView.setEnabled(true);
        if (results != null) {
            if(!statusInference){
                setButtonVisibility(View.VISIBLE);
            }
            if (results.size() > 0) {
                int i = 0;
                for (Classifier.Recognition recognition : results) {
                    int score = Math.round(recognition.getConfidence()*100);
                    if(score >= 10){
                        if(i == 0) {
                            predicted_breed = recognition.getTitle();
                            i++;
                        }
                        String text = String.format("%s: %d %%\n",recognition.getTitle(), score);
                        sb.append(text);
                    }
                }
                }
        } else {
            sb.append(getString(R.string.resultsHint));
        }
        result = sb.toString();
        resultView.setText(result);
    }



    protected void setCameraImage(Bitmap image) {
        imageSet = true;
        cameraBtn.setEnabled(false);
        imageView.setImageBitmap(image);
        imageView.setVisibility(View.VISIBLE);
        closeBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(CameraActivity.this,ClassifierActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
        );
        saveBtn.setOnClickListener(new View.OnClickListener() {
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
        bundle.putString("breed",predicted_breed);
        //save image to gallery
        bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        final String fileName = getString(R.string.app_name) + " " + System.currentTimeMillis() / 1000;
        fileUrl = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, fileName, predicted_breed);
        bundle.putString("image",fileUrl);
        saveBtn.setVisibility(View.GONE);
        Intent intent = new Intent(CameraActivity.this,StrayFormActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
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