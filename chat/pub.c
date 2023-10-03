//  Weather update server
//  Binds PUB socket to tcp://*:5556
//  Publishes random weather updates

#include "zhelpers.h"

int main (void)
{

    //  Prepare our context and publisher
    void *context = zmq_ctx_new();

    void *publisher = zmq_socket (context, ZMQ_PUB);
    int rc = zmq_bind (publisher, "tcp://127.0.0.1:5556");
    assert (rc == 0);

    char username[256];
    char topic[256];
    printf("Chat starting\n");
    printf("Enter your username:");
    scanf("%s", username);
    printf("Enter your topic:");
    scanf("%s", topic);

    while (1) {
        //  Send message to all subscribers
        char msg[1024];
        printf("->");

        // Use fgets to read a line from the console
        if (fgets(msg, sizeof(msg), stdin) == NULL) {
            printf("Error reading input\n");
        }
        msg[strlen(msg) - 1] = '\0';

        char update [2048];
        sprintf (update, "%s;%s;%s", username, topic, msg);
        s_send (publisher, update);

    }

    zmq_ctx_destroy (context);
    return 0;
}
