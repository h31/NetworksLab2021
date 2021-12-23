#ifndef _USER_LIST_H_
#define _USER_LIST_H_

#include "vector.h"

/* search for name and add if no exist and returns 1 */
/* else returns 0 */
int name_list_set_if_not_exist(vector name_list, size_t index, unsigned char *str, size_t len);


#endif