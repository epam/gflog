package com.epam.deltix.gflog.core.util;

import java.nio.ByteBuffer;

import static com.epam.deltix.gflog.core.util.Util.UNSAFE;


public final class UnsafeBuffer implements MutableBuffer {

    private long addressOffset;
    private int capacity;
    private byte[] byteArray;
    private ByteBuffer byteBuffer;

    public UnsafeBuffer() {
    }

    public UnsafeBuffer(final byte[] buffer) {
        wrap(buffer);
    }

    public UnsafeBuffer(final byte[] buffer, final int offset, final int length) {
        wrap(buffer, offset, length);
    }

    public UnsafeBuffer(final ByteBuffer buffer) {
        wrap(buffer);
    }

    public UnsafeBuffer(final ByteBuffer buffer, final int offset, final int length) {
        wrap(buffer, offset, length);
    }

    public UnsafeBuffer(final Buffer buffer) {
        wrap(buffer);
    }

    public UnsafeBuffer(final Buffer buffer, final int offset, final int length) {
        wrap(buffer, offset, length);
    }

    public UnsafeBuffer(final long address, final int length) {
        wrap(address, length);
    }

    @Override
    public void wrap(final byte[] buffer) {
        addressOffset = Util.ARRAY_BYTE_BASE_OFFSET;
        capacity = buffer.length;
        byteArray = buffer;
        byteBuffer = null;
    }

    @Override
    public void wrap(final byte[] buffer, final int offset, final int length) {
        if (Util.BOUNDS_CHECK_ENABLED) {
            Util.verifyBounds(buffer, offset, length);
        }

        addressOffset = Util.ARRAY_BYTE_BASE_OFFSET + offset;
        capacity = length;
        byteArray = buffer;
        byteBuffer = null;
    }

    @Override
    public void wrap(final ByteBuffer buffer) {
        byteBuffer = buffer;

        if (buffer.isDirect()) {
            byteArray = null;
            addressOffset = Util.address(buffer);
        } else {
            byteArray = Util.array(byteBuffer);
            addressOffset = Util.ARRAY_BYTE_BASE_OFFSET + Util.arrayOffset(byteBuffer);
        }

        capacity = buffer.capacity();
    }

    @Override
    public void wrap(final ByteBuffer buffer, final int offset, final int length) {
        if (Util.BOUNDS_CHECK_ENABLED) {
            Util.verifyBounds(offset, length, buffer.capacity());
        }

        byteBuffer = buffer;

        if (buffer.isDirect()) {
            byteArray = null;
            addressOffset = Util.address(buffer) + offset;
        } else {
            byteArray = Util.array(buffer);
            addressOffset = Util.ARRAY_BYTE_BASE_OFFSET + Util.arrayOffset(buffer) + offset;
        }

        capacity = length;
    }

    @Override
    public void wrap(final Buffer buffer) {
        addressOffset = buffer.address();
        capacity = buffer.capacity();
        byteArray = buffer.byteArray();
        byteBuffer = buffer.byteBuffer();
    }

    @Override
    public void wrap(final Buffer buffer, final int offset, final int length) {
        if (Util.BOUNDS_CHECK_ENABLED) {
            Util.verifyBounds(offset, length, buffer.capacity());
        }

        addressOffset = buffer.address() + offset;
        capacity = length;
        byteArray = buffer.byteArray();
        byteBuffer = buffer.byteBuffer();
    }

    @Override
    public void wrap(final long address, final int length) {
        addressOffset = address;
        capacity = length;
        byteArray = null;
        byteBuffer = null;
    }

    @Override
    public long address() {
        return addressOffset;
    }

    @Override
    public byte[] byteArray() {
        return byteArray;
    }

    @Override
    public ByteBuffer byteBuffer() {
        return byteBuffer;
    }

