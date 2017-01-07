package koh.game.entities.actors.character;

import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.protocol.client.BufUtils;
import koh.protocol.messages.game.achievement.AchievementFinishedMessage;
import koh.protocol.types.game.achievement.Achievement;
import koh.protocol.types.game.achievement.AchievementObjective;
import koh.protocol.types.game.achievement.AchievementRewardable;
import koh.protocol.types.game.achievement.AchievementStartedObjective;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.mina.core.buffer.IoBuffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Melancholia on 12/28/16.
 */
public class AchievementBook {
    //TODO level

    private final ConcurrentHashMap<Short, AchievementFinishedInfo> register = new ConcurrentHashMap<>(40);

    private final ConcurrentHashMap<Integer, ConcurrentHashMap<Short, AchievementInfo>> categories = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Short, AchievementInfo> ranged = new ConcurrentHashMap<>(40);
    @Getter
    private final List<Short> rewardsOnHold = Collections.synchronizedList(new ArrayList<>());
    @Getter
    private final Player player;
    @Getter @Setter
    private IoBuffer registerBuffer, advancementBuffer, rewardBuffer;

    public AchievementBook(Player player) {
        this.player = player;
    }

    private void test(){
        this.pushInfo(38, new AchievementInfo(373, new AchievementObjective[0], new AchievementStartedObjective[0],true));
        this.pushInfo(38, new AchievementInfo(374, new AchievementObjective[0], new AchievementStartedObjective[0],false));
        this.unlock((short)374);
        this.unlock((short)373);
        rewardsOnHold.clear();
    }

    public synchronized void init(){
        DAO.getAchievements().loadPlayerBook(this);
    }

    public boolean isRewarded(short el) {
        return this.rewardsOnHold.contains(el);
    }

    public int[] getFinishedAchievementsIds() {
        return  this.register.values()
                .stream()
                //.filter(AchievementFinishedInfo)
                .mapToInt(AchievementFinishedInfo::getAchievement)
                .toArray();
    }

    public AchievementRewardable[] getAchievementRewardable(){
        return this.register.values()
                .stream()
                .filter(c -> rewardsOnHold.contains(c.achievement))
                .map(ac -> new AchievementRewardable(ac.achievement,ac.finishedLevel))
                .toArray(AchievementRewardable[]::new);
    }


    public static class AchievementInfo extends Achievement {
        @Getter
        @Setter
        private boolean finished;

        public AchievementInfo(IoBuffer buf){
            super();
            this.deserialize(buf);
        }

        public AchievementInfo(int id, AchievementObjective[] finishedObjective, AchievementStartedObjective[] startedObjectives, boolean finished) {
            super(id, finishedObjective, startedObjectives);
            this.finished = finished;
        }

        @Override
        public void serialize(IoBuffer buf) {
            super.serialize(buf);
            buf.put(finished ? (byte) 1 : 0);
        }

        @Override
        public void deserialize(IoBuffer buf) {
            super.deserialize(buf);
            this.finished = buf.get() == 1;
        }

        public int size(){
            return 32 + (finishedObjective.length * 64) + (startedObjectives.length * 86) + 8;
        }
    }

    @AllArgsConstructor
    public static class AchievementFinishedInfo {
        @Getter
        @Setter
        private short achievement;
        @Getter
        @Setter
        public byte finishedLevel;
    }

    public void unlock(short achievement) {
        this.register.putIfAbsent(achievement, new AchievementFinishedInfo(achievement, (byte) this.player.getLevel()));
        this.rewardsOnHold.add(achievement);
        player.addAchievementPoints(DAO.getAchievements().find(achievement).getPoints());
    }

    public void emptyUnlock(int cat,short achi){
        this.pushInfo(cat, new AchievementInfo(achi, new AchievementObjective[0], new AchievementStartedObjective[0],true));
    }

    public boolean isUnlocked(short achievement) {
        return register.containsKey(achievement);
    }

    public void pushInfo(int cat, AchievementInfo info) {
        if (!categories.containsKey(cat)) {
            categories.put(cat, new ConcurrentHashMap<>());
        }
        categories.get(cat).put((short) info.id, info);
        ranged.put((short) info.id, info);
    }

    public synchronized void unAward(short ach){
        this.rewardsOnHold.remove(rewardsOnHold.indexOf(ach));
    }

    public synchronized void award(short ach){
        this.rewardsOnHold.add(ach);
    }

    public void recallAwards(){
        for (Short val : this.rewardsOnHold) {
            player.send(new AchievementFinishedMessage(val,this.register.get(val).finishedLevel));
        }
    }



    public Achievement[] getStartedAchievement(int category) {
        if (categories.containsKey(category)) {
            return categories.get(category).values()
                    .stream()
                    .filter(a -> !a.isFinished())
                    .toArray(Achievement[]::new);
        }
        return EMPTY;
    }

    public Achievement[] getFinishedAchievement(int category) {
        if (categories.containsKey(category)) {
            return categories.get(category).values()
                    .stream()
                    .filter(AchievementInfo::isFinished)
                    .toArray(Achievement[]::new);
        }
        return EMPTY;
    }

    public AchievementInfo getAchievement(short id){
        return this.ranged.get(id);
    }

    private final static Achievement[] EMPTY = new Achievement[0];

    public void addToRegister(final AchievementFinishedInfo info){
        this.register.put(info.getAchievement(),info);
    }

    public IoBuffer serializeRegister(IoBuffer buffer){
        if(buffer == null){
            buffer = IoBuffer.allocate(24 * register.size());
        }
        else
            buffer.clear();
        buffer.setAutoExpand(true);
        buffer.putInt(register.size());
        for (AchievementFinishedInfo achievement : register.values()) {
            buffer.putShort(achievement.achievement);
            buffer.put(achievement.finishedLevel);
        }
        return buffer;
    }


    public IoBuffer serializeAdvancements(IoBuffer buffer){
        if(buffer == null){
            buffer = IoBuffer.allocate(ranged.values().stream().mapToInt(AchievementInfo::size).sum() * ranged.size());
        }
        else
            buffer.clear();

        buffer.setAutoExpand(true);
        buffer.putInt(ranged.size());
        for(Map.Entry<Integer,ConcurrentHashMap<Short,  AchievementInfo>> cat : categories.entrySet()){
            for (Map.Entry<Short,  AchievementInfo> ranch : cat.getValue().entrySet()) {
                buffer.putInt(cat.getKey());
                ranch.getValue().serialize(buffer);
            }
        }
        return buffer;
    }

    public IoBuffer serializeRewards(IoBuffer buffer){
        if(buffer == null){
            buffer = IoBuffer.allocate(rewardsOnHold.size() * 16);
        }
        else
             buffer.clear();
        buffer.setAutoExpand(true);
        buffer.putInt(rewardsOnHold.size());
        for (Short aShort : rewardsOnHold) {
            buffer.putShort(aShort);
        }
        return buffer;
    }

    public boolean emptyRegister(){
        return register.isEmpty();
    }


    public boolean closeRange(){
        return ranged.isEmpty();
    }



}
