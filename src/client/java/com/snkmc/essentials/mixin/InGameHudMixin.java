package com.snkmc.essentials.mixin;

import com.snkmc.essentials.job.JobTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Feeds action-bar overlay messages to the job progress tracker. */
@Mixin(Gui.class)
public class InGameHudMixin {

    @Inject(method = "setOverlayMessage", at = @At("HEAD"))
    private void minepiece$jobProgress(Component message, boolean tinted, CallbackInfo ci) {
        if (message != null) {
            // TEMP SnkMC : relevé du format de l'action bar (à retirer avant release)
            com.snkmc.essentials.SnkmcEssentialsClient.LOGGER.info("[ActionBar] '{}'", message.getString());
            JobTracker.onActionBar(message.getString());
        }
    }
}
