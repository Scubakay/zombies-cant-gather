package com.scubakay.zombiescantgather.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
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

    public record Button<C>(
            Function<C, Text> text,
            Function<ServerPlayerEntity, Boolean> requires,
            Function<C, Text> tooltip,
            Function<C, String> command,
            int color,
            boolean suggestion
    ) {
        public Button(Function<C, Text> text, Function<ServerPlayerEntity, Boolean> requires, Function<C, Text> tooltip, Function<C, String> command, int color) {
            this(text, requires, tooltip, command, color, false); // default suggestion to false
        }
    }

    private Parameters parameters;
    private final String command;
    private final CommandContext<ServerCommandSource> context;
    private final D list;

    private Text header;
    private List<MutableText> rows = new ArrayList<>();
    private Text emptyListMessage = Text.literal("List is empty");
    private final List<Text> buttons = new ArrayList<>();

    private CommandPagination(CommandContext<ServerCommandSource> context, D list) {
        this.context = context;
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

    public CommandPagination<C, D> withRows(Function<C, MutableText> rowMapper, List<Button<C>> buttons) {
        this.rows = this.list.subList(parameters.fromIndex, parameters.toIndex)
                .stream().map(row -> {
                    AtomicReference<Text> button = new AtomicReference<>(null);
                    buttons.forEach(b -> button.set(button.get() == null ? getButton(b, row) : button.get().copy().append(getButton(b, row))));
                    return button.get().copy().append(rowMapper.apply(row));
                }).toList();
        return this;
    }

    public CommandPagination<C, D> withFooter(Function<Parameters, Text> emptyListMessage) {
        this.emptyListMessage = emptyListMessage.apply(this.parameters);
        return this;
    }

    public CommandPagination<C, D> withRefreshButton() {
        this.buttons.add(getRefreshButton());
        return this;
    }

    public CommandPagination<C, D> withButton(Button<C> button) {
        this.buttons.add(getButton(button));
        return this;
    }

    public void display() {
        if (header != null) {
            CommandUtil.reply(context, header);
            CommandUtil.reply(context, getTableBorder());
        }

        rows.forEach(row -> CommandUtil.reply(context, row));

        final Text pagination = getPagination(emptyListMessage);
        if (pagination != null) {
            CommandUtil.reply(context, pagination);
        }

        final Text buttonRow = getButtonRow();
        if (buttonRow != null) {
            CommandUtil.reply(context, getTableBorder());
            CommandUtil.reply(context, buttonRow);
        }
    }

    private static MutableText getTableBorder() {
        return Text.literal(" --------------------------- ")
                .styled(style -> style
                        .withColor(Colors.LIGHT_GRAY));
    }

    private Text getButtonRow() {
        AtomicReference<Text> buttonRow = new AtomicReference<>(null);
        buttons.forEach(button -> {
            if (buttonRow.get() == null) {
                buttonRow.set(button);
            } else {
                buttonRow.set(buttonRow.get().copy().append(button));
            }
        });
        return buttonRow.get();
    }

    private Text getPagination(Text emptyMessage) {
        if (parameters.pageCount() == 0) {
            return emptyMessage.copy().styled(style -> style
                    .withFormatting(Formatting.GREEN));
        } else if (parameters.pageCount() == 1) {
            return null;
        }
        return Text.literal(" ------- ").withColor(Colors.LIGHT_GRAY)
                .append(Text.literal("<< ")
                        .styled(style -> getPageLinkStyle(style, parameters.currentPage() > 1, "First page", 1)))
                .append(Text.literal("< ")
                        .styled(style -> getPageLinkStyle(style, parameters.currentPage() > 1, "Previous page", parameters.currentPage() - 1)))
                .append(Text.literal(parameters.currentPage() + " / " + parameters.pageCount())
                        .styled(style -> style
                                .withColor(Colors.WHITE)
                                //? >=1.21.5 {
                                .withClickEvent(new ClickEvent.ChangePage(1))
                                .withHoverEvent(new HoverEvent.ShowText(Text.literal(parameters.currentPage() + "/" + parameters.pageCount())))))
                //?} else {
                /*.withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "1"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(parameters.currentPage() + "/" + parameters.pageCount())))))
        *///?}
                .append(Text.literal(" >")
                        .styled(style -> getPageLinkStyle(style, parameters.currentPage() < parameters.pageCount(), "Next page", parameters.currentPage() + 1)))
                .append(Text.literal(" >>")
                        .styled(style -> getPageLinkStyle(style, parameters.currentPage() < parameters.pageCount(), "Last page", parameters.pageCount())))
                .append(Text.literal(" ------- ").withColor(Colors.LIGHT_GRAY));
    }

    private MutableText getRefreshButton() {
        return Text.literal("[Refresh]").styled(style ->
                getPageLinkStyle(style, true, "Refresh page", parameters.currentPage())
                        .withColor(Colors.YELLOW));
    }

    public static <C> MutableText getButton(Button<C> button) {
        return CommandPagination.getButton(button, null);
    }

    public static <C> MutableText getButton(Button<C> button, C item) {
        return Text.literal("[")
                .styled(style ->
                        CommandPagination.getButtonStyle(style, button.tooltip().apply(item), button.command().apply(item), button.suggestion())
                                .withColor(button.color()))
                .append(button.text().apply(item))
                .append(Text.literal("]"))
                .append(Text.literal(" ")
                        .styled(style -> style
                                //? >=1.21.5 {
                                .withClickEvent(new ClickEvent.ChangePage(1))
                                .withHoverEvent(new HoverEvent.ShowText(Text.empty()))
                                //?} else {
                                /*.withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "1"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.empty()))
                                *///?}
                                .withFormatting(Formatting.WHITE)));
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
                parameters.currentPage(),
                parameters.pageCount(),
                parameters.fromIndex(),
                parameters.toIndex(),
                parameters.pageSize()
        );
    }
}
