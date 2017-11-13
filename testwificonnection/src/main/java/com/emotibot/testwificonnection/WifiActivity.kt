package com.emotibot.testwificonnection

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_wifi.*

/**
 * Created by wangwei on 2017/11/9.
 */
class WifiActivity : AppCompatActivity(){

    lateinit var wifiManager:WifiManager

    lateinit var adapter:ArrayAdapter<ScanResult>

    var list:MutableList<ScanResult> = mutableListOf()

    @SuppressLint("WifiManagerLeak")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi)
        wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
//        adapter = object:ArrayAdapter<WifiConfiguration>(this, android.R.layout.simple_list_item_1, list){
//
//            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//                var view:View
//                if (convertView == null) {
//                    view = View.inflate(this@WifiActivity,android.R.layout.simple_list_item_1,null)
//                } else {
//                    view = convertView
//                }
//
//                var wifiConfig = getItem(position)
//                var content = wifiConfig.SSID + ":" +  wifiConfig.allowedKeyManagement
//                (view as TextView).text = content
//                return view
//            }
//        }
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        wifi_list.adapter = adapter
        wifi_list.setOnItemClickListener { parent, view, position, id ->
            var scanResult = adapter.getItem(position)
            var conf = WifiConfiguration()
            conf.SSID = String.format("\"%s\"", scanResult.SSID)
            conf.preSharedKey = String.format("\"%s\"", "emotibot007")
            var netid = wifiManager.addNetwork(conf)
            wifiManager.disconnect()
            wifiManager.enableNetwork(netid, true)
            wifiManager.reconnect()

        }
        refresh.setOnClickListener { refreshWifiList() }

    }

    fun refreshWifiList(){
        list.clear()
        if(!wifiManager.isWifiEnabled){
            wifiManager.isWifiEnabled = true
        }
        var result = wifiManager.startScan()
        Log.i("p2p","result:"+result)
        list.addAll(wifiManager.scanResults)
        adapter.notifyDataSetChanged()
    }
}