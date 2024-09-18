package com.androbohij.wbot.core;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * empty abstract class used to keep track of modules
 * 
 * <p>your module should extend this and <b>override</b> one or multiple of the methods 
 * available</p><p>also dont forget to include proper javadocs, including relevant tags for classes
 * and methods, see https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html
 * for more info</p>
 * 
 * @author iiandromedaa (androbohij)
 * @see SlashCommandModule
 */
public abstract class ListenerModule extends ListenerAdapter {}
