package src.keyzones;

import kotlin.Pair;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Coin;
import utils.PropertiesUtil;
import utils.TimeFrame;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static StaticUtils.python_interpreter_path;

public class KeyZonesRunner {
    private static final Logger logger = LoggerFactory.getLogger(KeyZonesPanel.class);

    private static final String p_path = new File(KeyZonesRunner.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParent()
            + PropertiesUtil.getProperty("python_path") + "keyzones/";
    private static final String img_temp_path = p_path+"temp/";

    private String argTStart = "None"; // DD/MM/YYYY
    private String argTEnd = "None";  // DD/MM/YYYY
    private String argZoneSize = "3"; // The zone's size percentage. The start and end prices are based on this value.
    private String argMergeDistance = "2"; // # The minimum distance% zones should have. If the distance between 2 zones is <= zone_merge_distance_limit% they will be merged.

    private Coin argCoin;
    private String argPath;
    private TimeFrame argInterval;

    static  {
        logger.info("Key zones script temp path: {}", img_temp_path);
    }

    public KeyZonesRunner(Coin coin, TimeFrame argInterval, String argTStart, String argTEnd) {
        this.argTStart = argTStart;
        this.argTEnd = argTEnd;

        this.argCoin = coin;
        argPath = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParent()
                + PropertiesUtil.getProperty("data.data_path") + coin + "/candles";
        this.argInterval = argInterval;
    }

    public Pair<ArrayList<String>, String> run() { // list of levels, path to img
        logger.info("Running key zones script wrapper for coin {} and interval {}", argCoin, argInterval);
        createTempDirectory();
        String pathImage = String.format("%slevels_%s_%s.png",img_temp_path, argCoin, argInterval);
        ProcessBuilder pb = new ProcessBuilder(python_interpreter_path, p_path + "levels.py",
                argPath, argTStart, argTEnd, argInterval.getTimeFrame(), argZoneSize, argMergeDistance,
                argCoin.getName(), pathImage);
        pb.redirectErrorStream(true);

        ArrayList<String> levels = new ArrayList<>();
        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                levels.add(line);
            }

            return new Pair<>(levels, pathImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createTempDirectory() {
        try {
            Files.createDirectories(Paths.get(img_temp_path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void deleteTempDirectory() {
        try {
            FileUtils.deleteDirectory(new File(img_temp_path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

