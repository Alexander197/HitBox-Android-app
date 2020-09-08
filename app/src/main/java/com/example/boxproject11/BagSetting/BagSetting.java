package com.example.boxproject11.BagSetting;

public class BagSetting {
    public int bagWeight;
    public int threshold;
    public boolean error = false;
    BagSetting(int bw, int th)
    {
        bagWeight = bw;
        threshold = th;
    }
    BagSetting()
    {
        error = true;
    }
}
