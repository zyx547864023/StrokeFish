package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.plugins.MagicRenderPlugin;
import data.util.fs_Util;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.*;
import java.util.List;

public class fs_MeiYingField extends BaseShipSystemScript {
    private String ID = "fs_MeiYingField";
    boolean init = false;

    public final Map<MissileAPI, MissileAPI> destroyMap = new HashMap<>();

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if(Global.getCombatEngine().isPaused()) return;
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        if (!init) {
            init = true;
            Global.getCombatEngine().addLayeredRenderingPlugin(new fs_MeiYingEveryFrameCombatPlugin(ship,new HashMap<MissileAPI, MissileAPI>()));
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        //关闭的时候把力场圆拿出来并
        if(Global.getCombatEngine().isPaused()) return;
        init = false;
    }

    public static class fs_MeiYingEveryFrameCombatPlugin extends BaseCombatLayeredRenderingPlugin {
        private float size = 0f;
        private float deviation = 0f;
        private float timer = 0f;
        private float alpha = 1f;
        private Vector2f leftPonit = new Vector2f();
        private Vector2f rightPonit = new Vector2f();
        private float shipjitterLevel = 0.1f;
        private static final float size_max = 180f;
        private static final float deviation_max = 190f;
        private static final Color JITTER_COLOR = new Color(255, 155, 255, 75);
        private static final Color COLOR = new Color(255, 255, 255, 100);
        private final ShipAPI ship;
        Map<MissileAPI, MissileAPI> destroyMap;
        CombatEngineAPI engine;
        public fs_MeiYingEveryFrameCombatPlugin(ShipAPI ship,Map<MissileAPI, MissileAPI> destroyMap) {
            this.ship = ship;
            this.destroyMap = destroyMap;
        }

        @Override
        public void advance(float amount) {
            engine = Global.getCombatEngine();
            if (engine.isPaused()) return;
            if (!ship.isAlive()) return;
            if (ship.isPhased()&&timer<4) timer = 4;
            timer += amount;

            ViewportAPI viewport = engine.getViewport();
            float alphaMult = viewport.getAlphaMult();
            if (alphaMult <= 0f) return;

            if (ship.getSystem() != null) {
                if (ship.getSystem().isActive() && "魅影力场".equals(ship.getSystem().getDisplayName())) {
                    float f = MathUtils.getRandom().nextFloat();
                    int i = (int) f;
                    shipjitterLevel = (float)(f-i+2);

                    leftPonit = MathUtils.getPoint(ship.getLocation(), deviation + shipjitterLevel * 5, ship.getFacing() - 60);
                    rightPonit = MathUtils.getPoint(ship.getLocation(), deviation + shipjitterLevel * 5, ship.getFacing() + 60);

                    alpha = viewport.getAlphaMult();
                    if (timer > 4) {
                        alpha = 5 - timer;
                    }

                    if (size < size_max) {
                        size = size + size_max * amount;
                    }
                    if (deviation < deviation_max) {
                        deviation = deviation + deviation_max * amount;
                    }

                    java.util.List<DamagingProjectileAPI> damagingProjectiles = engine.getProjectiles();
                    for (DamagingProjectileAPI damagingProjectile : damagingProjectiles) {
                        if (damagingProjectile instanceof MissileAPI) {
                            MissileAPI missile = (MissileAPI) damagingProjectile;
                            if (missile.getOwner() != ship.getOwner() && missile.isGuided()) {
                                turnMissile(leftPonit,missile,amount);
                                turnMissile(rightPonit,missile,amount);
                            }
                        }
                    }
                }
            }
        }

        public void init(CombatEngineAPI engine) {

        }

        @Override
        public float getRenderRadius() {
            return 1000000f;
        }

        @Override
        public EnumSet<CombatEngineLayers> getActiveLayers() {
            return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_LAYER);
        }

