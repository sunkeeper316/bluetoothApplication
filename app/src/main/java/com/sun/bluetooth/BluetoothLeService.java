package com.sun.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BluetoothLeService extends Service{
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager bluetoothManager;
    public BluetoothAdapter bluetoothAdapter;

    private BluetoothGatt bluetoothGatt;

    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_STRINGS = "com.example.bluetooth.le.EXTRA_STRINGS";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

    public BluetoothGattCharacteristic setNotificationsCharacteristic;
    public BluetoothGattCharacteristic setWriteCharacteristic;

    public static List<BluetoothGattService> bluetoothGattServiceList = new ArrayList<>();

    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback ( ){

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange (gatt, status, newState);
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                Log.i( TAG, " GATT server open");
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                bluetoothGatt.discoverServices();
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);

                //DLog.debug("==ACTION_GATT_DISCONNECTED==");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered (gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                didDiscoverCharacteristics(gatt.getServices());
                bluetoothGattServiceList = gatt.getServices ();
            } else
            {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged (gatt, characteristic);
            Log.i("DEBUG", "=收到廣播= ");
            String str_uuid = characteristic.getUuid().toString();
            str_uuid = str_uuid.toUpperCase();
            Log.i("DEBUG", "== onCharacteristicChanged: uuid= "+str_uuid);
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void didDiscoverCharacteristics(List<BluetoothGattService> gattServices) {
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

            for (BluetoothGattCharacteristic bluetoothGattCharacteristic : gattCharacteristics)
            {
                Log.i("DEBUG", "== onCharacteristicChanged: uuid= "+bluetoothGattCharacteristic.getUuid ().toString ());
                //setNotificationsCharacteristic = bluetoothGattCharacteristic;
                //這裡必須找出設定廣播的特徵點UUID，然後開啟接收廣播

                //setWriteCharacteristic = bluetoothGattCharacteristic;
                //這裡必須找出設定寫入的特徵點UUID，然後可以利用設定寫入

            }
        }
    }

    public List<BluetoothGattService> getServices (){
        return bluetoothGattServiceList;
    }

    public void setNotifications(){

        Log.i("DEBUG", "   == setbluetoothGattCharacteristic=="+ setNotificationsCharacteristic.getUuid ().toString () );
        boolean isNotification;
        isNotification = bluetoothGatt.setCharacteristicNotification ( setNotificationsCharacteristic,true );
        Log.i ( "DEBUG", "=isNotification=" + isNotification );
        if (isNotification) {
            for (BluetoothGattDescriptor bd : setNotificationsCharacteristic.getDescriptors ( )) {
                if (setNotificationsCharacteristic.getProperties ( ) == BluetoothGattCharacteristic.PROPERTY_INDICATE) {
                    bd.setValue (BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                } else if (setNotificationsCharacteristic.getProperties ( ) == BluetoothGattCharacteristic.PROPERTY_NOTIFY) {
                    bd.setValue (BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                }
                bluetoothGatt.writeDescriptor (bd);
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (setWriteCharacteristic != null){
                        setWriteBluetoothGatt(setWriteCharacteristic);
                    }

                }
            }, 500);

        }
    }
    private void setWriteBluetoothGatt(BluetoothGattCharacteristic bluetoothGattCharacteristic){
        Log.i("DEBUG", "   == WritebluetoothGattCharacteristic=="+ bluetoothGattCharacteristic.getUuid ().toString () );
        byte[] value={(byte)0x00};
        bluetoothGattCharacteristic.setValue (value);
        bluetoothGatt.writeCharacteristic ( bluetoothGattCharacteristic );
    }


    private void broadcastUpdate(final String action)
    {
        Log.i("DEBUG", "==broadcastUpdate action="+action);

        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {

        Log.i("DEBUG", "==broadcastUpdate action="+action+" char = "+characteristic);

        final Intent intent = new Intent(action);

        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0)
        {
            final StringBuilder stringBuilder = new StringBuilder(data.length);

            for(byte byteChar : data)
            {
                stringBuilder.append(String.format("%02X ", byteChar));
            }

            Log.i("DEBUG", "==stringBuilder="+action+" stringBuilder = "+stringBuilder.toString ());
            intent.putExtra(EXTRA_DATA,data);

        }

        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder
    {
        BluetoothLeService getService()
        {
            Log.i("DEBUG", "==getService==");

            return BluetoothLeService.this;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("DEBUG", "==onBind==");
        return new LocalBinder();
    }

    public void connect(final BluetoothDevice device)
    {
        Log.i("DEBUG", "==BluetoothLeSevices: connect== "+device);


        if (device == null)
        {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return;
        }

        // Justin Add
        if (bluetoothGatt != null) {
            Log.i("DEBUG", " ----- mBluetoothGatt != null. must close before connect");
            bluetoothGatt.close();  // *** Justin add
        }
        //

        Log.i("DEBUG", " ----- connectGatt");
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);

        Log.d(TAG, "Trying to create a new connection.");
        mConnectionState = STATE_CONNECTING;

        return;
    }
    public void disconnect()
    {
        Log.i("DEBUG", "== BluetoothLeService: disconnect==");

        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        bluetoothGatt.disconnect();
    }
    //-----------------------------------------------------------------
    public void close()
    {
        Log.i("DEBUG", "== BluetoothLeService: close==");

        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }
}
