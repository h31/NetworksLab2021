#include <stdio.h>
#include <sys/socket.h>
#include <netdb.h>
#include <sys/types.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include <time.h>
#include <sys/poll.h>

#include "vector.h"
#include "client.h"
#include "requests.h"
#include "name_list.h"

#define LISTEN_SOCK_INDEX 0

#define PORT 25565
#define INITIAL_CONN_SIZE 255

int sock_bind_and_listen(int port) {
    int sockfd = socket(AF_INET, SOCK_STREAM | SOCK_NONBLOCK, IPPROTO_TCP);

    if (sockfd == -1) {
        perror("can't create socket");
        exit(EXIT_FAILURE);
    }

    struct sockaddr_in addr;
    addr.sin_addr.s_addr = INADDR_ANY;
    addr.sin_family = AF_INET;
    addr.sin_port = htons(port);

    if (bind(sockfd,(struct sockaddr*)&addr, sizeof(addr)) == -1) {
        perror("error on bind() call");
        exit(EXIT_FAILURE);
    }

    if (listen(sockfd, SOCK_STREAM) == -1) {
        perror("error on listen() call");
        exit(EXIT_FAILURE);
    }
    return sockfd;
}

//except_i = 0 to send everyone
void send_except_index(vector pollfds, vector clients, size_t except_i, vector data) {
    size_t size = vector_size(clients);
    for (size_t j = 1; j < size ; j++) {
        if (j == except_i) continue;
        struct pollfd *resp_pollfd_ptr = (struct pollfd*) vector_get(pollfds, j);
        
        client_t *client = *(client_t**) vector_get(clients, j);
        if (client->state == AUTH_OK)
            send(resp_pollfd_ptr->fd, vector_get(data, 0), vector_size(data), 0);
    }
}

