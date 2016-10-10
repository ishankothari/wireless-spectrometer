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
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Ishan on 19-06-2015.
 */
public class DarkCalibrationActivity extends ActionBarActivity implements View.OnClickListener {

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
    String darkReading;
    Button bCaptureDarkReading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dark_calibration);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));

        bCaptureDarkReading = (Button)findViewById(R.id.bCaptureDarkReading);
        bCaptureDarkReading.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bCaptureDarkReading:

                ringProgressDialog = ProgressDialog.show(this, "Please wait",
                        "Fetching data from the spectrometer", true);
                ringProgressDialog.setCancelable(false);

                bCaptureDarkReading.setEnabled(false);
                bCaptureDarkReading.setBackgroundColor(Color.parseColor("#F44336"));
                try {
                    sendData("s\n");
                }
                catch (IOException e) {

                }
                break;
        }
    }

    void findBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            bCaptureDarkReading.setEnabled(false);
            bCaptureDarkReading.setBackgroundColor(Color.parseColor("#F44336"));
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
            bCaptureDarkReading.setEnabled(false);
            bCaptureDarkReading.setBackgroundColor(Color.parseColor("#F44336"));
            Toast.makeText(this, "Error connecting to device, please ensure device is powered on and" +
                    " not already connected",Toast.LENGTH_LONG).show();
        }
        finally {

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
                                                    reading += dataArray[b];
                                                    if(b != 511){
                                                        reading += ",";
                                                    }
                                                }
                                                Log.d("Reading", reading);
                                                darkReading = reading;
                                                Intent intent = new Intent(DarkCalibrationActivity.this, PlotDarkGraphActivity.class);
                                                intent.putExtra("dark-reading",darkReading);
                                                ringProgressDialog.dismiss();
                                                startActivity(intent);
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
