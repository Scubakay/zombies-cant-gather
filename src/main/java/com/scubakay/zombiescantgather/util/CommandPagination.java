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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CommandPagination<C, D extends List<C>> {
    public static final String PAGE_COMMAND = "page";
    private static final int DEFAULT_PAGE_SIZE = 10;

    public record Context(
            int pageCount,
            int fromIndex,
            int toIndex,
            int pageSize,
            int currentPage,
            int elementCount
    ) {
    }

    private Context context;
    private String command;
    private final CommandContext<ServerCommandSource> source;
    private final D list;

    private Text header;
    private List<MutableText> rows = new ArrayList<>();
    private Text emptyListMessage = Text.literal("List is empty");
    private final List<CommandReply<D>> buttons = new ArrayList<>();

    private CommandPagination(CommandContext<ServerCommandSource> source, D list) {
        this.source = source;
        this.list = list;

        // Get command information
        command = "/" + source.getInput().split(" " + PAGE_COMMAND)[0];
        int currentPage = 1;
        try {
            currentPage = IntegerArgumentType.getInteger(source, "page");
        } catch (Exception ignored) {
        }

        // Calculate parameters
        int elementCount = this.list.size();
        int pageCount = elementCount > 0 ? (elementCount - 1) / DEFAULT_PAGE_SIZE + 1 : 0;
        int fromIndex = Math.min(pageCount, currentPage - 1) * DEFAULT_PAGE_SIZE;
        int toIndex = Math.min(elementCount, fromIndex + DEFAULT_PAGE_SIZE);
        this.context = new Context(pageCount, fromIndex, toIndex, DEFAULT_PAGE_SIZE, currentPage, elementCount);
    }

    public static <C, D extends List<C>> CommandPagination<C, D> builder(CommandContext<ServerCommandSource> context, D list) {
        return new CommandPagination<>(context, list);
    }

    public static LiteralArgumentBuilder<ServerCommandSource> getPageCommand(Command<ServerCommandSource> command) {
        return CommandManager
                .literal(PAGE_COMMAND)
                .then(CommandManager
                        .argument(PAGE_COMMAND, IntegerArgumentType.integer(1))
                        .executes(command));
    }

    public CommandPagination<C, D> withPageSize(int pageSize) {
        this.context = new Context(
                this.context.pageCount(),
                this.context.fromIndex(),
                this.context.toIndex(),
                pageSize,
                this.context.currentPage(),
                this.context.elementCount()
        );
        return this;
    }

    public CommandPagination<C, D> withHeader(Function<Context, Text> headerBuilder) {
        this.header = headerBuilder.apply(this.context);
        return this;
    }

    public CommandPagination<C, D> withRows(Function<C, MutableText> rowMapper, List<CommandReply<C>> buttons) {
        this.rows = this.list.subList(context.fromIndex, context.toIndex).stream().map(row -> CommandReply.getButtonRow(row, buttons)
                .append(rowMapper.apply(row))).toList();
        return this;
    }

    public CommandPagination<C, D> withEmptyMessage(Function<Context, Text> emptyListMessage) {
        this.emptyListMessage = emptyListMessage.apply(this.context);
        return this;
    }

    public CommandPagination<C, D> withRefreshButton() {
        this.buttons.add(getRefreshButton());
        return this;
    }

    public CommandPagination<C, D> withButton(CommandReply<D> button) {
        this.buttons.add(button);
        return this;
    }

    public CommandPagination<C, D> withCommand(String command) {
        this.command = command;
        return this;
    }

    public void display() {
        CommandUtil.send(this.source, Text.literal("\n"));
        if (header != null) CommandUtil.send(this.source, header);
        CommandUtil.send(this.source, getTableBorder());
        rows.forEach(row -> CommandUtil.send(this.source, row));
        CommandUtil.send(this.source, getPagination(emptyListMessage));
        CommandUtil.send(this.source, CommandReply.getButtonRow(this.list, buttons));
    }

    private static MutableText getTableBorder() {
        return Text.literal("---------------------------")
                .styled(style -> style
                        .withColor(Colors.LIGHT_GRAY));
    }

    private Text getPagination(Text emptyMessage) {
        if (context.pageCount() == 0) {
            return emptyMessage.copy().styled(style -> style
                    .withFormatting(Formatting.GREEN))
                    .append(Text.literal("\n"))
                    .append(getTableBorder());
        } else if (context.pageCount() == 1) {
            return getTableBorder();
        }
        return Text.literal("------- ")
                .withColor(Colors.LIGHT_GRAY)
                .append(CommandReply.<Context>get(params -> Text.literal("<< "))
                        .withToolTip(params -> Text.literal("First Page"))
                        .withCommand(params -> getPageLink(1))
                        .withClickable(params -> params.currentPage() > 1)
                        .withColor(params -> params.currentPage() > 1 ? Colors.GREEN : Colors.GRAY)
                        .build(context))
                .append(CommandReply.<Context>get(params -> Text.literal("< "))
                        .withToolTip(params -> Text.literal("Previous page"))
                        .withCommand(params -> getPageLink(params.currentPage() - 1))
                        .withClickable(params -> params.currentPage() > 1)
                        .withColor(params -> params.currentPage() > 1 ? Colors.GREEN : Colors.GRAY)
                        .build(context))
                .append(Text.literal(context.currentPage() + " / " + context.pageCount()))
                .append(CommandReply.<Context>get(params -> Text.literal(" >"))
                        .withToolTip(params -> Text.literal("Next page"))
                        .withCommand(params -> getPageLink(params.currentPage() + 1))
                        .withClickable(params -> params.currentPage() < params.pageCount())
                        .withColor(params -> params.currentPage() < params.pageCount() ? Colors.GREEN : Colors.GRAY)
                        .build(context))
                .append(CommandReply.<Context>get(params -> Text.literal(" >>"))
                        .withToolTip(params -> Text.literal("Last page"))
                        .withCommand(params -> getPageLink(params.pageCount()))
                        .withClickable(params -> params.currentPage() < params.pageCount())
                        .withColor(params -> params.currentPage() < params.pageCount() ? Colors.GREEN : Colors.GRAY)
                        .build(context))
                .append(Text.literal(" -------").withColor(Colors.LIGHT_GRAY));
    }

    private CommandReply<D> getRefreshButton() {
        return CommandReply.<D>get(title -> Text.literal("Refresh"))
                .withToolTip(tooltip -> Text.literal("Refresh page"))
                .withCommand(command -> getPageLink(context.currentPage()))
                .withColor(color -> Colors.YELLOW)
                .withBrackets();
    }

    private String getPageLink(int page) {
        return String.format("%s page %s", this.command, page);
    }

    @Override
    public String toString() {
        return String.format("CommandPagination{currentPage=%s, pageCount=%s, fromIndex=%s, toIndex=%s, pageSize=%s}",
                context.currentPage(),
                context.pageCount(),
                context.fromIndex(),
                context.toIndex(),
                context.pageSize()
        );
    }
}
