package com.coutocode.allnet_android

import android.system.Os.*
import android.system.OsConstants.*
import android.util.Log
import java.lang.Thread.sleep


/**
 * Created by docouto on 3/19/18.
 */


class NetworkAPI {

    val DEBUG = "NETWORKAPI_DEBUG"

    external fun stopAllnetThreads(): Void
    external fun ableToConnect(): Int
    external fun reconnect(): Void
    external fun acacheSaveData(): Void
    external fun astartMain(): Int
    external fun initLog(): Void
    external fun addPipe(): Void

    var firstCall = true

    init {
        startAllnet()
        sleep(1)
    }

    private fun startAllnet() {
        if (!firstCall) {
            sleep(1)
            if (ableToConnect() == 1) {
                return
            }
            stopAllnetThreads()
            Log.d(DEBUG, "calling stop_allnet_threads\n")
            sleep(1)
            Log.d(DEBUG, "reconnecting xcommon to alocal\n")
            reconnect()
            sleep(1)
        }


        acacheSaveData()

        if (firstCall) {
            Log.d(DEBUG, "calling astart_main\n")
            initLog()
            val background = Thread({
                astartMain()
                Log.d(DEBUG, "astart_main has completed, starting multipeer thread\n")
                addPipe()
            })
            background.start()
        }
        Log.d(DEBUG, "astart_main has been started\n")
    }
}