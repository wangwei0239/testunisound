package com.emotibot.testwificonnection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var wifiP2p: WifiP2pManager
    private lateinit var mChannel: WifiP2pManager.Channel
    private lateinit var receiver: BroadcastReceiver
    private var peers: MutableList<WifiP2pDevice> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        wifiP2p = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        mChannel = wifiP2p.initialize(this, Looper.getMainLooper(), null)
        var intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        receiver = WifiDirectReceiver(wifiP2p, mChannel, this, peerListListener)
        registerReceiver(receiver, intentFilter)

        find_btn.setOnClickListener {
            wifiP2p.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
                override fun onFailure(p0: Int) {
                    Log.i("p2p", "discoverPeers failure")
                }

                override fun onSuccess() {
                    Log.i("p2p", "discoverPeers success")
                }

            })
        }
    }

    fun showWifiEnable(isEnable: Boolean) {
        wifi_state.text = if (isEnable) "WifiP2P is open" else "WifiP2P is close"
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private var peerListListener: WifiP2pManager.PeerListListener = object : WifiP2pManager.PeerListListener {
        override fun onPeersAvailable(newpeers: WifiP2pDeviceList?) {
            var refreshedPeers: Collection<WifiP2pDevice>? = newpeers!!.deviceList
            Log.i("p2p", "Peer list size:" + refreshedPeers!!.size)
            if (!refreshedPeers.equals(peers)) {
                peers.clear()
                peers.addAll(refreshedPeers)
            }
        }
    }

    class WifiDirectReceiver(mManager: WifiP2pManager, mChannel: WifiP2pManager.Channel, mainActivity: MainActivity, peerListListener: WifiP2pManager.PeerListListener) : BroadcastReceiver() {

        var mManager = mManager
        var mChannel = mChannel
        var mainActivity = mainActivity
        var peerListListener = peerListListener

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
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    Log.i("p2p", "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION")
                }
                else -> {
                    Log.i("p2p", "else is print")
                }
            }
        }
    }
}
