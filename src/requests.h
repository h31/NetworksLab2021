#ifndef _REQUESTS_H_
#define _REQUESTS_H_

#include "message.h"

#include <time.h>

#define TAG_SYS         1
#define TAG_NAME        6
#define TAG_TEXT        2
#define TAG_TIME        5

#define SYS_LOGIN_REQUEST   1
#define LOGIN_OK            1
#define NAME_USED           2
#define NAME_WRONG_FORMAT   3

#define SYS_LOGIN_NOTIFICATION  3
#define USER_CONNECTED          1
#define	USER_DISCONECTED        2

#define SYS_TEXT_MESSAGE        4
#define MESSAGE_OK              1


int requests_is_login_request(message_t *msg);

int requests_is_text_message(message_t *msg);

void requests_make_login_response(message_t *msg, unsigned char login_code);

void requests_make_response(message_t *msg, unsigned char sysCode, unsigned char respCode);

void requests_make_login_notification_response(message_t *msg, unsigned char response_code, unsigned char *name, size_t len);

void requests_append_time(message_t *msg, time_t *t);

#endif