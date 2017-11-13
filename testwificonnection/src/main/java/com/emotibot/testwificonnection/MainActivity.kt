package com.emotibot.testwificonnection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.os.AsyncTask
import android.os.Bundle
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var p2pManager: WifiP2pManager
    private lateinit var mChannel: WifiP2pManager.Channel
    private lateinit var receiver: BroadcastReceiver
    private var peers: MutableList<WifiP2pDevice> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        p2pManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        mChannel = p2pManager.initialize(this, Looper.getMainLooper(), null)
        var intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION)
        receiver = WifiDirectReceiver(p2pManager, mChannel, this, peerListListener)
        registerReceiver(receiver, intentFilter)
        adapter = ArrayAdapter<WifiP2pDevice>(this, android.R.layout.simple_list_item_1, peers)
        p2p_list.adapter = adapter
        p2p_list.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                var device: WifiP2pDevice = adapter.getItem(position)
                this@MainActivity.createConnection(device.deviceAddress, device.deviceName)
            }

        }

        find_btn.setOnClickListener {
            p2pManager.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
                override fun onFailure(p0: Int) {
                    Log.i("p2p", "discoverPeers failure")
                }

                override fun onSuccess() {
                    Log.i("p2p", "discoverPeers success")
                }

            })
        }

        listen.setOnClickListener{
            var dataAsync = DataAsync(result_content)
                dataAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }

        create_group.setOnClickListener { beGroupOwner() }

        send_msg.setOnClickListener {
            thread(start = true) {
                var socket: Socket = Socket()
                Log.i("p2p", "Address:"+info.groupOwnerAddress.hostAddress)
                socket.connect(InetSocketAddress(info.groupOwnerAddress, DataAsync.SOCKET_PORT))
                var op: OutputStreamWriter = OutputStreamWriter(socket.getOutputStream())
                op.write("账号：123 密码123"+Date().time)
                op.close()
                socket.close()
            }
        }
    }

    fun showWifiEnable(isEnable: Boolean) {
        wifi_state.text = if (isEnable) "WifiP2P is open" else "WifiP2P is close"
    }

    fun showP2pStatus(status: String) {
        p2p_status.text = "WifiP2P is " + status
    }

    fun showConnectionStatus(isConnected: Boolean) {
        connection_status.text = "Connection:" + if (isConnected) "Connected" else "Disconnected"
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    fun beGroupOwner() {
        p2pManager.createGroup(mChannel, object : WifiP2pManager.ActionListener {
            override fun onFailure(reason: Int) {
                Log.i("p2p", "Create group failure")
            }

            override fun onSuccess() {
                Log.i("p2p", "Create group success")
            }

        })
    }

    fun createConnection(address: String, name: String) {
//        var device:WifiP2pDevice
        var config: WifiP2pConfig = WifiP2pConfig()
        config.deviceAddress = address
        config.wps.setup = WpsInfo.PBC
        p2pManager.connect(mChannel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.i("p2p", "connect success")
            }

            override fun onFailure(reason: Int) {
                Log.i("p2p", "connect failure")
            }

        })
    }

    fun showPeerList() {
        adapter.notifyDataSetChanged()
    }

    private lateinit var adapter: ArrayAdapter<WifiP2pDevice>

    private var peerListListener: WifiP2pManager.PeerListListener = object : WifiP2pManager.PeerListListener {
        override fun onPeersAvailable(newpeers: WifiP2pDeviceList?) {
            var refreshedPeers: Collection<WifiP2pDevice>? = newpeers!!.deviceList
            Log.i("p2p", "Peer list size:" + refreshedPeers!!.size)
            peers.clear()
            peers.addAll(refreshedPeers)
            this@MainActivity.showPeerList()
        }
    }

    lateinit var info: WifiP2pInfo



    var infoListener: WifiP2pManager.ConnectionInfoListener = object : WifiP2pManager.ConnectionInfoListener {
        override fun onConnectionInfoAvailable(info: WifiP2pInfo) {
            this@MainActivity.info = info

            if (info!!.isGroupOwner) {
                var dataAsync = DataAsync(result_content)
                dataAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
        }

    }

    class WifiDirectReceiver(private var mManager: WifiP2pManager, private var mChannel: WifiP2pManager.Channel, private var mainActivity: MainActivity, private var peerListListener: WifiP2pManager.PeerListListener) : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            var action = intent?.action
            when (action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    var state = intent?.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    mainActivity.showWifiEnable(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    Log.i("p2p", "WIFI_P2P_PEERS_CHANGED_ACTION")
                    mManager.requestPeers(mChannel, peerListListener)
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    Log.i("p2p", "WIFI_P2P_PEERS_CHANGED_ACTION")
                    var networkinfo: NetworkInfo = intent!!.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)
                    mainActivity.showConnectionStatus(networkinfo.isConnected)
                    if (networkinfo.isConnected) {
                        mManager.requestConnectionInfo(mChannel, mainActivity.infoListener)
                    }
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    Log.i("p2p", "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION")
                }
                WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION -> {
                    var dis_status = intent?.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1)
                    mainActivity.showP2pStatus(if (dis_status == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) "开始" else if (dis_status == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) "结束" else "未知")
                }
                else -> {
                    Log.i("p2p", "else is print")
                }
            }
        }
    }
}
