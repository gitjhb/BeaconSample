package com.jhb.android.beacondemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class MainActivity extends Activity implements BeaconConsumer {

    private static final String TAG = "MainActivity";
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothAdapter mBluetoothAdapter;
    private ScanFilter mScanFilter;
    private ScanSettings mScanSettings;
    private ListView mListView;
    private AdvertiseData mAdvertiseData;
    private AdvertiseSettings mAdvertiseSettings;


    private BeaconManager beaconManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

        mListView= (ListView)findViewById(R.id.list);
        ListAdapter listAdapter= new ArrayAdapter<ScanRecord>(this,android.R.layout.simple_expandable_list_item_1,mDeviceList);
        mListView.setAdapter(listAdapter);

//        setScanFilter();
//        setScanSettings();

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.bind(this);



    }

    @Override
    protected void onStart() {
        super.onStart();
//        startScan();

        Beacon beacon = new Beacon.Builder()
                .setId1("2f234454-cf6d-4a0f-adf2-f4911ba9ffa6")
                .setId2("1")
                .setId3("2")
                .setManufacturer(0x0118)
                .setTxPower(-59)
                .setDataFields(Arrays.asList(new Long[] {0l}))
                .build();
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.i(TAG, "Advertisement start succeeded.");
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e(TAG, "Advertisement start failed with code: "+errorCode);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBluetoothLeScanner.stopScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
            }
        });
    }

    void startAdvertise(){
//        mBluetoothLeAdvertiser.startAdvertising(mAdvertiseSettings, mAdvertiseData, new AdvertiseData());
    }

    public void startScan(){
        mBluetoothLeScanner.startScan(Arrays.asList(mScanFilter), mScanSettings, mScanCallback);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private void setScanFilter() {
        ScanFilter.Builder mBuilder = new ScanFilter.Builder();
        ByteBuffer mManufacturerData = ByteBuffer.allocate(23);
        ByteBuffer mManufacturerDataMask = ByteBuffer.allocate(24);
        byte[] uuid = hexStringToByteArray(UUID.fromString("0CF052C297CA407C84F8B62AAC4E9020").toString());
        mManufacturerData.put(0, (byte)0xBE);
        mManufacturerData.put(1, (byte)0xAC);
        for (int i=2; i<=17; i++) {
            mManufacturerData.put(i, uuid[i-2]);
        }
        for (int i=0; i<=17; i++) {
            mManufacturerDataMask.put((byte)0x01);
        }
        mBuilder.setManufacturerData(224, mManufacturerData.array(), mManufacturerDataMask.array());
        mScanFilter = mBuilder.build();
    }

    private List<ScanRecord> mDeviceList=new ArrayList<ScanRecord>();

    protected ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            ScanRecord mScanRecord = result.getScanRecord();
            byte[] manufacturerData = mScanRecord.getManufacturerSpecificData(224);
            int mRssi = result.getRssi();

            if(result.getRssi()>-60) {
                mDeviceList.add(mScanRecord);
            }

        }
    };


    private void setScanSettings() {
        ScanSettings.Builder mBuilder = new ScanSettings.Builder();
        mBuilder.setReportDelay(0);
        mBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        mScanSettings = mBuilder.build();
    }

    public String calculateDistance(int txPower, double rssi) {
        double accuracy;
        if (rssi == 0) {
            accuracy = -1.0; // if we cannot determine accuracy, return -1.
        } else {
            double ratio = rssi * 1.0 / txPower;
            if (ratio < 1.0) {
                accuracy = Math.pow(ratio, 10);
            } else {
                accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            }
        }
        return getDistance(accuracy);
    }

    private String getDistance(double accuracy) {
        if (accuracy == -1.0) {
            return "Unknown";
        } else if (accuracy < 1) {
            return "Immediate";
        } else if (accuracy < 3) {
            return "Near";
        } else {
            return "Far";
        }
    }

    protected void setAdvertiseData() {
        AdvertiseData.Builder mBuilder = new AdvertiseData.Builder();
        ByteBuffer mManufacturerData = ByteBuffer.allocate(24);
        byte[] uuid = hexStringToByteArray(UUID.fromString("0CF052C297CA407C84F8B62AAC4E9020").toString());
        mManufacturerData.put(0, (byte)0xBE); // Beacon Identifier
        mManufacturerData.put(1, (byte)0xAC); // Beacon Identifier
        for (int i=2; i<=17; i++) {
            mManufacturerData.put(i, uuid[i-2]); // adding the UUID
        }
        mManufacturerData.put(18, (byte)0x00); // first byte of Major
        mManufacturerData.put(19, (byte)0x09); // second byte of Major
        mManufacturerData.put(20, (byte)0x00); // first minor
        mManufacturerData.put(21, (byte)0x06); // second minor
        mManufacturerData.put(22, (byte)0xB5); // txPower
        mBuilder.addManufacturerData(224, mManufacturerData.array()); // using google's company ID
        mAdvertiseData = mBuilder.build();
    }

    protected void setAdvertiseSettings() {
        AdvertiseSettings.Builder mBuilder = new AdvertiseSettings.Builder();
        mBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        mBuilder.setConnectable(false);
        mBuilder.setTimeout(0);
        mBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM);
        mAdvertiseSettings = mBuilder.build();
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an beacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {    }


        //ring
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Log.i(TAG, "The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away.");
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }
}
