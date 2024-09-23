package com.androbohij.wbot.modules.examples.ping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.androbohij.wbot.core.ListenerModule;
import com.androbohij.wbot.core.SlashCommandModule;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

/**
 * basic ping module, edits own message to show the bot's ping
 * <p>this makes a good example to see how slash command modules work in wbot
 * @author iiandromedaa (androbohij)
 * @version 1.0.0
 */
public class Ping extends ListenerModule implements SlashCommandModule {

    private static final Logger log = LoggerFactory.getLogger(Ping.class);

	@Override
	public void addCommand(CommandListUpdateAction commands) {
		commands.addCommands(
            Commands.slash("ping", "gets bot ping")
        );
        log.info("added Ping commands");
	}

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("ping"))
            ping(event);
    }

    private void ping(SlashCommandInteractionEvent event) {
        long time = System.currentTimeMillis();
        event.reply("pong").setEphemeral(true).flatMap(e -> {
            long ping = System.currentTimeMillis() - time;
            log.info("ping: " + ping + " ms");
            return event.getHook().editOriginalFormat("pong: %d ms", ping);
        }).queue();
    }
    
}
