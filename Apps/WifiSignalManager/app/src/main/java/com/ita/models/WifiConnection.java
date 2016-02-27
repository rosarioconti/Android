package com.ita.models;

/**
 * Created by rosarioconti on 19/02/16.
 */
public class WifiConnection {

    private String ssid = "";
    private String bssid = "";
    private int level = 0;

    public WifiConnection(String SSID, String BSSID, int Level) {
        this.ssid = SSID;
        this.bssid = BSSID;
        this.level = Level;
    }

    public String getSSID() {
        return this.ssid;
    }

    public void setSSID(String SSID) {
        this.ssid = SSID;
    }

    public String getBSSID() {
        return this.bssid;
    }

    public void setBSSID(String BSSID) {
        this.bssid = BSSID;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int Level) {
        this.level = Level;
    }

    public String toString() {
        String message = String.format("%s is the strongest signal.",
                this.ssid + " : " + this.bssid + " : " + this.level);
        return message;
    }

}
