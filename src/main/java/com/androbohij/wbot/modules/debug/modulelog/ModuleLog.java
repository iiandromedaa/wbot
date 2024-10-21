package com.androbohij.wbot.modules.debug.modulelog;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.androbohij.wbot.core.ListenerModule;
import com.androbohij.wbot.core.MetaModule;
import com.androbohij.wbot.core.SlashCommandModule;
import com.androbohij.wbot.core.Version;
import com.androbohij.wbot.core.Wbot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

/**
 * @author iiandromedaa (androbohij)
 */
@Version("1.0.0")
public class ModuleLog extends ListenerModule implements SlashCommandModule, MetaModule {

    private static final Logger log = LoggerFactory.getLogger(ModuleLog.class);
    private static Wbot wbot;

    @Override
	public void addCommand(CommandListUpdateAction commands) {
		commands.addCommands(
            Commands.slash("modulelog", "lists active modules and their versions")
        );
        log.info("added ModuleLog commands");
	}

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("modulelog"))
            listModules(event);
    }

    private void listModules(SlashCommandInteractionEvent event) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Active Modules");
        eb.setColor(new Color(235, 125, 52));

        for (Class<?> clazz : wbot.getModules()) {
            Version version = clazz.getAnnotation(Version.class);
            if (version == null)
                log.warn(clazz.getCanonicalName() + " is missing @Version annotation");
            else
                eb.addField(clazz.getSimpleName(), version.value(), false);
        }
        event.replyEmbeds(eb.build()).setEphemeral(true).queue();
    }

    @Override
    public void setWbot(Wbot w) {
        wbot = w;
    };
    
}