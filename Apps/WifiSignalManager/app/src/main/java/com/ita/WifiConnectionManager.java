package com.ita;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.ita.models.WifiConnection;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.List;

/**
 * Created by rosarioconti on 19/02/16.
 */
public class WifiConnectionManager extends BroadcastReceiver {

    private static WifiConnectionManager SHAREDINSTANCE;

    private static String configuredSSID = "MDC-ENT-DEVICES";  //Default
    private static Context mContext;

    private static WifiManager wifiManager;
    private static List<ScanResult> wifiConnections;
    private static WifiConnection currentConnection, newConnection;
    private static WifiConfiguration wifiConf;
    private static boolean useWifiScan = false;
    private static int thresold = 10;
    /**
     * The WifiConnectionManager singleton. Always call
     * <code>createSharedInstance()</code> before using this singleton.
     *
     * @return The WifiConnectionManager singleton
     */
    public static WifiConnectionManager sharedInstance() {
        return WifiConnectionManager.SHAREDINSTANCE;
    }

    protected WifiConnectionManager(Context context, boolean UseWifiScan) {

        useWifiScan = UseWifiScan;
        if (context != null) {
            mContext = context;
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            //cleanUpConfigurations();  //For Debugging use and state reset

            //The main SSID used on initialization
            if (wifiManager.getConnectionInfo().getSSID() != "") {
                configuredSSID = convertToQuotedString(wifiManager.getConnectionInfo().getSSID());
            }
            currentConnection = new WifiConnection(configuredSSID, wifiManager.getConnectionInfo().getBSSID(), -1);
            newConnection = new WifiConnection(configuredSSID, wifiManager.getConnectionInfo().getBSSID(), -1);

            int level = WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), thresold);
            currentConnection.setLevel(level);

            Log.d("current ", "current: " + currentConnection.toString());

            wifiConf = new WifiConfiguration();
            wifiConf.SSID = configuredSSID;
            wifiConf.BSSID = wifiManager.getConnectionInfo().getBSSID();
            wifiConf.status = WifiConfiguration.Status.ENABLED;
            wifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

