package com.beanfactory.yashp.gatewaycompanion;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //    ImageView onlineImageView;
//    ImageView gifImageView;
    DrawerLayout mDrawerLayout;
    TextView resultTextView;
    TextView urlTextView;
    CollapsingToolbarLayout collapsingToolbarLayout;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothServerSocket mBluetoothServerSocket;
    UUID guuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    String mac = "98:D3:71:F5:D0:84";

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // MAC-address of Bluetooth module (you must edit this line)
    private static String address = "98:D3:71:F5:D0:84";

    Handler h;
    private ConnectedThread mConnectedThread;
    final int RECIEVE_MESSAGE = 1;        // Status  for Handler
    private static final String TAG = "bluetooth2";
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder sb = new StringBuilder();

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection",e);
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
            btSocket.connect();
            Log.d(TAG, "....Connection ok...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }



    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultTextView = (TextView) findViewById(R.id.resulttext);
        urlTextView = (TextView) findViewById(R.id.gateurl);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar_main);
        collapsingToolbarLayout.setTitle("Dashboard");
        collapsingToolbarLayout.setExpandedTitleColor(getColor(R.color.color_white));
        collapsingToolbarLayout.setCollapsedTitleTextColor(getColor(R.color.color_white));
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_main));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);



        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView mNavigationView = findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // set item as selected to persist highlight
                item.setChecked(true);
                // close drawer when item is tapped
                mDrawerLayout.closeDrawers();

                // Add code here to update the UI based on the item selected
                // For example, swap UI fragments here
                switch (item.getItemId()) {
                    case R.id.aboutMenuId:
                        startActivity(new Intent(MainActivity.this, AboutActivity.class));
                }

                return true;
            }
        });

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:                                                   // if receive massage
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);                 // create string from bytes array
                        sb.append(strIncom);                                                // append string
                        int endOfLineIndex = sb.indexOf("\r\n");                            // determine the end-of-line
//                        if (endOfLineIndex > 0) {                                            // if end-of-line,
//                            String sbprint = sb.substring(0, endOfLineIndex);               // extract string
//                            sb.delete(0, sb.length());                                      // and clear
                            resultTextView.setText("Data from Arduino: " + sb.toString());            // update TextView
//                            btnOff.setEnabled(true);
//                            btnOn.setEnabled(true);
//                        }

                        String gateurl = "http://www.google.com";
                        if (sb.toString().contains("1")) {
                            resultTextView.setText("NOR Gate detected");
                            gateurl = "https://en.wikipedia.org/wiki/NOR_gate";
                            urlTextView.setText("More info");
                            urlTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://en.wikipedia.org/wiki/NOR_gate")));
                                }
                            });


                        }

                        else if (sb.toString().contains("2")){
                            resultTextView.setText("NOT Gate detected");
                            gateurl = "https://en.wikipedia.org/wiki/Inverter_(logic_gate)";
                            urlTextView.setText("More info");
                            urlTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://en.wikipedia.org/wiki/Inverter_(logic_gate)")));
                                }
                            });
                        }

                        else if (sb.toString().contains("3")) {
                            resultTextView.setText("AND Gate detected");
                            gateurl = "https://en.wikipedia.org/wiki/AND_gate";
                            urlTextView.setText("More info");
                            urlTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://en.wikipedia.org/wiki/AND_gate")));
                                }
                            });
                        }

                        else if (sb.toString().contains("4")) {
                            resultTextView.setText("OR Gate detected");
                            gateurl = "https://en.wikipedia.org/wiki/OR_gate";
                            urlTextView.setText("More info");
                            urlTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://en.wikipedia.org/wiki/OR_gate")));
                                }
                            });
                        }

                        else if (sb.toString().contains("5")) {
                            resultTextView.setText("NAND Gate detected");
                            gateurl = "https://en.wikipedia.org/wiki/NAND_gate";
                            urlTextView.setText("More info");
                            urlTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://en.wikipedia.org/wiki/NAND_gate")));
                                }
                            });
                        }

                        else if (sb.toString().contains("6")) {
                            resultTextView.setText("EXOR Gate detected");
                            gateurl = "https://en.wikipedia.org/wiki/XOR_gate";
                            urlTextView.setText("More info");
                            urlTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse( "https://en.wikipedia.org/wiki/XOR_gate")));
                                }
                            });
                        }

                        else if (sb.toString().contains("0")) {
                            resultTextView.setText("No Gate detected");
                            urlTextView.setText("Cannot show information");
//                            gateurl = "";
                        }

                        break;
                        //Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");

                }
            };
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

