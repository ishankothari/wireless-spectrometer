package redx.mit.edu.spectrometer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Ishan Kothari on 09-06-2015.
 */
public class GraphViewActivity extends ActionBarActivity implements View.OnClickListener{
    LineChart chart;
    int[] darkData;
    int[] referenceData;
    int[] data0,data1,data2,data3;
    boolean referenceState;
    SharedPreferences.Editor editor;
    int rowCount;
    float darkAverage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_view);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));

        chart = (LineChart) findViewById(R.id.lineChart);

        Bundle bundle = getIntent().getBundleExtra("data");

        referenceState = bundle.getBoolean("reference-state");

        darkData = new int[512];
        SharedPreferences sharedPreferences = getSharedPreferences("dark-reading-stored",0);
        String darkString = sharedPreferences.getString("dark-reading", "0");
        if (darkString.equals("0")) {
            Toast.makeText(this,"Please do a dark calibration first",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(GraphViewActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }

        String[] darkDataStringArray = darkString.split(",");
        for (int i = 0;i< 512; i++) {
            darkData[i] = Integer.parseInt(darkDataStringArray[i]);
            if (i % 2 == 0){
                darkAverage += darkData[i];
            }
        }

        darkAverage = darkAverage/256;

        if(referenceState == true) {
            referenceData = new int[512];
            String[] referenceDataStringArray = bundle.getString("reference-reading").split(",");
            for (int i = 0;i< 512; i++){
                referenceData[i] = Integer.parseInt(referenceDataStringArray[i]);
            }
        }

        data0 = new int[512];
        String[] dataStringArray0 = bundle.getString("reading-0").split(",");
        for (int i = 0;i< 512; i++) {
            data0[i] = Integer.parseInt(dataStringArray0[i]);
        }

        data1 = new int[512];
        String[] dataStringArray1 = bundle.getString("reading-1").split(",");
        for (int i = 0;i< 512; i++) {
            data1[i] = Integer.parseInt(dataStringArray1[i]);
        }

        data2 = new int[512];
        String[] dataStringArray2 = bundle.getString("reading-2").split(",");
        for (int i = 0;i< 512; i++) {
            data2[i] = Integer.parseInt(dataStringArray2[i]);
        }

        data3 = new int[512];
        String[] dataStringArray3 = bundle.getString("reading-3").split(",");
        for (int i = 0;i< 512; i++) {
            data3[i] = Integer.parseInt(dataStringArray3[i]);
        }

        LinearLayout llDataCancel = (LinearLayout)findViewById(R.id.llDataCancel);
        llDataCancel.setOnClickListener(this);

        LinearLayout llDataSave = (LinearLayout)findViewById(R.id.llDataSave);
        llDataSave.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populate();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.llDataSave:
                DatabaseHandler databaseHandler = new DatabaseHandler(this);
                databaseHandler.open();
                SharedPreferences sharedPreferences = getSharedPreferences("row-details-shared-preferences",0);
                editor = sharedPreferences.edit();
                rowCount = sharedPreferences.getInt("row-count", 0);
                databaseHandler.createEntry(""+rowCount, Arrays.toString(data0),
                        Arrays.toString(data1), Arrays.toString(data2), Arrays.toString(data3), Arrays.toString(darkData));
                databaseHandler.close();
                updateRowCount();
                Intent intent = new Intent(this,HomeActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.llDataCancel:
                Intent intent2 = new Intent(GraphViewActivity.this,HomeActivity.class);
                startActivity(intent2);
                finish();
                break;
        }
    }

    public void updateRowCount() {
            rowCount++;
            editor.putInt("row-count",rowCount);
            editor.commit();
    }

    void populate() {
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < 256; i++) {
            xVals.add((XAxisValues.xValuesArray[i]+"").substring(0,3));
        }

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();

        ArrayList<Entry> values1 = new ArrayList<Entry>();

        for (int i = 0; i < 512; i = i +2) {
            double val = data0[i];
            Log.d("0 reading value " + (i / 2), val + "");
            values1.add(new Entry((float) val, i/2));
        }

        LineDataSet lineDataSet0 = new LineDataSet(values1, "Reading 0");
        lineDataSet0.setLineWidth(2.5f);
        lineDataSet0.setDrawCircles(false);
        lineDataSet0.setDrawValues(false);
        lineDataSet0.setColor(Color.parseColor("#F44336"));
        dataSets.add(lineDataSet0);

        Log.d("Done!","Reading 0");

        ArrayList<Entry> values2 = new ArrayList<Entry>();

        for (int i = 0; i < 512; i = i +2) {
            double val = data1[i];
            values2.add(new Entry((float) val, i/2));
        }

        LineDataSet lineDataSet1 = new LineDataSet(values2, "Reading 1");
        lineDataSet1.setLineWidth(2.5f);
        lineDataSet1.setDrawCircles(false);
        lineDataSet1.setDrawValues(false);
        lineDataSet1.setColor(Color.parseColor("#9C27B0"));
        dataSets.add(lineDataSet1);

        Log.d("Done!", "Reading 1");

        ArrayList<Entry> values3 = new ArrayList<Entry>();

        for (int i = 0; i < 512; i = i +2) {
            double val = data2[i];
            values3.add(new Entry((float) val, i/2));
        }

        LineDataSet lineDataSet2 = new LineDataSet(values3, "Reading 2");
        lineDataSet2.setLineWidth(2.5f);
        lineDataSet2.setDrawCircles(false);
        lineDataSet2.setDrawValues(false);
        lineDataSet2.setColor(Color.parseColor("#3F51B5"));
        dataSets.add(lineDataSet2);

        Log.d("Done!", "Reading 2");

        ArrayList<Entry> values4 = new ArrayList<Entry>();

        for (int i = 0; i < 512; i = i +2) {
            double val = data3[i];
            values4.add(new Entry((float) val, i/2));
        }

        LineDataSet lineDataSet3 = new LineDataSet(values4, "Reading 3");
        lineDataSet3.setLineWidth(2.5f);
        lineDataSet3.setDrawCircles(false);
        lineDataSet3.setDrawValues(false);
        lineDataSet3.setColor(Color.parseColor("#03A9F4"));
        dataSets.add(lineDataSet3);

        Log.d("Done!", "Reading 3");

        if(referenceState == true) {
            ArrayList<Entry> values5 = new ArrayList<Entry>();

            for (int i = 0; i < 512; i = i + 2) {
                double val = referenceData[i];
                values5.add(new Entry((float) val, i/2));
            }

            LineDataSet d2 = new LineDataSet(values5, "Reference");
            d2.setLineWidth(2.5f);
            d2.setDrawCircles(false);
            d2.setDrawValues(false);

            d2.setColor(Color.parseColor("#009688"));
            dataSets.add(d2);
        }
        /*for (int z = 0; z < 2; z++) {

            ArrayList<Entry> values = new ArrayList<Entry>();

            for (int i = 0; i < 20; i++) {
                double val = (Math.random()*500);
                values.add(new Entry((float) val, i));
            }

            LineDataSet d = new LineDataSet(values, "DataSet " + (z + 1));
            d.setLineWidth(2.5f);
            d.setDrawCircles(false);
            d.setDrawValues(false);

            int color = mColors[z];
            d.setColor(color);
            d.setCircleColor(color);
            dataSets.add(d);
        }*/

        LineData data = new LineData(xVals, dataSets);
        chart.setData(data);
        chart.invalidate();
    }
}
