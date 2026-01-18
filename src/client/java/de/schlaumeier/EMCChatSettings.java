package de.schlaumeier;

import java.util.HashSet;
import java.util.Set;

public class EMCChatSettings {
    // Labels, deren Nachrichten nicht angezeigt werden
    private final Set<String> hiddenLabels = new HashSet<>();

    // Labels, deren Nachrichten einen Ton auslösen
    private final Set<String> pingLabels = new HashSet<>();

    // Spieler, die als Scammer markiert sind
    private final Set<String> scammerPlayers = new HashSet<>();

    // Toggle für illegale Nachrichtwarnungen
    private boolean warnIllegal = true;

    public EMCChatSettings() {
        // Default: hide nothing, ping self_harm and explicit_bullying
        pingLabels.add("self_harm");
        pingLabels.add("explicit_bullying");
    }

    public void hideLabel(String label) {
        hiddenLabels.add(label);
    }

    public void showLabel(String label) {
        hiddenLabels.remove(label);
    }

    public boolean isHidden(String label) {
        return hiddenLabels.contains(label);
    }

    public void addPingLabel(String label) {
        pingLabels.add(label);
    }

    public void removePingLabel(String label) {
        pingLabels.remove(label);
    }

    public boolean shouldPing(String label) {
        return pingLabels.contains(label);
    }

    public void addScammer(String player) {
        scammerPlayers.add(player);
    }

    public void removeScammer(String player) {
        scammerPlayers.remove(player);
    }

    public boolean isScammer(String player) {
        return scammerPlayers.contains(player);
    }

    public Set<String> getScammerPlayers() {
        return scammerPlayers;
    }

    public void setWarnIllegal(boolean warn) {
        warnIllegal = warn;
    }

    public boolean warnIllegal() {
        return warnIllegal;
    }
}
