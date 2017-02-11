package com.example.wanghui26.vrearthmoon;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created by wanghui26 on 2017/1/18.
 */
public class Moon {
    private String mVertexShader;
    private String mFragShader;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mNormalBuffer;
    private FloatBuffer mTexCoorBuffer;
    private int muMVPMatrixHandle;
    private int maPositionHandle;
    private int maTexCoorHandle;
    private int muMMatrixHandle;
    private int muCameraHandle;
    private int muLightLocationSunHandle;
    private int maNormalHandle;
    private int mProgram;

    private int vCount;

    public Moon(float r, MainActivity mainActivity) {
        initVertexData(r);
        initShader(mainActivity);
    }

    private void initVertexData(float r) {
        //顶点坐标数据的初始化================begin============================
        final float UNIT_SIZE = 0.5f;
        ArrayList<Float> alVertix = new ArrayList<Float>();//存放顶点坐标的ArrayList
        final float angleSpan = 10f;//将球进行单位切分的角度
        for (float vAngle = 90; vAngle > -90; vAngle = vAngle - angleSpan)//垂直方向angleSpan度一份
        {
            for (float hAngle = 360; hAngle > 0; hAngle = hAngle - angleSpan)//水平方向angleSpan度一份
            {//纵向横向各到一个角度后计算对应的此点在球面上的坐标
                double xozLength = r * UNIT_SIZE * Math.cos(Math.toRadians(vAngle));
                float x1 = (float) (xozLength * Math.cos(Math.toRadians(hAngle)));
                float z1 = (float) (xozLength * Math.sin(Math.toRadians(hAngle)));
                float y1 = (float) (r * UNIT_SIZE * Math.sin(Math.toRadians(vAngle)));

                xozLength = r * UNIT_SIZE * Math.cos(Math.toRadians(vAngle - angleSpan));
                float x2 = (float) (xozLength * Math.cos(Math.toRadians(hAngle)));
                float z2 = (float) (xozLength * Math.sin(Math.toRadians(hAngle)));
                float y2 = (float) (r * UNIT_SIZE * Math.sin(Math.toRadians(vAngle - angleSpan)));

                xozLength = r * UNIT_SIZE * Math.cos(Math.toRadians(vAngle - angleSpan));
                float x3 = (float) (xozLength * Math.cos(Math.toRadians(hAngle - angleSpan)));
                float z3 = (float) (xozLength * Math.sin(Math.toRadians(hAngle - angleSpan)));
                float y3 = (float) (r * UNIT_SIZE * Math.sin(Math.toRadians(vAngle - angleSpan)));

                xozLength = r * UNIT_SIZE * Math.cos(Math.toRadians(vAngle));
                float x4 = (float) (xozLength * Math.cos(Math.toRadians(hAngle - angleSpan)));
                float z4 = (float) (xozLength * Math.sin(Math.toRadians(hAngle - angleSpan)));
                float y4 = (float) (r * UNIT_SIZE * Math.sin(Math.toRadians(vAngle)));

                //构建第一三角形
                alVertix.add(x1);
                alVertix.add(y1);
                alVertix.add(z1);
                alVertix.add(x2);
                alVertix.add(y2);
                alVertix.add(z2);
                alVertix.add(x4);
                alVertix.add(y4);
                alVertix.add(z4);
                //构建第二三角形
                alVertix.add(x4);
                alVertix.add(y4);
                alVertix.add(z4);
                alVertix.add(x2);
                alVertix.add(y2);
                alVertix.add(z2);
                alVertix.add(x3);
                alVertix.add(y3);
                alVertix.add(z3);
            }
        }
        vCount = alVertix.size() / 3;

        float vertices[] = new float[vCount * 3];
        for (int i = 0; i < alVertix.size(); i++) {
            vertices[i] = alVertix.get(i);
        }
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        float texCoor[] = generateTexCoor((int) (360 / angleSpan), (int) (180 / angleSpan));
        ByteBuffer cbb = ByteBuffer.allocateDirect(texCoor.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        mTexCoorBuffer = cbb.asFloatBuffer();
        mTexCoorBuffer.put(texCoor);
        mTexCoorBuffer.position(0);
    }

    private void initShader(MainActivity mainActivity) {
        mVertexShader = ShaderUtil.loadFromAssetsFile("vertexMoon.sh", mainActivity.getResources());
        mFragShader = ShaderUtil.loadFromAssetsFile("fragMoon.sh", mainActivity.getResources());

        mProgram = ShaderUtil.createProgram(mVertexShader, mFragShader);
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        muMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMMatrix");
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        maTexCoorHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoor");
        maNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");
        muCameraHandle = GLES20.glGetUniformLocation(mProgram, "uCamera");
        muLightLocationSunHandle = GLES20.glGetUniformLocation(mProgram, "uLightLocation");
    }

    public void drawSelf(int texId) {
        GLES20.glUseProgram(mProgram);

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState.getFinalMatrix(), 0);
        GLES20.glUniformMatrix4fv(muMMatrixHandle, 1, false, MatrixState.getMMatrix(), 0);
        GLES20.glUniform3fv(muCameraHandle, 1, MatrixState.cameraFB);
        GLES20.glUniform3fv(muLightLocationSunHandle, 1, MatrixState.lightPositionFBSun);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer);
        GLES20.glVertexAttribPointer(maTexCoorHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, mTexCoorBuffer);
        GLES20.glVertexAttribPointer(maNormalHandle, 4, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer);

        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glEnableVertexAttribArray(maTexCoorHandle);
        GLES20.glEnableVertexAttribArray(maNormalHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);
    }

    private float[] generateTexCoor(int bw, int bh) {
        float[] result = new float[bw * bh * 6 * 2];
        float sizew = 1.0f/bw;
        float sizeh = 1.0f/bh;
        int c = 0;

        for(int i = 0; i < bh; i++) {
            for(int j = 0; j < bw; j++) {
                float s = j * sizew;
                float t = i * sizeh;

                result[c++] = s;
                result[c++] = t;

                result[c++] = s;
                result[c++] = t + sizeh;

                result[c++] = s + sizew;
                result[c++] = t;

                result[c++] = s + sizew;
                result[c++] = t;

                result[c++] = s;
                result[c++] = t + sizeh;

                result[c++] = s + sizew;
                result[c++] = t + sizeh;
            }
        }

        return result;
    }
}

