package de.adorsys.datasafe.types.api.utils;

import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomizableByteArrayOutputStreamTest extends BaseMockitoTest {

    private static final int INITIAL_CAPACITY = 1;
    private static final int MAX_SIZE = 10;
    private static final double FILL_FACTOR = 0.9;

    private CustomizableByteArrayOutputStream tested = new CustomizableByteArrayOutputStreamTestable(
            INITIAL_CAPACITY, MAX_SIZE, FILL_FACTOR
    );

    @Test
    void invalidArgs() {
        assertThrows(IllegalArgumentException.class, () -> new CustomizableByteArrayOutputStream(-1, 10, 0.5));
        assertThrows(IllegalArgumentException.class, () -> new CustomizableByteArrayOutputStream(1, -10, 0.5));
        assertThrows(IllegalArgumentException.class, () -> new CustomizableByteArrayOutputStream(1, 10, -0.5));
        assertThrows(IllegalArgumentException.class, () -> new CustomizableByteArrayOutputStream(-1, 10, 1.5));
    }

    @Test
    void writeSingleByteNoGrow() {
        int written = 10;

        tested.write(written);

        assertThat(tested.buffer).hasSize(1);
        assertThat(tested.buffer).containsExactly(written);
    }

    @Test
    void writeSingleByteGrows() {
        int written1 = 10;
        int written2 = 20;

        tested.write(written1);
        tested.write(written2);

        assertThat(tested.buffer).hasSize(2);
        assertThat(tested.buffer).containsExactly(written1, written2);
    }

    @Test
    void writeSingleByteGrowsExponentially() {
        int written1 = 10;
        int written2 = 20;
        int written3 = 20;

        tested.write(written1);
        tested.write(written2);
        tested.write(written3);

        assertThat(tested.buffer).hasSize(4);
        assertThat(tested.count).isEqualTo(3);
        assertThat(tested.buffer).startsWith(written1, written2, written3);
    }

    @Test
    void writeSingleArrayNoGrow() {
        byte[] written = {10};

        tested.write(written, 0, written.length);

        assertThat(tested.buffer).hasSize(1);
        assertThat(tested.count).isEqualTo(1);
        assertThat(tested.buffer).containsExactly(written);
    }

    @Test
    void writeSingleArrayGrows() {
        byte[] written = {10, 20};

        tested.write(written, 0, written.length);

        assertThat(tested.buffer).hasSize(2);
        assertThat(tested.count).isEqualTo(2);
        assertThat(tested.buffer).containsExactly(written);
    }

    @Test
    void writeSingleArrayMatchesWrittenSizeOnOverflow() {
        byte[] written = {10, 20, 30};

        tested.write(written, 0, written.length);

        assertThat(tested.buffer).hasSize(3);
        assertThat(tested.count).isEqualTo(3);
        assertThat(tested.buffer).startsWith(written);
    }

    @Test
    void writeSingleArrayGrowsExponentially() {
        byte[] written1 = {10, 20};
        byte[] written2 = {30};

        tested.write(written1, 0, written1.length);
        tested.write(written2, 0, written2.length);

        assertThat(tested.buffer).hasSize(4);
        assertThat(tested.count).isEqualTo(3);
        assertThat(tested.buffer).startsWith(10, 20, 30);
    }

    @Test
    void getBufferOrCopyGivesBuffer() {
        IntStream.range(0, 9).forEach(it -> tested.write(it));

        assertThat(tested.getBufferOrCopy()).hasSize(10);
        assertThat(tested.getBufferOrCopy()).startsWith(0, 1, 2, 3, 4, 5, 6, 7, 8);
        assertThat(tested.getBufferOrCopy()).isEqualTo(tested.buffer);
    }

    @Test
    void getBufferOrCopyGivesCopy() {
        tested.write(10);
        tested.write(20);
        tested.write(30);

        assertThat(tested.getBufferOrCopy()).hasSize(3);
        assertThat(tested.getBufferOrCopy()).containsExactly(10, 20, 30);
        assertThat(tested.getBufferOrCopy()).isNotEqualTo(tested.buffer);
    }

    @Test
    void size() {
        tested.write(10);
        tested.write(20);
        tested.write(30);

        assertThat(tested.size()).isEqualTo(3);
        assertThat(tested.buffer.length).isEqualTo(4);
    }

    @Test
    void reset() {
        tested.write(10);
        tested.write(20);

        tested.reset();

        assertThat(tested.count).isEqualTo(0);
        assertThat(tested.buffer).hasSize(INITIAL_CAPACITY);
    }

    public static class CustomizableByteArrayOutputStreamTestable extends CustomizableByteArrayOutputStream {

        CustomizableByteArrayOutputStreamTestable(
                int initialCapacity, int maxArraySize, double fillFactorToCopy) {
            super(initialCapacity, maxArraySize, fillFactorToCopy);
        }
    }
}
