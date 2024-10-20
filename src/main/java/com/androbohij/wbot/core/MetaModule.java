package com.androbohij.wbot.core;

/**
 * This interface exists to mark any modules that require a reference back to Wbot
 */
public interface MetaModule {
    public void setWbot(Wbot wbot);
}