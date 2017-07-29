package it.relab.redox.android_audio_rtp_spike;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {
    private final InetAddress remoteAddress = InetAddress.getByName("52.209.106.16");
    //    private final InetAddress remoteAddress = InetAddress.getByName("192.168.1.165");
    private final int remotePort = 22222;

    private AudioGroup audioGroup;
    private AudioStream outputStream;

    public MainActivity() throws UnknownHostException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                200);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ToggleButton button = (ToggleButton) findViewById(R.id.toggleButton);

        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startSpeak();
                } else {
                    stopSpeak();
                }
            }
        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            audioGroup = new AudioGroup();
            InetAddress localAddress = InetAddress.getByAddress(getLocalIPAddress());

            outputStream = new AudioStream(localAddress);
            outputStream.setCodec(AudioCodec.GSM_EFR);
            outputStream.setMode(AudioStream.MODE_NORMAL);

            Log.w("Local address", outputStream.getLocalAddress().toString() + ":" + outputStream.getLocalPort());
        } catch (SocketException | UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static byte[] getLocalIPAddress() {
        byte ip[] = null;
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();

                    if (inetAddress instanceof Inet6Address) continue;

                    if (!inetAddress.isLoopbackAddress()) {
                        ip = inetAddress.getAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.i("SocketException ", ex.toString());
        }
        return ip;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void startSpeak() {
        try {
            AudioManager audio =  (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audio.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioGroup.setMode(AudioGroup.MODE_NORMAL);

            String userRemoteAddress = ((EditText)findViewById(R.id.remoteAddress)).getText().toString();
            String userRemotePort = ((EditText)findViewById(R.id.remotePort)).getText().toString();

            boolean hasUserAddress = !userRemoteAddress.isEmpty();
            boolean hasUserPort = !userRemotePort.isEmpty();

            InetAddress chosenRemoteAddress = hasUserAddress ? InetAddress.getByName(userRemoteAddress) : remoteAddress;
            int chosenRemotePort = hasUserPort ? Integer.parseInt(userRemotePort) : remotePort;

            Log.w("Remote endpoint", chosenRemoteAddress.toString() + ":" + chosenRemotePort);

            outputStream.associate(chosenRemoteAddress, chosenRemotePort);
            outputStream.join(audioGroup);

            Log.i("start speak", "start speak");
        } catch (Exception e) {
            Log.e("----------------------", e.toString());
            e.printStackTrace();
        }
    }

    private void stopSpeak() {
        outputStream.join(null);
        audioGroup.setMode(AudioGroup.MODE_ON_HOLD);

        AudioManager audio =  (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.setMode(AudioManager.MODE_NORMAL);

        Log.i("stop speak", "stop speak");
    }
}
