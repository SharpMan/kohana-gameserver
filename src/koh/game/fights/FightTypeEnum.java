package koh.game.fights;

/**
 *
 * @author Neo-Craft
 */
public enum FightTypeEnum {

    FIGHT_TYPE_CHALLENGE(0),
    FIGHT_TYPE_AGRESSION(1),
    FIGHT_TYPE_PvMA(2),
    FIGHT_TYPE_MXvM(3),
    FIGHT_TYPE_PvM(4),
    FIGHT_TYPE_PvT(5),
    FIGHT_TYPE_PvMU(6),
    FIGHT_TYPE_PVP_ARENA(7);

    public byte value;

    FightTypeEnum(int val) {
        this.value = (byte) val;
    }
}
