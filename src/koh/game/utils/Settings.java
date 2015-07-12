package koh.game.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import koh.game.Logs;
import org.joda.time.DateTime;

/**
 *
 * @author Neo-Craft
 */
public class Settings {

    public static Map<String, Map<String, String>> Elements = new HashMap<>(100);
    private static final String Path = "Settings.ini";
    
   

    public static void Initialize() {
        Elements.clear();
        ReadSettings();
        Logs.DEBUG = GetBoolElement("Logging.Debug");
    }

    public static Map<String, String> GetGroup(String group) {
        return Elements.get(group);
    }

    public static String GetStringElement(String e) {
        return FastElement(e);
    }

    public static int GetIntElement(String e) {
        return Integer.parseInt(FastElement(e));
    }
    
     public static short GetShortElement(String e) {
        return Short.parseShort(FastElement(e));
    }

    public static boolean GetBoolElement(String e) {
        return Boolean.parseBoolean(FastElement(e));
    }

    public static String FastElement(String element) {
        String g = element.split("\\.")[0];
        String k = element.split("\\.")[1];
        return GetGroup(g).get(k);
    }

    private static void ReadSettings() {
        Map<String, String> currentGroup = null;
        try {
            BufferedReader config = new BufferedReader(new FileReader(Path));
            String line = "";
            while ((line = config.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith(";")) {
                    if (line.startsWith("[")) {
                        currentGroup = new HashMap<>();
                        Elements.put(line.replace("[", "").replace("]", ""), currentGroup);
                    } else if (currentGroup != null) {
                        String[] data = line.trim().split("=");
                        String key = data[0].trim();
                        String value = data.length <= 1 ? "" : data[1].trim();
                        currentGroup.put(key, value);
                    }
                }
            }
            config.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void Save() {
        try {
            File file = new File(Path);
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            
            for (Entry<String, Map<String,String>> group : Elements.entrySet())
            {
                output.write("[" + group.getKey() + "]");
                output.newLine();
                for (Entry<String,String> value : group.getValue().entrySet())
                {
                    output.write(value.getKey() + " = " + value.getValue());
                    output.newLine();
                }
                output.flush();
            }
            
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
