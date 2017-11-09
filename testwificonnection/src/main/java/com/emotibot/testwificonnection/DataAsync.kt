package com.emotibot.testwificonnection

import android.os.AsyncTask
import android.widget.TextView
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket

/**
 * Created by wangwei on 2017/11/9.
 */
class DataAsync : AsyncTask<Void, Void, String> {

    var showContent: TextView

    constructor(showContent: TextView) : super() {
        this.showContent = showContent
    }

    companion object {
        val SOCKET_PORT = 11190
    }

    override fun onPostExecute(result: String?) {
        showContent.text = result
    }

    override fun doInBackground(vararg params: Void?): String {
        try {
            var serverSocket: ServerSocket = ServerSocket(SOCKET_PORT)
            var client: Socket = serverSocket.accept()
            var ins: InputStream = client.getInputStream()
            var insr: InputStreamReader = InputStreamReader(ins)
            var br: BufferedReader = BufferedReader(insr)
            var result = br.readLine()
            serverSocket.close()
            return result
        } catch (e: Throwable) {
            return "Port in use"
        }


    }

}