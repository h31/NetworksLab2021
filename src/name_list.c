#include "name_list.h"

int name_list_set_if_not_exist(vector name_list, size_t index, unsigned char *str, size_t len) {
    size_t name_cnt = vector_size(name_list);
    if (name_cnt != 0) {
        for (size_t i = 0; i < name_cnt; i++) {
            if (i == index) continue;
            vector tmp_vec = *((vector*)vector_get(name_list, i));
            size_t name_len = vector_size(tmp_vec);
            if (name_len != len) continue;

            int equals = 1;
            for (size_t j = 0; j < len; j++) {
                unsigned char n = *((unsigned char*)vector_get(tmp_vec, j));
                // if (str[j] != *((unsigned char*)vector_get(tmp_vec, j))) break;
                if (str[j] != n) {
                    equals = 0;
                    break;
                }
            }
            if (equals) return 0;
        }
    }
    vector new_name = vector_new(sizeof(char), len);
    for (size_t i = 0; i < len; i++) {
        vector_append(new_name, &str[i]);
    }
    vector_append_vec(*((vector*)vector_get(name_list, index)), new_name);
    // vector_set(name_list, index, &new_name);
    return 1;
}