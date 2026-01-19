package de.schlaumeier.config;

import java.util.ArrayList;
import java.util.List;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "emcchattools")
public class EMCChatConfig implements ConfigData {
    public boolean enableSafetyWarnings = true;
    public boolean playPingOnSafety = false;
    public boolean playPingOnHelpAsk = false;
    public boolean playPingOnScammerAd = true;
    public boolean playPingOnScammerMessage = false;

    public boolean hideExplicitBullying = false;
    public boolean hideSexualContent = true;
    public boolean hideHateSpeech = false;
    public boolean hideSelfHarm = true;

    public boolean hideLegalAds = true;
    public boolean hideHelpAsks = false;

    public boolean hideScammerMessages = false;

    public float safetyThreshold = 0.70f;

    public List<String> scammerNames = new ArrayList<>();

    public boolean displayAlerts = false;
    public boolean displayScammerAlerts = true;
}
