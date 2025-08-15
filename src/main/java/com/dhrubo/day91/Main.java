package com.dhrubo.day91;

public class Main {

    public static void main(String[] args) {
        String logFilePath = "example-log-file.log";

        // Test reading with Files.readAllLines
        ReadWithFilesApi.readWithFilesReadAllLines(logFilePath);

        // Test reading with BufferedReader
        ReadWithBufferedReaderApi.readWithBufferedReader(logFilePath);

        MemoryMappedReader.processLogFile(logFilePath);
        MemoryMappedReader.demonstrateMemoryUsage(logFilePath);
    }
}


