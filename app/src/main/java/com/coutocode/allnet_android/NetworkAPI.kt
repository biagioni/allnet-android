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

    external fun stop_allnet_threads(): Void

    var firstCall = false

    fun startAllnet() {
        if (!firstCall) {
            sleep (1)
            //#if USE_ABLE_TO_CONNECT
//            if ableToConnect() {
//                return
//            }
            stop_allnet_threads()
            Log.d(DEBUG, "calling stop_allnet_threads\n")
            sleep (1)
           // #endif /* USE_ABLE_TO_CONNECT */
            Log.d(DEBUG, "reconnecting xcommon to alocal\n")
            //xChat.reconnect()
            sleep (1)
        }

//        application.beginBackgroundTask {
//            NSLog("allnet task ending background task (started by calling astart_main)\n")
//            acache_save_data()
//            self.xChat.disconnect()
//        }
        if (firstCall) {
//            Log.d(DEBUG, "calling astart_main\n")
//            DispatchQueue.global(qos: .userInitiated).async {
//            let args = ["allnet", "-v", "default", nil]
//            var pointer = args.map{Pointer(mutating: (($0 ?? "") as NSString).utf8String)}
//            astart_main(3, &pointer)
//            NSLog("astart_main has completed, starting multipeer thread\n")
//            multipeer_queue_indices(&self.self.multipeer_read_queue_index, &self.multipeer_write_queue_index)
//            self.multipeer_queues_initialized = 1
//            // the rest of this is the multipeer thread that reads from ad and forwards to the peers
//            let p = init_pipe_descriptor (self.allnet_log)
//            add_pipe(p, self.multipeer_read_queue_index, "AppDelegate multipeer read pipe from ad")
//            while (true) {  // read the ad queue, forward messages to the peers
//                var buffer: Pointer?
//                var from_pipe: Int32 = 0
//                var priority: UInt32 = 0
//                let n = receive_pipe_message_any(p, PIPE_MESSAGE_WAIT_FOREVER, &buffer, &from_pipe, &priority)
//                var debug_peers = 0;
//                for q in 0..<self.sessions.count {
//                    let s = self.sessions[q]
//                    debug_peers += s.connectedPeers.count
//                }
//                if debug_peers > 0{
//                    NSLog("multipeer thread got %d-byte message from ad, forwarding to %d peers\n", n, debug_peers)
//                }
//                if from_pipe == self.multipeer_read_queue_index && n > 0 {
//                    self.sendSession(buffer: buffer!, length: n)
//                }
//                if n > 0 && buffer != nil {
//                    free(buffer)
//                }
//            }
        }
           // NSLog("astart_main has been started\n")
       // }
    }
}