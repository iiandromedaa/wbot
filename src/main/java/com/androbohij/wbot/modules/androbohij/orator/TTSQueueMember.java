package com.androbohij.wbot.modules.androbohij.orator;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class TTSQueueMember {
    
    private final String text;
    private final long userId;
    private final String voice;
    private final SlashCommandInteractionEvent event;

    TTSQueueMember(String text, long userId, String voice, SlashCommandInteractionEvent event) {
        this.text = text;
        this.userId = userId;
        this.voice = voice;
        this.event = event;
    }

    TTSQueueMember(OptionMapping text, long userId, OptionMapping voice, SlashCommandInteractionEvent event) {
        this.text = text.getAsString();
        this.userId = userId;
        if (voice != null)
            this.voice = voice.getAsString();
        else
            this.voice = null;
        this.event = event;
    }

    public String getText() {
        return this.text;
    }


    public long getUserId() {
        return this.userId;
    }


    public SlashCommandInteractionEvent getEvent() {
        return this.event;
    }

    public String getVoice() {
        return this.voice;
    }

}
