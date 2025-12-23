package pigcart.particlerain.config.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

public class InputWidget extends EditBox {

    String unformattedValue;
    Function<Object, Component> valueFormatter;

    Pattern filteredChars;
    /// matches characters that aren't digits
    public static final Pattern NON_INTEGER = Pattern.compile("[^0-9]");
    /// matches characters that aren't digits or points
    public static final Pattern NON_FLOAT = Pattern.compile("[^0-9.]");
    /// matches characters that aren't valid in an identifier
    public static final Pattern NON_PATH = Pattern.compile("[^a-z0-9:/._-]");

    public InputWidget(int width, int x, String initialValue, Consumer<String> onValueChange, Function<Object, Component> valueFormatter) {
        super(Minecraft.getInstance().font, 0, 0, width, Widgets.BUTTON_HEIGHT, Component.empty());
        ((AbstractWidgetAccess)this).particle_rain$setOffset(x);
        unformattedValue = initialValue;
        this.valueFormatter = valueFormatter;
        this.setResponder((value) -> {
            if (!this.isFocused()) return; // when unfocused the value is formatted and should be ignored
            unformattedValue = value;
            try {
                onValueChange.accept(value);
                this.setTextColor(EditBox.DEFAULT_TEXT_COLOR);
            } catch (NumberFormatException ignored) {
                // keep old value if input invalid
                this.setTextColor(0xFFFF5555); // equivalent of ChatFormatting.RED. modern mc needs alpha specified
            }
        });
        format();
    }

    /*@Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isHovered) setFocused(false);
        return super.mouseClicked(mouseX, mouseY, button);

    }*/

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        format();
    }

    public void format() {
        if (this.isFocused()) {
            forceSetValue(unformattedValue);
        } else {
            forceSetValue(valueFormatter.apply(unformattedValue).getString());
        }
    }

    /// Sets the displayed value without validation
    public void forceSetValue(String value) {
        // AW'd because @Accesor requires method name 'setValue' that conflicts with the base game
        this.value = value;
        //? >=1.21.1 {
        /*moveCursorToEnd(false);
        *///?} else {
        moveCursorToEnd();
        //?}
        setHighlightPos(getCursorPosition());
    }

    /// Sets the filter used by [EditBox]
    /// @param pattern Regex describing characters to be omitted from input
    public void setFilter(Pattern pattern) {
        filteredChars = pattern;
        if (filteredChars != null) {
            this.setFilter((string) -> !filteredChars.matcher(string).find());
        } else {
            this.setFilter(Objects::nonNull);
        }
    }
}
