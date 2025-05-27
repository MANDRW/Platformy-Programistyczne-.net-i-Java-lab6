package com.image.imageapp;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppLogger {
    private static final String LOG_FILE = "app.txt";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public enum Level { INFO, ERROR, ACTION }

    public static synchronized void log(Level level, String message) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            String time = sdf.format(new Date());
            bw.write("[" + time + "] [" + level + "] " + message);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Failed to log message: " + e.getMessage());
        }
    }
}