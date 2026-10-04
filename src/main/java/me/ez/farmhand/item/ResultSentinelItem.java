package me.ez.farmhand.item;

import me.ez.farmhand.Config;
import me.ez.farmhand.util.FarmUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Harvest Compass: authoritative scan data, rendered quietly by a dedicated HUD. */
public class ResultSentinelItem extends Item {
    public ResultSentinelItem(Properties properties) { super(properties); }
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!Config.ENABLED.get() || !Config.SENTINEL_ENABLED.get()) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            scan(server, player, player.getItemInHand(hand));
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 20);
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (!(owner instanceof Player player) || (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND)
                || !Config.ENABLED.get() || !Config.SENTINEL_ENABLED.get()) return;
        if (level.getGameTime() % 100 == 0) scan(level, player, stack);
    }
    private static void scan(ServerLevel level, Player player, ItemStack stack) {
        int[] result = FarmUtil.scanCrops(level, player.blockPosition(), Config.SENTINEL_RADIUS.get());
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag survey = new CompoundTag();
        survey.putInt("Ready", result[0]); survey.putInt("Total", result[5]);
        survey.putInt("X", result[1]); survey.putInt("Y", result[2]); survey.putInt("Z", result[3]);
        root.put("FarmhandSurvey", survey);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }
}
