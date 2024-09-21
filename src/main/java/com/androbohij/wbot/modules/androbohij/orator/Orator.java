package com.androbohij.wbot.modules.androbohij.orator;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.androbohij.wbot.core.ListenerModule;
import com.androbohij.wbot.core.SaveLoad;
import com.androbohij.wbot.core.SlashCommandModule;
import static com.androbohij.wbot.modules.androbohij.orator.Orator.Voices.ASHLEY;
import static com.androbohij.wbot.modules.androbohij.orator.Orator.Voices.NEUROSAMA;
import static com.androbohij.wbot.modules.androbohij.orator.Orator.Voices.voiceFromString;
import com.microsoft.cognitiveservices.speech.AudioDataStream;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisOutputFormat;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

/**
 * @author iiandromedaa (androbohij)
 */
public class Orator extends ListenerModule implements SlashCommandModule {

    private static final Logger log = LoggerFactory.getLogger(Orator.class);

    private static final String SPEECHKEY = System.getenv("SPEECH_KEY");
    private static final String SPEECHREGION = System.getenv("SPEECH_REGION");
    private static final SpeechConfig SPEECHCONFIG = SpeechConfig.fromSubscription(SPEECHKEY, SPEECHREGION);
    private static final SpeechSynthesizer SPEECHSYNTHESIZER = new SpeechSynthesizer(SPEECHCONFIG, null);

    private static HashMap<Guild, TTSQueueWrapper> map = new HashMap<>();
    private static HashMap<Long, Voices> userVoicePrefs = new HashMap<>();

    //its safe i promise
	@SuppressWarnings("unchecked")
	@Override
	public void addCommand(CommandListUpdateAction commands) {
        SPEECHCONFIG.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Raw48Khz16BitMonoPcm);

