package de.schlaumeier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class IllegalMessageConfirmScreen extends Screen {

    private final String messageToSend;
    private final String label;

    protected IllegalMessageConfirmScreen(String label, String messageToSend) {
        super(Component.literal("Illegal Message Warning"));
        this.label = label;
        this.messageToSend = messageToSend;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Confirm button
        this.addRenderableWidget(Button.builder(
                Component.literal("§cSend anyway"),
                btn -> {
                    EMCChatToolsClient.getInstance().sendUnsafe(messageToSend);
                    Minecraft.getInstance().setScreen(null);
                }
        ).bounds(centerX - 110, centerY + 20, 100, 20).build());

        // Cancel button
        this.addRenderableWidget(Button.builder(
                Component.literal("Cancel"),
                btn -> Minecraft.getInstance().setScreen(null)
        ).bounds(centerX + 10, centerY + 20, 100, 20).build());
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(
                this.font,
                "⚠ Potentially illegal message detected",
                this.width / 2,
                this.height / 2 - 40,
                0xFFFF5555
        );

        graphics.drawCenteredString(
                this.font,
                "Category: " + label,
                this.width / 2,
                this.height / 2 - 25,
                0xFFFFFFFF
        );

        graphics.drawCenteredString(
                this.font,
                "\"" + messageToSend + "\"",
                this.width / 2,
                this.height / 2 - 5,
                0xFFAAAAAA
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
