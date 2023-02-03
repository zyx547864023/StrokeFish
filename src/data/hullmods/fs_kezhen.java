package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageListener;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.awt.*;

public class fs_kezhen extends BaseHullMod {
    public static final String KEY = "fs_Kezhenlistener";
    private final float godmodtime = 1.75f;
    private final float totaldamage = 5000f;

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float opad = 12.0F;
        Color highlight = new Color(255, 232, 87, 255);
        tooltip.addSectionHeading("数据分析", Alignment.MID, opad);


        LabelAPI label = tooltip.addPara(
                "舰船承受伤害总量超过 %s时，舰船将进入 %s秒的无敌时间",
                opad, highlight, String.valueOf(Math.round(totaldamage)), String.valueOf(godmodtime));
        label.setHighlight(String.valueOf(Math.round(totaldamage)), godmodtime + "秒");
        label.setHighlightColors(highlight, highlight, highlight, highlight, highlight, highlight, highlight);

    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

        if (!ship.hasListenerOfClass(MyDamageListener.class)) {
            ship.addListener(new MyDamageListener(ship));
        }
        if (!ship.getCustomData().containsKey(KEY)) {
            DataContainer data = new DataContainer();
            ship.setCustomData(KEY, data);
        }
        DataContainer data = (DataContainer) ship.getCustomData().get(KEY);
        if (data.damage > totaldamage) {
            data.damage = 0f;
            data.active = true;
        }
        if (data.active) {
            data.timer += amount;

            if (data.timer <= godmodtime && ship.isAlive()) {
                ship.getMutableStats().getHullDamageTakenMult().modifyMult(KEY, 0f);
                ship.getMutableStats().getArmorDamageTakenMult().modifyMult(KEY, 0f);
                ship.getMutableStats().getShieldDamageTakenMult().modifyMult(KEY, 0f);
            } else {
                data.timer = 0f;
                data.active = false;
                ship.getMutableStats().getHullDamageTakenMult().unmodifyMult(KEY);
                ship.getMutableStats().getArmorDamageTakenMult().unmodifyMult(KEY);
                ship.getMutableStats().getShieldDamageTakenMult().unmodifyMult(KEY);
            }
        }
    }

    private static class DataContainer {


        float damage = 0f;
        boolean active = false;
        float timer = 0f;
    }

    private static class MyDamageListener implements DamageListener {
        public ShipAPI ship;

        public MyDamageListener(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public void reportDamageApplied(Object source, CombatEntityAPI target, ApplyDamageResultAPI result) {
            float damage1 = result.getDamageToHull() + result.getDamageToShields() + result.getDamageToPrimaryArmorCell();
            if (!this.ship.getCustomData().containsKey(KEY)) {
                DataContainer data = new DataContainer();
                this.ship.setCustomData(KEY, data);
            }
            DataContainer data = (DataContainer) this.ship.getCustomData().get(KEY);
            data.damage += damage1;
        }
    }
}
