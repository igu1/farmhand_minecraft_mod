package me.ez.farmhand.network;

import me.ez.farmhand.Main;
import me.ez.farmhand.menu.GrowpostMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = Main.MOD_ID)
public record PostLabelPayload(int menuId, String label) implements CustomPacketPayload {
    public static final Type<PostLabelPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Main.MOD_ID, "post_label"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PostLabelPayload> CODEC = StreamCodec.of(
            (buffer, payload) -> { buffer.writeVarInt(payload.menuId); buffer.writeUtf(payload.label, 32); },
            buffer -> new PostLabelPayload(buffer.readVarInt(), buffer.readUtf(32)));
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, CODEC, (payload, context) -> {
            var player = context.player();
            if (player.containerMenu instanceof GrowpostMenu menu && menu.containerId == payload.menuId) menu.rename(player, payload.label);
        });
    }
}
