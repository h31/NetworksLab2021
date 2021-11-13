# DNS Replica

## Overview

The project introduces a DNS Resolver and a primitive DNS Name Server, both working with UDP Sockets.

### Resolver

[Link to entrypoint](./client/index.js)

The Resolver handles such DNS protocol features as:

- Multiple questions
- Data compressing
- Caching (with [Redis](./common-classes/convenient-redis.js))
- Inverse queries
- Following resource record types: A, AAAA, MX, TXT, SOA

### Name Server

[Link to entrypoint](./server/index.js)

The Name Server **does**:

- Have an own resource records database (also uses [Redis](./common-classes/convenient-redis.js))
- Handle multiple questions
- Handle and use data compressing
- Handle inverse queries
- Support the same resource record types as the Resolver
- Allow populating the database with custom data from a .json file, or accessing the database in CLI mode

The Name Server **does not**:

- Implement recursion. The server acts as an authoritative one
- Guarantee relevance of stored data. Should only be used for testing / educational purposes
- Thoroughly validate the received requests. The server expects it is only requested by clients using the same DNS
  protocol

## Interesting feature: working with bit data in Node.js

The [BitBuffer](./common-classes/bit-buffer.js) class is used to handle bit data.

### Representation

An array of ones and zeros is used to store the bit value of a BitBuffer: it's basically the binary code for whatever
original data that was passed to the constructor. Same as the native Buffer class, BitBuffer allows specifying
its `size`, so that data is checked to fit that size and completed with zeros if it's too short originally.

### What can be converted to bits

- **Integer Numbers**. For those, the procedure is pretty straight-forward: the number is converted to binary form, and
  prepended with zeros to fit the size (if provided)
- **Buffers**. The value of each byte is represented as a binary code of size 8
- **Strings**. The string is converted to a Buffer with the provided encoding, and then handled as a Buffer
- **Arrays**. If an array of values of the three described types is provided, the result is a concatenation of the
  BitBuffers made from the array elements. `eachSize` can be provided for array data, specifying `size` for each of the
  parts (`size` would be used to check the size of the resulting concatenation in such case). If the provided array only
  consists of ones and zeros, and no `eachSize` is specified, no extra work is done: the data is already binary and is
  stored as is.

### What can you do with BitBuffers

- A BitBuffer can be converted back to a Number / String / Buffer
- You can append data to the start (`bitBuffer.prepend()`) or end (`bitBuffer.append()`) of a BitBuffer. Notice that,
  unlike its big brother Buffer, BitBuffer does not use any kind of space allocation, so the resulting size is **not
  checked**
  when appending / prepending.
- You can concatenate a bunch of BitBuffers using `BitBuffer.concat()`. This method is really useful when building
  messages
- You can split a BitBuffer into several BitBuffers of specified sizes using `bitBuffer.split()`. This method is mostly
  used when parsing messages
- You can check whether a BitBuffer starts with some specific sequence of bites with `bitBuffer.startsWith()`