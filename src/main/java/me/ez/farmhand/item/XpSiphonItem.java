package me.ez.farmhand.item;

import me.ez.farmhand.Config;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

/** Collects a furnace's stored experience on right-click. */
public class XpSiphonItem extends Item {

    public XpSiphonItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!Config.ENABLED.get() || !Config.SIPHON_ENABLED.get()) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(context.getClickedPos()) instanceof AbstractFurnaceBlockEntity furnace
                && context.getPlayer() instanceof ServerPlayer serverPlayer) {
            furnace.awardUsedRecipesAndPopExperience(serverPlayer);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.PASS;
    }
}
