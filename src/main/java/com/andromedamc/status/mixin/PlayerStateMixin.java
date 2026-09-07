package com.andromedamc.status.mixin;

import com.andromedamc.status.StatusCatalog;
import de.maxhenkel.status.playerstate.Availability;
import de.maxhenkel.status.playerstate.PlayerState;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = PlayerState.class, remap = false)
public abstract class PlayerStateMixin {

    @Shadow
    private Availability availability;

    @Shadow
    private String state;

    @Inject(
            method = "<init>(Ljava/util/UUID;Lde/maxhenkel/status/playerstate/Availability;Ljava/lang/String;Z)V",
            at = @At("TAIL")
    )
    private void andromeda$sanitizeConstruction(UUID player, Availability availability, String state, boolean noSleep, CallbackInfo ci) {
        this.availability = Availability.NONE;
        this.state = StatusCatalog.sanitize(this.state);
    }

    @ModifyVariable(method = "setState", at = @At("HEAD"), argsOnly = true)
    private String andromeda$sanitizeSetState(String state) {
        return StatusCatalog.sanitize(state);
    }

    @ModifyVariable(method = "setAvailability", at = @At("HEAD"), argsOnly = true)
    private Availability andromeda$disableAvailability(Availability availability) {
        return Availability.NONE;
    }

    @Inject(method = "fromBytes", at = @At("RETURN"))
    private static void andromeda$sanitizeNetworkState(FriendlyByteBuf buf, CallbackInfoReturnable<PlayerState> cir) {
        PlayerState result = cir.getReturnValue();
        result.setAvailability(Availability.NONE);
        result.setState(StatusCatalog.sanitize(result.getState()));
    }
}
