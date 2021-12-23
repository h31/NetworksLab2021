#ifndef _MESSAGE_H_
#define _MESSAGE_H_

#include <stddef.h>

#include "frame.h"
#include "vector.h"

#define TAG_N   TAG_MASK + 1

typedef struct {
    int             tag_exist[TAG_N];
    vector          tag_data_vec[TAG_N];
    unsigned int    tag_cnt; 
} message_t;

void message_init(message_t *msg);

// void message_append(message_t *msg, frame_data_t *f);

void message_append(message_t *msg, unsigned char tag, unsigned char *data, size_t len);

void message_reset(message_t *msg);

void message_print(message_t *msg);

unsigned int message_get_tag_cnt(message_t *msg);

/* returns size of tag data and places pointer to first element via tag_data_ptr if it is not NULL */
/* if return val is 0 tag doesn't exist */
size_t message_get_tag(message_t *msg, unsigned char tag, unsigned char **tag_data_ptr);

/* it is needed to free return vector in order to prevent memory leackage */
vector message_convert_to_raw(message_t *msg);

#endif