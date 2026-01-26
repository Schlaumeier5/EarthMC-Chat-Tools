package de.schlaumeier;

import ai.onnxruntime.*;
import de.schlaumeier.config.EMCChatConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

public class EMCChatToolsClient implements ClientModInitializer {
    private static EMCChatToolsClient instance;
    public static EMCChatToolsClient getInstance() {
        return instance;
    }
    private EMCChatSettings settings;
    public EMCChatSettings getSettings() {
        return settings;
    }
    private String pendingIllegalMessage = null;
    private String pendingIllegalLabel = null;
    private boolean sendingUnsafe;
    private boolean loaded;

    private OrtEnvironment env;
    private OrtSession safetySession;
    private OrtSession communitySession;

    private static final float SAFETY_THRESHOLD = 0.70f;
    private static final float COMMUNITY_THRESHOLD = 0.70f;

    private static final Map<Integer, String> SAFETY_LABELS = Map.of(
        0, "other_safe",
        1, "self_harm",
        2, "explicit_bullying",
        3, "explicit_sexual_talk",
        4, "hate_speech"
    );

    private static final Map<Integer, String> COMMUNITY_LABELS = Map.of(
        0, "other",
        1, "help_ask",
        2, "legal_ad"
    );

    @Override
    public void onInitializeClient() {
        instance = this;
        env = OrtEnvironment.getEnvironment();
        //ClientReceiveMessageEvents.ALLOW_CHAT.register(this::onChatReceive);
        ClientSendMessageEvents.ALLOW_CHAT.register(this::onChatSend);
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!loaded) {
                loaded = true;
                try {
                    safetySession = loadONNXModel("models/safety_model.onnx");
                    communitySession = loadONNXModel("models/community_model.onnx");
                    System.out.println("ONNX models loaded successfully!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (pendingIllegalMessage != null) {
                Minecraft.getInstance().execute(() -> {
                    Minecraft.getInstance().setScreen(
                        new IllegalMessageConfirmScreen(pendingIllegalLabel, pendingIllegalMessage)
                    );
                    pendingIllegalLabel = null;
                    pendingIllegalMessage = null;
                });
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            JoinTracker.joinTime = System.currentTimeMillis();
        });
        AutoConfig.register(EMCChatConfig.class, GsonConfigSerializer::new);
        settings = new EMCChatSettings(AutoConfig.getConfigHolder(EMCChatConfig.class).getConfig());
    }

    /* ---------------- CHAT HOOKS ---------------- */

    public boolean onChatReceive(String message, @Nullable String sender, String chat, boolean isPrivate) {
        return message.length() < 7 || classifyAndNotify(message, sender, false, isPrivate);
    }

    private boolean onChatSend(String message) {
        try {
            Prediction safety = predict(safetySession, SAFETY_LABELS, message);

            if (!sendingUnsafe && !safety.label.equals("other_safe") && safety.score > SAFETY_THRESHOLD && settings.warnIllegal()) {
                openIllegalConfirmDialog(safety.label, message);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /* ---------------- CLASSIFICATION ---------------- */

    private boolean classifyAndNotify(String message, String player, boolean outgoing, boolean isPrivate) {
        try {
            Prediction safety = predict(safetySession, SAFETY_LABELS, message);

            // Illegal/Bad Behavior
            if (!safety.label.equals("other_safe") && safety.score > SAFETY_THRESHOLD && settings.displayAlerts()) {
                if (!settings.isHidden(safety.label)) {
                    notifyUser("SAFETY", safety);
                }

                if (settings.shouldPing(safety.label)) {
                    playPingSound();
                }
            }

            Prediction community = predict(communitySession, COMMUNITY_LABELS, message);

            // Community warnings (legal_ad, help_ask)
            if (!community.label.equals("other") && community.score > COMMUNITY_THRESHOLD && settings.displayAlerts()) {
                if (!settings.isHidden(community.label)) {
                    notifyUser("COMMUNITY", community);
                }
            }
            if (settings.shouldPing(player, message, community.label, isPrivate)) {
                playPingSound();
            }

            if (settings.isHidden(player, message, safety.label, isPrivate) || settings.isHidden(player, message, community.label, isPrivate)) {
                sendBlockedMessage(player + ": " + message + " §c(" + safety.label + ", " + community.label + ")");
                return false;
            }

            // Scammer check
            if (settings.displayScammerAlerts()) {
                for (String scammer : settings.getScammerPlayers()) {
                    if (scammer.equals(player)) {
                        notifyUser("SCAMMER", new Prediction("Scammer message detected", 1.0f));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /* ---------------- ONNX ---------------- */

    private Prediction predict(OrtSession session, Map<Integer, String> labels, String text)
            throws OrtException {

        OnnxTensor input = OnnxTensor.createTensor(env, new String[]{text});
        OrtSession.Result result = session.run(Collections.singletonMap("input", input));

        String[] resultText = (String[])(result.get(0).getValue());

        @SuppressWarnings("unchecked")
        OnnxMap scoreMap = ((List<OnnxMap>) (result.get(1).getValue())).get(0);

        /*
        int best = 0;
        for (int i = 1; i < logits[0].length; i++) {
            if (logits[0][i] > logits[0][best]) best = i;
        }

        return new Prediction(labels.get(best), logits[0][best]);
        */
       return new Prediction(resultText[0], (float)(scoreMap.getValue().get(resultText[0])));
    }

    /* ---------------- UTILS ---------------- */

    private OrtSession loadONNXModel(String path) throws Exception {
        InputStream is = Minecraft.getInstance().getResourceManager()
            .getResource(ResourceLocation.fromNamespaceAndPath("emcchattools", path))
            .get()
            .open();
        byte[] bytes = is.readAllBytes();

        return env.createSession(bytes);
    }

    private void sendBlockedMessage(String blocked) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().player == null) return;
            Minecraft.getInstance().player.displayClientMessage(
                Component.literal("§7§oBlocked message. Hover to see content").withStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(Component.literal(blocked)))), false);
        });
    }

    private void notifyUser(String type, Prediction p) {
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().player.displayClientMessage(
                Component.literal(
                    "§c[" + type + "] §f" + p.label + " §7(" +
                    String.format("%.2f", p.score) + ")"
                ),
                false
            );
        });
    }
    private void openIllegalConfirmDialog(String label, String message) {
        pendingIllegalMessage = message;
        pendingIllegalLabel = label;
    }


    private void playPingSound() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 1.0f);
        }
    }

    public void sendUnsafe(String message) {
        sendingUnsafe = true;
        Minecraft.getInstance().player.connection.sendChat(message);
        sendingUnsafe = false;
    }

    private record Prediction(String label, float score) {}
}