package de.adorsys.datasafe.cli.hacks;

import java.security.SecureRandom;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * This class substitutes SecureRandom during CLI generation, it should be overridden during runtime.
 */
public class NoOpRandom extends SecureRandom {

    public NoOpRandom() {
        super(null, null);
    }

    @Override
    public String getAlgorithm() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public synchronized void setSeed(byte[] seed) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public void setSeed(long seed) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public void nextBytes(byte[] bytes) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public byte[] generateSeed(int numBytes) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public int nextInt() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public int nextInt(int bound) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public long nextLong() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public boolean nextBoolean() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public float nextFloat() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public double nextDouble() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public synchronized double nextGaussian() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public IntStream ints(long streamSize) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public IntStream ints() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public LongStream longs(long streamSize) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public LongStream longs() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public DoubleStream doubles(long streamSize) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public DoubleStream doubles() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public int hashCode() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public boolean equals(Object obj) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String toString() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    protected void finalize() throws Throwable {
        throw new IllegalStateException("Not implemented");
    }
}
