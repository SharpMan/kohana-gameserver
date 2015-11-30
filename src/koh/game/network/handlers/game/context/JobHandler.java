package koh.game.network.handlers.game.context;

import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.messages.game.context.roleplay.job.JobCrafterDirectoryDefineSettingsMessage;
import koh.protocol.messages.game.context.roleplay.job.JobCrafterDirectorySettingsMessage;
import koh.protocol.types.game.context.roleplay.job.JobCrafterDirectorySettings;

/**
 *
 * @author Neo-Craft
 */
public class JobHandler {

    @HandlerAttribute(ID = JobCrafterDirectoryDefineSettingsMessage.M_ID)
    public static void HandleJobCrafterDirectoryDefineSettingsMessage(WorldClient Client, JobCrafterDirectoryDefineSettingsMessage Message) {
        Client.character.myJobs.getJob(Message.settings.jobId).minLevel = Message.settings.minLevel;
        Client.character.myJobs.getJob(Message.settings.jobId).free = Message.settings.free;
        Client.send(new JobCrafterDirectorySettingsMessage(new JobCrafterDirectorySettings[] { Message.settings }));

    }

}
