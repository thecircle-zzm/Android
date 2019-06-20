package nl.thecirclezzm.streaming.encoder.input.gl.render.filters;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import nl.thecirclezzm.streaming.R;
import nl.thecirclezzm.streaming.encoder.input.gl.Sprite;
import nl.thecirclezzm.streaming.encoder.input.gl.TextureLoader;
import nl.thecirclezzm.streaming.encoder.input.gl.render.filters.BaseFilterRender;
import nl.thecirclezzm.streaming.encoder.utils.gl.GlUtil;
import nl.thecirclezzm.streaming.encoder.utils.gl.StreamObjectBase;
import nl.thecirclezzm.streaming.encoder.utils.gl.TranslateTo;

/**
 * Created by pedro on 03/08/18.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
abstract public class BaseObjectFilterRender extends BaseFilterRender {

    @NonNull
    protected final TextureLoader textureLoader = new TextureLoader();
    @NonNull
    private final FloatBuffer squareVertexObject;
    @NonNull
    private final Sprite sprite;
    protected int uAlphaHandle = -1;
    @NonNull
    protected int[] streamObjectTextureId = new int[]{-1};
    protected StreamObjectBase streamObject;
    protected float alpha = 1f;
    protected boolean shouldLoad = false;
    private int program = -1;
    private int aPositionHandle = -1;
    private int aTextureHandle = -1;
    private int aTextureObjectHandle = -1;
    private int uMVPMatrixHandle = -1;
    private int uSTMatrixHandle = -1;
    private int uSamplerHandle = -1;
    private int uObjectHandle = -1;

    public BaseObjectFilterRender() {
        //rotation matrix
        // X, Y, Z, U, V
        //bottom left
        //bottom right
        //top left
        //top right
        float[] squareVertexDataFilter = {
                // X, Y, Z, U, V
                -1f, -1f, 0f, 0f, 0f, //bottom left
                1f, -1f, 0f, 1f, 0f, //bottom right
                -1f, 1f, 0f, 0f, 1f, //top left
                1f, 1f, 0f, 1f, 1f, //top right
        };
        squareVertex = ByteBuffer.allocateDirect(squareVertexDataFilter.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        squareVertex.put(squareVertexDataFilter).position(0);
        sprite = new Sprite();
        float[] vertices = sprite.getTransformedVertices();
        squareVertexObject = ByteBuffer.allocateDirect(vertices.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        squareVertexObject.put(vertices).position(0);
        Matrix.setIdentityM(MVPMatrix, 0);
        Matrix.setIdentityM(STMatrix, 0);
    }

    @Override
    protected void initGlFilter(@NonNull Context context) {
        String vertexShader = GlUtil.getStringFromRaw(context, R.raw.object_vertex);
        String fragmentShader = GlUtil.getStringFromRaw(context, R.raw.object_fragment);

        program = GlUtil.createProgram(vertexShader, fragmentShader);
        aPositionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        aTextureHandle = GLES20.glGetAttribLocation(program, "aTextureCoord");
        aTextureObjectHandle = GLES20.glGetAttribLocation(program, "aTextureObjectCoord");
        uMVPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        uSTMatrixHandle = GLES20.glGetUniformLocation(program, "uSTMatrix");
        uSamplerHandle = GLES20.glGetUniformLocation(program, "uSampler");
        uObjectHandle = GLES20.glGetUniformLocation(program, "uObject");
        uAlphaHandle = GLES20.glGetUniformLocation(program, "uAlpha");
    }

    @Override
    protected void drawFilter() {
        if (shouldLoad) {
            streamObjectTextureId = textureLoader.load();
            shouldLoad = false;
        }

        GLES20.glUseProgram(program);

        squareVertex.position(SQUARE_VERTEX_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false,
                SQUARE_VERTEX_DATA_STRIDE_BYTES, squareVertex);
        GLES20.glEnableVertexAttribArray(aPositionHandle);

        squareVertex.position(SQUARE_VERTEX_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(aTextureHandle, 2, GLES20.GL_FLOAT, false,
                SQUARE_VERTEX_DATA_STRIDE_BYTES, squareVertex);
        GLES20.glEnableVertexAttribArray(aTextureHandle);

        squareVertexObject.position(SQUARE_VERTEX_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(aTextureObjectHandle, 2, GLES20.GL_FLOAT, false,
                2 * FLOAT_SIZE_BYTES, squareVertexObject);
        GLES20.glEnableVertexAttribArray(aTextureObjectHandle);

        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, MVPMatrix, 0);
        GLES20.glUniformMatrix4fv(uSTMatrixHandle, 1, false, STMatrix, 0);
        //Sampler
        GLES20.glUniform1i(uSamplerHandle, 4);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, previousTexId);
        //Object
        GLES20.glUniform1i(uObjectHandle, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    }

    @Override
    public void release() {
        GLES20.glDeleteProgram(program);
        //DeleteTextures should be called in main thread or you will have issues.
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                GLES20.glDeleteTextures(streamObjectTextureId.length, streamObjectTextureId, 0);
                streamObjectTextureId = new int[]{-1};
            }
        });
        sprite.reset();
        if (streamObject != null) streamObject.recycle();
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setScale(float scaleX, float scaleY) {
        sprite.scale(scaleX, scaleY);
        squareVertexObject.put(sprite.getTransformedVertices()).position(0);
    }

    public void setPosition(float x, float y) {
        sprite.translate(x, y);
        squareVertexObject.put(sprite.getTransformedVertices()).position(0);
    }

    public PointF getScale() {
        return sprite.getScale();
    }

    public PointF getPosition() {
        return sprite.getTranslation();
    }

    public void setPosition(@NonNull TranslateTo positionTo) {
        sprite.translate(positionTo);
        squareVertexObject.put(sprite.getTransformedVertices()).position(0);
    }

    public void setDefaultScale(int streamWidth, int streamHeight) {
        sprite.scale(streamObject.getWidth() * 100f / streamWidth,
                streamObject.getHeight() * 100f / streamHeight);
        squareVertexObject.put(sprite.getTransformedVertices()).position(0);
    }
}
