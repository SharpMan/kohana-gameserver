package koh.game.entities.item;

/**
 *
 * @author Neo-Craft
 */
public class ItemLivingObject {
    
     public static int GetObviAppearanceBySkinId(int skin, int type) {
        switch (type) {
            case 9233:
                return 1115 + skin;
            case 9234:
                return 1135 + skin;
            case 12425:
                return 1469 + skin;
            case 12424:
                return 1489 + skin;
            case 13211:
                return 1687 + skin;
            case 13213 :
                return 1707 + skin;
        }
        return 0;
    }

    public static int GetLevelByObviXp(int xp) {
        if (xp < 10) {
            return 1;
        } else if (xp < 21) {
            return 2;
        } else if (xp < 33) {
            return 3;
        } else if (xp < 46) {
            return 4;
        } else if (xp < 60) {
            return 5;
        } else if (xp < 75) {
            return 6;
        } else if (xp < 91) {
            return 7;
        } else if (xp < 108) {
            return 8;
        } else if (xp < 126) {
            return 9;
        } else if (xp < 145) {
            return 10;
        } else if (xp < 165) {
            return 11;
        } else if (xp < 186) {
            return 12;
        } else if (xp < 208) {
            return 13;
        } else if (xp < 231) {
            return 14;
        } else if (xp < 255) {
            return 15;
        } else if (xp < 280) {
            return 16;
        } else if (xp < 306) {
            return 17;
        } else if (xp < 333) {
            return 18;
        } else if (xp < 361) {
            return 19;
        } else {
            return 20;
        }
    }

}
