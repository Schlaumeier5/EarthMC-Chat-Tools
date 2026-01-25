package de.schlaumeier.mixin.client;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.schlaumeier.EMCChatToolsClient;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {
    private static Optional<String> extractUsername(Component text) {
        return text.visit((style, string) -> {
            ClickEvent click = style.getClickEvent();
            if (click == null) return Optional.empty();

            if (click.action() == ClickEvent.Action.SUGGEST_COMMAND) {
                String cmd = ((ClickEvent.SuggestCommand)click).command();
                if (cmd.startsWith("/msg ")) {
                    String[] parts = cmd.split(" ");
                    if (parts.length >= 2) {
                        return Optional.of(parts[1]);
                    }
                }
            }

            return Optional.empty();
        }, text.getStyle());
        
    }


    @Inject(
        method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void emcchattools$onAddMessage(Component content, @Nullable MessageSignature messageSignature, @Nullable GuiMessageTag messageTag, CallbackInfo ci) {
        String message = content.getString();
        String username;
        String chat = null;
        boolean isPrivate = false;
        if (message.contains(" -> ") && message.contains("[") && message.contains("me]")) {
            username = message.substring(1).split(" \\->", 2)[0];
            isPrivate = true;
        } else {
            Optional<String> maybeUsername = extractUsername(content);
            if (maybeUsername.isEmpty()) username = null;
            else username = maybeUsername.get();
        }
        System.out.println("Username: " + username);
        if (isPrivate){
            chat = "private";
        } else if (message.split(" ", 2)[1].startsWith("[Discord]")) {
            chat = "discord";
        } else if (message.startsWith("[")) {
            chat = message.substring(1).split("\\]", 2)[0].toLowerCase();
            if (chat.contains(":")) { // Timestamp, added by some mods
                message = message.split("\\]", 2)[1].substring(1);
                chat = message.substring(1).split("\\]", 2)[0].toLowerCase();
            }
            if (message.startsWith("(Filtered) ")) {
                message = message.substring(11);
                chat = message.substring(1).split("\\]", 2)[0].toLowerCase();
            }
            while (message.startsWith("+") || Character.isEmoji(message.charAt(0))) {
                message = message.substring(1).strip();
                chat = message.substring(1).split("\\]", 2)[0].toLowerCase();
            }
            if (chat.contains("|")) chat = "global";
            if (chat == "discord") {
                chat = "staff-discord";
                message = message.split("\\]", 2)[1];
            }
        } else {
            return;
        }
        message = message.split("\\]", 2)[1];
        if (!message.contains(": ") || (username == null && chat != "discord" && chat != "staff-discord")) return;
        message = message.split(": ", 2)[1];
        chat = chat.replace(" ", "-");
        if (!EMCChatToolsClient.getInstance().onChatReceive(message, username, chat, isPrivate)) {
            ci.cancel();
        }
    }
}
