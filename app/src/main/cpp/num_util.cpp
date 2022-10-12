//
// Created by HP on 2021/10/23.
//

#include "num_util.h"

int32_t btoi(char *arr) {
    int x = 0;
    x |= arr[0] & 0xff;
    x = (x << 8) | (arr[1] & 0xff);
    x = (x << 8) | (arr[2] & 0xff);
    x = (x << 8) | (arr[3] & 0xff);
    return x;
}

char *itob(int32_t a) {
    byte[] data = new byte[4];
    data[3] |= a & 0xFF;
    data[2] |= (a >> 8) & 0xFF;
    data[1] |= (a >> 16) & 0xFF;
    data[0] |= (a >> 24) & 0xFF;
    return data;
}

int64_t btol(char *arr) {
    long x = 0;
    x |= arr[0] & 0xff;
    x = (x << 8) | (arr[1] & 0xff);
    x = (x << 8) | (arr[2] & 0xff);
    x = (x << 8) | (arr[3] & 0xff);
    x = (x << 8) | (arr[4] & 0xff);
    x = (x << 8) | (arr[5] & 0xff);
    x = (x << 8) | (arr[6] & 0xff);
    x = (x << 8) | (arr[7] & 0xff);
    return x;
}

char *ltob(int64_t a) {
    byte[] data = new byte[8];
    data[7] |= a & 0xFF;
    data[6] |= (a >> 8) & 0xFF;
    data[5] |= (a >> 16) & 0xFF;
    data[4] |= (a >> 24) & 0xFF;
    data[3] |= (a >> 32) & 0xFF;
    data[2] |= (a >> 40) & 0xFF;
    data[1] |= (a >> 48) & 0xFF;
    data[0] |= (a >> 56) & 0xFF;
    return data;
}