package com.androbohij.wbot.modules.androbohij.orator;

import com.androbohij.wbot.core.ListenerModule;
import com.androbohij.wbot.core.SlashCommandModule;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

/**
 * @author iiandromedaa (androbohij)
 */
public class Orator extends ListenerModule implements SlashCommandModule {

	@Override
	public void addCommand(CommandListUpdateAction commands) {
		commands.addCommands(

        );
	}

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        
    }
    
}
