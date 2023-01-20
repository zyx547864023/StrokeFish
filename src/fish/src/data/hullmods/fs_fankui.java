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

public class fs_fankui extends BaseHullMod {
    private final float range=2000f;
    private final float damage=100f;
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float opad = 12.0F;
        Color highlight = new Color(255, 232, 87, 255);
        tooltip.addSectionHeading("数据分析", Alignment.MID, opad);


        LabelAPI label = tooltip.addPara(
                "舰船周围 %s范围内的敌方舰船使用战术系统时，\n敌方舰船将会根据舰船级别受到 %s/%s/%s/%s点能量伤害",
                opad, highlight, String.valueOf(Math.round(range)), String.valueOf(Math.round(damage)),String.valueOf(Math.round(damage*2f)),String.valueOf(Math.round(damage*3f)),String.valueOf(Math.round(damage*4f)));
        label.setHighlight(String.valueOf(Math.round(range)), String.valueOf(Math.round(damage)),String.valueOf(Math.round(damage*2f)),String.valueOf(Math.round(damage*3f)),String.valueOf(Math.round(damage*4f)));
        label.setHighlightColors(highlight, highlight, highlight, highlight, highlight, highlight, highlight);

    }
    public static final String KEY = "fs_Fankuilistener";

    private static class DataContainer {
        boolean init=false;
    }
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        for(ShipAPI target: Global.getCombatEngine().getShips()){
            if(!target.isFighter()&&target.getOwner()!=ship.getOwner()&&!target.isDrone()){
                float d = Vector2f.sub(ship.getLocation(), target.getLocation(), new Vector2f()).length() - ship.getShieldRadiusEvenIfNoShield() - target.getShieldRadiusEvenIfNoShield();
                //计算目标与舰船之间的距离。
                if (!target.getCustomData().containsKey(KEY)) {
                    DataContainer data = new DataContainer();
                    target.setCustomData(KEY, data);
                }
                DataContainer data = (DataContainer) target.getCustomData().get(KEY);
                if(d<range){
                    if(target.getSystem().isActive()) {
                        if (!data.init) {
                            data.init = true;
                            Fs_hullsizeint size=new Fs_hullsizeint();
                            if (target.getAllWeapons().size() > 0) {
                                Global.getCombatEngine().applyDamage(target, target.getAllWeapons().get(Math.max(0, (int) Math.round((target.getAllWeapons().size() - 1) * Math.random()))).getLocation(), damage*size.Getsize(target), DamageType.ENERGY, 0f, true, false, ship);
                            } else {
                                Global.getCombatEngine().applyDamage(target, target.getLocation(), damage*size.Getsize(target), DamageType.ENERGY, 0f, true, false, ship);
                            }
                        }
                    }
                        else {
                            data.init=false;
                        }

                }
            }
        }
    }
}
