package com.epam.deltix.gflog.core.util;

import java.nio.ByteBuffer;


public interface Buffer {

    int capacity();

    long address();


    byte[] byteArray();

    ByteBuffer byteBuffer();


    long getLong(int index);

    int getInt(int index);

    short getShort(int index);

    char getChar(int index);

    byte getByte(int index);


    void getBytes(int index, byte[] dst);

    void getBytes(int index, byte[] dst, int offset, int length);


    void getBytes(int index, MutableBuffer dstBuffer, int dstIndex, int length);


    void getBytes(int index, ByteBuffer dstBuffer, int length);

    void getBytes(int index, ByteBuffer dstBuffer, int dstOffset, int length);

}
