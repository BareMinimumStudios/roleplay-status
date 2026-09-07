package com.andromedamc.status.mixin;

import de.maxhenkel.status.playerstate.PlayerState;
import de.maxhenkel.status.playerstate.PlayerStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = PlayerStateManager.class, remap = false)
public interface PlayerStateManagerAccessor {

    @Accessor("states")
    ConcurrentHashMap<UUID, PlayerState> andromeda$getStates();
}
