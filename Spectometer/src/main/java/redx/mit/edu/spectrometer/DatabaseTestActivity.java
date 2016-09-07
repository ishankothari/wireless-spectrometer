package redx.mit.edu.spectrometer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Created by Ishan on 18-06-2015.
 */
public class DatabaseTestActivity extends ActionBarActivity implements View.OnClickListener{

    int rowCount = 0;
    TextView tvRowCount;
    SharedPreferences.Editor editor;
    Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_test);

        tvRowCount= (TextView)findViewById(R.id.tvRowCount);
        SharedPreferences sharedPreferences = getSharedPreferences("row-details-shared-preferences",0);
        editor = sharedPreferences.edit();
        rowCount = sharedPreferences.getInt("row-count", 0);
        updateRowCount(false);

        random = new Random();

        Button bAddDatabaseRow = (Button)findViewById(R.id.bAddDatabaseRow);
        bAddDatabaseRow.setOnClickListener(this);

        Button bGenerateExcelFile = (Button)findViewById(R.id.bGenerateExcelFile);
        bGenerateExcelFile.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bAddDatabaseRow:
                addDummyRowToDB();
                break;
            case R.id.bGenerateExcelFile:
                generateExcelFile();
                break;
        }
    }

    public void updateRowCount(boolean increment) {
        if(increment == true){
            rowCount++;
            editor.putInt("row-count",rowCount);
            editor.commit();
        }
        tvRowCount.setText("So far " + rowCount + " entries in database");
    }

    public void addDummyRowToDB() {
        DatabaseHandler databaseHandler = new DatabaseHandler(this);
        databaseHandler.open();
        databaseHandler.createEntry(rowCount+"",""+random.nextDouble(),
                ""+random.nextDouble(),""+random.nextDouble(),""+random.nextDouble(),""+random.nextDouble());
        databaseHandler.close();
        updateRowCount(true);
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

        sheet1.setColumnWidth(0, (15 * 500));
        sheet1.setColumnWidth(1, (15 * 500));
        sheet1.setColumnWidth(2, (15 * 500));
        sheet1.setColumnWidth(3, (15 * 500));
        sheet1.setColumnWidth(4, (15 * 500));
        sheet1.setColumnWidth(5, (15 * 500));

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

        }
        // Create a path where we will place our List of objects on external storage
        //File file = new File(Environment.getExternalStorageDirectory()+ File.separator + "spectrumData.xls");
        File file = new File(getExternalFilesDir(null), "file.xls");
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            wb.write(os);
            Log.w("FileUtils", "Writing file" + file);
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