        try {
            //if a null is somehow saved, prevent that from destroying the bot
            HashMap<Long, Voices> load = SaveLoad.load(this.getClass(), HashMap.class);
			userVoicePrefs = (load == null) ? new HashMap<>() : load;
		} catch (IOException e) {
            log.error("IOException reading saved hashmap", e.getCause());
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
                //check if queue exists, if not create it
                if (map.get(event.getGuild()) == null) {
                    map.put(event.getGuild(), new TTSQueueWrapper());
                }

                TTSQueueWrapper queue = map.get(event.getGuild());

                if (!event.getGuild().getMember(event.getUser()).getVoiceState().inAudioChannel()) {
                    event.reply("you arent in a vc").setEphemeral(true).queue(
                        m -> m.deleteOriginal().queueAfter(5, TimeUnit.SECONDS)
                    );
                    return; 
                }

                event.reply("tts queued").setEphemeral(true).queue(
                    m -> m.deleteOriginal().queueAfter(5, TimeUnit.SECONDS)
                );

                //if the queue is empty, create a queue reading thread
                if (queue.isEmpty()) {
                    queue.add(new TTSQueueMember(event.getOption("content"), 
                        event.getUser().getIdLong(), event.getOption("voice"), event));
                    Thread t = new Thread() {
                        @Override
                        public void run() {
                            while (!queue.isEmpty()) {
                                try {
                                    Thread.sleep(ttsRun(queue.peek()));
                                    queue.poll();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    t.start();
                } else {
                    //if the queue is not empty, simply add an entry and the thread will handle it
                    queue.add(new TTSQueueMember(event.getOption("content"), 
                        event.getUser().getIdLong(), event.getOption("voice"), event));
                }
                break;
            case "setvoice":
                //TODO logging, log this, this breaks often enough to be concerning
                setVoicePrefs(event.getOption("voice").getAsString(), event);
                break;
            case "listvoices":
                listVoices(event);
                break;
            default:
                break;
        }
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        AudioChannelUnion acu = event.getChannelLeft();
        GuildVoiceState gvs = event.getGuild().getSelfMember().getVoiceState();
        if (acu != null) {
            if (gvs.inAudioChannel() && gvs.getChannel().equals(acu)) {
                if (gvs.getChannel().getMembers().size() == 1 
                    && gvs.getChannel().getMembers().contains(event.getGuild().getSelfMember())) {
                    event.getGuild().getAudioManager().closeAudioConnection();
                }
            }
        }
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        SaveLoad.save(this.getClass(), userVoicePrefs);
    }

    /**
     * mode without a voice parameter, attempts to load user setting from map, 
     * uses default voice otherwise
     * @param text
     * @param event
     */
    private long tts(String text, SlashCommandInteractionEvent event) {
        if (userVoicePrefs == null || userVoicePrefs.get(event.getUser().getIdLong()) == null) {
            return tts(text, null, event);
        } else {
            return tts(text, userVoicePrefs.get(event.getUser().getIdLong()).name(), event);
        }
    }

    private long tts(String text, String voice, SlashCommandInteractionEvent event) {
        IntermediaryHandler handler = joinUserVoiceChannel(event);
        if (handler == null) {
            event.reply("you arent currently in a vc!!").setEphemeral(true).queue();
            return 0;
        }
        Voices enumVoice = voiceFromString(voice) != null ? voiceFromString(voice) : Voices.AVA;
        String ssml = """
                    <speak version="1.0" xmlns="http://www.w3.org/2001/10/synthesis" xmlns:mstts="http://www.w3.org/2001/mstts" xmlns:emo="http://www.w3.org/2009/10/emotionml" xml:lang="%l">
                        <voice name="%d">
                            <prosody pitch="%p">
                                %s
                            </prosody>
                        </voice>
                    </speak>
                    """;
        ssml = ssml.replace("%s", text);
        if (enumVoice.equals(NEUROSAMA)) {
            ssml = ssml.replace("%d", ASHLEY.voice);
            ssml = ssml.replace("%p", "25%");
            ssml = ssml.replace("%l", "en-US");
        } else {
            ssml = ssml.replace("%d", enumVoice.voice);
            ssml = ssml.replace("%p", "0%");
            ssml = ssml.replace("%l", enumVoice.voice.substring(0, 5));
        }

        event.getChannel().sendMessage(event.getUser().getName() + " said: " + "*" + text + "*")
            .queue(m -> m.delete().queueAfter(5, TimeUnit.MINUTES));

        log.info(event.getUser().getName() + " said: " + "\"" + text + "\"" 
            + " using " + enumVoice.toString());
        
        SpeechSynthesisResult result = SPEECHSYNTHESIZER.SpeakSsml(ssml);
        AudioDataStream stream = AudioDataStream.fromResult(result);

        byte[] buf = new byte[4096];
        while (stream.readData(buf) > 0) {
            //this isnt right, this SHOULDNT be right, but 48khz sample rate made it sped up, this somehow fixed it
            //i dont know why, i dont know how, the conversion code isnt mine, but it works so lets not touch it
            
            byte[] conv = handler.convert(buf, new AudioFormat(16000, 16, 1, true, false),
                AudioSendHandler.INPUT_FORMAT);
            handler.send(conv);
        }
        //10000 ticks (100ns) per ms
        //+0.5s delay
        return (result.getAudioDuration()/10000) + 500;
    }

    private void setVoicePrefs(String voice, SlashCommandInteractionEvent event) {
        if (voiceFromString(voice) != null) {
            userVoicePrefs.put(event.getUser().getIdLong(), voiceFromString(voice));
            event.reply("voice set to " + voiceFromString(voice)).setEphemeral(true).queue();
            SaveLoad.save(this.getClass(), userVoicePrefs);
            log.info(event.getUser().getName() + " set voice to " + voiceFromString(voice));
        } else {
            event.reply("couldnt find a voice with that name").setEphemeral(true).queue();
        }
    }

    private void listVoices(SlashCommandInteractionEvent event) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Voice options");
        eb.setColor(Color.BLUE);
        eb.addField("English", "Ava\nAndrew\nEmma\nBrian\nJenny\nAmber\nAna\nAshley\nEric\nRoger\nBlue\nNeurosama", false);
        eb.addField("Japanese", "Nanami_jp\nKeita_jp", false);
        eb.addField("Spanish", "Elivra_es\nDario_es", false);
        eb.addField("Portuguese", "Francisca_pt\nAntonio_pt", false);
        eb.addField("French", "Denise_fr\nHenri_fr", false);
        eb.addField("Br*tish", "Sonia_gb", false);
        eb.addField("Catalan", "Joana_ca\nEnric_ca", false);
        eb.setFooter("voice names are case insensitive");
        event.replyEmbeds(eb.build()).setEphemeral(true).queue();
    }

    /**
     * attempts to connect to the voice channel the user is in
     * @param event
     * @return audiosendhandler
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

    private long ttsRun(TTSQueueMember ttsQueueMember) {
        if (ttsQueueMember == null)
            return 0;
        
        if (ttsQueueMember.getVoice() == null)
            return tts(ttsQueueMember.getText(), ttsQueueMember.getEvent());
        else
            return tts(ttsQueueMember.getText(), ttsQueueMember.getVoice(), ttsQueueMember.getEvent());
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
        NANAMI_JP("ja-JP-NanamiNeural"),
        KEITA_JP("ja-JP-KeitaNeural"),
        ELVIRA_ES("es-ES-ElviraNeural"),
        DARIO_ES("es-ES-DarioNeural"),
        FRANCISCA_PT("pt-BR-FranciscaNeural"),
        ANTONIO_PT("pt-BR-AntonioNeural"),
        DENISE_FR("fr-FR-DeniseNeural"),
        HENRI_FR("fr-FR-HenriNeural"),
        SONIA_GB("en-GB-SoniaNeural"),
        JOANA_CA("ca-ES-JoanaNeural"),
        ENRIC_CA("ca-ES-EnricNeural");

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
