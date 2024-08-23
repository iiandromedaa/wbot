package com.androbohij.wbot.modules.examples.ping;

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

	@Override
	public void addCommand(CommandListUpdateAction commands) {
        System.out.println("added 'ping' command");
		commands.addCommands(
            Commands.slash("ping", "gets bot ping")
        );
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
            System.out.printf("ping: %d ms", ping);
            return event.getHook().editOriginalFormat("pong: %d ms", ping);
        }).queue();
    }
    
}
