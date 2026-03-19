package cc.androbohij.wbot.modules.androbohij.orator;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.androbohij.wbot.modules.androbohij.orator.Orator.Voices;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class TTSPrefs {
    
    private final Logger log;
    private HashMap<Long, Voices> userVoicePrefs;
    private HashMap<Guild, Channel> autoTTSChannels;
    
    TTSPrefs() {
        log = LoggerFactory.getLogger(TTSPrefs.class);
        userVoicePrefs = new HashMap<>();
        autoTTSChannels = new HashMap<>();    
    }

    public Voices getUserVoice(long userId) {
        return userVoicePrefs.get(userId);
    }

    public void putUserVoice(long userId, Voices voice) {
        userVoicePrefs.put(userId, voice);
    }

    public Channel getAutoTTSChannel(Guild guild) {
        return autoTTSChannels.get(guild);
    }

    /**
     * this shouldn't cause any issues because channels are unique
     * and there shouldn't be any situation where a channel occurs
     * mapped to the wrong guild
     * @param channel
     * @return
     */
    public boolean isChannelAutoTTS(Channel channel) {
        return autoTTSChannels.containsValue(channel);
    }

    public void setChannelAutoTTS(SlashCommandInteractionEvent event) {
        try {
            autoTTSChannels.put(event.getGuild(), event.getMessageChannel());
            event.reply("autotts enabled in #" + event.getChannel().getName()).queue();
        } catch(IllegalStateException ise) {
            log.error(ise.getLocalizedMessage() + " while setting autotts channel");
            event.reply("you aren't in a message channel somehow!").setEphemeral(true).queue(
                m -> m.deleteOriginal().queueAfter(5, TimeUnit.SECONDS)
            );
        }
    }

    public void unsetChannelAutoTTS(SlashCommandInteractionEvent event) {
        if (autoTTSChannels.remove(event.getGuild()) != null) {
            event.reply("autotts disabled in #" + event.getChannel().getName()).queue();
        }
    }

}
