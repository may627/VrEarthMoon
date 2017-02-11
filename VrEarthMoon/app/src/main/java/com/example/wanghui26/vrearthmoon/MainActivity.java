/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.wanghui26.vrearthmoon;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import java.io.InputStream;
import javax.microedition.khronos.egl.EGLConfig;
import static com.example.wanghui26.vrearthmoon.Constant.*;

/**
 * A Google VR sample application.
 *
 * <p>The TreasureHunt scene consists of a planar ground grid and a floating
 * "treasure" cube. When the user looks at the cube, the cube will turn gold.
 * While gold, the user can activate the Cardboard trigger, either directly
 * using the touch trigger on their Cardboard viewer, or using the Daydream
 * controller-based trigger emulation. Activating the trigger will in turn
 * randomly reposition the cube.
 */
public class MainActivity extends GvrActivity implements GvrView.StereoRenderer {

    private static final String TAG = "VrEarthMoon";

    //private static final float Z_NEAR = 0.1f;
    private static final float Z_NEAR = 1f;
    private static final float Z_FAR = 100.0f;

    Earth earth;
    Moon moon;
    Space spaceSmall;
    Space spaceBig;

    int textureId;
    int textureEarthDay;
    int textureEarthNight;
    int textureMoon;

    float eAngel;
    float cAngle;

    /**
     * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
     *
     * @param label Label to report in case of error.
     */
    private static void checkGLError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }

    /**
     * Sets the view to our GvrView and initializes the transformation matrices we will use
     * to render our scene.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeGvrView();
    }

    public void initializeGvrView() {
        setContentView(R.layout.activity_main);

        GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        gvrView.setRenderer(this);
        gvrView.setTransitionViewEnabled(true);

        // Enable Cardboard-trigger feedback with Daydream headsets. This is a simple way of supporting
        // Daydream controller input for basic interactions using the existing Cardboard trigger API.
        gvrView.enableCardboardTriggerEmulation();

        if (gvrView.setAsyncReprojectionEnabled(true)) {
            // Async reprojection decouples the app framerate from the display framerate,
            // allowing immersive interaction even at the throttled clockrates set by
            // sustained performance mode.
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }

        setGvrView(gvrView);
    }

    @Override
    public void onPause() {
        super.onPause();
        Constant.threadFlag = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.threadFlag = true;
    }

    @Override
    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
        MatrixState.setCamera(0,0f,7.2f,0f,0f,0f,0f,1.0f,0.0f);

        textureEarthDay = initTexture(R.drawable.earth);
        textureEarthNight = initTexture(R.drawable.earthn);
        textureMoon = initTexture(R.drawable.moon);

        MatrixState.setLightLocationSun(100, 5, 0);

        new Thread() {
            @Override
            public void run() {
                while(threadFlag) {
                    eAngel = (eAngel +2) % 360;
                    cAngle = (cAngle + 0.2f) % 360;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();
    }

    /**
     * Creates the buffers we use to store information about the 3D world.
     *
     * <p>OpenGL doesn't use Java arrays, but rather needs data in a format it can understand.
     * Hence we use ByteBuffers.
     *
     * @param config The EGL configuration used when creating the surface.
     */
    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        Constant.CURR_DRAW_MODE = GLES20.GL_POINTS;

        GLES20.glClearColor(0f, 0f, 0f, 1.0f);

        earth = new Earth(3.0f, this);
        moon = new Moon(1.5f, this);
        spaceSmall = new Space(100, 0, 1000, this);
        spaceBig = new Space(20, 0, 500, this);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        MatrixState.setInitStack();
        MatrixState.setLightLocationSun(100,5,0);
    }

    public int initTexture(int drawId) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        textureId = textures[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        InputStream is = this.getResources().openRawResource(drawId);
        Bitmap bitmapTmp;
        bitmapTmp = BitmapFactory.decodeStream(is);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmapTmp, 0);
        bitmapTmp.recycle();

        return textureId;
    }

    /**
     * Prepares OpenGL ES before we draw a frame.
     *
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {

        // Build the camera matrix and apply it to the ModelView.
        //Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        MatrixState.setCamera(0,0f,7.2f,0f,0f,0f,0f,1.0f,0.0f);

        checkGLError("onReadyToDraw");
    }

    /**
     * Draws a frame for an eye.
     *
     * @param eye The eye to render. Includes all required transformations.
     */
    @Override
    public void onDrawEye(Eye eye) {
        float[] projMatrix;
        float[] viewMatrix = new float[16];

        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        projMatrix = eye.getPerspective(Z_NEAR, Z_FAR);
        MatrixState.setProjMatrix(projMatrix);
        Matrix.multiplyMM(viewMatrix, 0, eye.getEyeView(), 0, MatrixState.getmVMatrix(), 0);
        MatrixState.setmVMatrix(viewMatrix);

        MatrixState.pushMatrix();
        MatrixState.rotate(eAngel, 0, 1, 0);
        earth.drawSelf(textureEarthDay, textureEarthNight);
        MatrixState.translate(2f, 0, 0);
        MatrixState.rotate(eAngel, 0, 1, 0);
        moon.drawSelf(textureMoon);
        MatrixState.popMatrix();

        MatrixState.pushMatrix();
        MatrixState.rotate(cAngle, 0, 1, 0);
        spaceBig.drawSelf();
        spaceSmall.drawSelf();
        MatrixState.popMatrix();
    }

    @Override
    public void onFinishFrame(Viewport viewport) {}
}
