package koh.game.network.codec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/**
 * Class de gestion d'�criture de logs dans un fichier
 *
 * @author Alleos13
 *
 */
public class Logger {

    private BufferedWriter out;
    private int bufferSize;

    /**
     *
     * @param filePath Chemin d'acc�s relatif ou absolue du fichier o� �crire
     * les logs.
     */
    public Logger(String filePath, int bufferSize) {
        File fichier = new File(filePath);

        try {
            FileWriter tmpWriter = new FileWriter(fichier, true);
            out = new BufferedWriter(tmpWriter);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        //setBufferSize(bufferSize);
    }

    /**
     * Ajoute une String dans le buffer. Elle seras �crite lorsque le buffer
     * seras plein ou � l'appel de la fonction "write()".
     *
     * @param toAdd Chaine de caract�re � placer dans le buffer en vue d'une
     * �criture.
     */
    public void addToLog(String toAdd) {
        if (out == null) {
            return;
        }

        String date = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" + Calendar.getInstance().get(+Calendar.MINUTE) + ":" + Calendar.getInstance().get(Calendar.SECOND);
        //toWrite.add(date + ": " + toAdd);

        //if(toWrite.size() >= bufferSize)
        write(date + ": " + toAdd);
    }

    /**
     * Vide le buffer en �crivant tout son contenue dans le fichier de sortie.
     */
    public synchronized void write(String m) {
        if (out == null) {
            return;
        }
        try {
            out.write(m);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * �crit le contenue du buffer par un appel � la fonction "write()" et ferme
     * le flux de sortie par la suite.
     */
    public void close() {
        try {
            //write();
            if (out != null) {
                out.close();
            }
            out = null;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Place une chaine de retour � la ligne dans le buffer.
     *
     */
    public void newLine() {
        if (out == null) {
            return;
        }
        write("\r\n");
    }

    /**
     * D�finit la taille du buffer. Elle influence le temps entre deux phase
     * d'�criture dans le fichier de sortie. Une taille plus petite r�sulte
     * d'une �criture fr�quente mais plus rapide. Une taille plus grande r�sulte
     * d'une �criture plus rare mais plus longue.
     *
     * @param newSize La nouvelle taille du buffer. Si c'est une valeur insens�
     * (<= 0), la valeur par d�faut (20) seras appliqu�.
     */
    public void setBufferSize(int newSize) {
        if (bufferSize <= 0) {
            bufferSize = 20;
        }

        this.bufferSize = newSize;
    }
}