int main(void) {
    vector name_list = vector_new(sizeof(vector), INITIAL_CONN_SIZE);
    vector pollfd_vec = vector_new(sizeof(struct pollfd), INITIAL_CONN_SIZE);
    vector clients_vec = vector_new(sizeof(client_t), INITIAL_CONN_SIZE);

 // empty client because pollfd_vec[i] corresponds to clients_vec[i]
 // but pollfd_vec[0] is accept socket

    vector_append(clients_vec, NULL);
    int tcp_sock = sock_bind_and_listen(PORT);
 
    struct pollfd tmp_pollfd;
    tmp_pollfd.fd = tcp_sock;
    tmp_pollfd.events = POLLIN;

    vector_append(pollfd_vec, &tmp_pollfd);

    struct sockaddr addr_in;
    socklen_t len = sizeof(addr_in);
    unsigned char frame_buf[sizeof(frame_t)];
    client_t *tmp_client_ptr;
    struct pollfd *tmp_pollfd_ptr;

    while (1) {
        int eventn = poll((struct pollfd*) vector_get(pollfd_vec, 0), vector_size(pollfd_vec), -1);
        if (eventn == -1) {
            perror("error on poll() call");
            exit(EXIT_FAILURE);
        } else if (eventn == 0) { // timeout is set to infinite but it is better to check 
            continue;
        }

        if (((struct pollfd*)vector_get(pollfd_vec, LISTEN_SOCK_INDEX))->revents & POLLIN) { // process new connection
            eventn -= 1;
            int client_fd = accept(tcp_sock, (struct sockaddr*)&addr_in, &len);

            if (client_fd == -1 ) {
                perror("something wrong");
                exit(EXIT_FAILURE);
            }
            tmp_client_ptr = (client_t*) malloc(sizeof(client_t));
            client_init(tmp_client_ptr);
            vector_append(clients_vec, &tmp_client_ptr); 
            vector tmp = vector_new(sizeof(char), 10);
            vector_append(name_list, &tmp);

            memset(&tmp_pollfd, 0, sizeof(tmp_pollfd));
            tmp_pollfd.fd = client_fd;
            tmp_pollfd.events = POLLIN | POLLERR | POLLHUP;
            vector_append(pollfd_vec, &tmp_pollfd);
            printf("new client\n");
        }

        size_t size = vector_size(pollfd_vec);

        for (size_t i = 1; i < size && eventn != 0; i++) { // in loop
            tmp_pollfd_ptr = (struct pollfd*) vector_get(pollfd_vec, i);
            tmp_client_ptr = *((client_t**)vector_get(clients_vec, i));

            if (tmp_pollfd_ptr->revents & POLLERR || tmp_pollfd_ptr->revents & POLLHUP) {
                printf("user disconnected!\n");
                close(tmp_pollfd_ptr->fd);
                vector_remove(pollfd_vec, i);
                vector_remove(clients_vec, i);

                vector name = *(vector*)vector_get(name_list, i-1);
                message_t msg;
                message_init(&msg);
                requests_make_login_notification_response(&msg, USER_DISCONECTED, vector_get(name, 0), vector_size(name));
                
                vector raw = message_convert_to_raw(&msg);

                vector_free(name);
                vector_remove(name_list, i-1);

                if (tmp_client_ptr->state == AUTH_OK)
                    send_except_index(pollfd_vec, clients_vec, 0, raw);

                vector_free(raw);
                client_free(tmp_client_ptr);
                eventn -= 1;
                i -= 1;
                continue;
            }

            if (tmp_pollfd_ptr->revents & POLLIN) {
                int len = recv(tmp_pollfd_ptr->fd, frame_buf, sizeof(frame_buf), 0);
                client_process_data(tmp_client_ptr, frame_buf, len);
                eventn -= 1;
                continue;
            }
        }

        size = vector_size(clients_vec);
        time_t cur_time;
        time(&cur_time);
        for (size_t i = 1; i < size; i++) {
            client_t *tmp_client_ptr = *((client_t**)vector_get(clients_vec, i));
            struct pollfd *tmp_pollfd_ptr = (struct pollfd*) vector_get(pollfd_vec, i);

            if (tmp_client_ptr->message_received) {
                tmp_client_ptr->message_received = 0;

                switch (tmp_client_ptr->state) {
                case NOT_AUTHENTICATED:
                    if (!requests_is_login_request(&tmp_client_ptr->msg)) break;

                    unsigned char *name;
                    size_t name_len = message_get_tag(&tmp_client_ptr->msg, TAG_NAME, &name);
                    if (!name_list_set_if_not_exist(name_list, i-1, name, name_len)) break;

                    message_t msg;
                    message_init(&msg);

                    requests_make_login_notification_response(&msg, USER_CONNECTED, name, name_len);
                    requests_append_time(&msg, &cur_time);
                    vector raw = message_convert_to_raw(&msg);
                    send_except_index(pollfd_vec, clients_vec, i, raw);

                    vector_free(raw);
                    message_reset(&msg);
                    requests_make_response(&msg, SYS_LOGIN_REQUEST, LOGIN_OK);
                    requests_append_time(&msg, &cur_time);
                    raw = message_convert_to_raw(&msg);
                    send(tmp_pollfd_ptr->fd, vector_get(raw, 0), vector_size(raw), 0);
                    vector_free(raw);
                    message_reset(&msg);
                    tmp_client_ptr->state = AUTH_OK;
                    break;
                case AUTH_OK:
                    if (requests_is_text_message(&tmp_client_ptr->msg)) {
                        message_t msg;
                        requests_make_response(&msg, SYS_TEXT_MESSAGE, MESSAGE_OK);
                        requests_append_time(&msg, &cur_time);
                        raw = message_convert_to_raw(&msg);
                        send(tmp_pollfd_ptr->fd, vector_get(raw, 0), vector_size(raw), 0);
                        vector_free(raw);
                        message_reset(&msg);

                        vector name = *(vector*)vector_get(name_list, i-1);
                        message_append(&tmp_client_ptr->msg, TAG_NAME,vector_get(name, 0), vector_size(name));
                        requests_append_time(&tmp_client_ptr->msg, &cur_time);
                        raw = message_convert_to_raw(&tmp_client_ptr->msg);
                        send_except_index(pollfd_vec, clients_vec, i, raw);
                    }
                    break;
                }
                message_reset(&tmp_client_ptr->msg);
            }
        }
    }

    close(tcp_sock);
    vector_free(pollfd_vec);
}