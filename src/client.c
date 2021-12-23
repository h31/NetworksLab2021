#include "client.h"

#include <stdio.h>
#include <stdlib.h>

void client_free(client_t *client) {
    frame_free(&client->frame);
    free(client);
};

void client_init(client_t* client) {
    frame_init(&client->frame);
    message_init(&client->msg);
    client->state = NOT_AUTHENTICATED;
    client->message_received = 0;
}

void client_process_data(client_t *client, unsigned char *data, size_t len) {
    unsigned char *first_unused_char = data; 
    int unused_n = len;

    while (unused_n != 0) {
        unused_n = frame_append(&client->frame, first_unused_char, unused_n, &first_unused_char);

        if (frame_filled(&client->frame)) { //frame ready
            frame_data_t f_data;
            frame_get_data(&client->frame, &f_data);
            message_append(&client->msg, f_data.tag, f_data.data, f_data.len);
            
            if (!frame_has_next(&client->frame)) {
                client->message_received = 1;
                return;
            }
        }
    }
}