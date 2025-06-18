package com.scubakay.zombiescantgather.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Colors;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class CommandReply<C> {
    Function<C, MutableText> text;
    boolean suggestion;
    Function<ServerPlayerEntity, Boolean> requires;
    Function<C, Text> tooltip;
    Function<C, Boolean> clickable = x -> true;
    Function<C, String> command;
    Function<C, Integer> color;
    List<CommandReply<C>> buttons;
    boolean brackets = false;

    private CommandReply(Function<C, MutableText> text) {
        this.text = text;
    }

    public static <C> CommandReply<C> get(Function<C, MutableText> text) {
        return new CommandReply<>(text);
    }

    public static <C> MutableText getButtonRow(C context, List<CommandReply<C>> buttons) {
        AtomicReference<MutableText> rowButtons = new AtomicReference<>(null);
        buttons.forEach(button -> {
            MutableText existingButtons = rowButtons.get();
            existingButtons = existingButtons != null ? existingButtons.append(button.build(context)) : button.build(context);
            existingButtons.append(CommandUtil.getResetSpace());
            rowButtons.set(existingButtons);
        });
        return rowButtons.get() != null ? rowButtons.get() : Text.empty();
    }

    public CommandReply<C> withButtons(List<CommandReply<C>> buttons) {
        this.buttons = buttons;
        return this;
    }

    public CommandReply<C> requires(Function<ServerPlayerEntity, Boolean> requires) {
        this.requires = requires;
        return this;
    }

    public CommandReply<C> withToolTip(Function<C, Text> tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public CommandReply<C> withClickable(Function<C, Boolean> clickable) {
        this.clickable = clickable;
        return this;
    }

    public CommandReply<C> withCommand(Function<C, String> command) {
        this.command = command;
        return this;
    }

    public CommandReply<C> withSuggestion(Function<C, String> command) {
        this.command = command;
        this.suggestion = true;
        return this;
    }

    public CommandReply<C> withColor(Function<C, Integer> color) {
        this.color = color;
        return this;
    }

    public CommandReply<C> withBrackets() {
        this.brackets = true;
        return this;
    }

    public MutableText build(C context) {
        // Get buttons and add brackets if needed
        MutableText buttons = this.buttons != null ? getButtonRow(context, this.buttons) : Text.empty();
        MutableText reply = brackets ?
                Text.literal("[").append(this.text.apply(context)).append(Text.literal("]"))
                : this.text.apply(context);

        // Get empty style with whatever we need
        Style style = Style.EMPTY.withColor(this.color != null ? this.color.apply(context) : Colors.WHITE);
        boolean clickable = this.clickable != null ? this.clickable.apply(context) : false;
        style = styleWithCommand(style, this.command != null && clickable ? this.command.apply(context) : null, this.suggestion);
        style = styleWithTooltip(style, this.tooltip != null ? this.tooltip.apply(context) : Text.empty());
        Style finalStyle = style;

        // Append reply to buttons
        return buttons.append(reply.styled(s -> finalStyle));
    }

    public void display(CommandContext<ServerCommandSource> source, C context) {
        CommandUtil.reply(source, build(context));
    }

    private static Style styleWithTooltip(Style style, @Nullable Text tooltip) {
        //? >=1.21.5 {
        return style.withHoverEvent(new HoverEvent.ShowText(tooltip != null ? tooltip : Text.empty()));
        //?} else {
        /*return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip != null ? tooltip : Text.empty()));
        *///?}
    }

    private static Style styleWithCommand(Style style, String command, boolean suggestion) {
        //? >=1.21.5 {
        if (command == null) {
            return style.withClickEvent(new ClickEvent.ChangePage(1));
        } else {
            return style.withClickEvent(suggestion ? new ClickEvent.SuggestCommand(command) : new ClickEvent.RunCommand(command));
        }
        //?} else {
        /*if (command == null) {
            return style.withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "1"));
        } else {
            return style.withClickEvent(suggestion ? new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command) : new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        }
         *///?}
    }
}
