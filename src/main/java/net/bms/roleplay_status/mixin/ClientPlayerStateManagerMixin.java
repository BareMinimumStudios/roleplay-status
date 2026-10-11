package net.bms.roleplay_status.mixin;

import net.bms.roleplay_status.StatusCatalog;
import net.bms.roleplay_status.client.ClientStatusRuntime;
import de.maxhenkel.status.playerstate.ClientPlayerStateManager;
import de.maxhenkel.status.playerstate.PlayerState;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = ClientPlayerStateManager.class, remap = false)
public abstract class ClientPlayerStateManagerMixin {

    @Shadow
    public abstract PlayerState getState(UUID player);

    @Inject(
            target = @Desc(value = "getAvailabilityIcon", args = UUID.class, ret = ResourceLocation.class),
            at = @At("HEAD"),
            cancellable = true
    )
    private void roleplay$hideLegacyAvailability(UUID player, CallbackInfoReturnable<ResourceLocation> cir) {
        cir.setReturnValue(null);
    }

    @Inject(
            target = @Desc(value = "getActivityIcon", args = UUID.class, ret = ResourceLocation.class),
            at = @At("HEAD"),
            cancellable = true
    )
    private void roleplay$renderRoleplayStatus(UUID player, CallbackInfoReturnable<ResourceLocation> cir) {
        PlayerState playerState = getState(player);
        if (playerState == null) {
            cir.setReturnValue(null);
            return;
        }
        String status = StatusCatalog.sanitize(playerState.getState());
        // Always own the activity-icon result. If dynamic icons have not been
        // built yet, return null rather than falling through to Status' neutral icon.
        cir.setReturnValue(ClientStatusRuntime.icon(status));
    }
}
