package com.scubakay.zombiescantgather.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

public class CommandPagination {
    public static final String PAGE_COMMAND = "page";

    private final String command;
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
        command = context.getInput().split(" " + PAGE_COMMAND)[0];

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

    public Text getPagination(String emptyMessage) {
        if (this.pageCount == 0) {
            return Text.literal(emptyMessage)
                    .styled(style -> style
                            .withFormatting(Formatting.GREEN));
        } else if (this.pageCount == 1) {
            return Text.empty();
        }
        return Text.literal(" ------- ").withColor(Colors.LIGHT_GRAY)
                .append(Text.literal("<< ")
                        .styled(style -> getPageLinkStyle(style, this.currentPage > 1, "First page", 1)))
                .append(Text.literal("< ")
                        .styled(style -> getPageLinkStyle(style, this.currentPage > 1, "Previous page", this.currentPage - 1)))
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
                        .styled(style -> getPageLinkStyle(style, this.currentPage < this.pageCount, "Next page", this.currentPage + 1)))
                .append(Text.literal(" >>")
                        .styled(style -> getPageLinkStyle(style, this.currentPage < this.pageCount, "Last page", this.pageCount)))
                .append(Text.literal(" ------- ").withColor(Colors.LIGHT_GRAY));
    }

    public MutableText getRefreshButton() {
        return Text.literal("[Refresh]").styled(style ->
                getPageLinkStyle(style, true, "Refresh page", this.currentPage)
                        .withColor(Colors.YELLOW));
    }

    private String getPageLink(int page) {
        return String.format("/%s page %s", this.command, page);
    }

    private Style getPageLinkStyle(Style style, boolean clickable, String tooltip, int page) {
        //? >=1.21.5 {
        ClickEvent click = clickable ? new ClickEvent.RunCommand(getPageLink(page)) : new ClickEvent.ChangePage(1);
        HoverEvent hover = new HoverEvent.ShowText(Text.literal(tooltip));
        //?} else {
        /*ClickEvent click = clickable ? new ClickEvent(ClickEvent.Action.RUN_COMMAND, getPageLink(page)) : new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "1");
        HoverEvent hover =new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(tooltip));
        *///?}
        return style.withColor(clickable ? Colors.GREEN : Colors.GRAY)
                .withClickEvent(click)
                .withHoverEvent(hover);
    }

    @Override
    public String toString() {
        return String.format("CommandPagination{currentPage=%s, pageCount=%s, fromIndex=%s, toIndex=%s, pageSize=%s}",
                currentPage, pageCount, fromIndex, toIndex, pageSize);
    }
}
