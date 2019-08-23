package de.adorsys.datasafe.types.api.utils;

import lombok.Synchronized;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * {@link ByteArrayOutputStream}-alike stream that has customized maximum capacity and growing strategy in
 * order to minimize memory usage.
 */
public class CustomizableByteArrayOutputStream extends OutputStream {

    private final double fillFactorToCopy;
    private final int maxArraySize;
    private final int initialCapacity;

    protected byte[] buffer;
    protected int count;

    /**
     * @param initialCapacity  Initial buffer capacity
     * @param maxArraySize     Maximum array(buffer) size
     * @param fillFactorToCopy If buffer fill factor is less than this value
     *                         {@link CustomizableByteArrayOutputStream#getBufferOrCopy()} will return buffer copy,
     *                         that contains all data but has smaller size than holding entire buffer.
     */
    public CustomizableByteArrayOutputStream(int initialCapacity, int maxArraySize, double fillFactorToCopy) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("Initial capacity must be > 0: " + initialCapacity);
        }

        if (maxArraySize <= 0) {
            throw new IllegalArgumentException("Max array size must be > 0: " + maxArraySize);
        }

        if (fillFactorToCopy < 0 || fillFactorToCopy > 1.0) {
            throw new IllegalArgumentException("Fill factor for Array.copy must be in [0, 1]: " + fillFactorToCopy);
        }

        this.initialCapacity = initialCapacity;
        this.buffer = new byte[this.initialCapacity];
        this.maxArraySize = maxArraySize;
        this.fillFactorToCopy = fillFactorToCopy;
    }

    @Override
    @Synchronized
    public void write(int byteToWrite) {
        ensureCapacity(this.count + 1);
        this.buffer[count] = (byte) byteToWrite;
        this.count += 1;
    }

    @Override
    @Synchronized
    public void write(byte[] buffer, int off, int len) {
        if ((off < 0) || (off > buffer.length) || (len < 0) ||
                ((off + len) - buffer.length > 0)) {
            throw new IndexOutOfBoundsException();
        }

        ensureCapacity(this.count + len);
        System.arraycopy(buffer, off, this.buffer, this.count, len);
        this.count += len;
    }

    /**
     * Optimized resulting data array that has actual length equal to
     * {@link CustomizableByteArrayOutputStream#size()}.
     * Prevents keeping too large objects in memory while upload request finishes.
     * Copies buffer to allow its cleanup if fill factor is too small.
     */
    @Synchronized
    public byte[] getBufferOrCopy() {
        double fillFactor = (double) this.count / this.buffer.length;

        if (fillFactor >= this.fillFactorToCopy) {
            return this.buffer;
        }

        return Arrays.copyOf(this.buffer, count);
    }

    @Synchronized
    public int size() {
        return this.count;
    }

    @Override
    public void close() {
        // NOP
    }

    @Synchronized
    public void reset() {
        this.count = 0;
        this.buffer = new byte[initialCapacity];
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= buffer.length) {
            return;
        }

        grow(minCapacity);
    }

    private void grow(int minCapacity) {
        int oldCapacity = this.buffer.length;

        int newCapacity = Math.min(oldCapacity << 1, this.maxArraySize);

        if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
        }

        if (newCapacity > this.maxArraySize) {
            throw new OutOfMemoryError();
        }

        this.buffer = Arrays.copyOf(this.buffer, newCapacity);
    }
}
