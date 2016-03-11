package koh.game.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Melancholia on 3/6/16.
 */
public class TutorialText {

    private static final Map<Integer, String> texts = new LinkedHashMap<>();

    public static void init() {
        try {
            BufferedReader config = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/tutorial.ini"));
            String line = "";
            while ((line = config.readLine()) != null) {
                synchronized (texts) {
                    if (!line.isEmpty() && !line.startsWith(";")) {
                        final String[] data = line.trim().split("=");
                        texts.put(Integer.parseInt(data[0].trim()), data.length <= 1 ? "" : data[1].trim());
                    }
                }
            }
            config.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Set<Map.Entry<Integer, String>> getTexts(){
        return texts.entrySet();
    }

}
