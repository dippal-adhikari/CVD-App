//package com.example.selfiesegmentation;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.util.AttributeSet;
//import android.view.View;
//
//public class OverlayView extends View {
//    private Bitmap overlayBitmap;
//    private final Paint paint = new Paint();
//
//    public OverlayView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    public void setOverlayBitmap(Bitmap bitmap) {
//        this.overlayBitmap = bitmap;
//        invalidate(); // Redraw the view
//    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        if (overlayBitmap != null) {
//            canvas.drawBitmap(overlayBitmap, 0, 0, paint);
//        }
//    }
//}

package com.example.cvd_draft_1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class OverlayView extends View {
    private Bitmap overlayBitmap;
    private final Paint paint = new Paint();

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOverlayBitmap(Bitmap bitmap) {
        this.overlayBitmap = bitmap;
        invalidate(); // Redraw the view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (overlayBitmap != null) {
            // Get the size of the view
            int canvasWidth = getWidth();
            int canvasHeight = getHeight();

            // Get the size of the bitmap
            int bitmapWidth = overlayBitmap.getWidth();
            int bitmapHeight = overlayBitmap.getHeight();

            // Scale the bitmap to match the size of the view
            Matrix matrix = new Matrix();
            float scaleX = (float) canvasWidth / bitmapWidth;
            float scaleY = (float) canvasHeight / bitmapHeight;
            matrix.setScale(scaleX, scaleY);

            // Draw the scaled bitmap
            canvas.drawBitmap(overlayBitmap, matrix, paint);
        }
    }
}

