package com.dhrubo.day91;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

class MemoryMappedReader {
    public static void processLogFile(String path) {
        try (RandomAccessFile file = new RandomAccessFile(path, "r");
             FileChannel channel = file.getChannel()) {

            long fileSize = channel.size();
            System.out.println("File size: " + fileSize / (1024 * 1024) + "MB");

            // This is the critical moment - we're asking the OS to create the mapping
            MappedByteBuffer buffer = channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    0,
                    fileSize
            );

            // Let's demonstrate different access patterns to show the performance characteristics
            demonstrateRandomAccess(buffer);
            demonstrateSequentialAccess(buffer);
            demonstratePatternSearch(buffer);

        } catch (Exception e) {
            System.err.println("Something went wrong: " + e.getMessage());
        }
    }

    private static void demonstrateRandomAccess(MappedByteBuffer buffer) {
        System.out.println("\n=== Random Access Test ===");
        long startTime = System.nanoTime();

        // Jump around the file randomly - this shows the power of memory mapping
        long[] positions = {0, buffer.limit() / 4, buffer.limit() / 2, buffer.limit() * 3 / 4, buffer.limit() - 1};

        for (long pos : positions) {
            if (pos < buffer.limit()) {
                buffer.position((int) pos);
                byte b = buffer.get();
                System.out.println("Byte at position " + pos + ": " + (char) b);
            }
        }

        long endTime = System.nanoTime();
        System.out.println("Random access took: " + (endTime - startTime) / 1_000_000 + "ms");
        System.out.println("Notice how fast that was? No disk seeks needed after the first few page faults!");
    }

    private static void demonstrateSequentialAccess(MappedByteBuffer buffer) {
        System.out.println("\n=== Sequential Access Test ===");
        buffer.position(0); // Reset to beginning

        long startTime = System.nanoTime();
        int lineCount = 0;

        // This will trigger efficient sequential page loading
        while (buffer.hasRemaining() && lineCount < 1000) {
            byte b = buffer.get();
            if (b == '\n') {
                lineCount++;
            }
        }

        long endTime = System.nanoTime();
        System.out.println("Counted " + lineCount + " lines");
        System.out.println("Sequential access took: " + (endTime - startTime) / 1_000_000 + "ms");
        System.out.println("The OS optimized this by reading ahead!");
    }

    private static void demonstratePatternSearch(MappedByteBuffer buffer) {
        System.out.println("\n=== Pattern Search Test ===");
        buffer.position(0); // Reset to beginning

        byte[] errorPattern = "ERROR".getBytes(StandardCharsets.UTF_8);
        int errorCount = 0;
        long startTime = System.nanoTime();

        // This shows how we can treat the entire file like a giant array
        while (buffer.hasRemaining()) {
            if (matchesPattern(buffer, errorPattern)) {
                errorCount++;
                // Skip past the matched pattern to avoid double-counting
                buffer.position(Math.min(buffer.position() + errorPattern.length, buffer.limit()));
            } else {
                buffer.position(buffer.position() + 1);
            }
        }

        long endTime = System.nanoTime();
        System.out.println("Found " + errorCount + " errors");
        System.out.println("Pattern search took: " + (endTime - startTime) / 1_000_000 + "ms");
        System.out.println("We just searched through the entire file without loading it all into heap memory!");
    }

    private static boolean matchesPattern(MappedByteBuffer buffer, byte[] pattern) {
        if (buffer.remaining() < pattern.length) return false;

        int originalPosition = buffer.position();

        // Check if the pattern matches at current position
        for (int i = 0; i < pattern.length; i++) {
            if (buffer.get() != pattern[i]) {
                buffer.position(originalPosition); // Reset position
                return false;
            }
        }

        buffer.position(originalPosition); // Reset position for caller
        return true;
    }

    // Let's also show what happens with memory monitoring
    public static void demonstrateMemoryUsage(String path) {
        Runtime runtime = Runtime.getRuntime();

        long beforeMapping = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Heap memory before mapping: " + beforeMapping / (1024 * 1024) + "MB");

        try (RandomAccessFile file = new RandomAccessFile(path, "r");
             FileChannel channel = file.getChannel()) {

            long fileSize = channel.size();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);

            long afterMapping = runtime.totalMemory() - runtime.freeMemory();
            System.out.println("Heap memory after mapping " + fileSize / (1024 * 1024) + "MB file: " +
                    afterMapping / (1024 * 1024) + "MB");
            System.out.println("Heap memory increase: " + (afterMapping - beforeMapping) / 1024 + "KB");
            System.out.println("See? The file data isn't in our heap - it's managed by the OS!");

            // Access some data to trigger page faults
            for (int i = 0; i < Math.min(1000000, buffer.limit()); i += 4096) {
                buffer.get(i); // Access one byte per page (4KB)
            }

            long afterAccess = runtime.totalMemory() - runtime.freeMemory();
            System.out.println("Heap memory after accessing data: " + afterAccess / (1024 * 1024) + "MB");
            System.out.println("Still barely any heap usage!");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
