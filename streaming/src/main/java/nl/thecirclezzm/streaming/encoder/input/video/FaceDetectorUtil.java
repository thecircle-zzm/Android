package nl.thecirclezzm.streaming.encoder.input.video;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.camera2.params.Face;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * Created by pedro on 17/10/18.
 */

public class FaceDetectorUtil {

    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FaceParsed camera2Parse(@NonNull Face face, @NonNull View view, @NonNull PointF scale, int rotation,
                                   boolean isFrontCamera) {
        //Parse face
        RectF rect = new RectF(face.getBounds());
        Matrix matrix = new Matrix();
        matrix.setScale(isFrontCamera ? -1 : 1, 1);
        matrix.postRotate(rotation);
        matrix.postScale(1f, 1f);
        matrix.postTranslate(view.getWidth(), view.getHeight());
        matrix.mapRect(rect);
        return getFace(rect, scale, view);
    }

    @NonNull
    public FaceParsed camera1Parse(@NonNull Camera.Face face, @NonNull View view, @NonNull PointF scale, int rotation,
                                   boolean isFrontCamera) {
        //Parse face
        RectF rect = new RectF(face.rect);
        Matrix matrix = new Matrix();
        matrix.setScale(isFrontCamera ? -1 : 1, 1);
        matrix.postRotate(rotation);
        matrix.postScale(view.getWidth() / 2000f, view.getHeight() / 2000f);
        matrix.postTranslate(view.getWidth() / 2f, view.getHeight() / 2f);
        matrix.mapRect(rect);
        return getFace(rect, scale, view);
    }

    private FaceParsed getFace(RectF rectF, PointF scale, View view) {
        //Position
        float posX = rectF.centerX() * 100 / view.getWidth();
        float posY = rectF.centerY() * 100 / view.getHeight();
        PointF positionParsed = new PointF(posX - scale.x / 2, posY - scale.y / 2);
        //Scale
        float scaleX = rectF.width() * 100 / view.getWidth();
        float scaleY = rectF.height() * 100 / view.getHeight();
        PointF scaleParsed = new PointF(scaleX, scaleY);
        return new FaceParsed(positionParsed, scaleParsed);
    }

    public class FaceParsed {
        private PointF position;
        private PointF scale;

        public FaceParsed(PointF position, PointF scale) {
            this.position = position;
            this.scale = scale;
        }

        public PointF getPosition() {
            return position;
        }

        public void setPosition(PointF position) {
            this.position = position;
        }

        public PointF getScale() {
            return scale;
        }

        public void setScale(PointF scale) {
            this.scale = scale;
        }
    }
}