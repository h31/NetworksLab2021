#include "requests.h"

#include <time.h>

int requests_is_login_request(message_t *msg) {

    if (message_get_tag_cnt(msg) != 2) return 0;

    unsigned char *tag_sys;
    unsigned char *tag_name;

    size_t tag_sys_len = message_get_tag(msg, TAG_SYS, &tag_sys);
    size_t tag_name_len = message_get_tag(msg, TAG_NAME, &tag_name);

    if ((tag_sys_len != 1) | (*tag_sys != SYS_LOGIN_REQUEST) | (tag_name_len == 0)) return 0;

    return 1;
}

int requests_is_text_message(message_t *msg) {
    if (message_get_tag_cnt(msg) != 2) return 0;

    unsigned char *tag_sys;
    unsigned char *tag_text;

    size_t tag_sys_len = message_get_tag(msg, TAG_SYS, &tag_sys);
    size_t tag_text_len = message_get_tag(msg, TAG_TEXT, &tag_text);

    if ((tag_sys_len != 1) | (*tag_sys != SYS_TEXT_MESSAGE) | (tag_text_len == 0)) return 0;

    return 1;
}

void requests_make_login_response(message_t *msg, unsigned char login_code) {
    message_reset(msg);
    unsigned char sys_data[] = {SYS_LOGIN_REQUEST, login_code};
    message_append(msg, TAG_SYS, sys_data, sizeof(sys_data)/sizeof(*sys_data));
}

void requests_make_text_message_response(message_t *msg, unsigned char response_code) {
    message_reset(msg);
    unsigned char sys_data[] = {SYS_TEXT_MESSAGE, response_code};
    message_append(msg, TAG_SYS, sys_data, sizeof(sys_data)/sizeof(*sys_data));
}

void requests_make_login_notification_response(message_t *msg, unsigned char response_code, unsigned char *name, size_t len) {
    message_reset(msg);
    unsigned char sys_data[] = {SYS_LOGIN_NOTIFICATION, response_code};
    message_append(msg, TAG_SYS, sys_data, sizeof(sys_data)/sizeof(*sys_data));
    message_append(msg, TAG_NAME, name, len);
}

void requests_make_response(message_t *msg, unsigned char sysCode, unsigned char respCode) {
    message_reset(msg);
    unsigned char sys_data[] = {sysCode, respCode};
    message_append(msg, TAG_SYS, sys_data, sizeof(sys_data)/sizeof(*sys_data));
}

//implemented as in MarshalBinary in time.go
void requests_append_time(message_t *msg, time_t *t) {
    // struct tm *loc_time = localtime(&t);
    unsigned char time_arr[15];

    time_arr[0] = 1;  //version  
    time_arr[1] = *t >> 56;   // bytes 1-8: seconds
    time_arr[2] = *t >> 48;
    time_arr[3] = *t >> 40;
    time_arr[4] = *t >> 32;
    time_arr[5] = *t >> 24;
    time_arr[6] = *t >> 16;
    time_arr[7] = *t >> 8;
    time_arr[8] = *t;
    time_arr[9] = 0 >> 24; // bytes 9-12: nanoseconds
    time_arr[10] = 0 >> 16;
    time_arr[11] = 0 >> 8;
    time_arr[12] = 0;
    time_arr[13] = 0; // bytes 13-14: zone offset in minutes
    time_arr[14] = 0;
    message_append(msg, TAG_TIME, time_arr, sizeof(time_arr)/sizeof(time_arr[0]));
}