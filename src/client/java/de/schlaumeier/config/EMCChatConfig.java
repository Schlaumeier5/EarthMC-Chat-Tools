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

    public boolean hidePlayerVotes = false;

    public float safetyThreshold = 0.70f;

    public List<String> scammerNames = new ArrayList<>(List.of(
        "Panth_r", "Near_r", "LakelessLands", "Dishonesty", "Apportionment", "santosmeow", "Allags", "Skaless", "MalMeteor", "N0vat00n", "Taha_AbuObaida", "fruitapples", "Latvijaa", "Cavify", "josamshaa", "Daniel12615", "Zimin___________",
        "ItzGnnn", "hielluosymp", "Hashbrown_922", "GabrielCamelot", "Turingly", "Esvicks", "Jteller", "0P2W", "3P2W", "MustangCricket", "aerdisx", "_Duckybara_", "MarkCarney", "chillager_", "RealYoPick1", "VictorrOsimhen",
        "CornBall_03", "hamza921", "Nf_miracle", "EfeSultan", "TheRealPotatoGuy", "BsidesThat", "TheRealTomatoGuy", "JJMonster5259", "Bigfwuent", "promaniscool", "PolarEMC", "Honduras", "kxretta", "4O4N0TFOUND", "KyOt0",
        "Continentalia", "needlessrelic", "7Goy", "papayov", "ThundersGoodGirl", "Radical16625", "loukasFYD"
    ));

    public boolean displayAlerts = false;
    public boolean displayScammerAlerts = true;
}
