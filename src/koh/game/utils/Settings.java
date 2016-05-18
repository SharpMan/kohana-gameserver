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

import com.google.inject.Singleton;
import koh.utils.Enumerable;
import lombok.Getter;

/**
 *
 * @author Neo-Craft
 */

@Singleton
public class Settings {

    public final Map<String, Map<String, String>> elements = new HashMap<>(10);

    //Settings.ini
    private final String path;
    public Settings(String path) {
        this.path = path;
        this.elements.clear();
        this.readSettings();
        this.getStringElement("Register.Channels").split(",");
        this.registredChannels = new Byte[getStringElement("Register.Channels").split(",").length];
        for(int i =0; i < registredChannels.length; i++){
            this.registredChannels[i] = Byte.parseByte(getStringElement("Register.Channels").split(",")[i]);
        }
        TutorialText.init();
    }

    public Map<String, String> getGroup(String group) {
        return elements.get(group);
    }

    public String getStringElement(String e) {
        return fastElement(e);
    }

    @Getter
    private Byte[] registredChannels;


    public double getDoubleElement(String e) {
        return Double.parseDouble(fastElement(e));
    }

    public int getIntElement(String e) {
        return Integer.parseInt(fastElement(e));
    }

    public int getByteElement(String e) {
        return Byte.parseByte(fastElement(e));
    }

    public short getShortElement(String e){
        return Short.parseShort(fastElement(e));
    }

    public boolean getBoolElement(String e) {
        return Boolean.parseBoolean(fastElement(e));
    }

    public String fastElement(String element) {
        String g = element.split("\\.")[0];
        String k = element.split("\\.")[1];
        return getGroup(g).get(k);
    }

    private void readSettings() {
        Map<String, String> currentGroup = null;
        try {
            BufferedReader config = new BufferedReader(new FileReader(System.getProperty("user.dir")+path));
            String line = "";
            while ((line = config.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith(";")) {
                    if (line.startsWith("[")) {
                        currentGroup = new HashMap<>();
                        elements.put(line.replace("[", "").replace("]", ""), currentGroup);
                    } else if (currentGroup != null) {
                        final String[] data = line.trim().split("=");
                        final String key = data[0].trim();
                        final String value = data.length <= 1 ? "" : data[1].trim();
                        currentGroup.put(key, value);
                    }
                }
            }
            config.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            File file = new File(path);
            BufferedWriter output = new BufferedWriter(new FileWriter(file));

            for (Entry<String, Map<String,String>> group : elements.entrySet())
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

    public int[] getIntArray(String e) {
        return Enumerable.stringToIntArray(getStringElement(e));
    }
}
