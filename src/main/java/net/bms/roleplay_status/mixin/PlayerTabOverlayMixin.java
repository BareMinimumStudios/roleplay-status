package net.bms.roleplay_status.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.bms.roleplay_status.client.ClientStatusRuntime;
import net.bms.roleplay_status.client.TabIndicator;
import net.bms.roleplay_status.client.IndicatorRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {
    // Wrap the completed call, not its RETURN instructions: Figura and other
    // name mods may replace that return value after an ordinary RETURN injector.
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/components/PlayerTabOverlay;getNameForDisplay(Lnet/minecraft/client/multiplayer/PlayerInfo;)Lnet/minecraft/network/chat/Component;"))
    private Component roleplay$finalName(PlayerTabOverlay overlay, PlayerInfo info, Operation<Component> original) {
        return ClientStatusRuntime.decorate(info, original.call(overlay, info));
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"))
    private int roleplay$drawBadge(GuiGraphics graphics, Font font, Component text, int x, int y, int color,
                                  Operation<Integer> original) {
        Integer badgeColor = TabIndicator.color(text);
        if (badgeColor != null) {
            int alpha = (color & 0xFF000000) == 0 ? 0xFF000000 : color & 0xFF000000;
            IndicatorRenderer.draw(graphics, x + 1, y, alpha | badgeColor, TabIndicator.shape(text));
        }
        return original.call(graphics, font, text, x, y, color);
    }
}
