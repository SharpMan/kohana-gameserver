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
    public static void HandleJobCrafterDirectoryDefineSettingsMessage(WorldClient client, JobCrafterDirectoryDefineSettingsMessage Message) {
        client.getCharacter().getMyJobs().getJob(Message.settings.jobId).minLevel = Message.settings.minLevel;
        client.getCharacter().getMyJobs().getJob(Message.settings.jobId).free = Message.settings.free;
        client.send(new JobCrafterDirectorySettingsMessage(new JobCrafterDirectorySettings[] { Message.settings }));

    }

}
