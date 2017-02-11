package com.example.wanghui26.vrearthmoon;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import static com.example.wanghui26.vrearthmoon.Constant.*;
/**
 * Created by wanghui26 on 2016/12/19.
 */
public class MySurfaceView extends GLSurfaceView {
    SceneRenderer mRenderer;

    int textureId;
    int textureEarthDay;
    int textureEarthNight;
    int textureMoon;

    Earth earth;
    Moon moon;
    Space spaceSmall;
    Space spaceBig;
    float eAngel;
    float cAngle;
    float mPreviousX;
    float mPreviousY;
    float xAngle;
    float yAngle;
    private final float TOUCH_SCALE_FACTOR = 180.0F/320;

    public MySurfaceView(Context context) {
        super(context);
        this.setEGLContextClientVersion(2);
        mRenderer = new SceneRenderer();

        Constant.CURR_DRAW_MODE = GLES20.GL_POINTS;
        this.setRenderer(mRenderer);
        this.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx = x - mPreviousX;
                yAngle += dx * TOUCH_SCALE_FACTOR;
                float sunx = (float) (Math.cos(Math.toRadians(yAngle)) * 100);
                float sunz = -(float) (Math.sin(Math.toRadians(yAngle)) * 100);
                MatrixState.setLightLocationSun(sunx, 5, sunz);

                float dy = y - mPreviousY;
                xAngle += dy * TOUCH_SCALE_FACTOR;
                if (xAngle > 90)
                    xAngle = 90;
                else if (xAngle < -90)
                    xAngle = -90;

                float cy = (float) (7.2 * Math.sin(Math.toRadians(xAngle)));
                float cz = (float) (7.2 * Math.cos(Math.toRadians(xAngle)));
                float upy = (float) Math.cos(Math.toRadians(xAngle));
                float upz = (float) Math.sin(Math.toRadians(xAngle));
                MatrixState.setCamera(0, cy, cz, 0, 0, 0, 0, upy, upz);
        }

        mPreviousX = x;
        mPreviousY = y;

        return true;    // 处理完成，不需要再由其他人继续处理
    }

    private class SceneRenderer implements GLSurfaceView.Renderer {
        /*
        系统在每次重绘GLSurfaceView时调用此方法．此方法是绘制图形对象的主要的执行点．
         */
        @Override
        public void onDrawFrame(GL10 gl10) {
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

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

        /*
        当GLSurfaceView几何体改变时系统调用此方法，比如GLSurfaceView的大小改变或设备屏幕的方向改变．
        使用此方法来响应GLSurfaceView容器的变化．
         */
        @Override
        public void onSurfaceChanged(GL10 gl10, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
            Constant.ratio = (float) width / height;
            Log.e("Moon", "ratio : " + Constant.ratio);

            //调用此方法计算产生透视投影矩阵
            MatrixState.setProjectFrustum(-Constant.ratio, Constant.ratio, -1f, 1f, 3f, 100);
            //调用此方法产生摄像机9参数位置矩阵
            MatrixState.setCamera(0,0f,7.2f,0f,0f,0f,0f,1.0f,0.0f);
            GLES20.glEnable(GLES20.GL_CULL_FACE);  
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

        /*
        onSurfaceCreated():当创建GLSurfaceView时被调用，只调用一次．在这个方法中执行只发生一次的动作，
        比如设置OpenGL环境参数或初始化OpenGL图形对象．
         */
        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
            GLES20.glClearColor(0f, 0f, 0f, 1.0f);

            earth = new Earth(MySurfaceView.this, 2.0f);
            moon = new Moon(MySurfaceView.this, 1.0f);
            spaceSmall = new Space(100, 0, 1000, MySurfaceView.this);
            spaceBig = new Space(20, 0, 500, MySurfaceView.this);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);

            GLES20.glEnable(GLES20.GL_CULL_FACE);

            MatrixState.setInitStack();
            MatrixState.setLightLocationSun(100,5,0);
        }
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
}
