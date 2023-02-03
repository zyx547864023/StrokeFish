package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.awt.*;

public class fs_fightermod extends BaseHullMod {
    private final double time=0.5;
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float opad = 12.0F;
        Color highlight = new Color(255, 232, 87, 255);
        tooltip.addSectionHeading("数据分析", Alignment.MID, opad);


        LabelAPI label = tooltip.addPara(
                "战机重新整备速率增加 %s%%。",
                opad, highlight, String.valueOf(time*100));
        label.setHighlight(time*100+"%");
        label.setHighlightColors(highlight, highlight, highlight, highlight, highlight, highlight, highlight);

    }
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getFighterRefitTimeMult().modifyMult(id,(float) time);
    }
}
