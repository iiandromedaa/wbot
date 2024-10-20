package com.androbohij.wbot.modules.androbohij.orator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import net.dv8tion.jda.api.audio.AudioSendHandler;

public class IntermediaryHandler implements AudioSendHandler {

    byte[] remainder;
    private final Queue<byte[]> queue = new ConcurrentLinkedQueue<>();

    @Override
    public boolean canProvide() {
        return !queue.isEmpty();
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        byte[] data = queue.poll();
        return data == null ? null : ByteBuffer.wrap(data);
    }

    /**
     * i dont fully understand this math, i found another repo that does the audio byte magic so,, yoink
     * @param audio
     */
    public void send(byte[] audio) {
        int size = INPUT_FORMAT.getFrameSize() * (int) (INPUT_FORMAT.getFrameRate() * 0.02);

        if (remainder != null) {
            byte[] bytes = new byte[remainder.length + audio.length];
            System.arraycopy(remainder, 0, bytes, 0, remainder.length);
            System.arraycopy(audio, 0, bytes, remainder.length, audio.length);
            audio = bytes;
            remainder = null;
        }

        for (int i = 0; i < audio.length; i += size) {
            int end = Math.min(i + size, audio.length);

            if (end - i < size) {
                remainder = Arrays.copyOfRange(audio, i, end);
                break;
            }
            
            byte[] chunk = Arrays.copyOfRange(audio, i, end);
            queue.offer(chunk);
        }
    }

    public byte[] convert(byte[] audioData, AudioFormat sourceFormat, AudioFormat targetFormat) {
        try {
            // Create an AudioInputStream from the byte array
            AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(audioData), sourceFormat, audioData.length);

            // Create an AudioInputStream for the converted audio data
            AudioInputStream convertedAis = AudioSystem.getAudioInputStream(targetFormat, ais);

            // Create a ByteArrayOutputStream and write the converted audio data to it
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = convertedAis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            // Return the converted audio data
            return baos.toByteArray();

        } catch (IOException e) {
            return null;
        }
    }
    
}