package com.snkmc.essentials.help;

import com.snkmc.essentials.SnkmcEssentialsClient;
import com.snkmc.essentials.ServerDetector;
import com.snkmc.essentials.donate.DonateScreen;
import com.snkmc.essentials.hud.ParchmentRenderer;
import com.snkmc.essentials.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

/**
 * Book-style help popup explaining the mod's features and how to use them.
 * Required actions are highlighted in red. Shown automatically on first launch
 * (until dismissed) and reopenable with the H key.
 */
public class HelpScreen extends Screen {

    private static final int PANEL_W = 350;
    private static final int HEADER_H = 24;
    // Pied de page : rangée dismiss/close, puis rangée dédiée au don (mise en avant, tout en bas).
    private static final int FOOTER_H = 36; // SnkMC : pas de rangée de don
    private static final int TITLE_H = 11;
    private static final int LINE_H = 9;
    private static final int FEATURE_GAP = 5;
    private static final int BTN_H = 20;
    private static final int DONATE_ROW_GAP = 6;
    // SnkMC : économie et destinataire inconnus, don désactivé.
    private static final boolean DONATE_ENABLED = false;

    private static final int GOLD = 0xFFFFD27F;
    private static final int GRAY = 0xFFCBC8C7;
    private static final int RED = 0xFFFF6B6B;

    // Palette "parchemin doré" réservée au bouton de don, pour qu'il attire l'œil.
    private static final int DONATE_FILL = 0xFF8A6A44;
    private static final int DONATE_FILL_HOVER = 0xFFF0A857;
    private static final int DONATE_BORDER_LIGHT = 0xFFFFE9D5;
    private static final int DONATE_BORDER_DARK = 0xFF3A2A1C;
    private static final int DONATE_TEXT = 0xFFFFE9D5;
    private static final int DONATE_TEXT_HOVER = 0xFF3A2A1C;

    /** Glyphe de la monnaie du serveur (berries), rendu avec la police custom du pack. */
    private static final String BERRY = "实";
    private static final Identifier BERRY_FONT = Identifier.fromNamespaceAndPath("fonts", "icons");

    private record Line(String text, boolean important) {}

    private record Feature(String title, Line[] lines) {}

    private static Line info(String t) {
        return new Line(t, false);
    }

    private static Line important(String t) {
        return new Line(t, true);
    }

    private static String tr(String key) {
        return Component.translatable(key).getString();
    }

    // SnkMC : seules les features actives (parchemins) sont décrites.
    private static Feature[] buildFeatures() {
        return new Feature[]{
            new Feature(tr("snkmc.ui.help.scrolls.title"), new Line[]{
                info(tr("snkmc.ui.help.scrolls.line1")),
            }),
            new Feature(tr("snkmc.ui.help.weapons.title"), new Line[]{
                info(tr("snkmc.ui.help.weapons.line1")),
                info(tr("snkmc.ui.help.weapons.line2")),
            }),
            new Feature(tr("snkmc.ui.help.ah.title"), new Line[]{
                info(tr("snkmc.ui.help.ah.line1")),
            }),
            new Feature(tr("snkmc.ui.help.hud_editor.title"), new Line[]{
                important(tr("snkmc.ui.help.hud_editor.line1")),
                info(tr("snkmc.ui.help.hud_editor.line2")),
                info(tr("snkmc.ui.help.hud_editor.line3")),
            }),
            new Feature(tr("snkmc.ui.help.this_help.title"), new Line[]{
                info(tr("snkmc.ui.help.this_help.line1")),
            }),
        };
    }

    // Line counts per feature (fixed, not language-dependent) used for layout.
    private static final int[] FEATURE_LINE_COUNTS = {1, 2, 1, 3, 1};

    private static final int PANEL_H;

    static {
        int h = HEADER_H;
        for (int lineCount : FEATURE_LINE_COUNTS) {
            h += TITLE_H + lineCount * LINE_H + FEATURE_GAP;
        }
        PANEL_H = h + FOOTER_H;
    }

    private int left;
    private int top;

    public HelpScreen() {
        super(Component.literal("SnkMC Essentials — Aide"));
    }

    @Override
    protected void init() {
        left = (this.width - PANEL_W) / 2;
        top = (this.height - PANEL_H) / 2;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0xB0000000);
        ParchmentRenderer.renderPanel(ctx, left, top, PANEL_W, PANEL_H, "SnkMC Essentials");

