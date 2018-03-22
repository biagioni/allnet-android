//
// Created by hippolyte lacassagne on 05/09/2017.
//

/* gui_socket.c: implement functions needed by the GUI and send and receive
 * information across a socket */

#if defined(WIN32) || defined(WIN64)
#ifndef WINDOWS_ENVIRONMENT
#define WINDOWS_ENVIRONMENT
#define WINDOWS_ENVIRONMENT
#endif /* WINDOWS_ENVIRONMENT */
#endif /* WIN32 || WIN64 */

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <pthread.h>
#include <signal.h>
#include <sys/socket.h>
#include <netinet/ip.h>
#include <syslog.h>


#include "lib/util.h"
#include "lib/pipemsg.h"
#include "xchat/xcommon.h"
#include "android_xchat.h"

static pid_t xchat_socket_pid = -1;
static pid_t xchat_ui_pid = -1;

static void kill_if_not_self (pid_t pid, const char * desc)
{
    if ((pid != -1) && (pid != getpid ())) {
/* printf ("process %d killing %s process %d\n", getpid (), desc, pid); */
        kill (pid, SIGKILL);
    }
}

/* exit code should be 0 for normal exit, 1 for error exit */
void stop_chat_and_exit (int exit_code)
{
    kill_if_not_self (xchat_socket_pid, "xchat_socket");
    kill_if_not_self (xchat_ui_pid, "xchat_ui");
    exit (exit_code);
}

static int create_allnet_sock (const char * program_name,
                               const char * path, pd * p)
{
    //struct allnet_log * log = init_log ("xchat_socket");
   // *p = init_pipe_descriptor (log);
    int sock = xchat_init (program_name, path, *p);
    return sock;
}


int main_gui (int argc, char ** argv)
{
    /* use buffer + 12 to skip over most of the date (04/14 03:13:) */
    syslog (LOG_DAEMON | LOG_WARNING, "ok main_gui");
    /* general initialization */
    xchat_socket_pid = getpid ();  /* needed to properly kill other procs */
    //log_to_output (get_option ('v', &argc, argv));

    /* create the allnet socket and the GUI socket */
    pd p;
    /* argv [1] is normally NULL, unless someone specified a config directory */
    int allnet_sock = create_allnet_sock (argv [0], argv [1], &p);
    if (allnet_sock < 0)
        return 1;

    /* create the thread to handle messages from the GUI */
    void * args = malloc_or_fail (sizeof (int) * 2, "gui_socket main");
    ((int *) args) [0] = allnet_sock;
    ((int *) args) [1] = allnet_sock;
    pthread_t t;
    pthread_create (&t, NULL, gui_respond_thread, args);

    gui_socket_main_loop (allnet_sock, allnet_sock, p);  /* run until exit */

    stop_chat_and_exit (0);
    return 0;   /* should never be called */
}

