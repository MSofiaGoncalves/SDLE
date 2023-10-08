//  Weather update client
//  Connects SUB socket to tcp://localhost:5556
//  Collects weather updates and finds avg temp in zipcode

#include "zhelpers.h"

int main(int argc, char *argv[])
{
    //  Socket to talk to server
    printf("Collecting updates from weather server...\n");
    void *context = zmq_ctx_new();
    printf("created a new context");
    int rc;
    void *subscriberUS = zmq_socket(context, ZMQ_SUB);
    printf("after subscriberUS");
    rc = zmq_connect(subscriberUS, "tcp://localhost:5555");
    printf("before assertion");
    assert(rc == 0);
    printf("has connected to the port");
    //  Subscribe to zipcode, default is NYC, 10001
    const char *filter = (argc > 1) ? argv[1] : "10001 ";
    rc = zmq_setsockopt(subscriberUS, ZMQ_SUBSCRIBE,
                        filter, strlen(filter));
    assert(rc == 0);

    void *subscriberPT = zmq_socket(context, ZMQ_SUB);
    rc = zmq_connect(subscriberPT, "tcp://localhost:5555");
    assert(rc == 0);
    //  Subscribe to zipcode, default is NYC, 10001
    const char *filterPT = (argc > 1) ? argv[1] : "4555 ";
    rc = zmq_setsockopt(subscriberPT, ZMQ_SUBSCRIBE,
                        filterPT, strlen(filterPT));
    assert(rc == 0);

    zmq_pollitem_t items[] = {
        {subscriberUS, 0, ZMQ_POLLIN, 0},
        {subscriberPT, 0, ZMQ_POLLIN, 0}
        };

    long total_tempUS = 0;
    long total_tempPT = 0;
    int update_nbrUS = 0;
    int update_nbrPT = 0;

    while (update_nbrUS < 100 && update_nbrPT < 100)
    {

        zmq_poll(items, 2, -1);
        if (items[0].revents & ZMQ_POLLIN)
        {
            char msg[256];
            int size = zmq_recv(subscriberUS, msg, 255, 0);
            if (size != -1)
            {
                int zipcode, temperature, relhumidity;
                sscanf(msg, "%d %d %d",
                       &zipcode, &temperature, &relhumidity);
                total_tempUS += temperature;
                printf("Update for US: %d\n", temperature);
            }
            // free(msg);
            update_nbrUS++;
        }
        if (items[1].revents & ZMQ_POLLIN)
        {
            char msg[256];
            int size = zmq_recv(subscriberPT, msg, 255, 0);
            if (size != -1)
            {
                int zipcode, temperature, relhumidity;
                sscanf(msg, "%d %d %d",
                       &zipcode, &temperature, &relhumidity);
                total_tempPT += temperature;
                printf("Update for PT: %d\n", temperature);
            }
            // free(msg);
            update_nbrPT++;
        }
    }
    if (update_nbrUS != 0)
        printf("Average temperature for zipcode '%s' was %dF\n",
               filter, (int)(total_tempUS / update_nbrUS));
    if (update_nbrPT != 0)
        printf("Average temperature for zipcode '%s' was %dF\n",
               filterPT, (int)(total_tempPT / update_nbrPT));

    zmq_close(subscriberUS);
    zmq_close(subscriberPT);
    zmq_ctx_destroy(context);
    return 0;
}
