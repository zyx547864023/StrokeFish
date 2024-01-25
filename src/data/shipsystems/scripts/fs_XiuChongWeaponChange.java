package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.CollisionGridUtil;
import com.fs.starfarer.combat.entities.BallisticProjectile;
import data.scripts.plugins.MagicRenderPlugin;
import data.util.fs_Util;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;
import java.util.*;

public class fs_XiuChongWeaponChange extends BaseShipSystemScript {
    private CombatEngineAPI engine = Global.getCombatEngine();

    private final static String ID = "fs_XiuChongWeaponChange";
    private final static String WEAPON_ID = "fs_XiuChong_gun";
    private final static String PROJ_SPEC_ID = "fs_XiuChong_gun_scatter";
    private final static String FS_XIUCHONG_SHOT = "fs_XiuChong_shot";
    private final static String WEAPON_TYPE = "WEAPON_TYPE";
    private boolean isSet = false;
    private List<DamagingProjectileAPI> projectiles = new ArrayList<>();
    public static enum TYPE {
        SINGULAR,
        COMPLEX,
    }
    boolean init = false;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if(Global.getCombatEngine().isPaused()) return;
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        if (!init) {
            init = true;
            isSet = false;
            Global.getCombatEngine().addLayeredRenderingPlugin(new fs_XiuChongWeaponChangePlugin(ship));
        }
        if (!isSet&&state.equals(State.ACTIVE)) {
            if (ship.getCustomData().get(WEAPON_TYPE) == null) {
                //对发射前的所有弹丸进行一次标记
                projectiles = new ArrayList<>();
                for (DamagingProjectileAPI p : engine.getProjectiles()) {
                    //不在里面且是本船发射
                    if (projectiles.indexOf(p) == -1 && p.getSource() == ship) {
                        projectiles.add(p);
                    }
                }
                ship.setCustomData(WEAPON_TYPE, TYPE.COMPLEX);
            } else {
                if (ship.getCustomData().get(WEAPON_TYPE) == TYPE.COMPLEX) {
                    ship.setCustomData(WEAPON_TYPE, TYPE.SINGULAR);
                } else {
                    projectiles = new ArrayList<>();
                    for (DamagingProjectileAPI p : engine.getProjectiles()) {
                        //不在里面且是本船发射
                        if (projectiles.indexOf(p) == -1 && p.getSource() == ship) {
                            projectiles.add(p);
                        }
                    }
                    ship.setCustomData(WEAPON_TYPE, TYPE.COMPLEX);
                }
            }
            isSet = true;
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        isSet = false;
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) {return null;}
        if (ship.getCustomData().get(WEAPON_TYPE)==null) {
            return "切换成霰弹模式";
        }
        else {
            if (ship.getCustomData().get(WEAPON_TYPE)==TYPE.COMPLEX) {
                return "切换成压缩模式";
            }
            else {
                return "切换成霰弹模式";
            }
        }
    }

    public class fs_XiuChongWeaponChangePlugin extends BaseCombatLayeredRenderingPlugin {
        private final ShipAPI ship;

        public fs_XiuChongWeaponChangePlugin(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public void advance(float amount) {
            if (engine.isPaused()) return;
            if (!ship.isAlive()) return;
            try {
                //霰弹
                List<DamagingProjectileAPI> removeList = new ArrayList<>();
                if (ship.getCustomData().get(WEAPON_TYPE) == TYPE.COMPLEX) {
                    for (DamagingProjectileAPI p : engine.getProjectiles()) {
                        //不在里面且是本船发射
                        if (projectiles.indexOf(p) == -1 && p.getSource() == ship && FS_XIUCHONG_SHOT.equals(p.getProjectileSpecId())) {
                            for (WeaponAPI w : ship.getAllWeapons()) {
                                if (WEAPON_ID.equals(w.getSpec().getWeaponId())) {
                                    CombatEntityAPI newProjectile = null;
                                    for (int i = 0; i < 30; i++) {
                                        String thisProjSpecId = PROJ_SPEC_ID + MathUtils.getRandomNumberInRange(1, 3);

                                        //Global.getLogger(this.getClass()).info(thisProjSpecId);
                                        newProjectile = engine.spawnProjectile(ship, null,
                                                thisProjSpecId, p.getLocation(), p.getFacing() + MathUtils.getRandomNumberInRange(-30, 30),
                                                ship.getVelocity());
                                        Vector2f newVelocity = (Vector2f) ((BallisticProjectile)newProjectile).getVelocity().scale(MathUtils.getRandomNumberInRange(1.1f, 2f));
                                        ((BallisticProjectile)newProjectile).getVelocity().set(newVelocity);
                                    }
                                    removeList.add(p);
                                }
                            }
                        }

                    }
                    for (DamagingProjectileAPI r : removeList) {
                        engine.removeObject(r);
                    }
                }
            }
            catch (Exception e)
            {
                Global.getLogger(this.getClass()).info(e);
            }
        }

        public void init(CombatEngineAPI engine) {

        }

        @Override
        public float getRenderRadius() {
            return 0f;
        }

        @Override
        public EnumSet<CombatEngineLayers> getActiveLayers() {
            return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_LAYER);
        }

        @Override
        public void render(CombatEngineLayers layer, ViewportAPI viewport) {

        }

        @Override
        public boolean isExpired() {
            return false;
        }
    }
}