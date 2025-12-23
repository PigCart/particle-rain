package pigcart.particlerain.config.gui;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import pigcart.particlerain.ParticleRain;
import pigcart.particlerain.config.ConfigManager;
//? <=1.20.1 {
import net.minecraft.client.gui.GuiGraphics;
//?}

import java.lang.reflect.Field;

public class ConfigScreen extends Screen {
    final Object config;
    final Object configDefault;
    final Class<?> configGenericType;
    WidgetList list;
    protected final Screen lastScreen;
    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    public ConfigScreen(Screen lastScreen, Object config, Object configDefault, Class<?> configGenericType, Component title) {
        super(title);
        this.lastScreen = lastScreen;
        this.config = config;
        this.configDefault = configDefault;
        this.configGenericType = configGenericType;
    }
    public ConfigScreen(Screen lastScreen, Object config, Object configDefault, Component title) {
        super(title);
        this.lastScreen = lastScreen;
        this.config = config;
        this.configDefault = configDefault;
        this.configGenericType = null;
    }

    @Override
    protected void init() {
        addTitle();
        addContents();
        addFooter();

        //? >=1.21.1 {
        /*repositionElements();
        *///?} else {
        layout.arrangeElements();
        //?}
        layout.visitWidgets(this::addRenderableWidget);
    }

    protected void addTitle() {
        layout.addToHeader(new StringWidget(title, font));
    }

    protected void addContents() {
        list = new WidgetList(minecraft, width,
                /*? >=1.21.1 {*//*layout.getContentHeight()*//*?} else {*/height/*?}*/,
                32, height - 32, 25);
        Widgets.addOptionWidgets(this);

        //? <=1.20.1 {
        addWidget(list);
        //?} else {
        /*layout.addToContents(list);
         *///?}
    }

    protected void addFooter() {
        GridLayout.RowHelper row = layout.addToFooter(new GridLayout().columnSpacing(8)).createRowHelper(2);

        final Button resetButton = Button.builder(
                Component.translatable("controls.reset").append(" ").append(this.title),
                (button) -> {
                    ParticleRain.LOGGER.info("Reset config");
                    for (Field field : this.config.getClass().getFields()) {
                        try {
                            field.setAccessible(true);
                            field.set(config, field.get(configDefault));
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    refresh();
                }
        ).build();

        final Button doneButton = Button.builder(
                CommonComponents.GUI_DONE,
                (button) -> this.onClose()
        ).build();

        row.addChild(resetButton);
        row.addChild(doneButton);
    }

    //? >=1.21.1 {
    /*@Override
    protected void repositionElements() {
        layout.arrangeElements();
        if (list != null) list.updateSize(width, layout);
    }
    *///?}

    //? <=1.20.1 {
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        list.render(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    //?}

    @Override
    public void removed() {
        ConfigManager.save();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }

    public Screen getFreshScreen() {
        return new ConfigScreen(this.lastScreen, this.config, this.configDefault, this.configGenericType, this.title);
    }

    public void refresh() {
        final ConfigScreen freshScreen = new ConfigScreen(this.lastScreen, this.config, this.configDefault, this.configGenericType, this.title);
        minecraft.setScreen(freshScreen);
        freshScreen.list.setScrollAmount(
                //? >=1.21.4 {
                /*this.list.scrollAmount()
                *///?} else {
                this.list.getScrollAmount()
                 //?}
        );
    }

    public void add(AbstractWidget... widgets) {
        for (AbstractWidget widget : widgets) {
            widget.setX((width / 2 - 155) + ((AbstractWidgetAccess)widget).particle_rain$getOffset());
        }
        list.add(widgets);
    }
}