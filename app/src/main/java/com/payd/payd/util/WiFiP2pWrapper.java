package com.payd.payd.util;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.*;
import android.util.Log;
import android.util.Patterns;
import androidx.annotation.Nullable;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

public class WiFiP2pWrapper {
    final static String TAG = "WIFI_P2P_SERVICE";
    public static void discoverPeers(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, Runnable func) {
        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Discovering Peers");
                func.run();
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG, "Unable to discover peers "+i);
            }
        });
    }

    public static void stopDiscoveringPeers(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, Runnable func) {
        wifiP2pManager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Stopped Discovering Peers");
                func.run();
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG, "Unable to stop discovering peers "+i);
            }
        });
    }

    public static void requestPeers(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, Consumer<WifiP2pDeviceList> func) {
        wifiP2pManager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                func.accept(wifiP2pDeviceList);
            }
        });
    }

    public static void connect(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, String deviceaddress, Runnable func) {
        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
        wifiP2pConfig.deviceAddress = deviceaddress;
        wifiP2pConfig.groupOwnerIntent = WifiP2pConfig.GROUP_OWNER_INTENT_MIN;
        wifiP2pConfig.wps.setup = WpsInfo.PBC;
        wifiP2pManager.connect(channel, wifiP2pConfig, new WifiP2pManager.ActionListener(){

            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: Connected to "+deviceaddress);
                func.run();
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG, "onSuccess: unable to connect "+deviceaddress);
            }
        });
    }

    public static void connectionInfo(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, Consumer<WifiP2pInfo> func) {
        wifiP2pManager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
                Log.d(TAG, "onConnectionInfoAvailable: "+wifiP2pInfo);
                func.accept(wifiP2pInfo);
            }
        });
    }

    public static boolean isReceiverRegistered(Context context, BroadcastReceiver receiver) {
        Intent intent = new Intent(context, receiver.getClass());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        return pendingIntent != null;
    }

    public static void createGroup(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, Runnable func) {
        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
        wifiP2pConfig.groupOwnerIntent = 15;
        wifiP2pConfig.wps.setup = 0;
        wifiP2pManager.createGroup(channel, wifiP2pConfig, new WifiP2pManager.ActionListener(){
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: Created group");
                func.run();
            }
            @Override
            public void onFailure(int i) {
                Log.d(TAG, "onSuccess: unable to create group "+i);
            }
        });
    }

    public static void removeGroup(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, Runnable func, Runnable elseFunc) {
        wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener(){
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: removed group");
                func.run();
            }
            @Override
            public void onFailure(int i) {
                Log.d(TAG, "onSuccess: unable to remove from group "+i);
                elseFunc.run();
            }
        });
    }

    public static void requestGroupInfo(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, Consumer<WifiP2pGroup> func) {
        wifiP2pManager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                func.accept(wifiP2pGroup);
            }
        });
    }

    public static void requestDeviceInfo(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, Consumer<WifiP2pDevice> func) {
        wifiP2pManager.requestDeviceInfo(channel, new WifiP2pManager.DeviceInfoListener() {
            @Override
            public void onDeviceInfoAvailable(@Nullable WifiP2pDevice wifiP2pDevice) {
                func.accept(wifiP2pDevice);
            }
        });
    }

    public static String getLocalIPAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!addr.isLinkLocalAddress() && !addr.isLoopbackAddress() && addr instanceof Inet4Address && Pattern.matches("192.168.49.*", addr.getHostAddress())) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String convertDeviceAddressToIp(String deviceAddress) throws UnknownHostException {
        String[] addressParts = deviceAddress.split(":");
        byte[] deviceAddressBytes = new byte[addressParts.length];

        for (int i = 0; i < addressParts.length; i++) {
            deviceAddressBytes[i] = (byte) Integer.parseInt(addressParts[i], 16);
        }

        InetAddress deviceIpAddress = InetAddress.getByAddress(deviceAddressBytes);
        return deviceIpAddress.getHostAddress();
    }

}
