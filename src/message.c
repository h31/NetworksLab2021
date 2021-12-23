#include "message.h"

#include <stdlib.h>
#include <string.h>
#include <stdio.h>

void message_init(message_t *msg) {
    memset(msg->tag_exist, 0, sizeof(msg->tag_exist));
}

size_t message_get_tag(message_t *msg, unsigned char tag, unsigned char **tag_data_ptr) {
    if (tag > TAG_MASK) return 0;
    if (!msg->tag_exist[tag]) return 0;
    size_t len = vector_size(msg->tag_data_vec[tag]);
    if (tag_data_ptr != NULL) {
        *tag_data_ptr = vector_get(msg->tag_data_vec[tag], 0);
    }
    return len;
}

void message_reset(message_t *msg) {
    for (int i = 0; i < TAG_MASK; i++) {
        if (msg->tag_exist[i]) {
            vector_free(msg->tag_data_vec[i]);
            msg->tag_exist[i] = 0;
        }
    }
    msg->tag_cnt = 0;
}

void message_print(message_t *msg) {
    for (int i = 0; i < TAG_MASK; i++) {
        unsigned char *data;
        size_t len = message_get_tag(msg, i, &data);
        if (len != 0) {
            printf("tag %d: %lu\n", i, len);
            for (size_t j = 0; j < len; j++) {
                printf("%3x", data[j]);
            }
            printf("\n");
        }
    }
}

inline unsigned int message_get_tag_cnt(message_t *msg) {
    return msg->tag_cnt;
}

vector message_convert_to_raw(message_t *msg) {
    vector raw = vector_new(sizeof(char), 100);
    unsigned int tag_cnt = msg->tag_cnt;

    for (unsigned char i = 0; i < TAG_N; i++) {
        if (!msg->tag_exist[i]) continue;
        tag_cnt -= 1;
        size_t tag_len = vector_size(msg->tag_data_vec[i]);
        size_t data_index = 0;
        while (tag_len != 0) {
            unsigned char elem = (tag_len > 255 || tag_cnt > 0 ? 0x80 : 0) | i;
            vector_append(raw, &elem);
            unsigned char copy_len = (tag_len > 255 ? 255 : tag_len);
            vector_append(raw, &copy_len);
            vector_append_arr(raw, vector_get(msg->tag_data_vec[i], data_index), copy_len);
            // for (int j = 0; j < copy_len; j++) {
            //     vector_append(raw, vector_get(msg->tag_data_vec[i], data_index++));
            // }
            data_index += copy_len;
            tag_len -= copy_len;
        }
    }

    return raw;
}

void message_append(message_t *msg, unsigned char tag, unsigned char *data, size_t len) {
    if (!msg->tag_exist[tag]) {
        msg->tag_data_vec[tag] = vector_new(sizeof(*data), len);
        msg->tag_exist[tag] = 1;
        msg->tag_cnt += 1;
    }
    for (size_t i = 0; i < len; i++) {
        vector_append(msg->tag_data_vec[tag], &data[i]);
    }
}