package com.androbohij.wbot.core;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Set;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.internal.utils.JDALogger;

//TODO MORE JAVADOCS!!!!
/**
 * @author iiandromedaa (androbohij)
 * @version 1.0.0
 */
public class Wbot {

    private CommandListUpdateAction commands;
    private JDA wbot;

    /**
     * constructor for wbot
     * @param modules
     * @param slashes
     */
    public Wbot(Set<Class<?>> modules, Set<Class<?>> slashes) {
        JDALogger.setFallbackLoggerEnabled(false);
        wbot = JDABuilder.createLight(
                System.getenv("DISCORD_TOKEN"), 
                EnumSet.allOf(GatewayIntent.class)
            ).addEventListeners(new EventHandler(modules)).build();
        commands = wbot.updateCommands();
        for (Class<?> clazz : slashes) {
            addSubTypeCommand(clazz);
        }
        commands.queue();
        
    }

    /**
     * 
     * @param clazz
     */
    private void addSubTypeCommand(Class<?> clazz) {
        if (SlashCommandModule.class.isAssignableFrom(clazz)) {
            try {
                SlashCommandModule instance = (SlashCommandModule) clazz
                    .getDeclaredConstructor().newInstance();
                Method method = SlashCommandModule.class.getMethod(
                    "addCommand",
                    CommandListUpdateAction.class
                );
                method.invoke(instance, commands);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 
     * @param clazz
     * @param methodName
     * @param event
     */
    private void callModuleEvent(Class<?> clazz, String methodName, Event event) {
        if (ListenerModule.class.isAssignableFrom(clazz)) {
            try {
                ListenerModule instance = (ListenerModule) clazz
                    .getDeclaredConstructor().newInstance();
                Method method = ListenerModule.class.getMethod(
                    methodName,
                    event.getClass()
                );
                method.invoke(instance, event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 
     */
    class EventHandler extends ListenerAdapter {

        private Set<Class<?>> modules;
        
        EventHandler(Set<Class<?>> modules) {
            this.modules = modules;
        }

        @Override
        public void onReady(ReadyEvent event) {

        }

        @Override
        public void onMessageReceived(MessageReceivedEvent event) {

        }

        @Override
        public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
            for (Class<?> clazz : modules) {
                callModuleEvent(clazz, "onSlashCommandInteraction", event);
            }
        }

    }
    
}
