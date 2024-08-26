package com.androbohij.wbot.modules.androbohij.orator;

import java.io.IOException;
import java.util.HashMap;

import javax.sound.sampled.AudioFormat;

import com.androbohij.wbot.core.ListenerModule;
import com.androbohij.wbot.core.SaveLoad;
import com.androbohij.wbot.core.SlashCommandModule;
import com.microsoft.cognitiveservices.speech.AudioDataStream;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisOutputFormat;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import static com.androbohij.wbot.modules.androbohij.orator.Orator.Voices.*;

/**
 * @author iiandromedaa (androbohij)
 */
public class Orator extends ListenerModule implements SlashCommandModule {

    private static HashMap<Long, Voices> userVoicePrefs;
    private static String speechKey = System.getenv("SPEECH_KEY");
    private static String speechRegion = System.getenv("SPEECH_REGION");
    SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);
    SpeechSynthesizer speechSynthesizer = new SpeechSynthesizer(speechConfig, null);

    //its safe i promise
	@SuppressWarnings("unchecked")
	@Override
	public void addCommand(CommandListUpdateAction commands) {
        speechConfig.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Raw16Khz16BitMonoPcm);

        try {
			userVoicePrefs = SaveLoad.load(this.getClass(), HashMap.class);
		} catch (IOException e) {
			userVoicePrefs = new HashMap<>();
		}

		commands.addCommands(
            Commands.slash("tts", "speak your mind!")
                .addOption(OptionType.STRING, "content", "what youll say", true)
                //this had to be a long ass line,,,, string moment
                .addOption(OptionType.STRING, "voice", "voice to use (optional, defaults to AvaNeural or any saved voice setting)", false)
                .setGuildOnly(true),
            Commands.slash("setvoice", "choose a tts voice to use by default")
                .addOption(OptionType.STRING, "voice", "name of the voice to use (case insensitive)", true),
            Commands.slash("listvoices", "displays list of voice options")
        );
        System.out.println("added Orator commands");
	}

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "tts":
                if (event.getOption("voice") == null) {
                    tts(event.getOption("content").getAsString(), event);
                } else {
                    tts(event.getOption("content").getAsString(), 
                        event.getOption("voice").getAsString(), event);
                }
                break;
            case "setvoice":
                setVoicePrefs(event.getOption("voice").getAsString(), event);
                break;
            case "listvoices":
                break;
            default:
                break;
        }
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        System.out.println("shut");
        SaveLoad.save(this.getClass(), userVoicePrefs);
    }

    /**
     * mode without a voice parameter, attempts to load user setting from map, 
     * uses default voice otherwise
     * @param text
     * @param event
     */
    private void tts(String text, SlashCommandInteractionEvent event) {
        if (userVoicePrefs.get(event.getUser().getIdLong()) == null) {
            tts(text, null, event);
        } else {
            tts(text, userVoicePrefs.get(event.getUser().getIdLong()).voice, event);
        }
    }

    private void tts(String text, String voice, SlashCommandInteractionEvent event) {
        IntermediaryHandler handler = joinUserVoiceChannel(event);
        if (handler == null) {
            event.reply("you arent currently in a vc!!").setEphemeral(true).queue();
            return;
        }
        Voices enumVoice = voiceFromString(voice) != null ? voiceFromString(voice) : Voices.AVA;
        String ssml = """
                    <speak version="1.0" xmlns="http://www.w3.org/2001/10/synthesis" xmlns:mstts="http://www.w3.org/2001/mstts" xmlns:emo="http://www.w3.org/2009/10/emotionml" xml:lang="en-US">
                        <voice name="%d">
                            <prosody pitch="%p">
                                <s>%s</s>
                            </prosody>
                        </voice>
                    </speak>
                    """;
        ssml = ssml.replace("%s", text);
        if (enumVoice.equals(NEUROSAMA)) {
            ssml = ssml.replace("%d", ASHLEY.voice);
            ssml = ssml.replace("%p", "25%");
        } else {
            ssml = ssml.replace("%d", enumVoice.voice);
            ssml = ssml.replace("%p", "0%");
        }

        event.reply("tts sent").setEphemeral(true).queue();
        SpeechSynthesisResult result = speechSynthesizer.SpeakSsml(ssml);
        AudioDataStream stream = AudioDataStream.fromResult(result);

        byte[] buf = new byte[4096];
        while (stream.readData(buf) > 0) {
            //this isnt right, this SHOULDNT be right, but 48khz sample rate made it sped up, this somehow fixed it
            //i dont know why, i dont know how, the conversion code isnt mine, but it works so lets not touch it
            
            byte[] conv = handler.convert(buf, new AudioFormat(16000, 16, 1, true, false),
                AudioSendHandler.INPUT_FORMAT);
            handler.send(conv);
        }
    }

    private void setVoicePrefs(String voice, SlashCommandInteractionEvent event) {
        if (voiceFromString(voice) != null) {
            userVoicePrefs.put(event.getUser().getIdLong(), voiceFromString(voice));
            event.reply("voice set to " + voiceFromString(voice)).setEphemeral(true).queue();
        } else {
            event.reply("couldnt find a voice with that name").setEphemeral(true).queue();
        }
    }

    /**
     * attempts to connect to the voice channel the user is in
     * @param event
     * @return false if unable to join, true if joined
     */
    private IntermediaryHandler joinUserVoiceChannel(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        Member member = guild.getMember(event.getUser());
        if (!member.getVoiceState().inAudioChannel()) {
            return null;
        }
        AudioManager manager = guild.getAudioManager();
        manager.openAudioConnection(member.getVoiceState().getChannel().asVoiceChannel());
        manager.setSelfDeafened(true);
        IntermediaryHandler handler = new IntermediaryHandler();
        manager.setSendingHandler(handler);
        return handler;
    }

    enum Voices {

        NEUROSAMA("NEUROSAMA"),
        AVA("en-US-AvaNeural"),
        ANDREW("en-US-AndrewNeural"),
        EMMA("en-US-EmmaNeural"),
        BRIAN("en-US-BrianNeural"),
        JENNY("en-US-JennyNeural"),
        AMBER("en-US-AmberNeural"),
        ANA("en-US-AnaNeural"),
        ASHLEY("en-US-AshleyNeural"),
        ERIC("en-US-EricNeural"),
        ROGER("en-US-RogerNeural"),
        BLUE("en-US-BlueNeural"),
        FABLE("en-US-FableMultilingualNeural");
        
        private String voice;

        Voices(String voice) {
            this.voice = voice;
        }

        public static Voices voiceFromString(String string) {
            for (Voices v : Voices.values()) {
                if (v.name().equalsIgnoreCase(string))
                    return v;
            }
            return null;
        }

    }

}
