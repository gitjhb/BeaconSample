package com.jhb.android.beacondemo;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;

import java.util.Arrays;

/**
 * Created by hongbin.jia on 5/7/2018.
 */

public class MyBeacon {

    Beacon beacon;
    BeaconParser beaconParser;
    String id;
    String major;
    String minor;

    public MyBeacon(String id) {
        this.id = id;
        beacon= new Beacon.Builder()
                .setId1(id) // UUID for beacon
                .setId2("1") // Major for beacon
                .setId3("5") // Minor for beacon
                .setManufacturer(0x004C) // Radius Networks.0x0118  Change this for other beacon layouts//0x004C for iPhone
                .setTxPower(-56) // Power in dB
                .setDataFields(Arrays.asList(new Long[] {0l})) // Remove this for beacon layouts without d: fields
                .build();

        beaconParser= new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
    }

    public Beacon getBeacon() {
        return beacon;
    }

    public void setBeacon(Beacon beacon) {
        this.beacon = beacon;
    }

    public BeaconParser getBeaconParser() {
        return beaconParser;
    }

    public void setBeaconParser(BeaconParser beaconParser) {
        this.beaconParser = beaconParser;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
