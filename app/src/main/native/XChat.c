//
// Created by Tiago Do Couto on 3/20/18.
//


#include <lib/allnet_log.h>
#include <lib/pipemsg.h>
#include <xchat/xcommon.h>
#include <pthread.h>


static pd p;
static void * splitPacketBuffer = NULL;
int sock;

void initialize() {
    struct allnet_log * alog = init_log ("ios xchat");
    p = init_pipe_descriptor(alog);
    splitPacketBuffer = NULL;
    sock = xchat_init ("xchat", NULL, p);
}