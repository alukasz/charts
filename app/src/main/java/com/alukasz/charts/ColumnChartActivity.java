package com.alukasz.charts;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ColumnChartActivity extends AppCompatActivity {
    private int[][] chartValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_column_chart);

        Button btnDraw = (Button) findViewById(R.id.btnDraw);
        btnDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText textChartValues = (EditText) findViewById(R.id.textChartValues);
                if (parseChartValues(textChartValues.getText().toString()));
                {
                    LinearLayout chartContainer = (LinearLayout) findViewById(R.id.chartContainer);
                    chartContainer.removeAllViews();

                    ColumnChart columnChart = new ColumnChart(ColumnChartActivity.this, chartValues,
                            chartContainer.getWidth(), chartContainer.getHeight());

                    chartContainer.addView(columnChart);
                    columnChart.draw();
                }
            }
        });
    }

    private boolean parseChartValues(String values)
    {
        String[] pairsOfValues = values.split(",");
        chartValues = new int[pairsOfValues.length][2];
        for (int i = 0; i < pairsOfValues.length; i++) {

            String[] pair = pairsOfValues[i].split(":");
            try {
                chartValues[i][0] = Integer.valueOf(pair[0]);
                chartValues[i][1] = Integer.valueOf(pair[1]);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to parse chart values. Please check data.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }
}
