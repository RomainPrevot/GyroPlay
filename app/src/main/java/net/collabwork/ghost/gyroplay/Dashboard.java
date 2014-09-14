package net.collabwork.ghost.gyroplay;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;


public class Dashboard extends Activity implements SensorEventListener, View.OnClickListener {

    private Socket socket;

    private SensorManager sensorManager;
    private Sensor sensor;
    private TextView txtX;
    private TextView txtY;
    private TextView txtZ;
    private TextView txtT;
    private TextView txtHost;
    private Button btConnect;

    private float[] origin = new float[4];
    private boolean originInit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        txtX = (TextView) findViewById(R.id.txtX);
        txtY = (TextView) findViewById(R.id.txtY);
        txtZ = (TextView) findViewById(R.id.txtZ);
        txtT = (TextView) findViewById(R.id.txtT);
        txtHost = (TextView) findViewById(R.id.txtHost);
        btConnect = (Button) findViewById(R.id.btConnect);

        // http://developer.android.com/guide/topics/sensors/sensors_motion.html#sensors-motion-rotate
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        btConnect.setOnClickListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        originInit = false;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ioe) {
                // ignored
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (!originInit) {
            Log.d("Ghost", "init");
            System.arraycopy(sensorEvent.values, 0, origin, 0, 4);
            originInit = true;
        }
        float x = sensorEvent.values[0] - origin[0] * 100;
        float y = sensorEvent.values[1] - origin[1] * 100;
        float z = sensorEvent.values[2] - origin[2] * 100;

        txtX.setText(String.valueOf(x));
        txtY.setText(String.valueOf(y));
        txtZ.setText(String.valueOf(z));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onClick(View view) {
        String host = txtHost.getText().toString();
        Log.d("Ghost", "trying to connect to " + host);
        new ConnectionThread().execute(host);
    }

    private class ConnectionThread extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            final String host = strings[0];
            try {
                Socket socket = new Socket(host, 1337);
                socket.getOutputStream();
                socket.close();
                Dashboard.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Ghost", "Connected!");
                    }
                });
            } catch (final IOException ioe) {
                Dashboard.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Ghost", "Error: " + ioe.getMessage());
                    }
                });
            }
            return null;
        }
    }
}
