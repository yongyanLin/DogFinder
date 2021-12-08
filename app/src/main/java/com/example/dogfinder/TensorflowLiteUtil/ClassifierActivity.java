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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ImageReader.OnImageAvailableListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Size;
import com.example.dogfinder.Activity.IndexActivity;
import com.example.dogfinder.env.ImageUtils;
import java.io.IOException;
import java.util.List;


public class ClassifierActivity extends CameraActivity implements OnImageAvailableListener {

    private static final int INPUT_SIZE = 325;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Matrix frameToCropTransform;
    private int sensorOrientation;
    private Classifier classifier;


   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_FROM_GALLERY){
            predictImageClass(data.getData());
        }

   }

    public void predictImageClass(Uri imageUri) {
        getResults(null);
        try {
            Bitmap selectedImg;
            selectedImg = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setCameraImage(selectedImg);
                    resultTask = new resultTask();
                    resultTask.execute(selectedImg);
                }
            });
        } catch (IOException e) {
        }
    }


    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        previewWidth = size.getWidth();
        previewHeight = size.getHeight();
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Config.ARGB_8888);
        sensorOrientation = rotation - getScreenOrientation();
        frameToCropTransform = ImageUtils.getTransformationMatrix(
                previewWidth, previewHeight,
                INPUT_SIZE, INPUT_SIZE,
                sensorOrientation, true);
        Matrix cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);
    }

    protected synchronized void createClassifier() {
        if (classifier == null)
            try {
                classifier = Classifier.create(this);
            } catch (IOException e) {
                runOnUiThread(() -> {
                    statusBtn.setChecked(false);
                    cameraBtn.setEnabled(true);
                    Intent intent = new Intent(ClassifierActivity.this, IndexActivity.class);
                    startActivity(intent);
                });
            }
    }

    @Override
    protected void processImage() {
        if (!shortCut.get()&&!statusInference&&!imageSet) {
            readyForNextImage();
            return;
        }
        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
        Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        if (shortCut.compareAndSet(true, false) || statusInference) {
            runOnUiThread(() -> {
                if (!imageSet && !statusInference){
                    setCameraImage(croppedBitmap);
                }
                resultTask = new resultTask();
                resultTask.execute(croppedBitmap);
            });
        }
    }

    protected class resultTask extends AsyncTask<Bitmap, Void, List<Classifier.Recognition>> {
        @Override
        protected void onPreExecute() {

        }
        @Override
        protected List<Classifier.Recognition> doInBackground(Bitmap... bitmap) {
            createClassifier();
            if (!isCancelled() && classifier != null) {
                return classifier.recognizeImage(bitmap[0],sensorOrientation);
            }
            return null;
        }
        @Override
        protected void onPostExecute(List<Classifier.Recognition> recognitions) {
            if (!isCancelled()){
                getResults(recognitions);
            }
            readyForNextImage();
        }
    }

}
