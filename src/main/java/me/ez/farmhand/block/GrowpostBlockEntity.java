package me.ez.farmhand.block;

import java.util.UUID;
import me.ez.farmhand.Config;
import me.ez.farmhand.Init;
import me.ez.farmhand.menu.GrowpostMenu;
import me.ez.farmhand.util.GrowpostRegistry;
import me.ez.farmhand.util.ReadinessTracker;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class GrowpostBlockEntity extends BlockEntity implements MenuProvider {
    private UUID owner;
    private String label = "";
    private int radius = 8, threshold = 50, ready, total;
    private boolean firstAlert = true, thresholdAlert = true, fullAlert = true, protect = true, interlock = true;
    private int cooldown;
    private final ReadinessTracker tracker = new ReadinessTracker();
    public GrowpostBlockEntity(BlockPos pos, BlockState state) { super(Init.GROWPOST_BE.get(), pos, state); }
    public UUID owner() { return owner; }
    public void claim(Player player) { if (owner == null) { owner = player.getUUID(); setChanged(); } }
    public boolean canEdit(Player player) { return owner != null && owner.equals(player.getUUID()); }
    public String label() { return label; }
    public void setLabel(String value) {
        label = value.codePoints().filter(c -> !Character.isISOControl(c) && c != '\u00a7').limit(32)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString().strip();
        setChanged();
    }
    public Component getDisplayName() { return label.isEmpty() ? Component.translatable("block.farmhand.growpost") : Component.literal(label); }
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new GrowpostMenu(id, inventory, this); }
    public int setting(int index) {
        return switch (index) {
            case 0 -> radius; case 1 -> threshold;
            case 2 -> firstAlert ? 1 : 0; case 3 -> thresholdAlert ? 1 : 0; case 4 -> fullAlert ? 1 : 0;
            case 5 -> protect ? 1 : 0; case 6 -> interlock ? 1 : 0; case 7 -> ready; case 8 -> total;
            case 10 -> ReadinessTracker.signal(ready, total); default -> 0;
        };
    }
    public void edit(int button) {
        switch (button) {
            case 0 -> radius = Math.max(1, radius - 1);
            case 1 -> radius = Math.min(16, radius + 1);
            case 2 -> threshold = Math.max(5, threshold - 5);
            case 3 -> threshold = Math.min(100, threshold + 5);
            case 4 -> firstAlert = !firstAlert; case 5 -> thresholdAlert = !thresholdAlert; case 6 -> fullAlert = !fullAlert;
            case 7 -> protect = !protect; case 8 -> interlock = !interlock;
            default -> { return; }
        }
        // Settings changes aren't harvest events: initialize the new threshold without sending a notification.
        tracker.advance(ready, total, threshold);
        cooldown = 0;
        setChanged();
    }
    public boolean covers(BlockPos pos) {
        return Math.abs(pos.getX() - worldPosition.getX()) <= radius && Math.abs(pos.getZ() - worldPosition.getZ()) <= radius
                && Math.abs(pos.getY() - worldPosition.getY()) <= 3;
    }
    public boolean protects(BlockPos pos) { return protect && covers(pos); }
    public boolean pausesLamp(BlockPos pos) { return interlock && total > 0 && ready >= total && covers(pos); }
    public void onLoad() { super.onLoad(); if (level != null && !level.isClientSide()) GrowpostRegistry.add(level, worldPosition); }
    public void setRemoved() { if (level != null) GrowpostRegistry.remove(level, worldPosition); super.setRemoved(); }

    public static void tick(Level level, BlockPos pos, BlockState state, GrowpostBlockEntity post) {
        if (!(level instanceof ServerLevel server)) return;
        if (!Config.ENABLED.get()) {
            if (state.getValue(BlockStateProperties.POWER) != 0) {
                level.setBlock(pos, state.setValue(BlockStateProperties.POWER, 0), 3);
                level.updateNeighbourForOutputSignal(pos, state.getBlock());
            }
            return;
        }
        if (--post.cooldown > 0) return;
        post.cooldown = 100;
        int ready = 0, total = 0;
        boolean complete = true;
        for (BlockPos cropPos : BlockPos.betweenClosed(pos.offset(-post.radius, -2, -post.radius), pos.offset(post.radius, 2, post.radius))) {
            if (!level.hasChunkAt(cropPos)) { complete = false; continue; }
            var cropState = level.getBlockState(cropPos);
            if (cropState.getBlock() instanceof CropBlock crop) { total++; if (crop.isMaxAge(cropState)) ready++; }
        }
        // Never report a partly unloaded field as fully ready or pause its lamps.
        if (!complete) { post.ready = 0; post.total = 0; }
        else { post.ready = ready; post.total = total; }
        int signal = ReadinessTracker.signal(post.ready, post.total);
        if (state.getValue(BlockStateProperties.POWER) != signal) {
            level.setBlock(pos, state.setValue(BlockStateProperties.POWER, signal), 3);
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
        if (complete) {
            int crossed = post.tracker.advance(ready, total, post.threshold);
            int allowed = crossed & ((post.firstAlert ? 1 : 0) | (post.thresholdAlert ? 2 : 0) | (post.fullAlert ? 4 : 0));
            var player = post.owner == null ? null : server.getServer().getPlayerList().getPlayer(post.owner);
            if (allowed != 0 && player != null) {
                String milestone = (allowed & 4) != 0 ? "full" : (allowed & 2) != 0 ? "threshold" : "first";
                Component message = Component.literal("[").withStyle(ChatFormatting.GOLD)
                        .append(post.getDisplayName().copy().withStyle(ChatFormatting.GOLD)).append("] ")
                        .append(Component.translatable("farmhand.growpost.chat." + milestone, ready, total).withStyle(ChatFormatting.GREEN));
                player.sendSystemMessage(message);
            }
        }
        post.setChanged();
    }
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Label", label);
        if (owner != null) output.putString("Owner", owner.toString());
        output.putInt("Radius", radius); output.putInt("Threshold", threshold);
        output.putBoolean("FirstAlert", firstAlert); output.putBoolean("ThresholdAlert", thresholdAlert); output.putBoolean("FullAlert", fullAlert);
        output.putBoolean("Protect", protect); output.putBoolean("Interlock", interlock);
        output.putInt("Ready", ready); output.putInt("Total", total); output.putInt("Milestones", tracker.mask());
    }
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        label = input.getStringOr("Label", "");
        try { owner = UUID.fromString(input.getStringOr("Owner", "")); } catch (IllegalArgumentException e) { owner = null; }
        radius = Math.max(1, Math.min(16, input.getIntOr("Radius", 8)));
        threshold = Math.max(5, Math.min(100, input.getIntOr("Threshold", 50)));
        firstAlert = input.getBooleanOr("FirstAlert", true); thresholdAlert = input.getBooleanOr("ThresholdAlert", true); fullAlert = input.getBooleanOr("FullAlert", true);
        protect = input.getBooleanOr("Protect", true); interlock = input.getBooleanOr("Interlock", true);
        ready = Math.max(0, input.getIntOr("Ready", 0)); total = Math.max(ready, input.getIntOr("Total", 0));
        tracker.restore(input.getIntOr("Milestones", 0));
    }
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        Component name = components.get(DataComponents.CUSTOM_NAME);
        if (name != null) setLabel(name.getString());
    }
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (!label.isEmpty()) components.set(DataComponents.CUSTOM_NAME, Component.literal(label));
    }
}
