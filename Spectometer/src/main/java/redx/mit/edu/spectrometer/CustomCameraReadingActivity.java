package redx.mit.edu.spectrometer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

/**
 * Created by Ishan Kothari on 08-06-2015.
 */
public class CustomCameraReadingActivity extends ActionBarActivity implements View.OnClickListener {

    Bundle bundle;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    InputStream mmInputStream;
    OutputStream mmOutputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int dataIndex;
    volatile boolean stopWorker;
    ProgressDialog ringProgressDialog;
    int[] dataArray;
    String referenceReading;
    String objectReading1,objectReading2,objectReading3,objectReading0;
    int turn = 1;
    int time;
    Button bCaptureReferenceReading, bCaptureobjectReading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_camera_reading);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));

        bundle = getIntent().getBundleExtra("options");

        boolean referenceState = bundle.getBoolean("reference-state");
        time = bundle.getInt("time");

        bCaptureReferenceReading = (Button)findViewById(R.id.bCaptureReferenceReading);
        bCaptureReferenceReading.setOnClickListener(this);
        bCaptureReferenceReading.setEnabled(false);
        bCaptureReferenceReading.setBackgroundColor(Color.parseColor("#F44336"));

        bCaptureobjectReading = (Button)findViewById(R.id.bCaptureObjectReading);
        bCaptureobjectReading.setOnClickListener(this);
        bCaptureobjectReading.setEnabled(false);
        bCaptureobjectReading.setBackgroundColor(Color.parseColor("#F44336"));

        if(referenceState == false) {
            LinearLayout llobjectReading = (LinearLayout)findViewById(R.id.llObjectReading);
            llobjectReading.setVisibility(View.VISIBLE);
            bCaptureobjectReading.setBackgroundColor(Color.parseColor("#3F51B5"));
            bCaptureobjectReading.setEnabled(true);
        }
        else {
            LinearLayout llReferenceReading = (LinearLayout)findViewById(R.id.llReferenceReading);
            llReferenceReading.setVisibility(View.VISIBLE);
            bCaptureReferenceReading.setBackgroundColor(Color.parseColor("#3F51B5"));
            bCaptureReferenceReading.setEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        ringProgressDialog = ProgressDialog.show(this, "Initializing bluetooth connection", "Please " +
                "wait while the connection to the device is established", true);
        ringProgressDialog.setCancelable(false);

        findBT();
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            closeBT();
        }
        catch (IOException e) {

        }
    }

    @Override
    public void onClick(View v) {
        LinearLayout oldView, newView;
        switch (v.getId()) {
            case R.id.bCaptureReferenceReading:
                turn = 1;
                try {
                    sendData("s" + time + "#\n");
                }
                catch (IOException e){

                }
                oldView = (LinearLayout)findViewById(R.id.llReferenceReading);
                newView = (LinearLayout)findViewById(R.id.llObjectReading);
                switchViews(oldView,newView);
                break;
            case  R.id.bCaptureObjectReading:
                if(turn < 2) {
                    turn = 2;
                }else {
                    turn++;
                }
                bCaptureobjectReading.setEnabled(false);
                bCaptureobjectReading.setBackgroundColor(Color.parseColor("#F44336"));
                try {
                    sendData("s"+time+"#\n");
                }
                catch (IOException e) {

                }
                break;
        }
    }

    public void switchViews(View oldView, View newView) {
        oldView.setVisibility(View.GONE);
        try {
            Thread.sleep(700);
        }
        catch (InterruptedException e) {

        }
        finally {
            newView.setVisibility(View.VISIBLE);
        }
    }

    void findBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            bCaptureReferenceReading.setEnabled(false);
            bCaptureReferenceReading.setBackgroundColor(Color.parseColor("#F44336"));

            bCaptureobjectReading.setEnabled(false);
            bCaptureobjectReading.setBackgroundColor(Color.parseColor("#F44336"));
            Toast.makeText(this, "Bluetooth not available on your device",Toast.LENGTH_LONG).show();
        }

        if(!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            while (!mBluetoothAdapter.isEnabled());
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        boolean deviceSelected = false;

        if(pairedDevices.size() > 0) {
            final BluetoothDevice[] bluetoothDevices =  new BluetoothDevice[pairedDevices.size()];

            AlertDialog.Builder alertDialogBuidler = new AlertDialog.Builder(this);
            alertDialogBuidler.setTitle("Please select the paired spectrometer device.");

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.select_dialog_singlechoice);

            int i = 0;
            for(BluetoothDevice device : pairedDevices) {
                Log.d("Paired devices", i + " - " + device.getName());
                arrayAdapter.add(device.getName());
                bluetoothDevices[i++] = device;
            }

            alertDialogBuidler.setNegativeButton("Close",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            ringProgressDialog.dismiss();
                            finish();
                        }
                    });

            alertDialogBuidler.setAdapter(arrayAdapter,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mmDevice = bluetoothDevices[i];
                            openBT();
                        }
                    });
            alertDialogBuidler.show();
        }
        else {
            Toast.makeText(this, "No Bluetooth devices are paired.", Toast.LENGTH_LONG).show();
        }
    }

    void openBT() {
        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();

            beginListenForData();
            Toast.makeText(this,"Bluetooth Connected!",Toast.LENGTH_LONG).show();
        }
        catch (Exception e) {
            bCaptureReferenceReading.setEnabled(false);
            bCaptureReferenceReading.setBackgroundColor(Color.parseColor("#F44336"));

            bCaptureobjectReading.setEnabled(false);
            bCaptureobjectReading.setBackgroundColor(Color.parseColor("#F44336"));
            Toast.makeText(this, "Error connecting to device, please ensure device is powered on and" +
                    " not already connected",Toast.LENGTH_LONG).show();
        }
        finally {
            ringProgressDialog.dismiss();
        }
    }

    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //ASCII code for newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {

                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++) {
                                byte b = packetBytes[i];
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    String tempData = "";
                                    if(dataIndex == 0) {

                                    }
                                    for(int a = 0 ; a < data.length() -1 ; a++) {
                                        tempData += data.charAt(a);
                                    }

                                    dataArray[dataIndex] = Integer.parseInt(tempData);
                                    dataIndex++;

                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            if(dataIndex == 512) {
                                                dataIndex = 0;
                                                String reading = "";
                                                for(int b = 0; b <512 ; b++) {
                                                    reading += dataArray[b]+",";
                                                }
                                                reading+=0;
                                                Log.d("Reading", reading);
                                                if(turn == 1) {
                                                    referenceReading = reading;
                                                    bCaptureobjectReading.setEnabled(true);
                                                    bCaptureobjectReading.setBackgroundColor(Color.parseColor("#3F51B5"));
                                                }
                                                else if(turn == 2) {
                                                    bCaptureobjectReading.setEnabled(true);
                                                    bCaptureobjectReading.setBackgroundColor(Color.parseColor("#3F51B5"));
                                                    objectReading0 = reading;
                                                    bCaptureobjectReading.setText("Capture Reading 1");
                                                }
                                                else if(turn == 3) {
                                                    bCaptureobjectReading.setEnabled(true);
                                                    bCaptureobjectReading.setBackgroundColor(Color.parseColor("#3F51B5"));
                                                    objectReading1 = reading;
                                                    bCaptureobjectReading.setText("Capture Reading 2");
                                                }
                                                else if(turn == 4) {
                                                    bCaptureobjectReading.setEnabled(true);
                                                    bCaptureobjectReading.setBackgroundColor(Color.parseColor("#3F51B5"));
                                                    objectReading2 = reading;
                                                    bCaptureobjectReading.setText("Capture Reading 3");
                                                }
                                                else if(turn == 5) {
                                                    objectReading3 = reading;
                                                    Intent intent = new Intent(CustomCameraReadingActivity.this,GraphViewActivity.class);
                                                    bundle.putString("reference-reading",referenceReading);
                                                    bundle.putString("reading-0",objectReading0);
                                                    bundle.putString("reading-1",objectReading1);
                                                    bundle.putString("reading-2",objectReading2);
                                                    bundle.putString("reading-3",objectReading3);
                                                    intent.putExtra("data",bundle);
                                                    startActivity(intent);
                                                }
                                            }
                                        }
                                    });
                                }
                                else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    void sendData(String msg) throws IOException {
        dataIndex = 0;
        dataArray = new int[512];
        mmOutputStream.write(msg.getBytes());
    }

    void closeBT() throws IOException {
        try {
            stopWorker = true;
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();
        }
        catch (Exception e) {

        }
    }
}