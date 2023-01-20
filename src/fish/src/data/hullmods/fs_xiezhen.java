package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.loading.S;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class fs_xiezhen extends BaseHullMod {
    private final float range=2000f;
    private final float effect=0.1f;
    private final float maxeffect=0.5f;
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float opad = 12.0F;
        Color highlight = new Color(255, 232, 87, 255);
        tooltip.addSectionHeading("数据分析", Alignment.MID, opad);


        LabelAPI label = tooltip.addPara(
                "舰船周身 %s范围内己每增加一艘己方舰船，\n舰船实弹与能量武器伤害提升 %s%%，且最高不超过 %s%%，\n舰船护盾效率随之提升 一半的水平",
                opad, highlight, String.valueOf(Math.round(range)), String.valueOf(effect*100f),String.valueOf(maxeffect*100f));
        label.setHighlight(String.valueOf(Math.round(range)), effect*100f+"%",maxeffect*100f+"%","一半");
        label.setHighlightColors(highlight, highlight, highlight, highlight, highlight, highlight, highlight);

    }
    private final Object fs_xiezhen=new Object();

    private final String id="fs_xiezheneffect";
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        float num=0f;
        for(ShipAPI target: Global.getCombatEngine().getShips()){
            if(!target.isFighter()&&target.getOwner()==ship.getOwner()&&!target.isDrone()){
                float d = Vector2f.sub(ship.getLocation(), target.getLocation(), new Vector2f()).length() - ship.getShieldRadiusEvenIfNoShield() - target.getShieldRadiusEvenIfNoShield();
                //计算目标与舰船之间的距离。
                if (d <= range) {
                    num = num + 1f;
                }
            }
        }
        MutableShipStatsAPI stats=ship.getMutableStats();
        stats.getEnergyWeaponDamageMult().modifyMult(id,Math.min(1f+num*effect,1f+maxeffect));
        stats.getBallisticWeaponDamageMult().modifyMult(id,Math.min(1f+num*effect,1f+maxeffect));
        stats.getShieldDamageTakenMult().modifyMult(id,Math.min(1f+num*effect*0.5f,1f+maxeffect*0.5f));
        boolean player = false;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
        }
        if(player){
            Global.getCombatEngine().maintainStatusForPlayerShip(fs_xiezhen, "graphics/icons/hullsys/drone_pd_mid.png","谐振舰船数目",Math.round(num)+ "艘", false);
        }
    }
}
