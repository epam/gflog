package com.epam.deltix.gflog.core.util;

import java.nio.ByteBuffer;


public interface MutableBuffer extends Buffer {

    void wrap(byte[] buffer);

    void wrap(byte[] buffer, int offset, int length);

    void wrap(ByteBuffer buffer);

    void wrap(ByteBuffer buffer, int offset, int length);

    void wrap(Buffer buffer);

    void wrap(Buffer buffer, int offset, int length);

    void wrap(long address, int length);


    void putLong(int index, long value);

    void putInt(int index, int value);

    void putShort(int index, short value);

    void putChar(int index, char value);

    void putByte(int index, byte value);


    void putBytes(int index, byte[] src);

    void putBytes(int index, byte[] src, int offset, int length);


    void putBytes(int index, ByteBuffer srcBuffer, int length);

    void putBytes(int index, ByteBuffer srcBuffer, int srcIndex, int length);


    void putBytes(int index, Buffer srcBuffer);

    void putBytes(int index, Buffer srcBuffer, int srcIndex, int length);


    void setMemory(int index, int length, byte value);

}
