//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package pigcart.particlerain.config.gui;

import com.google.common.collect.ImmutableList;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public class WidgetList extends ContainerObjectSelectionList<WidgetList.Row> {

    public WidgetList(Minecraft minecraft, int width, int height, int topY, int bottomY, int itemHeight) {
        super(minecraft, width, height, topY,/*? <=1.20.1 {*/bottomY,/*?}*/ itemHeight);
        this.centerListVertically = false;
    }

    public void addRow(AbstractWidget... widgets) {
        this.addEntry(new Row(widgets));
    }

    public int getRowWidth() {
        return 310;
    }

    //? <=1.20.1 {
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 32;
    }
    //?}

    protected static class Row extends ContainerObjectSelectionList.Entry<Row> {
        private final List<AbstractWidget> widgets;

        Row(AbstractWidget... widgets) {
            this.widgets = ImmutableList.copyOf(widgets);
        }

        //? >=1.21.9 {
        /*public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
			renderEntryWidgets(guiGraphics, this.getContentY(), mouseX, mouseY, partialTick);
        }
        *///?} else {
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            renderEntryWidgets(guiGraphics, top, mouseX, mouseY, partialTick);
        }
        //?}

        private void renderEntryWidgets(GuiGraphics guiGraphics, int contentY, int mouseX, int mouseY, float partialTick) {
            int padding = Minecraft.getInstance().screen.width / 2 - 155;
            for (AbstractWidget widget : this.widgets) {
                widget.setY(contentY);
                widget.setX(((AbstractWidgetAccess)widget).pigcart$getOffset() + padding);
                widget.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        public List<? extends GuiEventListener> children() {
            return this.widgets;
        }

        public List<? extends NarratableEntry> narratables() {
            return this.widgets;
        }
    }
}
