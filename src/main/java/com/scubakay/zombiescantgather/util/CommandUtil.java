package com.scubakay.zombiescantgather.util;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.loader.impl.util.StringUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import org.apache.commons.lang3.StringUtils;

public class CommandUtil {
    public static void reply(CommandContext<ServerCommandSource> context, String reply, Object... args) {
        String message = StringUtils.capitalize(String.format(reply, args));
        context.getSource().sendFeedback(() -> Text.literal(StringUtil.capitalize(message)), false);
    }

    public static void reply(CommandContext<ServerCommandSource> context, Text reply) {
        context.getSource().sendFeedback(() -> reply, false);
    }

    /**
     * Just a space but it resets the click/hover/color styles
     */
    public static MutableText getResetSpace() {
        return Text.literal(" ").styled(style -> Style.EMPTY);
    }

    /**
     * Unclickable style with tooltip
     */
    public static Style getTooltipStyle(Style style, Text tooltip) {
        //? >=1.21.5 {
        ClickEvent click = new ClickEvent.ChangePage(1);
        HoverEvent hover = new HoverEvent.ShowText(tooltip);
        //?} else {
        /*ClickEvent click = new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "1");
        HoverEvent hover =new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.empty());
        *///?}
        return style.withClickEvent(click)
                .withHoverEvent(hover);
    }
}