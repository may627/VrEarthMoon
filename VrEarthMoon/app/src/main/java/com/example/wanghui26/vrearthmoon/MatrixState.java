package com.example.wanghui26.vrearthmoon;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by wanghui26 on 2016/12/19.
 */
public class MatrixState {
    private static float[] mVMatrix = new float[16];
    private static float[] mProjMatrix = new float[16];
    private static float[] mMVPMatrix;

    private static int stackTop = -1;
    private static float[][] mStack = new float[10][16];
    private static float[] currMatrix;

    public static float[] lightLocation = new float[] {0, 0, 0};
    public static FloatBuffer lightPositionFB;
    static ByteBuffer llbbL = ByteBuffer.allocateDirect(3 * 4);

    public static void setLightLocation(float x, float y, float z) {
        llbbL.clear();
        lightLocation[0] = x;
        lightLocation[1] = y;
        lightLocation[2] = z;

        llbbL.order(ByteOrder.nativeOrder());
        lightPositionFB = llbbL.asFloatBuffer();
        lightPositionFB.put(lightLocation);
        lightPositionFB.position(0);
    }

    static ByteBuffer llbb = ByteBuffer.allocateDirect(3 * 4);
    static float[] cameraLocation = new float[3];
    public static FloatBuffer cameraFB;
    public static void setCamera(float cx, float cy, float cz,
                                 float tx, float ty, float tz,
                                 float upx, float upy, float upz) {
        /*
            Matrix.setLookAtM(
                mVMatrix,       // 存储生成的矩阵元素的float[]类型数组
                0,              // 填充起始偏移量
                cx, cy, cz,     // 摄像机位置的X、Y、Z坐标
                tx, ty, tz,     // 观察目标点X、Y、Z坐标;通过摄像机位置和观察目标点得到的向量，即为观察方向
                upx, upy, upz   // up向量在X、Y、Z轴上的分量
            );
        */
        Matrix.setLookAtM(mVMatrix, 0, cx, cy, cz, tx, ty, tz, upx, upy, upz);

        cameraLocation[0] = cx;
        cameraLocation[1] = cy;
        cameraLocation[2] = cz;

        llbb.clear();
        llbb.order(ByteOrder.nativeOrder());
        cameraFB = llbb.asFloatBuffer();
        cameraFB.put(cameraLocation);
        cameraFB.position(0);
    }

    public static float[] lightDirection = new float[] {0, 0, 1};
    public static FloatBuffer lightDirectionFB;
    public static void setLightDirection(float x, float y, float z) {
        llbbL.clear();

        lightDirection[0] = x;
        lightDirection[1] = y;
        lightDirection[2] = z;

        llbbL.order(ByteOrder.nativeOrder());
        lightDirectionFB = llbbL.asFloatBuffer();
        lightDirectionFB.put(lightDirection);
        lightDirectionFB.position(0);
    }

    public static void setProjectOrtho(float left, float right,
                                       float bottom, float top,
                                       float near, float far) {
        Matrix.orthoM(mProjMatrix, 0, left, right, bottom, top, near, far);
    }

    public static float[] getFinalMatrix() {
        mMVPMatrix = new float[16];
        /*
        void multiplyMM (
                float[] result,     //float:The float array that holds the result.
                int resultOffset,   //int: The offset into the result array where the result is stored.
                float[] lhs,        //The float array that holds the left-hand-side matrix.
                int lhsOffset,      //int: The offset into the lhs array where the lhs is stored
                float[] rhs,        //The float array that holds the right-hand-side matrix.
                int rhsOffset)      //int: The offset into the rhs array where the rhs is stored.
         */
        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, currMatrix, 0);  // mMVPMatrix = mVMatrix * currMatrix; 注意矩阵相乘有方向
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);    // mMVPMatrix = mProjMatrix * mMVPMatrix;
        return mMVPMatrix;
    }

    public static void setProjectFrustum(float left, float right,
                                         float bottom, float up,
                                         float near, float far) {
        Matrix.frustumM(mProjMatrix, 0, left, right, bottom, up, near, far);
    }

    public static void setInitStack() {
        currMatrix = new float[16];

        /*
        Matrix.setRotateM(
            float[] rm,     //变换矩阵
            int rmOffset,   //变换矩阵的索引
            float a,        //旋转角度
            float x, float y, float z)  //设置将绕哪个轴旋转
         */
        Matrix.setRotateM(currMatrix, 0, 0, 1, 0, 0);
    }

    public static void pushMatrix() {
        stackTop++;
        for(int i = 0; i < 16; i++) {
            mStack[stackTop][i] = currMatrix[i];
        }
    }

    public static void popMatrix() {
        for(int i = 0; i < 16; i++) {
            currMatrix[i] = mStack[stackTop][i];
        }
        stackTop--;
    }
    public static void translate(float x, float y, float z) {
        Matrix.translateM(currMatrix, 0, x, y, z);
    }

    public static void rotate(float angle, float x, float y, float z) {
        Matrix.rotateM(currMatrix, 0, angle, x, y, z);
    }

    public static float[] getMMatrix() {
        return currMatrix;
    }

    public static void scale(float x, float y, float z) {
        Matrix.scaleM(currMatrix, 0, x, y, z);
    }

    public static float[] lightLocationSun=new float[]{0,0,0};//Ì«Ñô¶¨Î»¹â¹âÔ´Î»ÖÃ
    public static FloatBuffer lightPositionFBSun;
    public static void setLightLocationSun(float x,float y,float z)
    {
        lightLocationSun[0]=x;
        lightLocationSun[1]=y;
        lightLocationSun[2]=z;
        ByteBuffer llbb = ByteBuffer.allocateDirect(3 * 4);
        llbb.order(ByteOrder.nativeOrder());//ÉèÖÃ×Ö½ÚË³Ðò
        lightPositionFBSun = llbb.asFloatBuffer();
        lightPositionFBSun.put(lightLocationSun);
        lightPositionFBSun.position(0);
    }
}

