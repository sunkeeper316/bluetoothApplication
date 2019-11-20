package com.sun.bluetooth;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.BIND_AUTO_CREATE;

public class mainFragment extends Fragment{

    private RecyclerView recyclerView;
    private BluetoothManager bluetoothManager;
    public BluetoothAdapter bluetoothAdapter;
    private BluetoothLeService bluetoothLeService;
    private List<BluetoothDevice> devices = new ArrayList<>();
    private DeviceAdapter deviceAdapter;
    private View mainview;
    private ProgressBar progressBar;
//---------------------------------藍芽掃描回傳---------------------------------------

    private BluetoothAdapter.LeScanCallback leScanCallback=new BluetoothAdapter.LeScanCallback ( ){
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//            Log.i("DEBUG","=device=" + device.getName ());
            if (device.getName ( ) != null && bluetoothLeService != null) {
                Log.i ( "DEBUG", "=device=" + device.getName ( ));
                for (BluetoothDevice s : devices){
                    if(s.getName ().equals ( device.getName () )){
                        return;
                    }
                }
                devices.add(device);
                deviceAdapter.notifyDataSetChanged ();
            }
        }
    };

    //------------------------------廣播接收器---------------------------------------

    private BroadcastReceiver broadcastReceiver=new BroadcastReceiver ( ){
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action=intent.getAction ( );

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals (action)) {
                Log.i ("DEBUG", "====ACTION_GATT_CONNECTED====");

                Handler handler=new Handler ( );
                handler.postDelayed (new Runnable ( ){
                    @Override
                    public void run() {

                    }
                }, 10000);

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals (action)) {
                Log.i ("DEBUG", "====ACTION_GATT_DISCONNECTED====");


                Handler handler=new Handler ( );
                handler.postDelayed (new Runnable ( ){
                    @Override
                    public void run() {
                    }
                }, 300);
                //*/
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals (action)) {
                Log.i ("DEBUG", "====ACTION_GATT_SERVICES_DISCOVERED====");
                Handler handler=new Handler ( );
                handler.postDelayed (new Runnable ( ){
                    @Override
                    public void run() {
                        if(mainview != null){
                            progressBar.setVisibility ( View.GONE );
                            Navigation.findNavController ( mainview ).navigate ( R.id.action_mainFragment_to_detailFragment );
                        }
//                       if (bluetoothLeService.setNotificationsCharacteristic != null) { 如果有確定的特徵點可以從這設定廣播
//                            bluetoothLeService.setNotifications ( );
//                        }
                    }
                }, 500);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals (action)) {
                Log.i ("DEBUG", "====ACTION_DATA_AVAILABLE====");
                byte[] data=intent.getByteArrayExtra (BluetoothLeService.EXTRA_DATA);

                analysisData(data);
            }else {
                Log.i ("DEBUG", "==== Other ACTION ====" + action);

            }
        }
    };
    private ServiceConnection serviceConnection=new ServiceConnection ( ){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothLeService=((BluetoothLeService.LocalBinder) service).getService ( );
            Log.i ("DEBUG", "==== bluetoothLeService open ====" + bluetoothLeService);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothLeService=null;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater , ViewGroup container ,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate ( R.layout.fragment_main , container , false );
    }

    @Override
    public void onViewCreated(@NonNull View view , @Nullable Bundle savedInstanceState) {
        super.onViewCreated ( view , savedInstanceState );
        progressBar = view.findViewById ( R.id.progressBar );
        progressBar.setVisibility ( View.GONE );
        recyclerView = view.findViewById ( R.id.detail_recyclerView );
        deviceAdapter = new DeviceAdapter ( getContext (),devices );
        recyclerView.setLayoutManager ( new LinearLayoutManager ( getContext () ) );
        recyclerView.setAdapter ( deviceAdapter );
        mainview = view;

    }
    public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>{
        Context context;
        List<BluetoothDevice> devices;

        public DeviceAdapter(Context context ,List<BluetoothDevice> devices){
            this.context = context;
            this.devices = devices;
        }

        public class DeviceViewHolder extends RecyclerView.ViewHolder{
            TextView tv_device;
            public DeviceViewHolder(@NonNull View itemView) {
                super ( itemView );
                tv_device = itemView.findViewById ( R.id.tv_device );
            }
        }

        @Override
        public int getItemCount() {
            return devices.size ();
        }
        @NonNull
        @Override
        public DeviceAdapter.DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent , int viewType) {
            View item_view;
            item_view = LayoutInflater.from ( context ).inflate ( R.layout.item_view, parent, false );
            return new DeviceViewHolder ( item_view );
        }
        @Override
        public void onBindViewHolder(@NonNull DeviceAdapter.DeviceViewHolder holder , int position) {
            final BluetoothDevice device = devices.get( position);
            holder.tv_device.setText ( device.getName () );
            holder.itemView.setOnClickListener ( new View.OnClickListener ( ){
                @Override
                public void onClick(View view) {
                   if (bluetoothLeService != null) {
                       progressBar.setVisibility ( View.VISIBLE );
                       bluetoothLeService.connect ( device );
                   }
                }
            } );
        }
    }

    @Override
    public void onStart() {
        super.onStart ( );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        }
        initBLE ( );
    }
    @Override
    public void onStop() {
        super.onStop ( );
        if(bluetoothAdapter != null){
            bluetoothAdapter.stopLeScan ( leScanCallback );
            if(broadcastReceiver!=null && getContext ().isRestricted ())
            {
                getContext ().unregisterReceiver ( broadcastReceiver );
                getContext ().unbindService(serviceConnection);
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode , int resultCode , @Nullable Intent data) {
        super.onActivityResult ( requestCode , resultCode , data );
        if (resultCode == RESULT_OK) {
            Intent gattServiceIntent=new Intent (getContext (), BluetoothLeService.class);
            getContext ().bindService (gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
            bluetoothAdapter.startLeScan (leScanCallback);
            getContext ().registerReceiver (broadcastReceiver, makeGattUpdateIntentFilter ( ));
        } else {
            Toast.makeText (getContext (), "藍芽沒有開啟，沒有藍芽功能無法傳送資料。", Toast.LENGTH_SHORT).show ( );
        }
    }

    private void initBLE() {
        if (!getContext ().getPackageManager ( ).hasSystemFeature ( PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText ( getContext (), "BLE is not supported", Toast.LENGTH_SHORT).show ( );
        }

        bluetoothManager=(BluetoothManager) getContext ().getSystemService (Context.BLUETOOTH_SERVICE);
        bluetoothAdapter=bluetoothManager.getAdapter ( );

        if (bluetoothAdapter == null) {
            Toast.makeText (getContext (), "BLE is not supported", Toast.LENGTH_SHORT).show ( );
            return;
        }
        if (!bluetoothAdapter.isEnabled ( )) {
            Intent enableBtIntent=new Intent (BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult (enableBtIntent, RESULT_FIRST_USER);

        } else {
            Intent gattServiceIntent=new Intent (getContext (), BluetoothLeService.class);
            getContext ().bindService (gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
            bluetoothAdapter.startLeScan (leScanCallback);
            getContext ().registerReceiver (broadcastReceiver, makeGattUpdateIntentFilter ( ));

        }
        //判斷定位
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getContext ().checkSelfPermission ( Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions (new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                return;
            }
        }
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        Log.i ("DEBUG", "   == makeGattUpdateIntentFilter==");

        final IntentFilter intentFilter=new IntentFilter ( );
        intentFilter.addAction (BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction (BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction (BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private boolean analysisData(byte[] data) {
        Log.i ("DEBUG", "==analysisData==");
        StringBuilder strB=new StringBuilder ( );
        for (int i=0; i < data.length; i++) {
            strB.append (String.format (" %02x", data[i]));
        }
        Log.i ("DEBUG", strB.toString ( ));
        return true;
    }

}
