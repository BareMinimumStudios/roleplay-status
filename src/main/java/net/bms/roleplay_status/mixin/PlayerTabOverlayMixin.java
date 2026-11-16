package net.bms.roleplay_status.mixin;

import net.bms.roleplay_status.client.ClientStatusRuntime;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {
    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void roleplay$badge(PlayerInfo info, CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(ClientStatusRuntime.decorate(info, cir.getReturnValue()));
    }
}
