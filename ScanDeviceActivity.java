package com.example.zeno.bleproject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chipsen.bleservice.BluetoothLeService;

import java.util.ArrayList;

public class ScanDeviceActivity extends ListActivity {

    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private LeDeviceListAdapter mLeDeviceListAdapter;
     private BluetoothAdapter mBluetoothAdapter;
     private boolean mScanning;
     private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;


     private static final long SCAN_PERIOD = 10000;
    int user_data_count=0;
    boolean mScanning_run=false;
    BLE_Device mBLE_Device;

    ImageView imageView_search;
    ImageView imageView_search_infinity;


    boolean Search_state=true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_device_logo);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,	R.layout.custom_title1);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);




        Display_loop();
        mHandler = new Handler();
        mBLE_Device=new BLE_Device();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }


    private void Display_loop() {





        imageView_search=(ImageView)findViewById(R.id.imageView_search);




        imageView_search.setOnClickListener(new View.OnClickListener() { // search

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(Search_state){
                    Search_state=false;
                    mBLE_Device.setclear();
                    mLeDeviceListAdapter.clear();
                    scanLeDevice(true);
                    imageView_search.setImageResource(R.drawable.search_b_ing1);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }else{
                    Search_state=true;
                    scanLeDevice(false);
                    mLeDeviceListAdapter.clear();

                    imageView_search.setImageResource(R.drawable.search_b);

                }

            }
        });
        }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);


    }
    public void onDestroy() {
        super.onDestroy();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScanning_run=false;
        scanLeDevice(false);
        mHandler.removeMessages(0);
        mLeDeviceListAdapter.clear();
//        mscaneHandler.removeMessages(0);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;

        Intent resultIntent = new Intent();
        resultIntent.putExtra(NavigationActivity.EXTRAS_DEVICE_NAME, device.getName());
        resultIntent.putExtra(NavigationActivity.EXTRAS_DEVICE_ADDRESS,device.getAddress());
        setResult(Activity.RESULT_OK, resultIntent);

        finish();


    }


    private void scanLeDevice(final boolean enable) {
        user_data_count=0;
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                    gf1.showCover();
                    imageView_search.setImageResource(R.drawable.search_b);
                    Search_state=true;
                }
            }, SCAN_PERIOD);
//            gf1.showAnimation();
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
//            gf1.showCover();
            imageView_search.setImageResource(R.drawable.search_b);
            Search_state=true;
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }


    }






    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator =ScanDeviceActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @SuppressLint("InflateParams")
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            try{
                // General ListView optimization code.
                if (view == null) {

                    view = mInflator.inflate(R.layout.activity_device_scan, null);

                    viewHolder = new ViewHolder();
                    viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                    viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);



                    view.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) view.getTag();
                }

                BluetoothDevice device = mLeDevices.get(i);

                final String deviceName = device.getName();
                if (deviceName != null && deviceName.length() > 0){
                    viewHolder.deviceName.setText(deviceName);
                }
                else{
                    viewHolder.deviceName.setText(R.string.unknown_device);
                }
                Log.d(TAG, "Dbg__ DeviceScan =1 ");
                viewHolder.deviceAddress.setText(device.getAddress());


            }catch(Exception e){
                Log.d(TAG, "Dbg__DeviceScan err =2 ");
            }

            return view;
        }


    }

    // Device scan callback.

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,  final byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Dbg__DeviceScan st =00 ");
                    mLeDeviceListAdapter.addDevice(device);

                    Log.d(TAG, "Dbg__DeviceScan st =01 "+user_data_count);
                    mLeDeviceListAdapter.notifyDataSetChanged();




                }

            });


        }
    };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        LinearLayout linearLayout_user_value;


    }

    static class BLE_Device {
        String[] user_alldata=new String[500];
        String[] user_data=new String[500];
        String[] user_TX_level=new String[500];
        int[] rssi=new int[500];


        public void setclear() {
            for (int i = 0; i < 500; i++) {
                user_alldata[i]="";
                user_data[i]="";
                user_TX_level[i]="";
                rssi[i]=0;
            }

        }
    }
}
