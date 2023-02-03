package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.methods.Fs_hullsizeint;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class fs_funeng extends BaseHullMod {
    public static final String KEY = "fs_Funenglistener";
    private final float range = 2000f;
    private final float flux = 3000f;
    private final float damage = 30f;

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float opad = 12.0F;
        Color highlight = new Color(255, 232, 87, 255);
        tooltip.addSectionHeading("数据分析", Alignment.MID, opad);


        LabelAPI label = tooltip.addPara(
                "舰船每增长 %s幅能，舰船周围 %s范围内的\n所有敌方舰船将会根据舰船级别受到 %s/%s/%s/%s点能量伤害",
                opad, highlight, String.valueOf(Math.round(flux)), String.valueOf(Math.round(range)), String.valueOf(Math.round(damage)), String.valueOf(Math.round(damage * 2F)), String.valueOf(Math.round(damage * 3F)), String.valueOf(Math.round(damage * 4F)));
        label.setHighlight(String.valueOf(Math.round(flux)), String.valueOf(Math.round(range)), String.valueOf(Math.round(damage)), String.valueOf(Math.round(damage * 2F)), String.valueOf(Math.round(damage * 3F)), String.valueOf(Math.round(damage * 4F)));
        label.setHighlightColors(highlight, highlight, highlight, highlight, highlight, highlight, highlight);

    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (!ship.getCustomData().containsKey(KEY)) {
            DataContainer data = new DataContainer();
            ship.setCustomData(KEY, data);
        }
        DataContainer data = (DataContainer) ship.getCustomData().get(KEY);
        if (ship.getFluxTracker().getCurrFlux() >= data.nowflux) {
            data.exrflux += ship.getFluxTracker().getCurrFlux() - data.nowflux;
        }
        if (data.exrflux >= flux) {
            data.exrflux = 0f;
            for (ShipAPI target : Global.getCombatEngine().getShips()) {
                if (!target.isFighter() && target.getOwner() == ship.getOwner() && !target.isDrone()) {
                    float d = Vector2f.sub(ship.getLocation(), target.getLocation(), new Vector2f()).length() - ship.getShieldRadiusEvenIfNoShield() - target.getShieldRadiusEvenIfNoShield();
                    //计算目标与舰船之间的距离。

                    if (d < range) {
                        Fs_hullsizeint size = new Fs_hullsizeint();
                        if (target.getAllWeapons().size() > 0) {
                            Global.getCombatEngine().applyDamage(target, target.getAllWeapons().get(Math.max(0, (int) Math.round((target.getAllWeapons().size() - 1) * Math.random()))).getLocation(), damage * size.Getsize(target), DamageType.ENERGY, 0f, true, false, ship);
                        } else {
                            Global.getCombatEngine().applyDamage(target, target.getLocation(), damage * size.Getsize(target), DamageType.ENERGY, 0f, true, false, ship);
                        }
                    }
                }
                data.nowflux = ship.getCurrFlux();
            }
        }
    }

    private static class DataContainer {
        float exrflux = 0f;
        float nowflux = 0f;
    }
}
