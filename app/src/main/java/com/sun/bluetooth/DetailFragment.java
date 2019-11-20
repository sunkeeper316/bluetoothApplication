package com.sun.bluetooth;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment{
    private RecyclerView detail_recyclerView;

    ServiceAdapter serviceAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater , ViewGroup container ,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate ( R.layout.fragment_detail , container , false );
    }

    @Override
    public void onViewCreated(@NonNull View view , @Nullable Bundle savedInstanceState) {
        super.onViewCreated ( view , savedInstanceState );
        detail_recyclerView = view.findViewById ( R.id.detail_recyclerView );
        serviceAdapter = new ServiceAdapter ( getContext (), BluetoothLeService.bluetoothGattServiceList);
        detail_recyclerView.setLayoutManager ( new LinearLayoutManager ( getContext () ) );
        detail_recyclerView.setAdapter ( serviceAdapter );
    }

    public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>{

        Context context;
        List<BluetoothGattService> bluetoothGattServiceList;
        ServiceAdapter(Context context , List<BluetoothGattService> bluetoothGattServiceList){

            this.context = context;
            this.bluetoothGattServiceList = bluetoothGattServiceList;
        }
        public class ServiceViewHolder extends RecyclerView.ViewHolder{
            TextView tv_Service;
            RecyclerView CharacteristicRecyclerView;
            public ServiceViewHolder(@NonNull View itemView) {
                super ( itemView );
                tv_Service = itemView.findViewById ( R.id.tv_service );
                CharacteristicRecyclerView = itemView.findViewById ( R.id.CharacteristicRecyclerView );
            }
        }
        @Override
        public int getItemCount() {
            return bluetoothGattServiceList.size ();
        }
        @NonNull
        @Override
        public ServiceAdapter.ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent , int viewType) {
            View item_view;
            item_view = LayoutInflater.from ( context ).inflate ( R.layout.detail_item_view, parent, false );
            return new ServiceViewHolder (item_view );
        }


        @Override
        public void onBindViewHolder(@NonNull ServiceAdapter.ServiceViewHolder holder , int position) {
            BluetoothGattService bluetoothGattService = bluetoothGattServiceList.get ( position );
            List<BluetoothGattCharacteristic> bluetoothGattCharacteristicList = bluetoothGattService.getCharacteristics ();
            CharacteristicAdapter characteristicAdapter = new CharacteristicAdapter ( context,bluetoothGattCharacteristicList );

            holder.tv_Service.setText ( "Service :"+bluetoothGattService.getUuid ().toString () );
            holder.CharacteristicRecyclerView.setLayoutManager ( new LinearLayoutManager ( context ) );
            holder.CharacteristicRecyclerView.setAdapter ( characteristicAdapter );
        }
    }
    public class CharacteristicAdapter extends RecyclerView.Adapter<CharacteristicAdapter.CharacteristicViewHolder>{
        Context context;
        List<BluetoothGattCharacteristic> bluetoothGattCharacteristicList;
        CharacteristicAdapter(Context context , List<BluetoothGattCharacteristic> bluetoothGattCharacteristicList){
            this.context = context;
            this.bluetoothGattCharacteristicList = bluetoothGattCharacteristicList;
        }
        public class CharacteristicViewHolder extends RecyclerView.ViewHolder{
            TextView tv_Characteristic;
            public CharacteristicViewHolder(@NonNull View itemView) {
                super ( itemView );
                tv_Characteristic = itemView.findViewById ( R.id.tv_Characteristic );
            }
        }
        @Override
        public int getItemCount() {
            return bluetoothGattCharacteristicList.size ();
        }
        @NonNull
        @Override
        public CharacteristicAdapter.CharacteristicViewHolder onCreateViewHolder(@NonNull ViewGroup parent , int viewType) {
            View item_view;
            item_view = LayoutInflater.from ( context ).inflate ( R.layout.characteristic_item_view, parent, false );
            return new CharacteristicViewHolder (item_view );
        }


        @Override
        public void onBindViewHolder(@NonNull CharacteristicAdapter.CharacteristicViewHolder holder , int position) {
            BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattCharacteristicList.get ( position );
            if (bluetoothGattCharacteristic.getUuid ().toString () != null){

                holder.tv_Characteristic.setText ( "Characteristic :"+bluetoothGattCharacteristic.getUuid ().toString ()+ "\n"+getProperties ( bluetoothGattCharacteristic ));
                Log.i( "getProperties", "getProperties"+String.valueOf ( bluetoothGattCharacteristic.getProperties () ) );
            }

        }


        public String getProperties(BluetoothGattCharacteristic bluetoothGattCharacteristic){
            String properties = "";

            if ((bluetoothGattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_BROADCAST) > 0) {
                properties += "PROPERTY_BROADCAST\n";
            }
            if ((bluetoothGattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS) > 0) {
                properties += "PROPERTY_EXTENDED_PROPS\n";
            }
            if ((bluetoothGattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                properties += "PROPERTY_INDICATE\n";
            }
            if ((bluetoothGattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                properties += "PROPERTY_NOTIFY\n";
            }
            if ((bluetoothGattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                properties += "PROPERTY_READ\n";
            }
            if ((bluetoothGattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) > 0) {
                properties += "PROPERTY_SIGNED_WRITE\n";
            }
            if ((bluetoothGattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                properties += "PROPERTY_WRITE\n";
            }
            if ((bluetoothGattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                properties += "PROPERTY_WRITE_NO_RESPONSE\n";
            }

            return properties;

        }
    }

}
