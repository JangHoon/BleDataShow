package com.example.zeno.bleproject;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chipsen.bleservice.SampleGattAttributes;

public class DataShow extends BleFragment {


    private final static String TAG = NavigationActivity.class.getSimpleName();

    TextView showData;
    TextView textView;
    TextView showData2;
    ProgressBar progressBar;
    ProgressBar progressBar2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.data_show, container, false);

        showData=(TextView)v.findViewById(R.id.dataView);
        showData2=(TextView)v.findViewById(R.id.dataView2);
        progressBar=(ProgressBar)v.findViewById(R.id.progressBar);

        progressBar2=(ProgressBar)v.findViewById(R.id.progressBar2);
//        textView=(TextView)v.findViewById(R.id.textView2);
        return v;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void dataReceived(String  uudi_data, String data_string, byte[] row_data){
        // TODO Auto-generated method stub
        if(SampleGattAttributes.UART_READ_UUID.equals(uudi_data)){
            byte[] mBuffer = new byte[row_data.length+2];
            System.arraycopy(row_data, 0, mBuffer, 0, row_data.length);
            mBuffer[row_data.length]=0x0D;
            mBuffer[row_data.length+1]=0x0A;
            String s= new String(mBuffer);
//            String batteryValue=s.substring(6,11);
//            String powerValue=s.substring(1,6);

            batteryLife(mBuffer);
            powerData(mBuffer);

//            String batteryValue=s.substring(6,10);
//            float batteryLife=Float.parseFloat(batteryValue);
//            if()
//            Log.d(TAG,batteryValue);
//            int minValue=0;
//            int maxVlaue=70;
//            progressBar.setMin(minValue);
//            progressBar.setMax(maxVlaue);



//            progressBar.setProgress();
            Log.d(TAG,s);
//			mEmulatorView.write(mBuffer, mBuffer.length);
        }

    }



    public void batteryLife(byte[] mBuffer){
        String b = new String(mBuffer);
        String batteryValue=b.substring(6,10);
        int numLife=0;
        float batLife=Float.parseFloat(batteryValue);
        if(3.50<=batLife && batLife<=4.20) {
            float realLife = 70 - (((float) 4.20 - batLife) * 100);
            String rn = String.valueOf(realLife);
            Log.d(TAG, rn);
            numLife = (int) realLife;
            String batInt = String.valueOf(numLife);
            Log.d(TAG, batInt);



        }
        progressBar.setProgress(numLife);

        showData.setText(batteryValue);
//        }if(batLife<3.50 | batLife>4.20){
//
//        }

    }

    public void powerData(byte[] mBuffer){
        String p= new String(mBuffer);
        String powerValue=p.substring(3,5);
        int  realNumber;
        if(powerValue.equals("000") ){
            realNumber=0;
//            powerValue="0";
////            String zero ="0";
//            showData2.setText(powerValue);
//            showData2.setText(zero);
            Log.d(TAG,powerValue);
        }else {

            realNumber=Integer.parseInt(powerValue);


        }
        showData2.setText(powerValue);
        progressBar2.setProgress(realNumber);

    }
}