    @Override
    public void setMemory(final int index, final int length, final byte value) {
        if (Util.BOUNDS_CHECK_ENABLED) {
            Util.verifyBounds(index, length, capacity);
        }

        final long indexOffset = addressOffset + index;
        if (0 == (indexOffset & 1) && length > 64) {
            //TODO: check if setMemory uses memset for even addresses
            UNSAFE.putByte(byteArray, indexOffset, value);
            UNSAFE.setMemory(byteArray, indexOffset + 1, length - 1, value);
        } else {
            UNSAFE.setMemory(byteArray, indexOffset, length, value);
        }
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public long getLong(final int index) {
        if (Util.BOUNDS_CHECK_ENABLED) {
            Util.verifyBounds(index, Util.SIZE_OF_LONG, capacity);
        }

        return UNSAFE.getLong(byteArray, addressOffset + index);
    }

    @Override
    public void putLong(final int index, final long value) {
        if (Util.BOUNDS_CHECK_ENABLED) {
            Util.verifyBounds(index, Util.SIZE_OF_LONG, capacity);
        }

        UNSAFE.putLong(byteArray, addressOffset + index, value);
    }

    @Override
    public int getInt(final int index) {
        if (Util.BOUNDS_CHECK_ENABLED) {
            Util.verifyBounds(index, Util.SIZE_OF_INT, capacity);
        }

        return UNSAFE.getInt(byteArray, addressOffset + index);
    }

    @Override
    public void putInt(final int index, final int value) {
        if (Util.BOUNDS_CHECK_ENABLED) {
            Util.verifyBounds(index, Util.SIZE_OF_INT, capacity);
        }

        UNSAFE.putInt(byteArray, addressOffset + index, value);
    }

    @Override
    public short getShort(final int index) {
        if (Util.BOUNDS_CHECK_ENABLED) {
            Util.verifyBounds(index, Util.SIZE_OF_SHORT, capacity);
        }

        return UNSAFE.getShort(byteArray, addressOffset + index);
    }

    @Override
    public void putShort(final int index, final short value) {
        if (Util.BOUNDS_CHECK_ENABLED) {
            Util.verifyBounds(index, Util.SIZE_OF_SHORT, capacity);
        }

        UNSAFE.putShort(byteArray, addressOffset + index, value);
    }

    @Override
    public byte getByte(final int index) {
        if (Util.BOUNDS_CHECK_ENABLED) {
            Util.verifyBounds(index, capacity);
        }

        return UNSAFE.getByte(byteArray, addressOffset + index);
    }

    @Override
    public void putByte(final int index, final byte value) {
        if (Util.BOUNDS_CHECK_ENABLED) {
            Util.verifyBounds(index, capacity);
        }

        UNSAFE.putByte(byteArray, addressOffset + index, value);
    }

    @Override
    public void getBytes(final int index, final byte[] dst) {
        getBytes(index, dst, 0, dst.length);
    }

    @Override
    public void getBytes(final int index, final byte[] dst, final int offset, final int length) {
        if (Util.BOUNDS_CHECK_ENABLED) {
            Util.verifyBounds(index, length, capacity);
            Util.verifyBounds(dst, offset, length);
        }

        UNSAFE.copyMemory(byteArray, addressOffset + index, dst, Util.ARRAY_BYTE_BASE_OFFSET + offset, length);
    }

    @Override
    public void getBytes(final int index, final MutableBuffer dstBuffer, final int dstIndex, final int length) {
        dstBuffer.putBytes(dstIndex, this, index, length);
    }

    @Override
    public void getBytes(final int index, final ByteBuffer dstBuffer, final int length) {
        final int dstOffset = dstBuffer.position();
        getBytes(index, dstBuffer, dstOffset, length);
        dstBuffer.position(dstOffset + length);
    }

    @Override
    public void getBytes(final int index, final ByteBuffer dstBuffer, final int dstOffset, final int length) {
        if (Util.BOUNDS_CHECK_ENABLED) {
            Util.verifyBounds(index, length, capacity);
            Util.verifyBounds(dstBuffer, dstOffset, length);
        }

        final byte[] dstByteArray;
        final long dstBaseOffset;
        if (dstBuffer.isDirect()) {
            dstByteArray = null;
            dstBaseOffset = Util.address(dstBuffer);
        } else {
            dstByteArray = Util.array(dstBuffer);
            dstBaseOffset = Util.ARRAY_BYTE_BASE_OFFSET + Util.arrayOffset(dstBuffer);
        }

        UNSAFE.copyMemory(byteArray, addressOffset + index, dstByteArray, dstBaseOffset + dstOffset, length);
    }

    @Override
    public void putBytes(final int index, final byte[] src) {
        putBytes(index, src, 0, src.length);
    }

    @Override
    public void putBytes(final int index, final byte[] src, final int offset, final int length) {
        if (Util.BOUNDS_CHECK_ENABLED) {
            Util.verifyBounds(index, length, capacity);
            Util.verifyBounds(src, offset, length);
        }

        UNSAFE.copyMemory(src, Util.ARRAY_BYTE_BASE_OFFSET + offset, byteArray, addressOffset + index, length);
    }

    @Override
    public void putBytes(final int index, final ByteBuffer srcBuffer, final int length) {
        final int srcIndex = srcBuffer.position();
        putBytes(index, srcBuffer, srcIndex, length);
        srcBuffer.position(srcIndex + length);
    }

    @Override
    public void putBytes(final int index, final ByteBuffer srcBuffer, final int srcIndex, final int length) {
        if (Util.BOUNDS_CHECK_ENABLED) {
            Util.verifyBounds(index, length, capacity);
            Util.verifyBounds(srcBuffer, srcIndex, length);
        }

        final byte[] srcByteArray;
        final long srcBaseOffset;
        if (srcBuffer.isDirect()) {
            srcByteArray = null;
            srcBaseOffset = Util.address(srcBuffer);
        } else {
            srcByteArray = Util.array(srcBuffer);
            srcBaseOffset = Util.ARRAY_BYTE_BASE_OFFSET + Util.arrayOffset(srcBuffer);
        }

        UNSAFE.copyMemory(srcByteArray, srcBaseOffset + srcIndex, byteArray, addressOffset + index, length);
    }

    @Override
    public void putBytes(final int index, final Buffer srcBuffer) {
        if (Util.BOUNDS_CHECK_ENABLED) {
            Util.verifyBounds(index, srcBuffer.capacity(), capacity);
        }

        UNSAFE.copyMemory(
                srcBuffer.byteArray(),
                srcBuffer.address(),
                byteArray,
                addressOffset + index,
                srcBuffer.capacity());
    }

    @Override
    public void putBytes(final int index, final Buffer srcBuffer, final int srcIndex, final int length) {
        if (Util.BOUNDS_CHECK_ENABLED) {
            Util.verifyBounds(index, length, capacity);
            Util.verifyBounds(srcIndex, length, srcBuffer.capacity());
        }

        UNSAFE.copyMemory(
                srcBuffer.byteArray(),
                srcBuffer.address() + srcIndex,
                byteArray,
                addressOffset + index,
                length);
    }

    @Override
    public char getChar(final int index) {
        if (Util.BOUNDS_CHECK_ENABLED) {
            Util.verifyBounds(index, Util.SIZE_OF_CHAR, capacity);
        }

        return UNSAFE.getChar(byteArray, addressOffset + index);
    }

    @Override
    public void putChar(final int index, final char value) {
        if (Util.BOUNDS_CHECK_ENABLED) {
            Util.verifyBounds(index, Util.SIZE_OF_CHAR, capacity);
        }

        UNSAFE.putChar(byteArray, addressOffset + index, value);
    }

    public static UnsafeBuffer allocateHeap(final int capacity) {
        return new UnsafeBuffer(ByteBuffer.allocate(capacity));
    }

    public static UnsafeBuffer allocateDirect(final int capacity) {
        return new UnsafeBuffer(ByteBuffer.allocateDirect(capacity));
    }

    public static UnsafeBuffer allocateDirectAligned(final int capacity, final int alignment) {
        return new UnsafeBuffer(Util.allocateDirectAligned(capacity, alignment));
    }

    public static UnsafeBuffer allocateDirectedAlignedAndPadded(final int capacity, final int alignment) {
        return new UnsafeBuffer(Util.allocateDirectAlignedAndPadded(capacity, alignment));
    }

}
