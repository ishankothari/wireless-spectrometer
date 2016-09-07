package redx.mit.edu.spectrometer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

/**
 * Created by Ishan on 19-06-2015.
 */
public class PlotDarkGraphActivity extends ActionBarActivity implements View.OnClickListener {

    LineChart chart;
    int[] darkData;
    String intentString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot_dark_graph);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));

        chart = (LineChart) findViewById(R.id.lineChartDarkReading);
        chart.setDescription("Dark Calibration");

        darkData = new int[512];
        intentString = getIntent().getStringExtra("dark-reading");
        String[] darkDataStringArray = intentString.split(",");
        for (int i = 0;i< 512; i++){
            darkData[i] = Integer.parseInt(darkDataStringArray[i]);
        }

        LinearLayout llDarkCancel = (LinearLayout)findViewById(R.id.llDarkCancel);
        llDarkCancel.setOnClickListener(this);

        LinearLayout llDarkAccept = (LinearLayout)findViewById(R.id.llDarkAccept);
        llDarkAccept.setOnClickListener(this);

        populate();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.llDarkAccept:
                SharedPreferences sharedPreferences = getSharedPreferences("dark-reading-stored",0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("dark-reading",intentString);
                editor.commit();
                break;
        }
        Intent intent = new Intent(PlotDarkGraphActivity.this,DarkCalibrationActivity.class);
        startActivity(intent);
    }

    void populate() {
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < 256; i++) {
            xVals.add((XAxisValues.xValuesArray[i]+"").substring(0,3));
        }

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();

        ArrayList<Entry> values1 = new ArrayList<Entry>();

        for (int i = 0; i < 512; i = i +2) {
            double val = darkData[i];
            values1.add(new Entry((float) val, i/2));
        }

        LineDataSet d = new LineDataSet(values1, "Spectrum");
        d.setLineWidth(2.5f);
        d.setDrawCircles(false);
        d.setDrawValues(false);

        d.setColor(Color.parseColor("#4CAF50"));
        dataSets.add(d);

        LineData data = new LineData(xVals, dataSets);
        chart.setData(data);
        chart.invalidate();
    }
}
