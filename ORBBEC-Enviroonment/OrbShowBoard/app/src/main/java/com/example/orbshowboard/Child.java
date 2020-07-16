package com.example.orbshowboard;


public class Child {
    private String fullname;
    private boolean isChecked;
    DeviceInfo deviceInfo ;

    public Child(String fullname,DeviceInfo info) {
        this.fullname = fullname;
        this.isChecked = false;
        this.deviceInfo = info;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public void toggle() {
        this.isChecked = !this.isChecked;
    }

    public boolean getChecked() {
        return this.isChecked;
    }


    public String getFullname() {
        return fullname;
    }

    public DeviceInfo getDeviceInfo(){return deviceInfo;}

}