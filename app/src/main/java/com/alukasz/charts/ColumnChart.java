package com.alukasz.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class ColumnChart extends View implements Runnable {
    Context context;

    private Thread animator;

    // these should be an object
    private int[][] values;
    private Rect[][] columns;
    private float[][] columnsGrowth; // how much each column will grow in frame

    private int step = 1; // step on vertical scale

    private int canvasWidth;
    private int canvasHeight;

    private final int delay = 20; // 50fps
    private final int duration = 2000; // 2 second to draw chart
    private int frame; // current frame;
    private int frames; // number of frames

    private int bounceDuration = 2000;
    private int bounceFrame = 0;
    private int bounceFrames;
    private int bounceAmplitude = 25; // max amplitude of bounce
    private double bounces = 1.5; // number of bounces

    private boolean drawing = false; // true when columns are growing
    private boolean bouncing = false; // true when columns are bouncing

    private Paint paint;

    private int leftOffset;
    private int bottomOffset;
    private int topOffset;

    private int columnWidth;
    private int columnOffset = 10; // distance between columns in pair
    private int pairOffset = 40; // ditance between pairs

    private int maxValue; // max value on vertical scale
    private int heightPerPoint; // how much px is 1 value


    public ColumnChart(Context context, int[][] values, int width, int height) {
        super(context);
        this.context = context;
        this.values = values;

        frame = 1;
        frames = duration / delay;

        bounceFrames = bounceDuration / delay;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(24);
        paint.setStrokeWidth(1);

        canvasWidth = width;
        canvasHeight = height;
        leftOffset = width / 10;
        bottomOffset = leftOffset;
        topOffset = 45;

        calculateColumnWidth();
        calculateHeightPerPoint();
        calculateColumnsGrowthPerFrame();
        createColumns();
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
            // re-draw
            postInvalidate();
            updateColumns();
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }
        }

        while (bouncing) {
            if (bounceFrame >= bounceFrames) {
                bouncing = false;
            } else {
                bounceFrame = bounceFrame + 1;
            }
            // re-draw
            postInvalidate();
            bounce();
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
        drawBackground(canvas);

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

        if (frame == frames) {
            drawValue(canvas);
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    bounceFrame = 0;
                    bouncing = true;
                    animator = new Thread(ColumnChart.this);
                    animator.start();
                }
            });
        }
    }

    private void drawBackground(Canvas canvas) {
        paint.setColor(Color.BLACK);

        for (int i = step; i <= maxValue; i = i + step) {
            int y = canvasHeight - bottomOffset - i * heightPerPoint + 9; // + 9 - try to make number on the middle of line TODO remove magic number
            canvas.drawLine(5, y, 25, y, paint);
            canvas.drawText(String.valueOf(i), 32, y, paint);
        }

        for (int i = 0; i < values.length; i++) {
            int top = canvasHeight - bottomOffset / 2;
            int left = getColumnLeft(i, 0) + columnWidth + columnOffset / 2 - 8; // -8 - try to make number on the middle of group TODO remove magic number
            canvas.drawText(String.valueOf((char) (i + 65)), left, top, paint);
        }
    }

    private void drawValue(Canvas canvas) {
        paint.setColor(Color.BLACK);

        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[i].length; j++) {
                int left = getColumnLeft(i, j) + columnWidth / 2 - 11; // - 11 try to make on the middle of column TODO remove magic number
                int top  = getColumnTop(i, j) - 20 + getBounce(); // -20 offset between column top and value
                canvas.drawText(String.valueOf(values[i][j]), left, top, paint);
            }
        }
    }

    private void calculateColumnsGrowthPerFrame() {
        columnsGrowth = new float[values.length][2];
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[i].length; j++) {
                columnsGrowth[i][j] = values[i][j] * heightPerPoint / (float)frames;
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
        step = maxValue / 10 + 1;
        if (step % 2 != 0) {
            step += 1; // i don't like odd step :(
        }
        while (maxValue % step != 0) {
            maxValue += 1;
        }

        heightPerPoint = (canvasHeight - bottomOffset - topOffset) / maxValue;
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
        int drawingWidth = canvasWidth - leftOffset; // remove left offset

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

    private int getColumnTop(int pair, int column)
    {
        return (int)(canvasHeight - bottomOffset - (frame * columnsGrowth[pair][column]));
    }

    private int getBounce()
    {
        double angle = (360.0 * bounces / bounceFrames) * bounceFrame;

        return (int) (Math.sin(Math.toRadians(angle)) * bounceAmplitude);
    }

    private void updateColumns() {
        for (int i = 0; i < columns.length; i++) {
            for (int j = 0; j < columns[i].length; j++) {
                columns[i][j].top = getColumnTop(i, j);
            }
        }
    }

    private void bounce() {
        for (int i = 0; i < columns.length; i++) {
            for (int j = 0; j < columns[i].length; j++) {
                columns[i][j].top = getColumnTop(i, j) + getBounce();
            }
        }
    }
}
