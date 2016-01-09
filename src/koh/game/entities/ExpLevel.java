package koh.game.entities;

import lombok.Getter;

/**
 *
 * @author Alleos13
 */
public class ExpLevel {

    @Getter
    private final int level;
    @Getter
    private final long player,job,mount,guild;
    @Getter
    private final int guildMembers,livingObject,PvP;
    @Getter
    private final long tourmentors,bandits;

    public ExpLevel(int lvl, long a, long b, long c, long d, int e, int f, int g, long h, long i) {
        this.level = lvl;
        this.player = a;
        this.job = b;
        this.mount = c;
        this.guild = d;
        this.guildMembers = e;
        this.livingObject = f;
        this.PvP = g;
        this.tourmentors = h;
        this.bandits = i;
    }
}
