package com.androbohij.wbot.modules.androbohij.orator;

import java.util.Map;

import com.androbohij.wbot.core.ListenerModule;
import com.androbohij.wbot.core.SlashCommandModule;
import com.microsoft.cognitiveservices.speech.AudioDataStream;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

/**
 * @author iiandromedaa (androbohij)
 */
public class Orator extends ListenerModule implements SlashCommandModule {

    private static Map<Double, Double> userVoicePrefs;
    private static String speechKey = System.getenv("SPEECH_KEY");
    private static String speechRegion = System.getenv("SPEECH_REGION");
    private static SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);
    SpeechSynthesizer speechSynthesizer  = new SpeechSynthesizer(speechConfig);


	@Override
	public void addCommand(CommandListUpdateAction commands) {

        
        // speechConfig.setSpeechSynthesisVoiceName("en-US-AshleyNeural"); 

		commands.addCommands(
            Commands.slash("tts", "speak your mind!")
                .addOption(OptionType.STRING, "content", "what youll say", true)
                //this had to be a long ass line,,,, string moment
                .addOption(OptionType.STRING, "voice", "the voice to speak in (without this, it will use the default, or the saved option if you have one)", false)
                .setGuildOnly(true),
            Commands.slash("voice", "sets the voice youll use for the shorthand tts command")
                .addOption(OptionType.STRING, "voice", "name of the voice to use", true)
        );
        System.out.println("added Orator commands");
	}

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "tts":
                tts(event.getOption("content").getAsString(), "NEUROSAMA", event);
                break;
        }
    }

    /**
     * mode without a voice parameter, attempts to load user setting from map, 
     * uses default voice otherwise
     * @param text
     * @param event
     */
    private void tts(String text, SlashCommandInteractionEvent event) {

    }

    private void tts(String text, String voice, SlashCommandInteractionEvent event) {
        String ssml = """
                    <speak version="1.0" xmlns="http://www.w3.org/2001/10/synthesis" xmlns:mstts="http://www.w3.org/2001/mstts" xmlns:emo="http://www.w3.org/2009/10/emotionml" xml:lang="en-US">
                        <voice name="%d">
                            <prosody rate="-10.00%">
                                <prosody pitch="%p">
                                    <s>%s</s>
                                </prosody>
                            </prosody>
                        </voice>
                    </speak>
                    """;

        ssml = ssml.replace("%s", text);
        if (voice.equals("NEUROSAMA")) {
            ssml = ssml.replace("%d", "en-US-AshleyNeural");
            ssml = ssml.replace("%p", "25%");
        } else {
            ssml = ssml.replace("%d", voice);
            ssml = ssml.replace("%p", "0%");
        }

        try {
            SpeechSynthesisResult result = speechSynthesizer.SpeakSsmlAsync(ssml).get();
            AudioDataStream stream = AudioDataStream.fromResult(result);
            stream.saveToWavFileAsync("ssml.wav");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
