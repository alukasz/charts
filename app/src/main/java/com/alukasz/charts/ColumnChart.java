package com.alukasz.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class ColumnChart extends View implements Runnable {
    Context context;

    private Thread animator;

    int[][] values;
    Rect[][] columns;

    private int canvasWidth;
    private int canvasHeight;

    private final int delay = 40; // 25fps
    private final int duration = 2000; // 2 second to draw chart
    private float step; // percent of column to draw in 1 frame
    private int frame; // current frame;
    private int frames; // number of frames

    private boolean drawing = false; //

    private Paint paint;


    private int columnWidth = 50; // TODO calculate basing on offsets and canvas size
    private int columnOffset = 10; // distance between columns in pair
    private int pairOffset = 30; // ditance between pairs


    public ColumnChart(Context context, int[][] values, int width, int height) {
        super(context);
        this.context = context;
        this.values = values;

        step = delay / duration;
        frame = 1;
        frames = duration / delay;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(14);
        paint.setStrokeWidth(1);

        canvasWidth = width;
        canvasHeight = height;

        calculateColumnWidth();
        createColumns();
    }

    private void createColumns() {
        columns = new Rect[values.length][2];
        for (int i = 0; i < values.length; i++) {
            int left = getColumnLeft(i, 0);
            Rect column_one = new Rect(left, canvasHeight - 100, left + columnWidth, canvasHeight);
            columns[i][0] = column_one;

            left = getColumnLeft(i, 1);
            Rect column_two = new Rect(left, canvasHeight - 100, left + columnWidth, canvasHeight);
            columns[i][1] = column_two;
        }
    }

    private void calculateColumnWidth()
    {
        int drawingWidth = canvasWidth - 20; // left remove offset

        int pairs = values.length;
        int pairsTotalOffset = (pairs - 1) * pairOffset;
        int columnTotalOffset = (pairs) * columnOffset;

        columnWidth = (drawingWidth - pairsTotalOffset - columnTotalOffset) / (pairs * 2);
    }

    private int getColumnLeft(int pair, int column) {
        return 20 // initial offset
                + pair * pairOffset // offest between pairs
                + (pair + column) * columnOffset // offset between columns
                + (pair * 2 + column) * columnWidth; // width of all columns
    }

    private void updateColumns() {
        for (int i = 0; i < columns.length; i++) {
            for (int j = 0; j < columns[i].length; j++) {
                columns[i][j].top = columns[i][j].top - 2;
            }
        }
    }

    public void draw() {
        drawing = true;
        animator = new Thread(this);
        animator.start();
    }

    @Override
    public void run() {
        while (drawing) {
            if (frame >= frames) {
                drawing = false;
            } else {
                frame = frame + 1;
            }

            postInvalidate();

            updateColumns();



            // Wait then execute it again
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        canvasHeight = h;
        canvasWidth = w;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackground(paint, canvas);

        for (Rect[] pair : columns) {
            for (Rect column : pair) {
                canvas.drawRect(column, paint);
            }
        }
    }

    // Called by onDraw to draw the background
    private void drawBackground(Paint paint, Canvas canvas) {

    }
}
