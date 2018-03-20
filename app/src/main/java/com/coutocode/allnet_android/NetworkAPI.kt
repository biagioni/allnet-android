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

    fun startAllnet() {
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
//                while (true) {  // read the ad queue, forward messages to the peers
////                    let n = receive_pipe_message_any (p, PIPE_MESSAGE_WAIT_FOREVER, &buffer, &from_pipe, &priority)
////                    var debug_peers = 0;
////                    for q in 0..< self . sessions . count {
////                        let s = self . sessions [q]
////                        debug_peers += s.connectedPeers.count
////                    }
////                    if debug_peers > 0{
////                        NSLog("multipeer thread got %d-byte message from ad, forwarding to %d peers\n", n, debug_peers)
////                    }
////                    if from_pipe == self.multipeer_read_queue_index && n > 0 {
////                        self.sendSession(buffer: buffer!, length: n)
////                    }
////                    if n > 0 && buffer != nil {
////                        free(buffer)
////                    }
//                }
            })
            background.start()
        }
        Log.d(DEBUG, "astart_main has been started\n")
    }
}