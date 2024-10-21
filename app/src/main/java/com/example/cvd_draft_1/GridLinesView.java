package com.example.cvd_draft_1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class GridLinesView extends View {
    private Paint paint;

    public GridLinesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(0x80FFFFFF); // 50% transparent white
        paint.setStrokeWidth(6); // Adjust the thickness of the lines
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        // Draw vertical lines
        canvas.drawLine(width / 3, 0, width / 3, height, paint);
        canvas.drawLine(2 * width / 3, 0, 2 * width / 3, height, paint);

        // Draw horizontal lines
        canvas.drawLine(0, height / 3, width, height / 3, paint);
        canvas.drawLine(0, 2 * height / 3, width, 2 * height / 3, paint);
    }
}