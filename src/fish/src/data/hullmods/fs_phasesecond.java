package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModPlugin;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class fs_phasesecond extends BaseHullMod {

    public static final String KEY = "fs_phasesecondlistener";
    private static final IntervalUtil Interval = new IntervalUtil(0.3f, 0.5f);
    private final float totaltime = 8f;
    private final float redaytime = 10f;//totaltime不能大于readytime

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float opad = 12.0F;
        Color highlight = new Color(255, 232, 87, 255);
        tooltip.addSectionHeading("数据分析", Alignment.MID, opad);


        LabelAPI label = tooltip.addPara(
                "舰船进入相位时，会向舰船右侧产生一个完全一致的幻影分身，幻影分身的相位状态与本体保持同步且在 %s秒后消散，同时只能存在一个幻影分身。\n\n该效果需要一段时间预热，在进入战斗后 %s秒生效。",
                opad, highlight, String.valueOf(Math.round(totaltime)), String.valueOf(Math.round(redaytime)));
        label.setHighlight(String.valueOf(Math.round(totaltime)), String.valueOf(Math.round(redaytime)));
        label.setHighlightColors(highlight, highlight, highlight, highlight, highlight, highlight, highlight);

    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

        if (!ship.getCustomData().containsKey(KEY)) {
            DataContainer data = new DataContainer();
            ship.setCustomData(KEY, data);
        }
        DataContainer data = (DataContainer) ship.getCustomData().get(KEY);
        data.timer1 += amount/ship.getMutableStats().getTimeMult().getMult();
        if (data.timer1 >= redaytime) {
            if (ship.isPhased() && data.newShip == null) {
                if (!data.init) {
                    data.init = true;
                    data.active = true;
                }
            }
            if (!ship.isPhased() && !data.active) {
                data.init = false;
            }

            if (data.active) {
                if (data.newShip == null) {
                    FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, ship.getVariant());
                    data.newShip = Global.getCombatEngine().getFleetManager(ship.getOwner()).spawnFleetMember(member, ship.getLocation(), ship.getFacing(), 0f);
                    data.newShip.setHitpoints(ship.getHitpoints());
                    data.newShip.setCurrentCR(ship.getCurrentCR());
                    data.newShip.setCRAtDeployment(ship.getCurrentCR());
                    data.newShip.setControlsLocked(false);
                    data.newShip.setAlphaMult(1);
                    data.newShip.getFluxTracker().setCurrFlux(ship.getCurrFlux());
                    data.newShip.getFluxTracker().setHardFlux(ship.getFluxTracker().getHardFlux());
                    float[][] grid1 = ship.getArmorGrid().getGrid();
                    for (int x = 0; x < grid1.length; x++)
                        for (int y = 0; y < grid1[0].length; y++) {
                            data.newShip.getArmorGrid().setArmorValue(x, y, grid1[x][y]);
                        }
                } else {
                    data.newShip.getPhaseCloak().setCooldown(0f);
                    if(!data.newShip.getFluxTracker().isOverloaded()||!data.newShip.getFluxTracker().isVenting()) {
                        if (ship.getPhaseCloak().isActive() && !ship.getPhaseCloak().isChargedown()) {
                            data.newShip.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
                            if (data.newShip.getPhaseCloak().isChargeup()) {
                                data.use = true;
                            }
                            if (data.use && !data.newShip.getPhaseCloak().isActive()) {
                                data.newShip.getPhaseCloak().forceState(ShipSystemAPI.SystemState.ACTIVE, 0f);
                            }
                            if (!data.newShip.getPhaseCloak().isActive()) {
                                data.use = true;
                                data.newShip.getPhaseCloak().forceState(ShipSystemAPI.SystemState.IN, 0f);
                            }
                        } else if (data.newShip.getPhaseCloak().isStateActive()) {
                            data.use = false;
                            data.newShip.getPhaseCloak().forceState(ShipSystemAPI.SystemState.OUT, 0f);
                        } else if (!data.newShip.getPhaseCloak().isChargedown()) {
                            data.use = false;
                            data.newShip.getPhaseCloak().deactivate();
                        }
                    }
                }
                data.timer += amount;
                if (data.timer >= totaltime - 1f && data.timer < totaltime) {
                    data.newShip.setAlphaMult(totaltime - data.timer);
                }
                if (data.timer <= 1f) {
                    data.newShip.getLocation().set(new Vector2f(ship.getLocation().getX() + data.timer * 300f * (float) Math.cos(Math.toRadians(ship.getFacing() - 90f)), ship.getLocation().getY() + data.timer * 300f * (float) Math.sin(Math.toRadians(ship.getFacing() - 90f))));
                    Interval.advance(Global.getCombatEngine().getElapsedInLastFrame());
                    if (Interval.intervalElapsed()) {
                        Global.getCombatEngine().spawnEmpArcPierceShields(ship, ship.getLocation(), new SimpleEntity(data.newShip.getLocation()), new SimpleEntity(data.newShip.getLocation()), DamageType.ENERGY, 0f, 0f, 1000000f, null, 5f, new Color(20, 88, 143, 218), new Color(61, 59, 59, 218));
                    }
                }
                if (data.timer >= totaltime) {
                    if (data.newShip != null) {
                        data.newShip.setAlphaMult(0f);
                        data.newShip.setHitpoints(-2000f);
                        data.newShip = null;
                    }
                    data.timer = 0f;
                    data.active = false;
                }

            }else {
                data.use=false;
            }
        }
    }

    private static class DataContainer {

        ShipAPI newShip;
        boolean active = false;
        float timer = 0f;
        float timer1 = 0f;
        boolean init = false;
        boolean use=false;
    }
}
