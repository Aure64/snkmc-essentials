package com.snkmc.essentials;

import com.snkmc.essentials.boss.BossTimerHud;
import com.snkmc.essentials.boss.BossTracker;
import com.snkmc.essentials.boss.ModSounds;
import com.snkmc.essentials.boss.WaypointManager;
import com.snkmc.essentials.ascension.AscensionHud;
import com.snkmc.essentials.config.ConfigManager;
import com.snkmc.essentials.haki.HakiHud;
import com.snkmc.essentials.haki.HakiTimer;
import com.snkmc.essentials.help.HelpScreen;
import com.snkmc.essentials.hud.HudEditScreen;
import com.snkmc.essentials.hud.HudElementRegistry;
import com.snkmc.essentials.island.IslandDetector;
import com.snkmc.essentials.job.JobHud;
import com.snkmc.essentials.job.JobTracker;
import com.snkmc.essentials.pet.ActivePetsHud;
import com.snkmc.essentials.pet.MinionFeedLearner;
import com.snkmc.essentials.pet.MinionTooltip;
import com.snkmc.essentials.pet.PetStatTooltip;
import com.snkmc.essentials.quest.ParcheminHud;
import com.snkmc.essentials.update.UpdateChecker;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnkmcEssentialsClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(ModConstants.MOD_NAME);
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "main"));

    private static SnkmcEssentialsClient instance;
    private ConfigManager configManager;

    private KeyMapping editHudKey;
    private KeyMapping helpKey;

    private boolean pendingHelp = false;
    private boolean helpShownThisSession = false;
    private boolean pendingTelemetryNotice = false;

    @Override
    public void onInitializeClient() {
        instance = this;

        configManager = new ConfigManager();
        configManager.load();
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STOPPING
            .register(client -> configManager.flush());

        ModSounds.register();

        HudElementRegistry.init();
        IslandDetector.getInstance();

        // SnkMC : seuls les parchemins sont actifs pour l'instant (HUD métier retiré
        // à la demande de l'auteur).
        // Les autres features héritées de MinePiece (boss, pets, ascension, haki,
        // quêtes du pass, rareté, AH) restent dans le code mais ne sont pas
        // enregistrées tant que les écrans SnkMC correspondants n'ont pas été
        // relevés — les commandes /boss, /pets, /pass quests n'existent sans doute
        // pas ici et spammeraient le chat.
        HudElementRegistry.register(new ParcheminHud());
        // Armes : même NBT d'XP (items:experience-xp) que les armes MinePiece.
        HudElementRegistry.register(new AscensionHud());
        // Métier : écran /jobs identique à MinePiece, action bar à confirmer (XP décimale).
        HudElementRegistry.register(new JobHud());

        // Format AH confirmé identique sur SnkMC (dump du 2026-10-18) :
        // lore « Prix de vente / Prix moyen ». Périmètre voulu : parchemins + AH.
        com.snkmc.essentials.ah.AhTooltip.register();
        // Filtre de rareté (voile sur les raretés non choisies) : glyphes et textures
        // d'emblèmes identiques dans le pack SnkMC (vérifié le 2026-10-18).
        registerRarityScreenHooks();

        UpdateChecker.init();
        com.snkmc.essentials.telemetry.Telemetry.init();

        // Reset transient state (queues, last island) on every server join/disconnect.
        // Without this, refreshQueue can persist across reconnects and resume firing.
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ServerDetector.reset();
            IslandDetector.getInstance().reset();
            com.snkmc.essentials.network.BackgroundGuiRefresh.reset();
            BossTracker.getInstance().onConnectionChange();
            JobTracker.reset();
            if (!configManager.config().helpDismissed && !helpShownThisSession) {
                pendingHelp = true;
            }
            com.snkmc.essentials.telemetry.Telemetry.onJoinedServer();
            if (!com.snkmc.essentials.telemetry.Telemetry.wasAnnounced()) {
                pendingTelemetryNotice = true;
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            IslandDetector.getInstance().reset();
            com.snkmc.essentials.network.BackgroundGuiRefresh.reset();
            BossTracker.getInstance().onConnectionChange();
            com.snkmc.essentials.telemetry.Telemetry.onDisconnected();
        });

        registerKeybinds();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Not gated to MinePiece: players whose detection fails still get notified to update.
            UpdateChecker.tickNotify();

            // Idem : la télémétrie doit partir même si la détection MinePiece échoue.
            com.snkmc.essentials.telemetry.Telemetry.tick();
            if (pendingTelemetryNotice && client.player != null) {
                pendingTelemetryNotice = false;
                client.player.sendSystemMessage(
                    net.minecraft.network.chat.Component.translatable("snkmc.telemetry.notice")
                        .withColor(0xF0A857));
                com.snkmc.essentials.telemetry.Telemetry.markAnnounced();
            }

            // Auto-learn minion resource XP ratios from the feeding screen.
            MinionFeedLearner.tick();

            // Toujours, même hors MinePiece : un relevé en cours doit pouvoir se terminer.
            com.snkmc.essentials.network.BackgroundGuiRefresh.tick();
            configManager.tick();

            if (!ServerDetector.isOnSnkmc()) return;

            BossTracker.getInstance().tick();

            if (pendingHelp && client.screen == null && client.player != null) {
                client.setScreenAndShow(new HelpScreen());
                pendingHelp = false;
                helpShownThisSession = true;
            }

            while (helpKey.consumeClick()) {
                // Comptée ici uniquement : c'est une pression volontaire de H, à
                // distinguer de l'ouverture automatique au premier lancement (pendingHelp).
                com.snkmc.essentials.telemetry.Telemetry.feature("help_screen");
                client.setScreenAndShow(new HelpScreen());
            }
            while (editHudKey.consumeClick()) {
                client.setScreenAndShow(new HudEditScreen());
            }
        });

        LOGGER.info("SnkMC Essentials initialized with {} HUD elements",
                HudElementRegistry.getElements().size());
    }

    /**
     * Branche les clics de la barre rareté sur TOUS les AbstractContainerScreen (inventaire E inclus).
     * Le RENDU, lui, passe par {@code ScreenRenderMixin} (dessiné avant renderDeferredElements
     * pour rester sous l'infobulle serveur) — pas par afterRender qui dessinerait par-dessus.
     */
    private void registerRarityScreenHooks() {
        net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AFTER_INIT.register((client, screen, sw, sh) -> {
            if (screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> hs) {
                net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents.allowMouseClick(screen)
                    .register((s, click) ->
                        !com.snkmc.essentials.rarity.RarityScreenOverlay.onClick(hs, click.x(), click.y()));
            }
        });
    }

    private void registerKeybinds() {
        editHudKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.snkmc-essentials.edit_hud", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY));
        helpKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.snkmc-essentials.help", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY));
    }

    public static SnkmcEssentialsClient getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
}