            if (!checkIfConfiguredNetworkExist(newConnection.getBSSID())) {
                wifiManager.addNetwork(wifiConf);
            }
        }
    }

    public static void createSharedInstanceWifiConnectionManager(Context context, boolean UseWifiScan) {
        if (WifiConnectionManager.SHAREDINSTANCE == null) {
            WifiConnectionManager.SHAREDINSTANCE = new WifiConnectionManager(context,UseWifiScan);
        }
    }

    public WifiConnection searchAndConnectToStrongestBSSID() {

        if (mContext != null && useWifiScan) {
            wifiConnections = wifiManager.getScanResults();
            for (ScanResult scanResult : wifiConnections) {
                if (scanResult.SSID.contains(configuredSSID)) {
                    int signalLevel = WifiManager.calculateSignalLevel(scanResult.level, thresold);

                    Log.d("searchAndCStBSSID", "calculateSignalLevel: currentsignal" +currentConnection.getLevel() + " (( "+ scanResult.SSID + " - " + scanResult.BSSID + " - " + signalLevel);

                    if (signalLevel > currentConnection.getLevel() && newConnection.getSSID().contentEquals(currentConnection.getSSID())) {
                        Log.d("searchAndCStBSSID", "found a powerful connection: " + scanResult.SSID + " - " + scanResult.BSSID + " - " + signalLevel);

                        newConnection = new WifiConnection(scanResult.SSID, scanResult.BSSID, signalLevel);
                    }

                }
            }

            if (newConnection.getBSSID() != null && currentConnection.getBSSID() != null) {
                if (!newConnection.getBSSID().contentEquals(currentConnection.getBSSID()) &&
                        newConnection.getSSID().contentEquals(currentConnection.getSSID()) &&
                        newConnection.getLevel() > currentConnection.getLevel() ) {

                    Log.d("searchAndCStBSSID", " there is a stronger connection: " + newConnection.toString());


                    wifiConf = new WifiConfiguration();//checking
                    wifiConf.SSID = newConnection.getSSID();
                    wifiConf.BSSID = newConnection.getBSSID();
                    wifiConf.status = WifiConfiguration.Status.ENABLED;
                    wifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

                    //Add new connection if doesn't exist
                    if (!checkIfConfiguredNetworkExist(newConnection.getBSSID())) {
                        Log.d("searchAndCStBSSID", "collecting new connection if not configured already: " + newConnection.toString());
                        wifiManager.addNetwork(wifiConf);
                    }

                    //Check if connected already before connecting to new bssid
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    SupplicantState supplicantState = wifiInfo.getSupplicantState();

                    Log.d("searchAndCStBSSID", "SupplicantState: "  + supplicantState.toString());
                    if (supplicantState.equals(SupplicantState.DISCONNECTED) ||
                            supplicantState.equals(SupplicantState.DORMANT) ||
                            supplicantState.equals(SupplicantState.INACTIVE) ||
                            supplicantState.equals(SupplicantState.INVALID) ||
                            supplicantState.equals(SupplicantState.UNINITIALIZED)) {
                        Log.d("searchAndCStBSSID", "performing reconnection");

                        currentConnection.setSSID(newConnection.getSSID()); //still needed in case of new configuration
                        currentConnection.setBSSID(newConnection.getBSSID());
                        currentConnection.setLevel(newConnection.getLevel());

                        connectToBSSID(newConnection.getSSID(), newConnection.getBSSID());
                    }

                }
            }

            int rssi = wifiManager.getConnectionInfo().getRssi();
            int level = WifiManager.calculateSignalLevel(rssi, thresold);
            currentConnection.setLevel(level);


        }

        return (currentConnection != null) ? new WifiConnection(currentConnection.getSSID(), currentConnection.getBSSID(), currentConnection.getLevel()) : null ;

    }

    private static Boolean checkIfConfiguredNetworkExist(String BSSID){
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        if (list != null) {
            for (WifiConfiguration i : list) {
                if (i.BSSID != null) {
                    if (i.BSSID.trim().contentEquals(BSSID)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String getWifiState(int state)
    {
        switch(state)
        {
            case 0:
                return "DISABLING WIFI...";
            case 1:
                return "WIFI DISABLED.";
            case 2:
                return "ENABLING WIFI...";
            case 3:
                return "WIFI ENABLED.";
            case 4:
                return "WIFI_STATE_UNKNOWN!";
        }

        return "No wifi state found!";
    }

    /**
     * connectToBSSID
     * @desc it searchs for the configured network provided and connects to it
     * @input SSID , BSSID
     * @return null
     */
    private void connectToBSSID(String SSID, String BSSID) {
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.contentEquals( SSID )  && i.BSSID.contentEquals(BSSID) )  {
                Log.d("confcontained", "connecting to -> netw: " + i.SSID + " - connecting to : " + i.BSSID + " -netid : " + i.networkId);
                Log.d("CONNECTION", " did we enable? " + wifiManager.enableNetwork(i.networkId, true));
                Log.d("CONNECTION", " did we connect? " + wifiManager.reconnect());
                //wifiManager.enableNetwork(i.networkId, true);
                //wifiManager.reconnect();

                break;
            }
        }

    }

    /**
     * cleanUpConfigurations
     * @desc it will try clean up all configured network Ids
     * used only for debug
     * @return null
     */
    private void cleanUpConfigurations() {
        for (int i = 1; i < 1500; i++){
            wifiManager.removeNetwork(i);
        }
        wifiManager.saveConfiguration();
    }

    public List<ScanResult> getCurrentConnections(){
        return wifiConnections;
    }

    /**
     * getCurrentWifiInfo
     * @desc Provides wifi info of the connection
     * used only for debug
     * @return host address, BSSID , Wifi state
     */
    public String getCurrentWifiInfo(){

        try {

            int ipAddress = wifiManager.getDhcpInfo().ipAddress;
            byte[] ipAddressbyte = BigInteger.valueOf(ipAddress).toByteArray();
            for (int i = 0; i < ipAddressbyte.length / 2; i++) {
                int temp = ipAddressbyte[i];
                ipAddressbyte[i] = ipAddressbyte[ipAddressbyte.length - i - 1];
                ipAddressbyte[ipAddressbyte.length - i - 1] = (byte) temp;
            }

            InetAddress myaddr = InetAddress.getByAddress(ipAddressbyte);
            String hostaddr = myaddr.getHostAddress(); // numeric representation (such as "127.0.0.1")
            return "" + hostaddr + " - " + wifiManager.getConnectionInfo().getBSSID()+ "-" + getWifiState(wifiManager.getWifiState());
        }
        catch (Exception exc) {
            Log.d("Exception", exc.getMessage());
        }

        return "No ip." + " - " + wifiManager.getConnectionInfo().getBSSID() + "-" + getWifiState(wifiManager.getWifiState());
    }


    private static String convertToQuotedString(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }

        final int lastPos = string.length() - 1;
        if(lastPos > 0 && (string.charAt(0) == '"' && string.charAt(lastPos) == '"')) {
            return string;
        }

        return "\"" + string + "\"";
    }

    @Override
    public void onReceive(Context c, Intent intent) {
        Log.d("DisconnectWifi", "state change reveived! : " + intent.toString());
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        SupplicantState supplicantState = wifiInfo.getSupplicantState();

        Log.d("searchAndCStBSSID", "SupplicantState: "  + supplicantState.toString());
        if (supplicantState.equals(SupplicantState.DISCONNECTED) ||
                supplicantState.equals(SupplicantState.DORMANT) ||
                supplicantState.equals(SupplicantState.INACTIVE) ||
                supplicantState.equals(SupplicantState.INVALID) ||
                supplicantState.equals(SupplicantState.UNINITIALIZED)) {

            searchAndConnectToStrongestBSSID();
        }
    }
}
