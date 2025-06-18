package com.scubakay.zombiescantgather.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class CommandButton<C> {
    Function<C, Text> text;
    boolean suggestion;
    Function<ServerPlayerEntity, Boolean> requires;
    Function<C, Text> tooltip;
    Function<C, Boolean> clickable = x -> true;
    Function<C, String> command;
    Function<C, Integer> color;
    boolean brackets = false;

    private CommandButton(Function<C, Text> text) {
        this.text = text;
    }

    public static <C> CommandButton<C> run(Function<C, Text> text) {
        return new CommandButton<>(text);
    }

    public static <C> CommandButton<C> suggest(Function<C, Text> text) {
        CommandButton<C> button = new CommandButton<>(text);
        button.suggestion = true;
        return button;
    }

//    public static <C> MutableText getButtonRow(List<CommandButton<C>> buttons) {
//        return getButtonRow(null, buttons);
//    }

    public static <C> MutableText getButtonRow(C context, List<CommandButton<C>> buttons) {
        AtomicReference<MutableText> rowButtons = new AtomicReference<>(null);
        buttons.forEach(button -> {
            MutableText existingButtons = rowButtons.get();
            if (existingButtons != null) {
                existingButtons = existingButtons.append(button.build(context));
            } else {
                existingButtons = button.build(context);
            }
            existingButtons.append(CommandUtil.getResetSpace());
            rowButtons.set(existingButtons);
        });
        return rowButtons.get();
    }

    public CommandButton<C> requires(Function<ServerPlayerEntity, Boolean> requires) {
        this.requires = requires;
        return this;
    }

    public CommandButton<C> withToolTip(Function<C, Text> tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public CommandButton<C> withClickable(Function<C, Boolean> clickable) {
        this.clickable = clickable;
        return this;
    }

    public CommandButton<C> withCommand(Function<C, String> command) {
        this.command = command;
        return this;
    }

    public CommandButton<C> withColor(Function<C, Integer> color) {
        this.color = color;
        return this;
    }

    public CommandButton<C> withBrackets() {
        this.brackets = true;
        return this;
    }

    public MutableText build(C item) {
        MutableText button = brackets ? Text.literal("[") : Text.empty();
        button = button.copy().append(this.text.apply(item));
        button = brackets ? button.copy().append(Text.literal("]")) : button;
        return button.styled(style -> clickable.apply(item) ? getButtonStyle(
                        style,
                        this.tooltip.apply(item),
                        this.command.apply(item),
                        this.suggestion
                ) : CommandUtil.getResetStyle(style))
                .withColor(this.color.apply(item));
    }

    private static Style getButtonStyle(Style style, Text tooltip, String command, boolean suggestion) {
        //? >=1.21.5 {
        ClickEvent click = suggestion ? new ClickEvent.SuggestCommand(command) : new ClickEvent.RunCommand(command);
        HoverEvent hover = new HoverEvent.ShowText(tooltip);
        //?} else {
        /*ClickEvent click = suggestion ? new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command) : new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
        HoverEvent hover =new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip);
        *///?}
        return style.withClickEvent(click)
                .withHoverEvent(hover);
    }
}
