package com.androbohij.wbot.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

//TODO MORE JAVADOCS!!!!
/**
 * @author iiandromedaa (androbohij)
 * @version 2.0.0
 */
public class Wbot {

    private CommandListUpdateAction commands;
    private List<ListenerModule> modules = new ArrayList<ListenerModule>();
    private List<SlashCommandModule> slashes = new ArrayList<SlashCommandModule>();
    private List<MetaModule> metas = new ArrayList<MetaModule>();
    private JDA wbot;

    /**
     * constructor for wbot
     * @param modulesSet
     * @param slashes
     * @param metas
     */
    public Wbot(Set<Class<?>> modulesSet) {

        for (Class<?> clazz : modulesSet) {
            try {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                modules.add((ListenerModule) instance);
                if (SlashCommandModule.class.isAssignableFrom(clazz)) {
                    slashes.add((SlashCommandModule) instance);
                }
                if (MetaModule.class.isAssignableFrom(clazz)) {
                    metas.add((MetaModule) instance);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        wbot = JDABuilder.create(
                System.getenv("DISCORD_TOKEN"), 
                EnumSet.allOf(GatewayIntent.class)
            ).addEventListeners(new EventHandler(this.modules)).build();

        commands = wbot.updateCommands();
        for (SlashCommandModule slashCommandModule : slashes) {
            slashCommandModule.addCommand(commands);
        }
        for (MetaModule metaModule : metas) {
            metaModule.setWbot(this);
        }
        commands.queue();
        
    }

    /**
     * 
     * @param clazz
     * @param methodName
     * @param event
     */
    private void callModuleEvent(List<ListenerModule> modules, String methodName, Event event) {
        try {
            Method method = ListenerModule.class.getMethod(methodName, event.getClass());
            for (ListenerModule listenerModule : modules) {
                method.invoke(listenerModule, event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ListenerModule> getModules() {
        return modules;
    }

    /**
     * 
     */
    class EventHandler extends ListenerAdapter {

        private List<ListenerModule> modules;
        
        EventHandler(List<ListenerModule> modules) {
            this.modules = modules;
        }

        @Override
        public void onReady(ReadyEvent event) {
            callModuleEvent(modules, "onReady", event);
        }

        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            callModuleEvent(modules, "onMessageReceived", event);
        }

        @Override
        public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
            callModuleEvent(modules, "onGuildVoiceUpdate", event);
        }

        @Override
        public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
            callModuleEvent(modules, "onSlashCommandInteraction", event);
        }

        @Override
        public void onShutdown(ShutdownEvent event) {
            callModuleEvent(modules, "onShutdown", event);
        }

    }
    
}