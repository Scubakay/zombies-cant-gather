package com.scubakay.zombiescantgather.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

public class CommandPagination {
    public static final String PAGE_COMMAND = "page";

    public final int elementCount;
    int pageCount;
    public int fromIndex;
    public int toIndex;
    int pageSize;
    int currentPage = 1;

    public CommandPagination(CommandContext<ServerCommandSource> context, int elementCount, int pageSize) {
        try {
            this.currentPage = IntegerArgumentType.getInteger(context, "page");
        } catch (Exception ignored) {
        }

        this.elementCount = elementCount;
        this.pageSize = pageSize;
        this.pageCount = elementCount > 0 ? (elementCount - 1) / this.pageSize + 1 : 0;
        this.fromIndex = Math.min(pageCount, currentPage - 1) * this.pageSize;
        this.toIndex = Math.min(elementCount, fromIndex + this.pageSize);
    }

    public static LiteralArgumentBuilder<ServerCommandSource> getPageCommand(Command<ServerCommandSource> command) {
        return CommandManager
                .literal(PAGE_COMMAND)
                .then(CommandManager
                        .argument(PAGE_COMMAND, IntegerArgumentType.integer(1))
                        .executes(command));
    }

    public Text getPagination(String command, String emptyMessage) {
        if (this.pageCount == 0) {
            return Text.literal(emptyMessage)
                    .styled(style -> style
                            .withFormatting(Formatting.GREEN));
        } else if (this.pageCount == 1) {
            return Text.empty();
        }
        return Text.literal("<< ")
                .styled(style -> getPageLinkStyle(style, command, this.currentPage > 1, "First page", 1))
                .append(Text.literal("< ")
                        .styled(style -> getPageLinkStyle(style, command, this.currentPage > 1, "Previous page", this.currentPage - 1)))
                .append(Text.literal(this.currentPage + " / " + this.pageCount)
                        .styled(style -> style
                                .withColor(Colors.WHITE)
                                //? >=1.21.5 {
                                .withClickEvent(new ClickEvent.ChangePage(1))
                                .withHoverEvent(new HoverEvent.ShowText(Text.literal(this.currentPage + "/" + this.pageCount)))))
                //?} else {
                /*.withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "1"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(this.currentPage + "/" + this.pageCount)))))
        *///?}
                .append(Text.literal(" >")
                        .styled(style -> getPageLinkStyle(style, command, this.currentPage < this.pageCount, "Next page", this.currentPage + 1)))
                .append(Text.literal(" >>")
                        .styled(style -> getPageLinkStyle(style, command, this.currentPage < this.pageCount, "Last page", this.pageCount)));
    }

    private static String getPageLink(String command, int page) {
        return String.format("%s page %s", command, page);
    }

    private static Style getPageLinkStyle(Style style, String command, boolean clickable, String tooltip, int page) {
        style = style.withColor(clickable ? Colors.GREEN : Colors.GRAY);
        if (clickable) {
            //? >=1.21.5 {
            style = style.withClickEvent(new ClickEvent.RunCommand(getPageLink(command, page)))
                    .withHoverEvent(new HoverEvent.ShowText(Text.literal(tooltip)));
            //?} else {
            /*style = style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getPageLink(command, page)))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(tooltip)));
            *///?}
        } else {
            //? >=1.21.5 {
            style = style.withClickEvent(new ClickEvent.ChangePage(1))
                    .withHoverEvent(new HoverEvent.ShowText(Text.literal(tooltip)));
            //?} else {
            /*style = style.withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "1"))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(tooltip)));
            *///?}
        }
        return style;
    }

    @Override
    public String toString() {
        return String.format("CommandPagination{currentPage=%s, pageCount=%s, fromIndex=%s, toIndex=%s, pageSize=%s}",
                currentPage, pageCount, fromIndex, toIndex, pageSize);
    }
}