        int y = top + HEADER_H;
        for (Feature f : buildFeatures()) {
            RenderUtils.drawText(ctx, "» " + f.title(), left + 12, y, GOLD);
            y += TITLE_H;
            for (Line line : f.lines()) {
                RenderUtils.drawText(ctx, line.text(), left + 20, y, line.important() ? RED : GRAY);
                y += LINE_H;
            }
            y += FEATURE_GAP;
        }

        int btnY = topRowY();
        drawButton(ctx, left + 12, btnY, 140, tr("snkmc.ui.help.btn_dismiss"), mouseX, mouseY);
        drawButton(ctx, left + PANEL_W - 12 - 90, btnY, 90, tr("snkmc.ui.help.btn_close"), mouseX, mouseY);

        // Bouton de don : rangée dédiée tout en bas du panneau, traitement doré pour se distinguer.
        if (DONATE_ENABLED && ServerDetector.isOnSnkmc()) {
            drawDonateButton(ctx, left + 12, donateRowY(), PANEL_W - 24, tr("snkmc.ui.donate.btn_donate"), mouseX, mouseY);
        }
    }

    /** Rangée du haut (dismiss/close). */
    private int topRowY() {
        return top + PANEL_H - FOOTER_H + 12;
    }

    /** Rangée dédiée au don, sous la rangée dismiss/close. */
    private int donateRowY() {
        return topRowY() + BTN_H + DONATE_ROW_GAP;
    }

    /** Bouton de don : palette dorée/parchemin, bordure claire/sombre, glyphe berries. */
    private void drawDonateButton(GuiGraphicsExtractor ctx, int x, int y, int w, String label, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + BTN_H;
        int fill = hover ? DONATE_FILL_HOVER : DONATE_FILL;
        int textColor = hover ? DONATE_TEXT_HOVER : DONATE_TEXT;

        ctx.fill(x, y, x + w, y + BTN_H, fill);
        ctx.fill(x, y, x + w, y + 1, DONATE_BORDER_LIGHT);
        ctx.fill(x, y + BTN_H - 1, x + w, y + BTN_H, DONATE_BORDER_DARK);
        ctx.fill(x, y, x + 1, y + BTN_H, DONATE_BORDER_LIGHT);
        ctx.fill(x + w - 1, y, x + w, y + BTN_H, DONATE_BORDER_DARK);

        Font tr = Minecraft.getInstance().font;
        Component text = Component.literal(label + " ").append(Component.literal(BERRY)
                .withStyle(s -> s.withFont(new FontDescription.Resource(BERRY_FONT))));
        int tw = tr.width(text);
        ctx.text(tr, text, x + (w - tw) / 2, y + (BTN_H - tr.lineHeight) / 2, textColor, false);
    }

    private void drawButton(GuiGraphicsExtractor ctx, int x, int y, int w, String label, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + BTN_H;
        ctx.fill(x, y, x + w, y + BTN_H, hover ? 0xFF5A4632 : 0xFF3A2A1C);
        ctx.fill(x, y, x + w, y + 1, 0xFF8A6A44);
        ctx.fill(x, y + BTN_H - 1, x + w, y + BTN_H, 0xFF8A6A44);
        RenderUtils.drawCenteredText(ctx, label, x + w / 2, y + 6, 0xFFFFE9D5);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.button() == 0) {
            int btnY = topRowY();
            if (inside(click, left + 12, btnY, 140)) {
                var cfg = SnkmcEssentialsClient.getInstance().getConfigManager();
                cfg.config().helpDismissed = true;
                cfg.save();
                onClose();
                return true;
            }
            if (inside(click, left + PANEL_W - 12 - 90, btnY, 90)) {
                onClose();
                return true;
            }
            // Le don reste gate sur MinePiece à la fois pour l'affichage (render) et le clic.
            if (DONATE_ENABLED && ServerDetector.isOnSnkmc() && inside(click, left + 12, donateRowY(), PANEL_W - 24)) {
                Minecraft.getInstance().setScreenAndShow(new DonateScreen(this));
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    private boolean inside(MouseButtonEvent click, int x, int y, int w) {
        return click.x() >= x && click.x() <= x + w && click.y() >= y && click.y() <= y + BTN_H;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
