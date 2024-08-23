package com.androbohij.wbot.core;

import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

/**
 * main class of the bot, entry point for java etc etc
 * <p>performs some bootstrapping stuff for the bot like loading modules</p>
 * 
 * @author iiandromedaa (androbohij)
 */
public class Main {

    private static Set<Class<?>> modules;
    private static Set<Class<?>> slashes;

    public static void main(String[] args) {
        Reflections reflections = new Reflections("com.androbohij.wbot");
        modules = reflections.get(Scanners.SubTypes.of(ListenerModule.class).asClass());
        slashes = reflections.get(Scanners.SubTypes.of(SlashCommandModule.class).asClass());
        new Wbot(modules, slashes);
    }

}
