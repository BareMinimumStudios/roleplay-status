package net.bms.roleplay_status.mixin;

import net.bms.roleplay_status.client.RoleplayStatusScreen;
import de.maxhenkel.status.gui.StatusScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Replace the screen before initialization/rendering; never draw over Status. */
@Mixin(Minecraft.class)
public abstract class StatusScreenMixin {
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen roleplay$replaceStatusScreen(Screen screen) {
        return screen != null && screen.getClass() == StatusScreen.class ? new RoleplayStatusScreen() : screen;
    }
}
