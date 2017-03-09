package koh.game.entities.actors.character;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import koh.game.dao.DAO;
import koh.game.entities.ExpLevel;
import koh.game.entities.actors.Player;
import koh.game.entities.jobs.InteractiveSkill;
import koh.game.entities.jobs.JobGatheringInfos;
import koh.protocol.client.BufUtils;
import koh.protocol.client.enums.JobEnum;
import koh.protocol.messages.game.context.roleplay.job.JobExperienceUpdateMessage;
import koh.protocol.messages.game.context.roleplay.job.JobLevelUpMessage;
import koh.protocol.types.game.context.roleplay.job.JobCrafterDirectorySettings;
import koh.protocol.types.game.context.roleplay.job.JobDescription;
import koh.protocol.types.game.context.roleplay.job.JobExperience;
import koh.protocol.types.game.interactive.skill.SkillActionDescription;
import koh.protocol.types.game.interactive.skill.SkillActionDescriptionCollect;
import koh.protocol.types.game.interactive.skill.SkillActionDescriptionCraft;
import koh.utils.Couple;
import lombok.ToString;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class JobBook {

    private Map<Byte, JobInfo> myJobs = Collections.synchronizedMap(new HashMap<Byte, JobInfo>());

    public void addExperience(Player actor, byte parentJobId, int value) {
        ExpLevel floor;

        this.myJobs.get(parentJobId).xp += value;

        Integer lastLevel = this.myJobs.get(parentJobId).jobLevel;

        do {
            floor = DAO.getExps().getLevel(this.myJobs.get(parentJobId).jobLevel + 1);

            if (floor.getJob() < this.myJobs.get(parentJobId).xp) {
                this.myJobs.get(parentJobId).jobLevel++;
            }
        } while (floor.getJob() < this.myJobs.get(parentJobId).xp && this.myJobs.get(parentJobId).jobLevel != 200);

        if (this.myJobs.get(parentJobId).jobLevel != lastLevel) {
            actor.send(new JobLevelUpMessage((byte)this.myJobs.get(parentJobId).jobLevel,new JobDescription(parentJobId, DAO.getJobTemplates().streamSkills().filter(Skill -> Skill.getParentJobId() == parentJobId && this.myJobs.get(parentJobId).jobLevel >= Skill.getLevelMin()).map(Skill -> jobToSkill(Skill, this.myJobs.get(parentJobId))).toArray(SkillActionDescription[]::new))));
        }

        actor.send(new JobExperienceUpdateMessage(new JobExperience(parentJobId, (byte) this.myJobs.get(parentJobId).jobLevel, this.myJobs.get(parentJobId).xp, DAO.getExps().getLevel(this.myJobs.get(parentJobId).jobLevel).getJob(), DAO.getExps().getLevel(this.myJobs.get(parentJobId).jobLevel + 1).getJob())));
    }

    @ToString
    public class JobInfo {

        public byte id;
        public int xp, jobLevel, minLevel;
        public boolean free;
        public int gatheringItems, craftedItems;

        public JobInfo(byte id) {
            this.id = id;
            this.xp = 0;
            this.jobLevel = 1;
            this.free = true;
        }

        public JobInfo(IoBuffer buf) {
            this.id = buf.get();
            this.xp = buf.getInt();
            this.jobLevel = buf.getInt();
            if(id == 16 || id == 63){
                jobLevel = 200;
            }
            this.minLevel = buf.getInt();
            this.free = BufUtils.readBoolean(buf);
            this.gatheringItems = buf.getInt();
            this.craftedItems = buf.getInt();
        }

        public void serialize(IoBuffer buf) {
            buf.put(id);
            buf.putInt(xp);
            buf.putInt(jobLevel);
            buf.putInt(minLevel);
            BufUtils.writeBoolean(buf, free);
            buf.putInt(gatheringItems);
            buf.putInt(craftedItems);
        }

        public JobGatheringInfos jobEntity(int startLevel) {
            return DAO.getJobTemplates().findGathJob(DAO.getJobTemplates().findHightJob(startLevel));
        }

        public Couple<Integer, Integer> quantity(int startLevel) {
            return DAO.getJobTemplates().findGathJob(DAO.getJobTemplates().findHightJob(startLevel)).levelMinMax(this.jobLevel);
        }

        public byte probability() {
            return (byte) ((5 + (0.5 * jobLevel)) > 100 ? 100 : (5 + (0.5 * jobLevel)));
        }

        public void totalClear() {
            try {
                this.id = 0;
                this.xp = 0;
                this.jobLevel = 0;
                this.minLevel = 0;
                this.free = false;
                this.gatheringItems = 0;
                this.craftedItems = 0;
                this.finalize();
            } catch (Throwable ex) {
            }
        }
    }

    public void deserializeEffects(byte[] binary) {
        if (binary.length <= 0) {
            for (JobEnum Job : JobEnum.values()) {
                this.addJob(new JobInfo(Job.value));
            }
            return;
        }
        IoBuffer buf = IoBuffer.wrap(binary);
        int len = buf.getInt();
        for (int i = 0; i < len; i++) {
            this.addJob(new JobInfo(buf));
        }
    }

    public void addJob(JobInfo info) {
        this.myJobs.put(info.id, info);
    }

    public JobDescription[] getDescriptions() {
        return this.myJobs.values().stream().map(Job -> new JobDescription(Job.id, DAO.getJobTemplates().streamSkills().filter(Skill -> Skill.getParentJobId() == Job.id && Job.jobLevel >= Skill.getLevelMin()).map(Skill -> jobToSkill(Skill, Job)).toArray(SkillActionDescription[]::new))).toArray(JobDescription[]::new);
    }

    public JobExperience[] getExperiences() {
        return this.myJobs.values().stream().map(Job -> new JobExperience(Job.id, (byte) Job.jobLevel, Job.xp, DAO.getExps().getLevel(Job.jobLevel).getJob(), DAO.getExps().getLevel(Job.jobLevel + 1).getJob())).toArray(JobExperience[]::new);
    }

    public JobCrafterDirectorySettings[] getSettings() {
        return this.myJobs.values().stream().map(Job -> new JobCrafterDirectorySettings(Job.id, (byte) Job.minLevel, Job.free)).toArray(JobCrafterDirectorySettings[]::new);
    }

    public SkillActionDescription jobToSkill(InteractiveSkill skill, JobInfo job) {
        if (skill.isForgemagus()) {
            return new SkillActionDescriptionCraft(skill.getID(), job.probability());
        } else if (skill.getGatheredRessourceItem() != -1) {
            return new SkillActionDescriptionCollect(skill.getID(), (byte) 30, job.quantity(skill.getLevelMin()).first, job.quantity(skill.getLevelMin()).second);
        } else {
            return new SkillActionDescriptionCraft(skill.getID(), (byte) 100);
        }
        /*else  {
         throw new Error(String.format("Unknow skill %s Ability %s job %s", skill.id, skill.type, job.id));
         }*/
    }

    public byte[] serialize() {
        IoBuffer buf = IoBuffer.allocate(1);
        buf.setAutoExpand(true);

        buf.putInt(this.myJobs.size());
        this.myJobs.values().forEach(Spell -> Spell.serialize(buf));

        return buf.array();
    }

    public JobInfo getJob(byte id) {
        return myJobs.get(id);
    }

    public void totalClear() {
        try {
            myJobs.values().stream().forEach((s) -> {
                s.totalClear();
            });
            myJobs.clear();
            myJobs = null;
            this.finalize();
        } catch (Throwable ex) {

        }
    }

}
