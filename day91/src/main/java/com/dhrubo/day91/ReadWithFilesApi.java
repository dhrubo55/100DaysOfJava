package com.dhrubo.day91;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ReadWithFilesApi {
    public static void readWithFilesReadAllLines(String logFilePath) {
        System.out.println("\n=== Files.readAllLines() Test ===");

        Runtime runtime = Runtime.getRuntime();
        long beforeReading = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Heap memory before reading: " + beforeReading / (1024 * 1024) + "MB");

        long startTime = System.nanoTime();

        try {
            List<String> lines = Files.readAllLines(Paths.get(logFilePath));

            long afterReading = runtime.totalMemory() - runtime.freeMemory();
            long endTime = System.nanoTime();

            System.out.println("Lines read: " + lines.size());
            System.out.println("Reading took: " + (endTime - startTime) / 1_000_000 + "ms");
            System.out.println("Heap memory after reading: " + afterReading / (1024 * 1024) + "MB");
            System.out.println("Heap memory increase: " + (afterReading - beforeReading) / (1024 * 1024) + "MB");
            System.out.println("Notice: The entire file is now loaded in heap memory!");

            // Demonstrate processing the lines
            long errorCount = lines.stream()
                    .filter(line -> line.contains("ERROR"))
                    .count();

            System.out.println("Found " + errorCount + " error lines");

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
