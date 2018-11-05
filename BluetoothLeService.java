package com.example.zeno.bleproject;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;


public class BluetoothLeService extends Service {

    private static final String TAG = BluetoothLeService.class.getSimpleName();

    private final IBinder mBinder = new LocalBinder();
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    public static final String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public static final String EXTRA_DATA_STRING = "com.example.bluetooth.le.EXTRA_DATA_STRING";
    public static final String EXTRA_DATA_RAW = "com.example.bluetooth.le.EXTRA_DATA_RAW";
    public static final String UUID_STRING = "com.example.bluetooth.le.UUID_STRING";
    public static final String UUID_INTENT = "com.example.bluetooth.le.UUID_INTENT";
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
    {
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(BluetoothLeService.TAG, "park_Blue_Connected to GATT server.");

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                String intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);

                Log.i(BluetoothLeService.TAG, "park_Blue_Attempting to start service discovery:");
                BluetoothLeService.this.mBluetoothGatt.discoverServices();
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                String intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(BluetoothLeService.TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            Log.w(BluetoothLeService.TAG, "park_Blue_onServicesDiscovered received1: ");
            if (status == BluetoothGatt.GATT_SUCCESS)
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            else
                Log.w(BluetoothLeService.TAG, "park_Blue_onServicesDiscovered received: " + status);
        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            Log.i(BluetoothLeService.TAG, "park_Blue_onCharacteristicRead()00 : ");
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                Log.i(BluetoothLeService.TAG, "park_Blue_onCharacteristicRead()01 : ");
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            Log.i(BluetoothLeService.TAG, "park_Blue_onCharacteristicChanged()11 : ");
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            Log.i(BluetoothLeService.TAG, "park_Blue_onCharacteristicWrite_length()0 : ");
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                byte[] data = characteristic.getValue();

                Log.i(BluetoothLeService.TAG, "park_Blue_onCharacteristicWrite_length() : " + data.length);

                Log.i(BluetoothLeService.TAG, "park_Blue_onCharacteristicWrite() : " + new String(data));
            }
        }
    };

    private void broadcastUpdate(String action)
    {
        Log.i(TAG, "park_Blue broadcastUpdate()0 : ");
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(String action, BluetoothGattCharacteristic characteristic) {
        Intent intent = new Intent(action);
        intent.putExtra(UUID_INTENT , characteristic.getValue());

        Log.d(TAG, "park_Blue_Service broadcastUpdate_0 :" + characteristic.getUuid().toString());
        intent.putExtra(UUID_STRING, characteristic.getUuid().toString());

        byte[] data = characteristic.getValue();

        Log.d(TAG, "park_Blue_Service broadcastUpdate_1 :" + data.length);

        if ((data != null) && (data.length > 0)) {
            StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data) {
                stringBuilder.append(String.format("%02X ", new Object[] { Byte.valueOf(byteChar) }));
            }
            Log.d(TAG, "park_Blue_Service broadcastUpdate_2 :" + stringBuilder.toString());

            intent.putExtra(EXTRA_DATA_STRING, stringBuilder.toString());
            intent.putExtra(EXTRA_DATA_RAW , data);
        }

        sendBroadcast(intent);
    }

    public IBinder onBind(Intent intent)
    {
        Log.d(TAG, "park_Blue_Service has been bound");
        return mBinder;
    }

    public boolean onUnbind(Intent intent)
    {
        close();
        return super.onUnbind(intent);
    }

    public boolean initialize()
    {
        Log.d(TAG, "park_Blue_Initializing service.");

        if (this.mBluetoothManager == null) {
            this.mBluetoothManager = ((BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE));
            if (this.mBluetoothManager == null) {
                Log.e(TAG, "park_Blue_Unable to initialize BluetoothManager.");
                return false;
            }
        }

        this.mBluetoothAdapter = this.mBluetoothManager.getAdapter();
        if (this.mBluetoothAdapter == null) {
            Log.e(TAG, "park_Blue_Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public boolean connect(String address)
    {
        if ((this.mBluetoothAdapter == null) || (address == null)) {
            Log.w(TAG, "park_Blue_BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if ((this.mBluetoothDeviceAddress != null) && (address.equals(this.mBluetoothDeviceAddress)) &&
                (this.mBluetoothGatt != null)) {
            Log.d(TAG, "park_Blue_Trying to use an existing mBluetoothGatt for connection.");
            if (this.mBluetoothGatt.connect()) {
                mConnectionState = 1;
                return true;
            }
            return false;
        }

        BluetoothDevice device = this.mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "park_Blue_Device not found.  Unable to connect.");
            return false;
        }

        this.mBluetoothGatt = device.connectGatt(this, false, this.mGattCallback);
        Log.d(TAG, "park_Blue_Trying to create a new connection.");
        this.mBluetoothDeviceAddress = address;
        mConnectionState = 1;
        return true;
    }

    public void disconnect()
    {
        Log.w(TAG, "park_Blue_BluetoothAdapter disconnect__2");
        if ((this.mBluetoothAdapter == null) || (this.mBluetoothGatt == null)) {
            Log.w(TAG, "park_Blue_BluetoothAdapter disconnect");
            return;
        }
        this.mBluetoothGatt.disconnect();
    }

    public void close()
    {
        Log.w(TAG, "park_Blue_BluetoothAdapter close");
        if (this.mBluetoothGatt == null) {
            return;
        }
        this.mBluetoothGatt.close();
        this.mBluetoothGatt = null;
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        Log.w(TAG, "park_Blue_readCharacteristic");
        if ((this.mBluetoothAdapter == null) || (this.mBluetoothGatt == null))
        {
            Log.w(TAG, "park_Blue_BluetoothAdapter not initialized_ERROR");
            return;
        }
        Log.w(TAG, "park_Blue_BluetoothAdapter readCharacteristic");

        this.mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled)
    {
        this.mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] data)
    {
        Log.d(TAG, "park_Blue_writeCharacteristic:" + new String(data));

        characteristic.setValue(data);
        characteristic.setWriteType(2);
        this.mBluetoothGatt.writeCharacteristic(characteristic);
        Log.d(TAG, "park_Blue_writeCharacteristic");
    }

    public void writeCharacteristic_NO_RESPONSE(BluetoothGattCharacteristic characteristic, byte[] data)
    {
        Log.d(TAG, "park_Blue_writeCharacteristic:" + new String(data));

        characteristic.setValue(data);
        characteristic.setWriteType(1);
        this.mBluetoothGatt.writeCharacteristic(characteristic);
        Log.d(TAG, "park_Blue_writeCharacteristic");
    }

    public List<BluetoothGattService> getSupportedGattServices()
    {
        Log.d(TAG, "park_Blue_getSupportedGattServices");
        if (this.mBluetoothGatt == null) return null;

        return this.mBluetoothGatt.getServices();
    }

    public class LocalBinder extends Binder
    {


        public BluetoothLeService getService()
        {
            Log.d(BluetoothLeService.TAG, "park_Blue_LocalBinder :");
            return BluetoothLeService.this;
        }
    }
}
