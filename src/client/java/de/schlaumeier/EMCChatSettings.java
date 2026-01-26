package de.schlaumeier;

import java.util.List;

import de.schlaumeier.config.EMCChatConfig;

public class EMCChatSettings {
    private final EMCChatConfig config;

    public EMCChatSettings(EMCChatConfig config) {
        this.config = config;
    }

    public boolean isHidden(String label) {
        if (label.equals("explicit_bullying")) {
            return config.hideExplicitBullying;
        } else if (label.equals("self_harm")) {
            return config.hideSelfHarm;
        } else if (label.equals("explicit_sexual_talk")) {
            return config.hideSexualContent;
        } else if (label.equals("hate_speech")) {
            return config.hideHateSpeech;
        } else if (label.equals("help_ask")) {
            return config.hideHelpAsks;
        } else if (label.equals("legal_ad")) {
            return config.hideLegalAds;
        } else {
            return false;
        }
    }
    public boolean isHidden(String user, String text, String label, boolean isPrivate) {
        if (isPrivate && config.hideScammerMessages) {
            for (String scammer : getScammerPlayers()) {
                if (scammer.equals(user)) {
                    return true;
                }
            }
        }
        return isHidden(label);
    }

    public boolean shouldPing(String label) {
        if (label.equals("explicit_bullying") || label.equals("self_harm")
                || label.equals("explicit_sexual_talk") || label.equals("hate_speech")) {
            return config.playPingOnSafety;
        } else if (label.equals("help_ask")) {
            return config.playPingOnHelpAsk;
        } else {
            return false;
        }
    }

    public boolean shouldPing(String username, String text, String label, boolean isPrivate) {
        if (label.equals("explicit_bullying") || label.equals("self_harm")
                || label.equals("explicit_sexual_talk") || label.equals("hate_speech")) {
            return config.playPingOnSafety;
        } else if (label.equals("help_ask")) {
            return config.playPingOnHelpAsk;
        } else if (label.equals("legal_ad") && config.playPingOnScammerAd) {
            for (String scammer : getScammerPlayers()) {
                if (scammer != null && scammer.equals(username)) {
                    return true;
                }
            }
        } else if (isPrivate && config.playPingOnScammerMessage) {
            for (String scammer : getScammerPlayers()) {
                if (scammer != null && scammer.equals(username)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addScammer(String player) {
        config.scammerNames.add(player);
    }

    public void removeScammer(String player) {
        config.scammerNames.remove(player);
    }

    public boolean isScammer(String player) {
        return config.scammerNames.contains(player);
    }

    public List<String> getScammerPlayers() {
        return config.scammerNames;
    }

    public void setWarnIllegal(boolean warn) {
        config.enableSafetyWarnings = warn;
    }

    public boolean warnIllegal() {
        return config.enableSafetyWarnings;
    }

    public boolean displayAlerts() {
        return config.displayAlerts;
    }

    public boolean displayScammerAlerts() {
        return config.displayScammerAlerts;
    }

    public boolean displayVoteMessages() {
        return !config.hidePlayerVotes;
    }
}
