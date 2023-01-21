package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.scripts.plugins.MagicRenderPlugin;
import data.scripts.util.MagicUI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.EnumSet;

public class fs_pearlshield extends BaseShipSystemScript {
    private float exrflux=0f;
    private float nowhardflux=0f;
    private float timer=0f;
    private float nowflux=0f;
    private float maxflux=0f;
    private boolean init=false;
    private boolean plugininit=false;
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        if(!init){
            init=true;
            nowflux=ship.getCurrFlux();
            nowhardflux=ship.getFluxTracker().getHardFlux();
        }
        timer+=Global.getCombatEngine().getElapsedInLastFrame();

        if(exrflux<=10000f+maxflux) {
            if (timer <= 1f) {
                ship.setJitter(ship,new Color(255,255,255,255),1f,1,3f);
                if (ship.getFluxTracker().getCurrFlux() >= nowflux) {
                    maxflux += ship.getFluxTracker().getCurrFlux() - nowflux;
                    ship.getFluxTracker().setCurrFlux(nowflux);
                }//使舰船幅能总量不再上涨
                if (ship.getFluxTracker().getHardFlux() >= nowhardflux) {
                    ship.getFluxTracker().setHardFlux(nowhardflux);
                }//使舰船硬幅能水平不再上涨
            } else {
                 if (ship.getFluxTracker().getCurrFlux() >= nowflux) {
                    exrflux += ship.getFluxTracker().getCurrFlux() - nowflux;//计量被消除的幅能总增长数额。
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
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        if(!plugininit){
            plugininit=true;
            Global.getCombatEngine().addLayeredRenderingPlugin(new fs_pearlshieldplugin(ship));
        }
        //舰船进入战场，添加Plugin生成UI。
         exrflux=0f;
         nowhardflux=0f;
         nowflux=0f;
         init=false;
         timer=0f;
         maxflux=0f;
    }
    public class fs_pearlshieldplugin implements CombatLayeredRenderingPlugin {
        private final ShipAPI ships;
        public fs_pearlshieldplugin(ShipAPI ship) {
            ships=ship;
        }
        public void init(CombatEntityAPI entity) {

        }

        @Override
        public void cleanup() {

        }

        @Override
        public boolean isExpired() {
            return false;
        }

        @Override
        public void advance(float amount) {
            float length=exrflux / (10000f+maxflux);
            MagicUI.drawInterfaceStatusBar(ships,length,new Color(0, 166, 255, 218),new Color(32, 166, 178, 218),0,"力场",Math.round(exrflux));
            MagicUI.drawHUDStatusBar(ships,length,new Color(0, 166, 255, 218),new Color(32, 166, 178, 218),1,Math.round(exrflux)+"/"+Math.round(10000f+maxflux),"额外幅能管道",false);
        }

        @Override
        public EnumSet<CombatEngineLayers> getActiveLayers() {
            return null;
        }

        @Override
        public float getRenderRadius() {
            return 0;
        }

        @Override
        public void render(CombatEngineLayers layer, ViewportAPI viewport) {

        }

    }
}