        @Override
        public void render(CombatEngineLayers layer, ViewportAPI viewport) {
            //if (engine.isPaused()) return;
            if (fs_Util.hide(engine)) return;
            if (!ship.isAlive()) return;
            if (ship.getSystem().isActive() && "fs_MeiYingField".equals(ship.getSystem().getId())) {
                /*
                RippleDistortion leftRipple = new RippleDistortion(leftPonit, new Vector2f());
                leftRipple.setSize(size-10);
                leftRipple.setIntensity(25f);
                leftRipple.fadeInSize(0.2f);
                leftRipple.fadeInIntensity(0.2f);
                leftRipple.setFrameRate(45f);
                DistortionShader.addDistortion(leftRipple);//生成扭曲效果

                RippleDistortion rightRipple = new RippleDistortion(rightPonit, new Vector2f());
                rightRipple.setSize(size-10);
                rightRipple.setIntensity(25f);
                rightRipple.fadeInSize(0.2f);
                rightRipple.fadeInIntensity(0.2f);
                rightRipple.setFrameRate(45f);
                DistortionShader.addDistortion(rightRipple);//生成扭曲效果
                */
                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glPushMatrix();
                GL11.glLoadIdentity();
                GL11.glOrtho(viewport.getLLX(), viewport.getLLX() + viewport.getVisibleWidth(), viewport.getLLY(),
                        viewport.getLLY() + viewport.getVisibleHeight(), -1,
                        1);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glPushMatrix();
                GL11.glLoadIdentity();
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                GL11.glTranslatef(0.01f, 0.01f, 0);

                drawArc(JITTER_COLOR, alpha, 722f, leftPonit, size, 0f, 0f, 0f, 0f, size / 100 / viewport.getViewMult() + 3);
                drawArc(JITTER_COLOR, alpha, 722f, rightPonit, size, 0f, 0f, 0f, 0f, size / 100 / viewport.getViewMult() + 3);

                /*
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glPopMatrix();
                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glPopMatrix();
                GL11.glPopAttrib();
                */

                ship.setJitterUnder(this, JITTER_COLOR, shipjitterLevel, 11, 0, 3f + shipjitterLevel);
                ship.setJitter(this, JITTER_COLOR, shipjitterLevel, 4, 0, 0 + shipjitterLevel);

                if(engine!=null)
                    if(engine.isPaused()) return;
                SpriteAPI leftSprite = Global.getSettings().getSprite("fs_beamsystem", "fs_bs_1");
                MagicRenderPlugin.addSingleframe(leftSprite, leftPonit, CombatEngineLayers.ABOVE_SHIPS_LAYER);
                leftSprite.setColor(JITTER_COLOR);
                leftSprite.setSize((float) (size * 2.2), (float) (size * 2.2));
                leftSprite.setAlphaMult(alpha*255);
                SpriteAPI rightSprite = Global.getSettings().getSprite("fs_beamsystem", "fs_bs_1");
                MagicRenderPlugin.addSingleframe(rightSprite, rightPonit, CombatEngineLayers.ABOVE_SHIPS_LAYER);
                rightSprite.setColor(JITTER_COLOR);
                rightSprite.setSize((float) (size * 2.2), (float) (size * 2.2));
                rightSprite.setAlphaMult(alpha*255);
            }
        }

        @Override
        public boolean isExpired() {
            if(timer >= 5f)
            {
                for (MissileAPI missileAPI : destroyMap.keySet()) {
                    Vector2f oldLocation = missileAPI.getLocation();
                    if(MathUtils.getDistance(leftPonit, missileAPI.getLocation()) <= deviation||MathUtils.getDistance(rightPonit, missileAPI.getLocation()) <= deviation) {
                        if(missileAPI.getWeapon()!=null)
                            if(missileAPI.getWeapon().getSpec()!=null) {
                                String oldId = missileAPI.getWeapon().getSpec().getWeaponId();
                                if(oldId!=null) {
                                    if (ship != null) {
                                        try {
                                            DamagingProjectileAPI newProjectile = (DamagingProjectileAPI) engine.spawnProjectile(ship, null,
                                                    oldId,
                                                    oldLocation,
                                                    ship.getFacing(),
                                                    ship.getVelocity());
                                            MissileAPI newMissile = (MissileAPI) newProjectile;
                                            engine.applyDamageModifiersToSpawnedProjectileWithNullWeapon(
                                                    ship, WeaponAPI.WeaponType.MISSILE, false, newMissile.getDamage());
                                            engine.removeObject(missileAPI);
                                        }catch (Exception e)
                                        {
                                            Global.getLogger(this.getClass()).info(e);
                                        }

                                    }
                                }
                            }
                    }
                }
            }
            return timer >= 5f;
        }

        //private void turnMissile(Vector2f point,float deviation,ShipAPI ship,MissileAPI missile,Map destroyMap,float amount)
        private void turnMissile(Vector2f point,MissileAPI missile,float amount)
        {
            if (MathUtils.getDistance(point, missile.getLocation()) <= deviation) {
                missile.setFlightTime(missile.getMaxFlightTime());
                missile.setArmingTime(missile.getMaxFlightTime());
                float turn = MathUtils.getShortestRotation(missile.getFacing(),ship.getFacing());
                float turnRate = missile.getMaxTurnRate()*amount;
                if(Math.abs(turn)>5)//5
                {
                    if(turn>0) {
                        missile.setFacing(MathUtils.clampAngle(missile.getFacing() + 5));
                    }
                    else {
                        missile.setFacing(MathUtils.clampAngle(missile.getFacing() - 5));
                    }
                }
                else {
                    missile.setFacing(ship.getFacing());
                }
                missile.getVelocity().set(new Vector2f((point.x - missile.getLocation().x)*3, (point.y - missile.getLocation().y)*3));
                if(destroyMap.get(missile)==null)
                {
                    destroyMap.put(missile,missile);
                }
            }
        }

        private void drawArc(Color color, float alpha, float angle, Vector2f loc, float radius, float aimAngle, float aimAngleTop, float x, float y, float thickness){
            GL11.glLineWidth(thickness);
            GL11.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte)Math.max(0, Math.min(Math.round(alpha * 255f), 255)) );
            GL11.glBegin(GL11.GL_LINE_STRIP);
            for(int i = 0; i < Math.round(angle); i++){
                GL11.glVertex2f(
                        loc.x + (radius * (float)Math.cos(Math.toRadians(aimAngleTop + i)) + x * (float)Math.cos(Math.toRadians(aimAngle - 90f)) - y * (float)Math.sin(Math.toRadians(aimAngle - 90f))),
                        loc.y + (radius * (float)Math.sin(Math.toRadians(aimAngleTop + i)) + x * (float)Math.sin(Math.toRadians(aimAngle - 90f)) + y * (float)Math.cos(Math.toRadians(aimAngle - 90f)))
                );
            }
            GL11.glEnd();
            GL11.glPopMatrix();
        }
    }
}