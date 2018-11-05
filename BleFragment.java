

package com.example.zeno.bleproject;

import android.app.Fragment;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;



import java.util.List;

public abstract class BleFragment extends Fragment {
	private final static String TAG = BleFragment.class.getSimpleName();

	com.example.zeno.bleproject.SampleGattAttributes sampleGattAttributes;
	private com.example.zeno.bleproject.BluetoothLeService mBluetoothLeService;
	BluetoothGattCharacteristic UART_Read;
	BluetoothGattCharacteristic UART_Write;

    

    

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(TAG, "BleFragment_Service Connected");
            com.example.zeno.bleproject.BluetoothLeService.LocalBinder binder = (com.example.zeno.bleproject.BluetoothLeService.LocalBinder) service;
            mBluetoothLeService=binder.getService();
            if (!mBluetoothLeService.initialize()) {
                getActivity().finish();
            }
           
            findCharacteristic();
            setBLEstate();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            	byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA_RAW);
            	String data_string = intent.getStringExtra(BluetoothLeService.EXTRA_DATA_STRING);
            	String uudi_data = intent.getStringExtra(BluetoothLeService.UUID_STRING);
            	
            	dataReceived(uudi_data,data_string,data);
            	dataReceived1(uudi_data,data_string,data);
            	 
            	
            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {


            	findCharacteristic();
            	setBLEstate();
            	
            }
            
        }
    };
   
    
    @Override
    public void onCreate (Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);


        Intent gattServiceIntent = new Intent(getActivity(),com.example.zeno.bleproject.BluetoothLeService.class);
        getActivity().bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        
        
    }

	
    
	public void findCharacteristic() {
    	// Find BLE112 service for writing to
        List<BluetoothGattService> gattServices = mBluetoothLeService.getSupportedGattServices();
        
        if (gattServices == null) return;
        String uuid = null;
       
        // Loops through available GATT Services.

        for (BluetoothGattService gattService : gattServices)
        {
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics)
            {
                uuid = gattCharacteristic.getUuid().toString();
                if (sampleGattAttributes.UART_READ_UUID.equals(uuid)){
                    UART_Read = gattCharacteristic;
                	mBluetoothLeService.setCharacteristicNotification( UART_Read, true);// Notification on ����
                }
                if (sampleGattAttributes.UART_WRITE_UUID.equals(uuid)) UART_Write = gattCharacteristic;

            }
        }
	}
	

	


	public void dataReceived(String uuid, String data, byte[] row_data){}
	

	public void dataReceived1(String uuid, String data, byte[] row_data){}


	public void setBLEstate(){}

    public void sendData(byte[] data) {}
    

    public void setUART_Write(byte[] data) {
    	if (UART_Write != null) {
    		mBluetoothLeService.writeCharacteristic_NO_RESPONSE(UART_Write, data);
        }
    }


    @Override
	public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        
    }

    @Override
	public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(mServiceConnection);
        mBluetoothLeService = null;
      
    }
    
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        return intentFilter;
    }    
    
    
}
