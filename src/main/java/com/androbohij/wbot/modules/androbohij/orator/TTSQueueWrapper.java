package com.androbohij.wbot.modules.androbohij.orator;

import java.util.concurrent.LinkedBlockingQueue;

public class TTSQueueWrapper {
    
    private final LinkedBlockingQueue<TTSQueueMember> queue;

    TTSQueueWrapper() {
        queue = new LinkedBlockingQueue<>();
    }

    public TTSQueueMember peek() {
        return queue.peek();
    }

    public TTSQueueMember poll() {
        return queue.poll();
    }

    public void add(TTSQueueMember ttsQueueMember) {
        queue.add(ttsQueueMember);
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void clear() {
        queue.clear();
    }

}