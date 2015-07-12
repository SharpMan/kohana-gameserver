package koh.game.entities;

/**
 *
 * @author Alleos13
 */
public class ExpLevel {

    public final int level;
    public final long Player;
    public final long Job;
    public final long Mount;
    public final long Guild;
    public final int GuildMembers;
    public final int LivingObject;
    public final int PvP;
    public final long Tourmentors;
    public final long Bandits;

    public ExpLevel(int lvl, long a, long b, long c, long d, int e, int f, int g, long h, long i) {
        this.level = lvl;
        this.Player = a;
        this.Job = b;
        this.Mount = c;
        this.Guild = d;
        this.GuildMembers = e;
        this.LivingObject = f;
        this.PvP = g;
        this.Tourmentors = h;
        this.Bandits = i;
    }
}
