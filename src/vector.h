#ifndef _VECTOR_H_
#define _VECTOR_H_

#include <stddef.h>

typedef void* vector;

vector vector_new(unsigned int base_size, size_t initial_cap);

void* vector_get(vector self, size_t index);

// if elem == NULL then adds element without initialization
int vector_append(vector self, void *elem);

int vector_append_vec(vector self, vector n);

// size of elements in arr must equal to self vector base size
int vector_append_arr(vector self, void* arr, size_t n_elem);

int vector_remove(vector self, size_t index);

int vector_set(vector self, size_t index, void *val);

size_t vector_size(vector self);

/* doesn't free data wich can be pointed if contains pointers */
void vector_free(vector self);

#endif