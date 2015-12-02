package koh.game.entities;

/**
 *
 * @author Alleos13
 */
public class ExpLevel {

    public final int level;
    public final long player;
    public final long job;
    public final long mount;
    public final long guild;
    public final int guildMembers;
    public final int livingObject;
    public final int PvP;
    public final long tourmentors;
    public final long bandits;

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
