package me.ez.farmhand.item;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.ez.farmhand.Config;
import me.ez.farmhand.util.FarmUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Scans for fully-grown crops on use, and pings while held. */
public class ResultSentinelItem extends Item {

    private static final Map<UUID, Integer> LAST_COUNT = new HashMap<>();

    public ResultSentinelItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!Config.ENABLED.get() || !Config.SENTINEL_ENABLED.get()) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server) {
            int[] result = FarmUtil.scanCrops(server, player.blockPosition(), Config.SENTINEL_RADIUS.get());
            report(player, result);
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 10);
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (!(owner instanceof Player player) || slot != EquipmentSlot.MAINHAND
                || !Config.ENABLED.get() || !Config.SENTINEL_ENABLED.get()) {
            return;
        }
        if (level.getGameTime() % 40L != 0L) {
            return;
        }
        int[] result = FarmUtil.scanCrops(level, player.blockPosition(), Config.SENTINEL_RADIUS.get());
        Integer previous = LAST_COUNT.get(player.getUUID());
        if (result[0] > 0 && (previous == null || previous != result[0])) {
            LAST_COUNT.put(player.getUUID(), result[0]);
            report(player, result);
        } else if (result[0] == 0) {
            LAST_COUNT.remove(player.getUUID());
        }
    }

    private static void report(Player player, int[] result) {
        if (result[0] <= 0) {
            player.sendOverlayMessage(Component.translatable("farmhand.sentinel.none"));
            return;
        }
        player.sendOverlayMessage(Component.translatable("farmhand.sentinel.ready",
                result[0], result[1], result[2], result[3]));
    }
}
