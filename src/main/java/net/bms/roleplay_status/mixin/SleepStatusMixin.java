package net.bms.roleplay_status.mixin;

import net.bms.roleplay_status.state.ServerStatuses;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

/** Exclude opted-out players from the population required to skip night. */
@Mixin(SleepStatus.class)
public abstract class SleepStatusMixin {
    @Shadow private int activePlayers;
    @Shadow private int sleepingPlayers;
    @Shadow public abstract int sleepersNeeded(int percentage);

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void roleplay$population(List<ServerPlayer> players, CallbackInfoReturnable<Boolean> cir) {
        if (players.stream().noneMatch(ServerStatuses::excludedFromSleep)) return;
        int previousActive = activePlayers;
        int previousSleeping = sleepingPlayers;
        activePlayers = 0;
        sleepingPlayers = 0;
        for (ServerPlayer player : players) {
            if (player.isSpectator() || ServerStatuses.excludedFromSleep(player)) continue;
            activePlayers++;
            if (player.isSleeping()) sleepingPlayers++;
        }
        cir.setReturnValue((previousSleeping > 0 || sleepingPlayers > 0)
                && (previousActive != activePlayers || previousSleeping != sleepingPlayers));
    }

    @Inject(method = "areEnoughDeepSleeping", at = @At("HEAD"), cancellable = true)
    private void roleplay$deepSleep(int percentage, List<ServerPlayer> players, CallbackInfoReturnable<Boolean> cir) {
        if (players.stream().noneMatch(ServerStatuses::excludedFromSleep)) return;
        long count = players.stream().filter(p -> !p.isSpectator() && !ServerStatuses.excludedFromSleep(p))
                .filter(ServerPlayer::isSleepingLongEnough).count();
        cir.setReturnValue(count > 0 && count >= sleepersNeeded(percentage));
    }
}
