package koh.game.entities.actors.character;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import koh.game.dao.ExpDAO;
import koh.game.dao.JobDAO;
import koh.game.dao.SpellDAO;
import koh.game.entities.ExpLevel;
import koh.game.entities.actors.Player;
import koh.game.entities.jobs.InteractiveSkill;
import koh.game.entities.jobs.JobGatheringInfos;
import koh.game.entities.spells.LearnableSpell;
import koh.protocol.client.BufUtils;
import koh.protocol.client.enums.JobEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.context.roleplay.job.JobExperienceUpdateMessage;
import koh.protocol.messages.game.context.roleplay.job.JobLevelUpMessage;
import koh.protocol.types.game.context.roleplay.job.JobCrafterDirectorySettings;
import koh.protocol.types.game.context.roleplay.job.JobDescription;
import koh.protocol.types.game.context.roleplay.job.JobExperience;
import koh.protocol.types.game.interactive.skill.SkillActionDescription;
import koh.protocol.types.game.interactive.skill.SkillActionDescriptionCollect;
import koh.protocol.types.game.interactive.skill.SkillActionDescriptionCraft;
import koh.utils.Couple;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class JobBook {

    private Map<Byte, JobInfo> myJobs = Collections.synchronizedMap(new HashMap<Byte, JobInfo>());

    public void addExperience(Player Actor, byte parentJobId, int Value) {
        ExpLevel Floor;

        this.myJobs.get(parentJobId).XP += Value;

        Integer LastLevel = this.myJobs.get(parentJobId).jobLevel;

        do {
            Floor = ExpDAO.GetFloorByLevel(this.myJobs.get(parentJobId).jobLevel + 1);

            if (Floor.Job < this.myJobs.get(parentJobId).XP) {
                this.myJobs.get(parentJobId).jobLevel++;
            }
        } while (Floor.Job < this.myJobs.get(parentJobId).XP && this.myJobs.get(parentJobId).jobLevel != 200);

        if (this.myJobs.get(parentJobId).jobLevel != LastLevel) {
            Actor.Send(new JobLevelUpMessage((byte)this.myJobs.get(parentJobId).jobLevel,new JobDescription(parentJobId, JobDAO.Skills.values().stream().filter(Skill -> Skill.parentJobId == parentJobId && this.myJobs.get(parentJobId).jobLevel >= Skill.levelMin).map(Skill -> JobToSkill(Skill, this.myJobs.get(parentJobId))).toArray(SkillActionDescription[]::new))));
        }

        Actor.Send(new JobExperienceUpdateMessage(new JobExperience(parentJobId, (byte) this.myJobs.get(parentJobId).jobLevel, this.myJobs.get(parentJobId).XP, ExpDAO.GetFloorByLevel(this.myJobs.get(parentJobId).jobLevel).Job, ExpDAO.GetFloorByLevel(this.myJobs.get(parentJobId).jobLevel + 1).Job)));
    }

    public class JobInfo {

        public byte ID;
        public int XP, jobLevel, minLevel;
        public boolean free;
        public int gatheringItems, craftedItems;

        public JobInfo(byte id) {
            this.ID = id;
            this.XP = 0;
            this.jobLevel = 1;
            this.free = true;
        }

        public JobInfo(IoBuffer buf) {
            this.ID = buf.get();
            this.XP = buf.getInt();
            this.jobLevel = buf.getInt();
            this.minLevel = buf.getInt();
            this.free = BufUtils.readBoolean(buf);
            this.gatheringItems = buf.getInt();
            this.craftedItems = buf.getInt();
        }

        public void Serialize(IoBuffer buf) {
            buf.put(ID);
            buf.putInt(XP);
            buf.putInt(jobLevel);
            buf.putInt(minLevel);
            BufUtils.writeBoolean(buf, free);
            buf.putInt(gatheringItems);
            buf.putInt(craftedItems);
        }

        public JobGatheringInfos JobEntity(int StartLevel) {
            return JobDAO.GatheringJobs.get(JobDAO.GatheringJobs.keySet().stream().filter(x -> StartLevel >= x).mapToInt(x -> x).max().getAsInt());
        }

        public Couple<Integer, Integer> Quantity(int StartLevel) {
            return JobDAO.GatheringJobs.get(JobDAO.GatheringJobs.keySet().stream().filter(x -> StartLevel >= x).mapToInt(x -> x).max().getAsInt()).LevelMinMax(this.jobLevel);
        }

        public byte Probability() {
            return (byte) ((5 + (0.5 * jobLevel)) > 100 ? 100 : (5 + (0.5 * jobLevel)));
        }

        public void totalClear() {
            try {
                this.ID = 0;
                this.XP = 0;
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

    public void DeserializeEffects(byte[] binary) {
        if (binary.length <= 0) {
            for (JobEnum Job : JobEnum.values()) {
                this.AddJob(new JobInfo(Job.value));
            }
            return;
        }
        IoBuffer buf = IoBuffer.wrap(binary);
        int len = buf.getInt();
        for (int i = 0; i < len; i++) {
            this.AddJob(new JobInfo(buf));
        }
    }

    public void AddJob(JobInfo Info) {
        this.myJobs.put(Info.ID, Info);
    }

    public JobDescription[] GetDescriptions() {
        return this.myJobs.values().stream().map(Job -> new JobDescription(Job.ID, JobDAO.Skills.values().stream().filter(Skill -> Skill.parentJobId == Job.ID && Job.jobLevel >= Skill.levelMin).map(Skill -> JobToSkill(Skill, Job)).toArray(SkillActionDescription[]::new))).toArray(JobDescription[]::new);
    }

    public JobExperience[] GetExperiences() {
        return this.myJobs.values().stream().map(Job -> new JobExperience(Job.ID, (byte) Job.jobLevel, Job.XP, ExpDAO.GetFloorByLevel(Job.jobLevel).Job, ExpDAO.GetFloorByLevel(Job.jobLevel + 1).Job)).toArray(JobExperience[]::new);
    }

    public JobCrafterDirectorySettings[] GetSettings() {
        return this.myJobs.values().stream().map(Job -> new JobCrafterDirectorySettings(Job.ID, (byte) Job.minLevel, Job.free)).toArray(JobCrafterDirectorySettings[]::new);
    }

    public SkillActionDescription JobToSkill(InteractiveSkill Skill, JobInfo Job) {
        if (Skill.isForgemagus) {
            return new SkillActionDescriptionCraft(Skill.ID, Job.Probability());
        } else if (Skill.gatheredRessourceItem != -1) {
            System.out.println(Skill.gatheredRessourceItem);
            return new SkillActionDescriptionCollect(Skill.ID, (byte) 30, Job.Quantity(Skill.levelMin).first, Job.Quantity(Skill.levelMin).second);
        } else {
            return new SkillActionDescriptionCraft(Skill.ID, (byte) 100);
        }
        /*else  {
         throw new Error(String.format("Unknow Skill %s Ability %s Job %s", Skill.ID, Skill.Type, Job.ID));
         }*/
    }

    public byte[] Serialize() {
        IoBuffer buf = IoBuffer.allocate(1);
        buf.setAutoExpand(true);

        buf.putInt(this.myJobs.size());
        this.myJobs.values().forEach(Spell -> Spell.Serialize(buf));

        return buf.array();
    }

    public JobInfo GetJob(byte id) {
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
