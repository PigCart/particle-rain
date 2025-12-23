package pigcart.particlerain.config.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.network.chat.Component;
//? if >=1.21.11 {
/*import net.minecraft.client.gui.ActiveTextCollector;
*///?}

/// Based on the 1.20.1 version of [net.minecraft.client.gui.components.StringWidget], as the alignment functionality was later removed.
public class LabelWidget extends AbstractStringWidget {
    private float alignX;

    public LabelWidget(int width, int height, Component message) {
        super(0, 0, width, height, message, Minecraft.getInstance().font);
        this.alignX = 0.5F;
        this.active = false;
    }

    private LabelWidget horizontalAlignment(float horizontalAlignment) {
        this.alignX = horizontalAlignment;
        return this;
    }

    public LabelWidget alignLeft() {
        return this.horizontalAlignment(0.0F);
    }

    public LabelWidget alignRight() {
        return this.horizontalAlignment(1.0F);
    }

    //? if >=1.21.11 {
    /*public void visitLines(ActiveTextCollector activeTextCollector) {
        // abstract method unused due to overriding renderWidget
    }
    *///?}

    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Component component = this.getMessage();
        Font font = this.getFont();
        int x = this.getX() + Math.round(this.alignX * (float)(this.getWidth() - font.width(component)));
        int y = this.getY() + (getHeight() - 9) / 2;
        guiGraphics.drawString(font, component, x, y, 0xFFFFFFFF);
    }
}
