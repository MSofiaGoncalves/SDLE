//  Weather update server
//  Binds PUB socket to tcp://*:5556
//  Publishes random weather updates

#include "zhelpers.h"

int main (void)
{
    //  Prepare our context and publisher
    void *context = zmq_ctx_new ();
    void *publisherUS = zmq_socket (context, ZMQ_PUB);
    int rc = zmq_bind (publisherUS, "tcp://*:5556");
    assert (rc == 0);
    void *publisherPT = zmq_socket (context, ZMQ_PUB);
    rc = zmq_bind (publisherPT, "tcp://*:5557");
    assert (rc == 0);

    //  Initialize random number generator
    srandom ((unsigned) time (NULL));
    while (1) {
        //  Get values that will fool the boss
        int zipcode, temperature, relhumidity;
        zipcode     = randof (100000);
        temperature = randof (215) - 80;
        relhumidity = randof (50) + 10;

        //  Send message to all subscribers
        char update [20];
        sprintf (update, "%05d %d %d", zipcode, temperature, relhumidity);
        s_send (publisherUS, update);

        ////////////////*************************///////////////////

        //  Get values that will fool the boss
        zipcode     = randof (10000);
        temperature = randof (215) - 80;
        relhumidity = randof (50) + 10;

        //  Send message to all subscribers
        char updatePT [20];
        sprintf (updatePT, "%04d %d %d", zipcode, temperature, relhumidity);
        //printf("%s\n", updatePT);
        s_send (publisherPT, updatePT);
    }
    zmq_close (publisherUS);
    zmq_close (publisherPT);
    zmq_ctx_destroy (context);
    return 0;
}
