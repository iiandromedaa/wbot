package com.androbohij.wbot.core;

import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

//dont worry about the weird formatting, it looks good in the intellisense, that's what matters
/**
 * <p><b>implement</b> this interface if you want your module to make use of slash commands
 * you must implement and <b>override</b> the method addCommand
 * <p> you must also <b>extend</b> {@link com.androbohij.wbot.core.ListenerModule ListenerModule}
 * in order to have your command called by a SlashCommandInteractionEvent</p>
 * 
 * 
 * <p><b>Example:</b></p>
 * <pre><code>
 * class MyCoolCommand extends ListenerModule implements SlashCommandModule {
 * 
 *    {@literal @Override}
 * <br>    public void addCommand(CommandListUpdateAction commands) {
 *	    commands.addCommands(
 *            Commands.slash("simon", "simon says")
 *                .addOption(STRING, "content", "what the bot should say", true);
 *        );
 *    }
 * </br>    {@literal @Override}
 *    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
 *	    switch (event.getName()) {
 *            case "simon":
 *                simonSays(event, event.getOption("content").getAsString();
 *                break;
 *        }
 *    }
 *}
 * </code></pre>
 * @author iiandromedaa (androbohij)
 * @see ListenerModule
 */
public interface SlashCommandModule {

    /**
     * also define setup or variables for your module here, thats fine
     * <p>also have a sysout with the format "added {YourModuleHere} commands" 
     * after all commands have been added
     * @param commands the commandlist from wbot
     */
    public void addCommand(CommandListUpdateAction commands);
    
}