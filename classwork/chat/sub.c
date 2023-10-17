//  Weather update client
//  Connects SUB socket to tcp://localhost:5556
//  Collects weather updates and finds avg temp in zipcode

#include "zhelpers.h"

int main(int argc, char *argv[])
{
    void *context = zmq_ctx_new();
    int rc;
    void *subscriber = zmq_socket(context, ZMQ_SUB);
    rc = zmq_connect(subscriber, "tcp://127.0.0.1:5555");
    assert(rc == 0);

    //  Subscribe to zipcode, default is NYC, 10001
    const char *filter = (argc > 1) ? argv[1] : "10001";
    rc = zmq_setsockopt(subscriber, ZMQ_SUBSCRIBE,
                        NULL, 0);
    assert(rc == 0);

    zmq_pollitem_t items[] = {
        {subscriber, 0, ZMQ_POLLIN, 0},
        };

    while (1)
    {
        zmq_poll(items, 1, -1);
        if (items[0].revents & ZMQ_POLLIN)
        {
            char* msg = (char*)malloc(1024);
            int size = zmq_recv(subscriber, msg, 1024, 0);
            if (size != -1)
            {
                char* username = strtok(msg, ";");
                char* topic = strtok(NULL, ";");
                char* message = strtok(NULL, ";");
                printf("(%s)[%s]: %s\n", topic, username, message);
            }
            free(msg);
        }
    }

    zmq_close(subscriber);
    zmq_ctx_destroy(context);
    return 0;
}
