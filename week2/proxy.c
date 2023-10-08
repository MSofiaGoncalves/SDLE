#include "zhelpers.h"

int main (void){
    //  Prepare our context and publisher
    void *context = zmq_ctx_new();

    //  This is where the weather server sits
    void *frontend = zmq_socket(context, ZMQ_XSUB);
    zmq_connect (frontend, "tcp://localhost:5557");

    //  This is our public endpoint for subscribers
    void *backend = zmq_socket(context, ZMQ_XPUB);
    zmq_bind (backend, "tcp://*:5555");

    //  Run the proxy until the user interrupts us
    zmq_proxy (frontend, backend, NULL);

    // Close sockets and terminate context
    zmq_close (frontend);
    zmq_close (backend);
    zmq_ctx_destroy (context);
}