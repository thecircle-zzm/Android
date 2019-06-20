package nl.thecirclezzm.streaming.encoder.input.gl.render;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import nl.thecirclezzm.streaming.R;
import nl.thecirclezzm.streaming.encoder.input.gl.render.BaseRenderOffScreen;
import nl.thecirclezzm.streaming.encoder.utils.gl.GlUtil;
import nl.thecirclezzm.streaming.encoder.utils.gl.PreviewSizeCalculator;

/**
 * Created by pedro on 29/01/18.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ScreenRender {

    @NonNull
    private final FloatBuffer squareVertex;

    @NonNull
    private final float[] MVPMatrix = new float[16];
    @NonNull
    private final float[] STMatrix = new float[16];
    private boolean AAEnabled = false;  //FXAA enable/disable

    private int texId;

    private int program = -1;
    private int uMVPMatrixHandle = -1;
    private int uSTMatrixHandle = -1;
    private int aPositionHandle = -1;
    private int aTextureHandle = -1;
    private int uSamplerHandle = -1;
    private int uResolutionHandle = -1;
    private int uAAEnabledHandle = -1;

    private int streamWidth;
    private int streamHeight;

    public ScreenRender() {
        //rotation matrix
        // X, Y, Z, U, V
        //bottom left
        //bottom right
        //top left
        //top right
        float[] squareVertexData = {
                // X, Y, Z, U, V
                -1f, -1f, 0f, 0f, 0f, //bottom left
                1f, -1f, 0f, 1f, 0f, //bottom right
                -1f, 1f, 0f, 0f, 1f, //top left
                1f, 1f, 0f, 1f, 1f, //top right
        };
        squareVertex =
                ByteBuffer.allocateDirect(squareVertexData.length * BaseRenderOffScreen.FLOAT_SIZE_BYTES)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
        squareVertex.put(squareVertexData).position(0);
        Matrix.setIdentityM(MVPMatrix, 0);
        Matrix.setIdentityM(STMatrix, 0);
    }

    public void initGl(@NonNull Context context) {
        GlUtil.checkGlError("initGl start");
        String vertexShader = GlUtil.getStringFromRaw(context, R.raw.simple_vertex);
        String fragmentShader = GlUtil.getStringFromRaw(context, R.raw.fxaa);

        program = GlUtil.createProgram(vertexShader, fragmentShader);
        aPositionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        aTextureHandle = GLES20.glGetAttribLocation(program, "aTextureCoord");
        uMVPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        uSTMatrixHandle = GLES20.glGetUniformLocation(program, "uSTMatrix");
        uSamplerHandle = GLES20.glGetUniformLocation(program, "uSampler");
        uResolutionHandle = GLES20.glGetUniformLocation(program, "uResolution");
        uAAEnabledHandle = GLES20.glGetUniformLocation(program, "uAAEnabled");
        GlUtil.checkGlError("initGl end");
    }

    public void draw(int width, int height, boolean keepAspectRatio) {
        GlUtil.checkGlError("drawScreen start");

        PreviewSizeCalculator.calculateViewPort(keepAspectRatio, width, height, streamWidth,
                streamHeight);

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(program);

        squareVertex.position(BaseRenderOffScreen.SQUARE_VERTEX_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false,
                BaseRenderOffScreen.SQUARE_VERTEX_DATA_STRIDE_BYTES, squareVertex);
        GLES20.glEnableVertexAttribArray(aPositionHandle);

        squareVertex.position(BaseRenderOffScreen.SQUARE_VERTEX_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(aTextureHandle, 2, GLES20.GL_FLOAT, false,
                BaseRenderOffScreen.SQUARE_VERTEX_DATA_STRIDE_BYTES, squareVertex);
        GLES20.glEnableVertexAttribArray(aTextureHandle);

        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, MVPMatrix, 0);
        GLES20.glUniformMatrix4fv(uSTMatrixHandle, 1, false, STMatrix, 0);
        GLES20.glUniform2f(uResolutionHandle, width, height);
        GLES20.glUniform1f(uAAEnabledHandle, AAEnabled ? 1f : 0f);

        GLES20.glUniform1i(uSamplerHandle, 5);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE5);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
        //draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GlUtil.checkGlError("drawScreen end");
    }

    public void release() {
        GLES20.glDeleteProgram(program);
    }

    public void setTexId(int texId) {
        this.texId = texId;
    }

    public boolean isAAEnabled() {
        return AAEnabled;
    }

    public void setAAEnabled(boolean AAEnabled) {
        this.AAEnabled = AAEnabled;
    }

    public void setStreamSize(int streamWidth, int streamHeight) {
        this.streamWidth = streamWidth;
        this.streamHeight = streamHeight;
    }
}
