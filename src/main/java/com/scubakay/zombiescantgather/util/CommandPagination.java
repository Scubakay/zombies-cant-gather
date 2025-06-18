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

    public record Parameters(
            int pageCount,
            int fromIndex,
            int toIndex,
            int pageSize,
            int currentPage,
            int elementCount
    ) {
    }

    private Parameters parameters;
    private String command;
    private final CommandContext<ServerCommandSource> source;
    private final D list;

    private Text header;
    private List<MutableText> rows = new ArrayList<>();
    private Text emptyListMessage = Text.literal("List is empty");
    private final List<CommandReply<C>> buttons = new ArrayList<>();

    private CommandPagination(CommandContext<ServerCommandSource> context, D list) {
        this.source = context;
        this.list = list;

        // Get command information
        command = context.getInput().split(" " + PAGE_COMMAND)[0];
        int currentPage = 1;
        try {
            currentPage = IntegerArgumentType.getInteger(context, "page");
        } catch (Exception ignored) {
        }

        // Calculate parameters
        int elementCount = this.list.size();
        int pageCount = elementCount > 0 ? (elementCount - 1) / DEFAULT_PAGE_SIZE + 1 : 0;
        int fromIndex = Math.min(pageCount, currentPage - 1) * DEFAULT_PAGE_SIZE;
        int toIndex = Math.min(elementCount, fromIndex + DEFAULT_PAGE_SIZE);
        this.parameters = new Parameters(pageCount, fromIndex, toIndex, DEFAULT_PAGE_SIZE, currentPage, elementCount);
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
        this.parameters = new Parameters(
                this.parameters.pageCount(),
                this.parameters.fromIndex(),
                this.parameters.toIndex(),
                pageSize,
                this.parameters.currentPage(),
                this.parameters.elementCount()
        );
        return this;
    }

    public CommandPagination<C, D> withHeader(Function<Parameters, Text> headerBuilder) {
        this.header = headerBuilder.apply(this.parameters);
        return this;
    }

    public CommandPagination<C, D> withRows(Function<C, MutableText> rowMapper, List<CommandReply<C>> buttons) {
        this.rows = this.list.subList(parameters.fromIndex, parameters.toIndex).stream().map(row -> CommandReply.getButtonRow(row, buttons)
                .append(rowMapper.apply(row))).toList();
        return this;
    }

    public CommandPagination<C, D> withEmptyMessage(Function<Parameters, Text> emptyListMessage) {
        this.emptyListMessage = emptyListMessage.apply(this.parameters);
        return this;
    }

    public CommandPagination<C, D> withRefreshButton() {
        this.buttons.add(getRefreshButton());
        return this;
    }

    public CommandPagination<C, D> withButton(CommandReply<C> button) {
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
        CommandUtil.send(this.source, CommandReply.getButtonRow(null, buttons));
    }

    private static MutableText getTableBorder() {
        return Text.literal("---------------------------")
                .styled(style -> style
                        .withColor(Colors.LIGHT_GRAY));
    }

    private Text getPagination(Text emptyMessage) {
        if (parameters.pageCount() == 0) {
            return emptyMessage.copy().styled(style -> style
                    .withFormatting(Formatting.GREEN))
                    .append(Text.literal("\n"))
                    .append(getTableBorder());
        } else if (parameters.pageCount() == 1) {
            return getTableBorder();
        }
        return Text.literal("------- ")
                .withColor(Colors.LIGHT_GRAY)
                .append(CommandReply.<Parameters>get(params -> Text.literal("<< "))
                        .withToolTip(params -> Text.literal("First Page"))
                        .withCommand(params -> getPageLink(1))
                        .withClickable(params -> params.currentPage() > 1)
                        .withColor(params -> params.currentPage() > 1 ? Colors.GREEN : Colors.GRAY)
                        .build(parameters))
                .append(CommandReply.<Parameters>get(params -> Text.literal("< "))
                        .withToolTip(params -> Text.literal("Previous page"))
                        .withCommand(params -> getPageLink(params.currentPage() - 1))
                        .withClickable(params -> params.currentPage() > 1)
                        .withColor(params -> params.currentPage() > 1 ? Colors.GREEN : Colors.GRAY)
                        .build(parameters))
                .append(Text.literal(parameters.currentPage() + " / " + parameters.pageCount()))
                .append(CommandReply.<Parameters>get(params -> Text.literal(" >"))
                        .withToolTip(params -> Text.literal("Next page"))
                        .withCommand(params -> getPageLink(params.currentPage() + 1))
                        .withClickable(params -> params.currentPage() < params.pageCount())
                        .withColor(params -> params.currentPage() < params.pageCount() ? Colors.GREEN : Colors.GRAY)
                        .build(parameters))
                .append(CommandReply.<Parameters>get(params -> Text.literal(" >>"))
                        .withToolTip(params -> Text.literal("Last page"))
                        .withCommand(params -> getPageLink(params.pageCount()))
                        .withClickable(params -> params.currentPage() < params.pageCount())
                        .withColor(params -> params.currentPage() < params.pageCount() ? Colors.GREEN : Colors.GRAY)
                        .build(parameters))
                .append(Text.literal(" -------").withColor(Colors.LIGHT_GRAY));
    }

    private CommandReply<C> getRefreshButton() {
        return CommandReply.<C>get(title -> Text.literal("Refresh"))
                .withToolTip(tooltip -> Text.literal("Refresh page"))
                .withCommand(command -> getPageLink(parameters.currentPage()))
                .withColor(color -> Colors.YELLOW)
                .withBrackets();
    }

    private String getPageLink(int page) {
        return String.format("/%s page %s", this.command, page);
    }

    @Override
    public String toString() {
        return String.format("CommandPagination{currentPage=%s, pageCount=%s, fromIndex=%s, toIndex=%s, pageSize=%s}",
                parameters.currentPage(),
                parameters.pageCount(),
                parameters.fromIndex(),
                parameters.toIndex(),
                parameters.pageSize()
        );
    }
}