//        Following is my code
//
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (mBluetoothAdapter == null)
//            Toast.makeText(getApplicationContext(), "No bluetooth adapter found", Toast.LENGTH_SHORT).show();
//        else {
//
//            if (!mBluetoothAdapter.isEnabled()) {
//                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBtIntent, 0);
//                Toast.makeText(getApplicationContext(), "Bluetooth turned on\n Now pair with GATEway", Toast.LENGTH_SHORT).show();
//            } else
//                Toast.makeText(getApplicationContext(), "Bluetooth is already turned on\n Now pair with GATEway", Toast.LENGTH_SHORT).show();
//
//        }

//        gifImageView = (ImageView) findViewById(R.id.electron_gif);
//        gifImageView.setImageResource(R.drawable.atom);
//        Glide.with(this).load("https://i.gifer.com/9pW1.gif").into(new DrawableImageViewTarget(gifImageView));

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);


//        onlineImageView = (ImageView) findViewById(R.id.onlineImage);
//
//        String url = "https://loading.io/spinners/rolling/lg.curve-bars-loading-indicator.gif";
//
//        Glide.with(getApplicationContext()).load(url).into(onlineImageView);
//        Glide.with(getApplicationContext()).asGif().load(url).into(onlineImageView);


//        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//        BluetoothDevice gateway = null;
//        BluetoothSocket mBluetoothSocket = null;
//
//        Log.d("TAG", "Showing paired devices details #######################");
//        if (pairedDevices.size() > 0) {
//            // There are paired devices. Get the name and address of each paired device.
//            for (BluetoothDevice device : pairedDevices) {
//                String deviceName = device.getName();
//                if (deviceName == "HC-05") {
////                    guuid = device.getUuids()[1].getUuid();
//                    gateway = device;
//                    try {
//                        mBluetoothSocket = device.createRfcommSocketToServiceRecord(guuid);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    Toast.makeText(getApplicationContext(), "Device name:" + deviceName + " MAC address " + device.getAddress() + " UUIDs " + device.getUuids()[0], Toast.LENGTH_SHORT).show();
//                }
//
//                String deviceHardwareAddress = device.getAddress(); // MAC address
//                Log.d("TAG", "Device name:" + deviceName + " MAC address" + deviceHardwareAddress + " UUIDs " + device.getUuids()[0]);
//            }
//        }
//        if (mBluetoothSocket != null) {
//            try {
//                mBluetoothSocket.connect();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        try {
//            mBluetoothServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("RFCOMM", guuid);
//            Log.d("TAG", "\n\n#####################Bluetooth Server socket created ######################\n\n");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Log.d("TAG", "\n\n#####################UUID of gateway is ######################\n\n" + guuid);

//        InputStream inputStream = null;
//        try {
//             inputStream = mBluetoothSocket.getInputStream();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        byte[] buffer = new byte[1024];  // buffer store for the stream
//        int bytes; // bytes returned from read()
//
//        StringBuilder readMessage = new StringBuilder();
//
//        try {
////            bytes = inputStream.read(buffer);
//            bytes = mBluetoothSocket.getInputStream().read(buffer);
//
//            String read = new String(buffer, 0, bytes);
//            readMessage.append(read);
//            Log.d("TAG", "##########Input String############### " +readMessage.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


//        try {
//            mBluetoothServerSocket.accept();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    //    @Override
//    protected void onPostCreate(Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//        drawerListener.syncState();
//    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()

            long t= System.currentTimeMillis();
            long end = t+5000;
            // Keep listening to the InputStream until an exception occurs
            while (System.currentTimeMillis() < end) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
//        public void write(String message) {
//            Log.d(TAG, "...Data to send: " + message + "...");
//            byte[] msgBuffer = message.getBytes();
//            try {
//                mmOutStream.write(msgBuffer);
//            } catch (IOException e) {
//                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
//            }
//        }
    }

}
