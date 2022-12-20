package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.scripts.plugins.MagicRenderPlugin;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class fs_pearlshield extends BaseShipSystemScript {
    private float exrflux=0f;
    private float nowhardflux=0f;
    private float timer=0f;
    private float nowflux=0f;
    private float maxflux=0f;
    private boolean init=false;
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        boolean player = false;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
        }
        if(!init){
            init=true;
            nowflux=ship.getCurrFlux();
            nowhardflux=ship.getFluxTracker().getHardFlux();
        }
        timer+=Global.getCombatEngine().getElapsedInLastFrame();

        if(exrflux<=10000f+maxflux) {
            float length=exrflux / (10000f+maxflux);
            if (timer <= 1f) {
                ship.setJitter(ship,new Color(255,255,255,255),1f,1,3f);
                if (ship.getFluxTracker().getCurrFlux() >= nowflux) {
                    maxflux += ship.getFluxTracker().getCurrFlux() - nowflux;
                    ship.getFluxTracker().setCurrFlux(nowflux);
                }
                if (ship.getFluxTracker().getHardFlux() >= nowhardflux) {
                    ship.getFluxTracker().setHardFlux(nowhardflux);
                }
            } else {
                SpriteAPI sprite2 = Global.getSettings().getSprite("fs_systems", "fs_pshield2");
                MagicRenderPlugin.addSingleframe(sprite2, new Vector2f(ship.getLocation().getX() - ship.getSpriteAPI().getWidth() * 0.6f - 50f + 50f*length, ship.getLocation().getY() + ship.getSpriteAPI().getWidth() * 0.6f), CombatEngineLayers.ABOVE_SHIPS_LAYER);
                sprite2.setAngle(180f);
                sprite2.setAlphaMult(Math.min(1f,(timer-1f)));
                sprite2.setSize(100f * length, 20f);
                SpriteAPI sprite1 = Global.getSettings().getSprite("fs_systems", "fs_pshield1");
                MagicRenderPlugin.addSingleframe(sprite1, new Vector2f(ship.getLocation().getX() - ship.getSpriteAPI().getWidth() * 0.6f, ship.getLocation().getY() + ship.getSpriteAPI().getWidth() * 0.6f), CombatEngineLayers.ABOVE_SHIPS_LAYER);
                sprite1.setAngle(0f);
                sprite1.setAlphaMult(Math.min(1f,(timer-1f)));
                sprite1.setSize(100f, 20f);
                if (ship.getFluxTracker().getCurrFlux() >= nowflux) {
                    exrflux += ship.getFluxTracker().getCurrFlux() - nowflux;
                    ship.getFluxTracker().setCurrFlux(nowflux);
                }
                if (ship.getFluxTracker().getHardFlux() >= nowhardflux) {
                    ship.getFluxTracker().setHardFlux(nowhardflux);
                }
            }
            nowflux = ship.getFluxTracker().getCurrFlux();
            nowhardflux = ship.getFluxTracker().getHardFlux();
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
         exrflux=0f;
         nowhardflux=0f;
         nowflux=0f;
         init=false;
         timer=0f;
         maxflux=0f;
    }
}
