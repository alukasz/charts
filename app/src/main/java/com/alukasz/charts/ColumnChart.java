package com.alukasz.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import static android.R.attr.topOffset;

public class ColumnChart extends View implements Runnable {
    Context context;

    private Thread animator;

    int[][] values;
    Rect[][] columns;
    int[][] columnsGrowth; // how much each column will grow in frame

    private int canvasWidth;
    private int canvasHeight;

    private final int delay = 40; // 25fps
    private final int duration = 2000; // 2 second to draw chart
    private float step; // percent of column to draw in 1 frame
    private int frame; // current frame;
    private int frames; // number of frames

    private boolean drawing = false; //

    private Paint paint;

    private int leftOffset;
    private int bottomOffset;
    private int topOffset;

    private int columnWidth = 50; // TODO calculate basing on offsets and canvas size
    private int columnOffset = 10; // distance between columns in pair
    private int pairOffset = 40; // ditance between pairs

    private int maxValue;
    private int heightPerPoint;


    public ColumnChart(Context context, int[][] values, int width, int height) {
        super(context);
        this.context = context;
        this.values = values;

        step = delay / duration;
        frame = 1;
        frames = duration / delay;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(24);
        paint.setStrokeWidth(1);

        canvasWidth = width;
        canvasHeight = height;
        leftOffset = width / 10;
        bottomOffset = leftOffset;
        topOffset = 50;

        calculateColumnWidth();
        calculateHeightPerPoint();
        calculateColumnsGrowthPerFrame();
        createColumns();
    }

    private void calculateColumnsGrowthPerFrame() {
        columnsGrowth = new int[values.length][2];
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[i].length; j++) {
                columnsGrowth[i][j] = values[i][j] * heightPerPoint / frames;
            }
        }
    }

    private void calculateHeightPerPoint() {
        int max = 0;
        for (int[] pair : values) {
            for (int value : pair)
                if (value > max) {
                    max = value;
                }
        }

        maxValue = max;
        heightPerPoint = (canvasHeight - bottomOffset - topOffset) / max;
    }

    private void createColumns() {
        columns = new Rect[values.length][2];
        for (int i = 0; i < values.length; i++) {
            int bottom = canvasHeight - bottomOffset;
            int top = bottom;

            int left = getColumnLeft(i, 0);
            int right = left + columnWidth;
            Rect column_one = new Rect(left, top, right, bottom);
            columns[i][0] = column_one;

            left = getColumnLeft(i, 1);
            right = left + columnWidth;
            Rect column_two = new Rect(left, top, right, bottom);
            columns[i][1] = column_two;
        }
    }

    private void calculateColumnWidth()
    {
        int drawingWidth = canvasWidth - leftOffset; // left remove offset

        int pairs = values.length;
        int pairsTotalOffset = (pairs - 1) * pairOffset;
        int columnTotalOffset = (pairs) * columnOffset;

        columnWidth = (drawingWidth - pairsTotalOffset - columnTotalOffset) / (pairs * 2);
    }

    private int getColumnLeft(int pair, int column) {
        return leftOffset // initial offset
                + pair * pairOffset // offest between pairs
                + (pair + column) * columnOffset // offset between columns
                + (pair * 2 + column) * columnWidth; // width of all columns
    }

    private void updateColumns() {
        for (int i = 0; i < columns.length; i++) {
            for (int j = 0; j < columns[i].length; j++) {
                columns[i][j].top = columns[i][j].top  - columnsGrowth[i][j];
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

        int count = 1;
        for (Rect[] pair : columns) {
            for (Rect column : pair) {
                if (count++ % 2 == 0) {
                    paint.setColor(Color.RED);
                } else {
                    paint.setColor(Color.BLUE);
                }
                canvas.drawRect(column, paint);
            }
        }
    }

    // Called by onDraw to draw the background
    private void drawBackground(Paint paint, Canvas canvas) {
        paint.setColor(Color.BLACK);
        for (int i = 1; i <= maxValue; i++) {
            int y = canvasHeight - bottomOffset - i * heightPerPoint;
            canvas.drawLine(5, y, 25, y, paint);
            canvas.drawText(String.valueOf(i), 40, y + 9, paint); // + 9 - try to make number on the middle of line
        }
    }
}
