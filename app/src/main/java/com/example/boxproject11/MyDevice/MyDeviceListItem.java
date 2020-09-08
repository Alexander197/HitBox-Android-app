package com.example.boxproject11.MyDevice;

public class MyDeviceListItem {
    private String name;
    private int connectionState;

    public MyDeviceListItem(String name, int state)
    {
        this.name = name;
        this.connectionState = state;
    }
    public String getName()
    {
        return this.name;
    }
    public int getConnectionState()
    {
        return this.connectionState;
    }
}
