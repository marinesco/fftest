package org.fftest.fftest

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray

/**
 * Created by Marina Zhdanova
 * Contact by email: m.zhdanova@rambler.ru | telegram: t.me/marinesco
 **/

class TickerViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = this::class.java.simpleName
    private var socket: Socket? = null
    private val tickersList : MutableLiveData<List<TickerModel>> by lazy {
        MutableLiveData<List<TickerModel>>()
    }

    companion object {
        private const val WS_SOCKETURL = "https://ws3.tradernet.ru/"
    }

    init {
        initSocket()
    }

    fun getUpdaties() : MutableLiveData<List<TickerModel>> = tickersList

    private fun getTickersToWatchChanges(): List<String> =
        listOf("RSTI","GAZP","MRKZ","RUAL","HYDR","MRKS","SBER",
        "FEES","TGKA","VTBR","ANH.US","VICL.US","BURG.US","NBL.US",
        "YETI.US","WSFS.US","NIO.US","DXC.US","MIC.US","HSBC.US",
        "EXPN.EU","GSK.EU","SHP.EU","MAN.EU","DB1.EU","MUV2.EU",
        "TATE.EU","KGF.EU","MGGT.EU","SGGD.EU")

    private fun initSocket() {
        socket = IO.socket(WS_SOCKETURL).apply {
            on("q") {
                tickersList.postValue(Gson().fromJson(it[0].toString(), TickerQModel::class.java).data)
            }
            on(Socket.EVENT_CONNECT) {
                Log.i(TAG, "socket $WS_SOCKETURL connected")
                emit("sup_updateSecurities2", JSONArray(getTickersToWatchChanges()))
            }
            on(Socket.EVENT_DISCONNECT) {
                Log.i(TAG, "socket $WS_SOCKETURL disconnected")
            }
        }
    }

    fun subscribeToUpdaties() {
        socket?.connect()
    }

    fun unsubscribeFromUpdaties() {
        socket?.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        unsubscribeFromUpdaties()
    }
}