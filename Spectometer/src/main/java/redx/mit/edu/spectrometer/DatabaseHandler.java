package redx.mit.edu.spectrometer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Ishan on 18-06-2015.
 */
public class DatabaseHandler {

    public static final String KEY_ROW_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_SPECTRUM_0 = "spectrum_0";
    public static final String KEY_SPECTRUM_1 = "spectrum_1";
    public static final String KEY_SPECTRUM_2 = "spectrum_2";
    public static final String KEY_SPECTRUM_3 = "spectrum_3";
    public static final String KEY_DARK_SPECTRUM = "dark_spectrum";

    public static final String DATABASE_NAME = "SpectrumDataCollectionDB";
    public static final String TABLE_NAME = "SpectrumCollection";
    public static final int DATABASE_VERSION = 1;

    Context context;
    SQLiteDatabase sqLiteDatabase;
    DatabaseHelper databaseHelper;

    public DatabaseHandler() {

    }

    public DatabaseHandler(Context context) {
        this.context = context;
    }

    public DatabaseHandler open() {
        databaseHelper = new DatabaseHelper(context);
        sqLiteDatabase = databaseHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        databaseHelper.close();
    }

    public void createEntry(String name, String spectrum0, String spectrum1
            , String spectrum2, String spectrum3,String darkSpectrum) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NAME,name);
        contentValues.put(KEY_SPECTRUM_0,spectrum0);
        contentValues.put(KEY_SPECTRUM_1,spectrum1);
        contentValues.put(KEY_SPECTRUM_2,spectrum2);
        contentValues.put(KEY_SPECTRUM_3,spectrum3);
        contentValues.put(KEY_DARK_SPECTRUM,darkSpectrum);
        sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        Toast.makeText(context,name+" added",Toast.LENGTH_SHORT).show();
    }

    public DatabaseDataFormat[] getData() {
        String[] columns = {KEY_ROW_ID,KEY_NAME,KEY_SPECTRUM_0,
                KEY_SPECTRUM_1,KEY_SPECTRUM_2,KEY_SPECTRUM_2,
        KEY_SPECTRUM_3,KEY_DARK_SPECTRUM};
        Cursor cursor = sqLiteDatabase.query(TABLE_NAME,columns,null,null,null,null,null);

        DatabaseDataFormat[] result = new DatabaseDataFormat[cursor.getCount()];

        int rowIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int nameIndex = cursor.getColumnIndex(KEY_NAME);
        int spectrum0Index = cursor.getColumnIndex(KEY_SPECTRUM_0);
        int spectrum1Index = cursor.getColumnIndex(KEY_SPECTRUM_1);
        int spectrum2Index = cursor.getColumnIndex(KEY_SPECTRUM_2);
        int spectrum3Index = cursor.getColumnIndex(KEY_SPECTRUM_3);
        int darkSpectrumIndex = cursor.getColumnIndex(KEY_DARK_SPECTRUM);

        int i = 0;

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
            result[i] = new DatabaseDataFormat();
            result[i].setRow(cursor.getInt(rowIndex));
            result[i].setName(cursor.getString(nameIndex));
            result[i].setSpectrum0(cursor.getString(spectrum0Index));
            result[i].setSpectrum1(cursor.getString(spectrum1Index));
            result[i].setSpectrum2(cursor.getString(spectrum2Index));
            result[i].setSpectrum3(cursor.getString(spectrum3Index));
            result[i].setDarkReading(cursor.getString(darkSpectrumIndex));
            Log.d("Row " + i, result[i].toString());
            i++;
        }
        return result;
    }


    public class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            String query = "CREATE TABLE " + TABLE_NAME + "( " +
                    KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_NAME + " TEXT, " +
                    KEY_SPECTRUM_0 + " TEXT, " +
                    KEY_SPECTRUM_1 + " TEXT, " +
                    KEY_SPECTRUM_2 + " TEXT, " +
                    KEY_SPECTRUM_3 + " TEXT, " +
                    KEY_DARK_SPECTRUM + " TEXT)";
            sqLiteDatabase.execSQL(query);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }
}
