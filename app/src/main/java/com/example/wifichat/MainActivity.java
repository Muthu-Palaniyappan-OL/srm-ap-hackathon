package com.example.wifichat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.InetAddresses;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    Button btnoff,btndiscover,btnsend;
    ListView listView;
    TextView read_msg_box,ConnectionsStatus;
    EditText writemsg;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    WifiManager wifimanager;
    WifiP2pManager manager;

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    static final int MESSAGE_READ = 1;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialWork();
        exqListener();
    }

    Handler handler = new Handler(new Handler.Callback(){
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff,0,msg.arg1);
                    read_msg_box.setText(tempMsg);
                    break;
            }
            return true;
        }
    });
    private void exqListener() {
        btnoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(wifimanager.isWifiEnabled()){
                    wifimanager.setWifiEnabled(false);
                    btnoff.setText("ON");
                }else{
                    wifimanager.setWifiEnabled(true);
                    btnoff.setText("OFF");
                }
            }
        });

        btndiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        ConnectionsStatus.setText("Discovery Started");
                    }

                    @Override
                    public void onFailure(int i) {
                        ConnectionsStatus.setText("Discovery Failed");
                    }
                });
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final WifiP2pDevice device = deviceArray[i];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(),"Connected to "+device.deviceName,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getApplicationContext(),"Failed to connect ",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = writemsg.getText().toString();
                //sendReceive.write(msg.getBytes());
                sendtask t1=new sendtask(msg);
                t1.execute();

            }
        });

    }

    public class sendtask extends AsyncTask<Void, Void, Void> {

        String message;


        sendtask(String msg) {
            message=msg;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            sendReceive.write(message.getBytes());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

    }
    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;

            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                ConnectionsStatus.setText("Payee");
                serverClass = new ServerClass();
                serverClass.start();
            }else if(wifiP2pInfo.groupFormed){
                ConnectionsStatus.setText("Payer");
                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();
            }
        }
    };
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public class ServerClass extends Thread{
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try{
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            }catch (IOException e){
                e.printStackTrace();
            }

        };
    }
    private class SendReceive extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket skt){
            socket = skt;
            try{
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();

            }catch (IOException e){
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while(socket!=null){
                try{
                    bytes = inputStream.read(buffer);
                    if(bytes>0){
                        handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public class ClientClass extends Thread{
        Socket socket;
        String hostAdd;

        public  ClientClass(InetAddress hostAddress){
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            try{
                socket.connect(new InetSocketAddress(hostAdd,8888),500);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            }catch (IOException e){
                e.printStackTrace();
            }
        };
    }
    
    // Init function for MainActivity
    private void initialWork () {
        btnoff = (Button) findViewById(R.id.onOff);
        btndiscover = (Button) findViewById(R.id.discover);
        btnsend = (Button) findViewById(R.id.sendButton);
        listView = (ListView) findViewById(R.id.peerListView);
        read_msg_box = (TextView) findViewById(R.id.readMsg);
        ConnectionsStatus = (TextView) findViewById(R.id.connectionStatus);
        writemsg = (EditText) findViewById(R.id.writeMsg);

        wifimanager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        receiver = new WiFiDirectBroadcastReceiver(manager,channel,this);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if(!peerList.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                int index = 0;

                for(WifiP2pDevice device : peerList.getDeviceList()){
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }

                ArrayAdapter<String> adapter= new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,deviceNameArray);
                listView.setAdapter(adapter);
            }

            if (peers.size()==0) {
                Toast.makeText(getApplicationContext(),"No device found",Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };

}
