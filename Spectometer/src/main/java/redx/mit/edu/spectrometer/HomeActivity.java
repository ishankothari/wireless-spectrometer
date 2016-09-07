package redx.mit.edu.spectrometer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class HomeActivity extends ActionBarActivity implements View.OnClickListener {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_1 = 100;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_2 = 101;

    int rowCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));

        SharedPreferences sharedPreferences = getSharedPreferences("row-details-shared-preferences", 0);
        rowCount = sharedPreferences.getInt("row-count", 0);

        Button bCaptureImage = (Button) findViewById(R.id.bCaptureImage);
        bCaptureImage.setOnClickListener(this);
        bCaptureImage.setText("Start capture for " + rowCount);

        Button bGenerateExcelFile = (Button) findViewById(R.id.bGenerateExcelFile);
        bGenerateExcelFile.setOnClickListener(this);

        Button bStartDarkReadingActivity =(Button)findViewById(R.id.bStartDarkReadingActivity);
        bStartDarkReadingActivity.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bCaptureImage:
                Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                SharedPreferences sharedPreferences = getSharedPreferences("image-count",0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                int currentCount = sharedPreferences.getInt("image-count",0);
                File file = new File(Environment.getExternalStorageDirectory()+ File.separator + "Spectrometer/img-"+rowCount+"-reference0.jpg");
                Log.d("File check for tablet",file.toString());
                currentCount++;
                editor.putInt("image-count",currentCount);
                editor.commit();
                intent1.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(intent1, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_1);
                break;
            case R.id.bStartDarkReadingActivity:
                Intent intent2 = new Intent(this,DarkCalibrationActivity.class);
                startActivity(intent2);
                break;
            case R.id.bGenerateExcelFile:
                generateExcelFile();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_1) {
            Log.d("RESULT CODE",resultCode+"");
            if (resultCode == RESULT_OK) {
                Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                SharedPreferences sharedPreferences = getSharedPreferences("image-count",0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                int currentCount = sharedPreferences.getInt("image-count",0);
                File file = new File(Environment.getExternalStorageDirectory()+ File.separator + "Spectrometer/img-"+rowCount+"-reference1.jpg");
                Log.d("File check for tablet",file.toString());
                currentCount++;
                editor.putInt("image-count", currentCount);
                editor.commit();
                intent1.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(intent1, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_2);
                Toast.makeText(this,"Image Stored!",Toast.LENGTH_LONG).show();

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
            }
        }else {
            Intent intent = new Intent(HomeActivity.this, OptionsActivity.class);
            startActivity(intent);
        }
    }

    public void generateExcelFile() {
        DatabaseHandler databaseHandler = new DatabaseHandler(this);
        databaseHandler.open();
        DatabaseDataFormat[] results = databaseHandler.getData();
        databaseHandler.close();

        // check if available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.w("FileUtils", "Storage not available or read only");
            return;
        }

        //New Workbook
        Workbook wb = new HSSFWorkbook();

        Cell c = null;

        //New Sheet
        Sheet sheet1 = null;
        sheet1 = wb.createSheet("Data");

        // Generate column headings
        Row row = sheet1.createRow(0);

        c = row.createCell(0);
        c.setCellValue(DatabaseHandler.KEY_ROW_ID);

        c = row.createCell(1);
        c.setCellValue(DatabaseHandler.KEY_NAME);

        c = row.createCell(2);
        c.setCellValue(DatabaseHandler.KEY_SPECTRUM_0);

        c = row.createCell(3);
        c.setCellValue(DatabaseHandler.KEY_SPECTRUM_1);

        c = row.createCell(4);
        c.setCellValue(DatabaseHandler.KEY_SPECTRUM_2);

        c = row.createCell(5);
        c.setCellValue(DatabaseHandler.KEY_SPECTRUM_3);

        c = row.createCell(6);
        c.setCellValue(DatabaseHandler.KEY_DARK_SPECTRUM);

        sheet1.setColumnWidth(0, (15 * 500));
        sheet1.setColumnWidth(1, (15 * 500));
        sheet1.setColumnWidth(2, (15 * 500));
        sheet1.setColumnWidth(3, (15 * 500));
        sheet1.setColumnWidth(4, (15 * 500));
        sheet1.setColumnWidth(5, (15 * 500));
        sheet1.setColumnWidth(6, (15 * 500));

        for (int i = 0; i < results.length ; i++) {
            row = sheet1.createRow(i+1);
            c = row.createCell(0);
            c.setCellValue(results[i].getRow());
            c = row.createCell(1);
            c.setCellValue(results[i].getName());
            c = row.createCell(2);
            c.setCellValue(results[i].getSpectrum0());
            c = row.createCell(3);
            c.setCellValue(results[i].getSpectrum1());
            c = row.createCell(4);
            c.setCellValue(results[i].getSpectrum2());
            c = row.createCell(5);
            c.setCellValue(results[i].getSpectrum3());
            c = row.createCell(6);
            c.setCellValue(results[i].getDarkReading());
        }
        // Create a path where we will place our List of objects on external storage
        //File file = new File(Environment.getExternalStorageDirectory()+ File.separator + "spectrumData.xls");
        File file = new File(getExternalFilesDir(null), "file.xls");
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            wb.write(os);
            Log.w("FileUtils", "Writing file" + file);
            Toast.makeText(this,"Data exported to excel file",Toast.LENGTH_LONG).show();
        }
        catch (IOException e) {
            Log.w("FileUtils", "Error writing " + file, e);
        }
        catch (Exception e) {
            Log.w("FileUtils", "Failed to save file", e);
        }
        finally {
            try {
                if (null != os)
                    os.close();
            }
            catch (Exception ex) {
            }
        }
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }
}
