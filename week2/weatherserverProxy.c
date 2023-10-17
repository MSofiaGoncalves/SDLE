//  Weather update server
//  Binds PUB socket to tcp://*:5556
//  Publishes random weather updates

#include "zhelpers.h"

int main (void)
{

    //  Prepare our context and publisher
    void *context = zmq_ctx_new();
    printf("Tem context\n");

    void *publisherUS = zmq_socket (context, ZMQ_PUB);
    printf("publisherUS feito\n");

    //int rc = zmq_bind (publisherUS, "tcp://*:5558");

    //assert (rc == 0);
    void *publisherPT = zmq_socket (context, ZMQ_PUB);
    printf("publisherPT feito\n");
    //rc = zmq_bind (publisherPT, "tcp://*:5557");
    //assert (rc == 0);

    while(1){

        zmq_pollitem_t items[] = {
                {publisherUS, 0, ZMQ_POLLIN, 0},
                {publisherPT, 0, ZMQ_POLLIN, 0}
        };
        printf("polled the items\n");

        //está a ficar preso aqui
        int rc = zmq_poll(items, 2, -1);

        printf("rc: %d\n", rc);

        assert(rc == 0);

        printf("revents 0: %hd\n", items[0].revents);
        printf("revents 1: %hd\n", items[1].revents);

        if(items[0].revents & ZMQ_POLLIN){
            rc = zmq_bind(publisherUS, "tcp://*5556");
            printf("Deu bind ao US\n");
            assert (rc == 0);
        } else if(items[1].revents & ZMQ_POLLIN){
            rc = zmq_bind(publisherPT, "tcp://*5556");
            printf("Deu bind ao PT\n");
            assert (rc == 0);
        }

        printf("depois do if else\n");

    //}





    //  Initialize random number generator
        srandom ((unsigned) time (NULL));
    //while (1) {
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
        printf("%s\n", updatePT);
        s_send (publisherPT, updatePT);
    }


    zmq_close (publisherUS);
    zmq_close (publisherPT);
    zmq_ctx_destroy (context);
    return 0;
}
