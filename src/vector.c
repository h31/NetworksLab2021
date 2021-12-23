#include "vector.h"

#include <stdlib.h>
#include <string.h>

typedef struct {
    unsigned int base_size;
    void *arr;
    size_t size;
    size_t cap;
} vector_t;

vector vector_new(unsigned int base_size, size_t initial_cap) {
    vector_t* vec = malloc(sizeof(vector_t));
    vec->base_size = base_size;
    vec->cap = initial_cap;
    vec->size = 0;
    vec->arr = malloc(base_size * initial_cap);
    return (vector)vec;
}

void* vector_get(vector self, size_t index) {
    vector_t *vec = (vector_t*)self;
    if (index >= vec->size) return NULL;
    return vec->arr + (vec->base_size * index);
}

int vector_append(vector self, void *elem) {
    vector_t *vec = (vector_t*)self;
    if (vec->size + 1 == vec->cap) {
        vec->cap *= 2;
        vec->arr = realloc(vec->arr, vec->base_size * vec->cap);
    }
    if (elem != NULL)
        memcpy(vec->arr + (vec->size * vec->base_size), elem, vec->base_size);
    vec->size++;
    return 1;
}

size_t vector_size(vector self) {
    return ((vector_t*)self)->size;
}

void vector_free(vector self) {
    free(((vector_t*)self)->arr);
    free(self);
}

int vector_remove(vector self, size_t index) {
    vector_t *vec = (vector_t*)self;
    if (index >= vec->size) return 0;
    vec->size -= 1;    
    memcpy(vec->arr + vec->base_size * index, vec->arr + vec->base_size * vec->size, vec->base_size);
    return 1;
}

int vector_set(vector self, size_t index, void *val) {
    vector_t *vec = (vector_t*)self;
    if (index >= vec->size) return 0;
    memcpy(vec->arr + index * vec->base_size, val, vec->base_size);
    return 1;
}

int vector_append_vec(vector self, vector n) {
    vector_t *vec = (vector_t*)self;
    vector_t *append_vec = (vector_t*)n;

    if (vec->base_size != append_vec->base_size) return 0;

    int ncap = append_vec->size - (vec->cap - vec->size);
    if (ncap > 0) {
        vec->cap += ncap;
        vec->arr = realloc(vec->arr, vec->cap * vec->base_size);
    }
    memcpy(vec->arr + vec->size * vec->base_size, append_vec->arr, append_vec->size * append_vec->base_size);
    vec->size += append_vec->size;
    return 1;
}

int vector_append_arr(vector self, void* arr, size_t n_elem) {
    vector_t *vec = (vector_t*)self;

    int ncap = n_elem - (vec->cap - vec->size);
    if (ncap > 0) {
        vec->cap += ncap;
        vec->arr = realloc(vec->arr, vec->cap * vec->base_size);
    }
    memcpy(vec->arr + vec->size * vec->base_size, arr, n_elem * vec->base_size);
    vec->size += n_elem;
    return 1;
}