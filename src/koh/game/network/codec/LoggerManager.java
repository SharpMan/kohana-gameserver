package koh.game.network.codec;

import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author SharpMan
 */
public class LoggerManager {
    
    private static Map<String, Logger> Loggers = Collections.synchronizedMap(new HashMap<String, Logger>());
    
    /*public static Logger getLoggerByIp(String ip){
        if(Loggers.containsKey(ip))
            return Loggers.get(ip);
        else{
            Logger l = new Logger("Socrate/"+Main.FolderLogName+"/"+ip+".txt",0);
            Loggers.put(ip, l);
            return l;
        } 
    }*/
    
    public static void checkFolder(String name){
        if(!new File(name).exists()){
            new File(name).mkdir();
        }
    }
    
    public static String getDate(){
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+"-"+(Calendar.getInstance().get(Calendar.MONTH)+1)+"-"+Calendar.getInstance().get(Calendar.YEAR);
    }
    
}
