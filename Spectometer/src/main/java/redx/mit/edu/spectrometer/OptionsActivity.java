package redx.mit.edu.spectrometer;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * Created by Ishan Kothari on 10-06-2015.
 */
public class OptionsActivity extends ActionBarActivity implements View.OnClickListener {


    int time = 1024;
    RadioGroup rgSingleOrContinuous;
    TextView tvIntegrationTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));
        rgSingleOrContinuous = (RadioGroup)findViewById(R.id.rgSingleOrContinuous);
        rgSingleOrContinuous.check(R.id.rbSingleShot);

        Button bBeginSpectrumCapture = (Button)findViewById(R.id.bBeginSpectrumCapture);
        bBeginSpectrumCapture.setOnClickListener(this);

        Button bPlus1ms = (Button)findViewById(R.id.bPlus1ms);
        bPlus1ms.setOnClickListener(this);

        Button bMinus1ms = (Button)findViewById(R.id.bMinus1ms);
        bMinus1ms.setOnClickListener(this);

        Button bPlus10ms = (Button)findViewById(R.id.bPlus10ms);
        bPlus10ms.setOnClickListener(this);

        Button bMinus10ms = (Button)findViewById(R.id.bMinus10ms);
        bMinus10ms.setOnClickListener(this);

        Button bPlus100ms = (Button)findViewById(R.id.bPlus100ms);
        bPlus100ms.setOnClickListener(this);

        Button bMinus100ms = (Button)findViewById(R.id.bMinus100ms);
        bMinus100ms.setOnClickListener(this);

        tvIntegrationTime = (TextView)findViewById(R.id.tvIntegrationTime);
        tvIntegrationTime.setText(time+"");
    }

    @Override
    public void onClick(View v) {
        String spectrumViewType="";
        switch (rgSingleOrContinuous.getCheckedRadioButtonId()) {
            case R.id.rbSingleShot:
                spectrumViewType = "single";
                break;
            case R.id.rbContinuous:
                spectrumViewType = "continuous";
                break;
        }

        switch (v.getId()) {
            case R.id.bPlus1ms:
                time++;
                break;
            case R.id.bMinus1ms:
                time--;
                break;
            case R.id.bPlus10ms:
                time += 10;
                break;
            case R.id.bMinus10ms:
                time -= 10;
                break;
            case R.id.bPlus100ms:
                time += 100;
                break;
            case R.id.bMinus100ms:
                time -= 100;
                break;
            case R.id.bBeginSpectrumCapture:
                CheckBox cbReference = (CheckBox)findViewById(R.id.cbReference);
                boolean referenceState = cbReference.isChecked();

                Bundle bundle = new Bundle();
                bundle.putInt("time",time);
                bundle.putString("spectrum-view-type",spectrumViewType);
                bundle.putBoolean("reference-state",referenceState);

                Intent intent = new Intent(OptionsActivity.this,CustomCameraReadingActivity.class);
                intent.putExtra("options",bundle);
                startActivity(intent);
                break;
        }

        if(time < 1024) {
            time = 1024;
        }

        tvIntegrationTime.setText(time + "");

    }
}
