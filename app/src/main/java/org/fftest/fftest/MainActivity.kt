package org.fftest.fftest

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var viewModel : TickerViewModel? = null
    private var networkConnectionReceiver: ConnectivityManager.NetworkCallback? = null
    private var isInternetConnected: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initLayout()

        viewModel = ViewModelProvider(this).get(TickerViewModel::class.java).apply {
            getUpdaties().observe(this@MainActivity, Observer { tickerList ->
                if (!tickerList.isNullOrEmpty()) {
                    updateTickerData(tickerList)
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        showProgressIndicator()
        registerConnectivityNetworkMonitor()
    }

    override fun onPause() {
        super.onPause()
        viewModel?.unsubscribeFromUpdaties()
        unregisterConnectivityNetworkMonitor()
    }

    private fun initLayout() {
        activity_main_ticker_recyclerview.layoutManager = LinearLayoutManager(this)
        activity_main_ticker_recyclerview.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    fun updateTickerData(tickerDataList: List<TickerModel>) {
        hideProgressIndicator()

        if (activity_main_ticker_recyclerview.adapter == null) {
            activity_main_ticker_recyclerview.adapter = TickerAdapter(tickerDataList.toMutableList())
        } else {
            for (item in tickerDataList) {
                (activity_main_ticker_recyclerview.adapter as TickerAdapter).updateItem(item)
            }
        }
    }

    private fun registerConnectivityNetworkMonitor() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkConnectionReceiver = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                super.onLost(network)
                isInternetConnected = false
                showNegativeSnackbar(R.string.msg_connection_lost)
                showProgressIndicator()
            }

            override fun onUnavailable() {
                super.onUnavailable()
                isInternetConnected = false
                showNegativeSnackbar(R.string.msg_connection_unavailable)
                showProgressIndicator()
            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                viewModel?.subscribeToUpdaties()
                if (!isInternetConnected) {
                    isInternetConnected = true
                    showPositiveSnackbar(R.string.msg_connection_available)
                }
            }
        }

        networkConnectionReceiver?.let {
            cm.registerNetworkCallback(NetworkRequest.Builder().build(), it)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (cm.activeNetworkInfo?.isConnectedOrConnecting != true) {
                isInternetConnected = false
                showNegativeSnackbar(R.string.msg_connection_unavailable)
            }
        }
    }

    private fun unregisterConnectivityNetworkMonitor() {
        networkConnectionReceiver?.let {
            (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).unregisterNetworkCallback(it)
        }
    }

    private fun showProgressIndicator() {
        runOnUiThread {
            activity_main_ticker_progress_indicator.visibility = View.VISIBLE
        }
    }

    private fun hideProgressIndicator() {
        runOnUiThread {
            activity_main_ticker_progress_indicator.visibility = View.GONE
        }
    }

    fun showPositiveSnackbar(@StringRes msgResId: Int) {
        snackbar(msgResId, ContextCompat.getColor(this@MainActivity, R.color.colorGreen))
    }

    fun showNegativeSnackbar(@StringRes msgResId: Int) {
        snackbar(msgResId, ContextCompat.getColor(this@MainActivity, R.color.colorRed), Snackbar.LENGTH_INDEFINITE)
    }

    fun Activity.snackbar(@StringRes msgResId: Int, @ColorInt backgroundColor: Int, duration: Int = Snackbar.LENGTH_LONG) {
        runOnUiThread {
            Snackbar.make(this.window.decorView.rootView, msgResId, duration).apply {
                setActionTextColor(ContextCompat.getColor(this@snackbar, android.R.color.white))
                setBackgroundTint(backgroundColor)
                setAction(android.R.string.ok) { }

                val textView = view.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
                textView.isSingleLine = false
                textView.setTextColor(ContextCompat.getColor(this@snackbar, android.R.color.white))
                show()
            }
        }
    }
}