package me.ez.farmhand.client;

import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.ez.farmhand.block.MachineBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Actual inventory contents in the nest/trough; extraction keeps live inventory out of rendering. */
public final class MachineContentsRenderer<T extends MachineBlockEntity>
        implements BlockEntityRenderer<T, MachineContentsRenderer.State> {
    private final ItemModelResolver resolver;
    public MachineContentsRenderer(BlockEntityRendererProvider.Context context) { resolver = context.itemModelResolver(); }
    public static final class State extends BlockEntityRenderState {
        public final List<Entry> contents = new ArrayList<>();
        public boolean coop;
    }
    private record Entry(int slot, int copies, ItemStackRenderState item) {}
    public State createRenderState() { return new State(); }
    public void extractRenderState(T machine, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(machine, state, partialTicks, cameraPosition, breakProgress);
        state.coop = machine.kind() == 0;
        state.contents.clear();
        for (int slot = 0; slot < machine.getContainerSize(); slot++) {
            var stack = machine.getItem(slot);
            if (stack.isEmpty()) continue;
            var item = new ItemStackRenderState();
            resolver.updateForTopItem(item, stack, ItemDisplayContext.FIXED, machine.getLevel(), null,
                    (int) machine.getBlockPos().asLong() + slot);
            state.contents.add(new Entry(slot, Math.min(3, 1 + (stack.getCount() - 1) / 16), item));
        }
    }
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        for (Entry entry : state.contents) {
            for (int copy = 0; copy < entry.copies; copy++) {
                poses.pushPose();
                float x = 0.34f + entry.slot % 3 * 0.16f;
                float z = (state.coop ? 0.36f : 0.34f) + entry.slot / 3 * 0.16f;
                poses.translate(x + copy * 0.012f, (state.coop ? 0.35f : 0.326f) + copy * 0.009f, z + copy * 0.012f);
                if (!state.coop) poses.mulPose(Axis.XP.rotationDegrees(90));
                poses.scale(0.17f, 0.17f, 0.17f);
                entry.item.submit(poses, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poses.popPose();
            }
        }
    }
    public int getViewDistance() { return 32; }
}
