package redx.mit.edu.spectrometer;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by Ishan on 14-12-2015.
 */
public class NewCameraTestActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        }
        catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
}
