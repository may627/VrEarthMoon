package com.example.wanghui26.vrearthmoon;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by wanghui26 on 2017/1/18.
 */
public class Space {
    String mVertexShader;
    String mFragShader;
    private int mProgram;
    private int muMVPMatrixHandle;
    private int maPositionHandle;
    private int muPointSizeHandle;
    private int vCount;
    private float yAngle;
    private float scale;
    FloatBuffer mVertexBuffer;

    static final float UNIT_SIZE = 10f;

    public Space(float scale, float yAngle, int vCount, MySurfaceView mySurfaceView) {
        this.scale = scale;
        this.yAngle = yAngle;
        this.vCount = vCount;

        initVertexData();
        initShader(mySurfaceView);
    }

    private void initVertexData() {
        float vertices[] = new float[vCount * 3];

        for(int i = 0; i < vCount; i++) {
            double angleTempJD = Math.PI * 2 * Math.random();       // 方位角
            double angleTempWD = Math.PI * (Math.random() - 0.5f);  // 仰角，－90~90

            vertices[3 * i] = (float) (UNIT_SIZE * Math.cos(angleTempWD) * Math.sin(angleTempJD));
            vertices[3 * i + 1] = (float) (UNIT_SIZE * Math.sin(angleTempWD));
            vertices[3 * i + 2] = (float)(UNIT_SIZE * Math.cos(angleTempWD) * Math.cos(angleTempJD));
        }
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);
    }

    private void initShader(MySurfaceView mySurfaceView) {
        mVertexShader = ShaderUtil.loadFromAssetsFile("vertexSpace.sh", mySurfaceView.getResources());
        mFragShader = ShaderUtil.loadFromAssetsFile("fragSpace.sh", mySurfaceView.getResources());

        mProgram = ShaderUtil.createProgram(mVertexShader, mFragShader);
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        muPointSizeHandle = GLES20.glGetAttribLocation(mProgram, "uPointSize");
    }

    public void drawSelf() {
        GLES20.glUseProgram(mProgram);

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false,  MatrixState.getFinalMatrix(), 0);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer);
        GLES20.glUniform1f(muPointSizeHandle, scale);
        GLES20.glEnableVertexAttribArray(maPositionHandle);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vCount);
    }
}
