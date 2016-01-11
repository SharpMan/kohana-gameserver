package koh.game.controllers;

import java.util.regex.Pattern;

import koh.game.Main;
import koh.game.network.WorldClient;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;

/**
 *
 * @author Neo-Craft
 */
public class PlayerController {

    private final static String[] RANDOMABLE_NAMES = "noob;mr;con;ven;bana;sam;ron;fou;pui;to;fu;lol;rien;bank;cap;chap;fort;faible;rigolo;salo;dou;soleil;gentil;mechant;bad;killer;fight;gra;evil;dark;jerry;fatal;haut;bas;arc;epe;cac;ec;mai;invo;tro;com;koi;bou;let;top;fun;fai;sony;avion;bouftou;kani;meulou;faur;asus;choa;chau;cho;miel;beur;pain;cry;big;sma;to;day;bi;cih;fuck;osef;geni;bou;che;zizi;scania;dave;swi;cas;que;chi;er;mer;de;nul;dodo;a;b;c;d;e;f;g;h;i;j;k;l;m;n;o;p;q;r;s;t;u;v;w;x;y;z;a;e;i;o;u;y".split(";");

    private final static String ENCRYPTED_REGEX = "^[A-Z][a-z]{2,9}(?:-[A-Z][a-z]{2,9}|[a-z]{1,10})$";
    public final static Pattern ENCRYPTED_MATCHER = Pattern.compile(ENCRYPTED_REGEX);
    public final static String[] FORBIDDEN_NAMES = new String[]{
        "XELOR", "IOP", "FECA", "ENIRIPSA",
        "SADIDA", "ECAFLIP", "ENUTROF", "PANDAWA",
        "SRAM", "CRA", "OSAMODAS", "SACRIEUR",
        "DROP", "MULE", "ADMIN", "MODO", "ANIM"
    };

    public static void sendServerMessage(String message, String color) {
        Main.worldServer().sendPacket(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 0, new String[]{
                "<font color=\"#" + color + "\">" + message + "</font>"
        }));
    }

    public static void sendServerMessage(WorldClient c, String message) {
        if (c == null) {
            return;
        }
        c.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 0, new String[]{
            message
        }));
    }
    
    public static void SendServerErrorMessage(WorldClient c, String message) {
        if (c == null) {
            return;
        }
        c.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 0, new String[]{
            message
        }));
    }

    public static void sendServerMessage(WorldClient c, String message, String color) {
        sendServerMessage(c, "<font color=\"#" + color + "\">" + message + "</font>");
    }

    public static String GenerateName() {
        String rep = "";
        int tiree = 0;
        int maxi = (int) Math.floor(Math.random() * 4D) + 2;
        for (int x = 0; x < maxi; x++) {
            rep = (new StringBuilder(String.valueOf(rep))).append(RANDOMABLE_NAMES[(int) Math.floor(Math.random() * (double) RANDOMABLE_NAMES.length)]).toString();
            if (maxi >= 3 && x == 0 && tiree == 0 && (int) Math.floor(Math.random() * 2D) == 1) {
                rep = (new StringBuilder(String.valueOf(rep))).append("-").toString();
                tiree = 1;
            }
        }
        rep = (new StringBuilder(String.valueOf(rep.substring(0, 1).toUpperCase()))).append(rep.substring(1)).toString();
        return rep;
    }

    public static boolean isValidName(String packet) {
        return ENCRYPTED_MATCHER.matcher(packet).matches();
    }

    public static boolean isValidName2(String n) {
        boolean isValid = true;
        String name = n.toLowerCase();
        if (name.length() < 4 || name.length() > 20 || name.contains("mj") || name.contains("modo") || name.contains("admin")) {
            isValid = false;
        }
        if (isValid) {
            int tiretCount = 0;
            char exLetterA = ' ';
            char exLetterB = ' ';
            for (char curLetter : name.toCharArray()) {
                if (!((curLetter >= 'a' && curLetter <= 'z') || curLetter == '-')) {
                    isValid = false;
                    break;
                }
                if (curLetter == exLetterA && curLetter == exLetterB) {
                    isValid = false;
                    break;
                }
                if (curLetter >= 'a' && curLetter <= 'z') {
                    exLetterA = exLetterB;
                    exLetterB = curLetter;
                }
                if (curLetter == '-') {
                    if (tiretCount >= 1) {
                        isValid = false;
                        break;
                    } else {
                        tiretCount++;
                    }
                }
            }
        }
        return isValid;
    }

}
