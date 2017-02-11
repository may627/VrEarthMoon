package com.example.wanghui26.vrearthmoon;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by wanghui26 on 2016/12/19.
 */

public class ShaderUtil {

    public static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if(shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);

            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if(compiled[0] == 0) {
                Log.e("GLES20_ERROR", "Could not compile shader" + shaderType + ":");
                Log.e("GLES20_ERROR", GLES20.glGetShaderInfoLog(shader));

                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }

        return shader;
    }

    public static int createProgram(String vertextSource, String FragSource) {
        int vertextShader = loadShader(GLES20.GL_VERTEX_SHADER, vertextSource);
        if(vertextShader == 0)
            return 0;

        int fragShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FragSource);
        if(fragShader == 0)
            return 0;

        int program = GLES20.glCreateProgram();
        if(program != 0) {
            GLES20.glAttachShader(program, vertextShader);
            checkGlError("glAttachShader");

            GLES20.glAttachShader(program, fragShader);
            checkGlError("glAttachShader");

            GLES20.glLinkProgram(program);

            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if(linkStatus[0] != GLES20.GL_TRUE) {
                Log.e("GLES20_ERROR", "Could not link program: ");
                Log.e("GLES20_ERROR", GLES20.glGetProgramInfoLog(program));

                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    public static void checkGlError(String op) {
        int error;
        while((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("GLES20_ERROR", op + ": gl_error: " + error);
            throw new RuntimeException(op + ": gl_error " +error);
        }
    }

    public static String loadFromAssetsFile(String fname, Resources r) {
        String result = null;

        try {
            InputStream in = r.getAssets().open(fname);
            int ch = 0;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((ch = in.read()) != -1)
                baos.write(ch);

            byte[] buff = baos.toByteArray();
            baos.close();
            in.close();

            result = new String(buff, "UTF-8");
            result = result.replaceAll("\\r\\n", "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
