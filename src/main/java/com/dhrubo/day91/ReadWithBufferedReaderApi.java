package com.dhrubo.day91;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadWithBufferedReaderApi {
    public static void readWithBufferedReader(String logFilePath) {
        System.out.println("\n=== BufferedReader Test ===");

        Runtime runtime = Runtime.getRuntime();
        long beforeReading = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Heap memory before reading: " + beforeReading / (1024 * 1024) + "MB");

        long startTime = System.nanoTime();

        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            int lineCount = 0;
            int errorCount = 0;

            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (line.contains("ERROR")) {
                    errorCount++;
                }
            }

            long afterReading = runtime.totalMemory() - runtime.freeMemory();
            long endTime = System.nanoTime();

            System.out.println("Lines read: " + lineCount);
            System.out.println("Reading took: " + (endTime - startTime) / 1_000_000 + "ms");
            System.out.println("Heap memory after reading: " + afterReading / (1024 * 1024) + "MB");
            System.out.println("Heap memory increase: " + (afterReading - beforeReading) / (1024 * 1024) + "MB");
            System.out.println("Notice: BufferedReader processes line by line without loading entire file!");
            System.out.println("Found " + errorCount + " error lines");

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
