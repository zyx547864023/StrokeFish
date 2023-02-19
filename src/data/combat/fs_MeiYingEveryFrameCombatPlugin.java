package data.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.combat.listeners.WeaponRangeModifier;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.plugins.MagicRenderPlugin;
import data.shipsystems.scripts.fs_MeiYingField;
import data.util.fs_Util;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class fs_MeiYingEveryFrameCombatPlugin implements EveryFrameCombatPlugin {
    public static final float size_max = 180;
    public static final float deviation_max = 190;
    public String ID = "fs_MeiYingField";
    public String MISSILE_DATA_ID = "fs_MeiYingEveryFrameCombatPlugin";
    public static final Color JITTER_COLOR = new Color(255,155,255,75);
    public static final Color COLOR = new Color(255,255,255,100);
    protected boolean showWhenInterfaceIsHidden = false;
    protected boolean hide(CombatEngineAPI engine){
        if (engine == null || engine.getCombatUI() == null || engine.getPlayerShip() == null) return true;
        if(!engine.getPlayerShip().isAlive() || engine.getPlayerShip().isHulk()) return true;
        if (engine.getCombatUI().isShowingCommandUI() || engine.isUIShowingDialog()) return true;
        return !engine.isUIShowingHUD() && !showWhenInterfaceIsHidden;
    }

    public void advance(float amount, List<InputEventAPI> events) {
        //if (Global.getCombatEngine().isPaused()) return;
        CombatEngineAPI engine = Global.getCombatEngine();
        if(hide(engine)) return;
        ViewportAPI viewport = engine.getViewport();
        float alphaMult = viewport.getAlphaMult();
        if(alphaMult <= 0f) return;
        List<ShipAPI> shipList = engine.getShips();
        fs_Util.LocalData localData = (fs_Util.LocalData)engine.getCustomData().get(ID);
        fs_Util.MissilesData missilesData = (fs_Util.MissilesData)engine.getCustomData().get(MISSILE_DATA_ID);
        Map<ShipAPI, fs_Util.MineData> mineData = localData.mineData;
        Map<MissileAPI, fs_Util.MissileData> missileData = missilesData.missileData;

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

        for(ShipAPI s:shipList)
        {
            if(s.getSystem()!=null) {
                if (s.getSystem().isActive() && "魅影力场".equals(s.getSystem().getDisplayName())) {
                    float f = MathUtils.getRandom().nextFloat();
                    int i = (int) f;
                    float shipjitterLevel = f-i+2;
                    s.setJitterUnder(this, JITTER_COLOR, shipjitterLevel, 11, 0, 3f + shipjitterLevel);
                    s.setJitter(this, JITTER_COLOR, shipjitterLevel, 4, 0, 0 + shipjitterLevel);

                    fs_Util.MineData m = mineData.get(s);
                    Vector2f leftPonit = MathUtils.getPoint(s.getLocation(), m.deviation+shipjitterLevel*5, s.getFacing() - 60);
                    Vector2f rightPonit = MathUtils.getPoint(s.getLocation(), m.deviation+shipjitterLevel*5, s.getFacing() + 60);
                    drawArc(JITTER_COLOR, viewport.getAlphaMult(), 361f, leftPonit, m.size, 0f, 0f, 0f, 0f, m.size/100+3);
                    drawArc(JITTER_COLOR, viewport.getAlphaMult(), 361f, rightPonit, m.size, 0f, 0f, 0f, 0f, m.size/100+3);
                    SpriteAPI leftSprite = Global.getSettings().getSprite("fs_beamsystem", "fs_bs_1");
                    MagicRenderPlugin.addSingleframe(leftSprite, leftPonit, CombatEngineLayers.ABOVE_SHIPS_LAYER);
                    leftSprite.setColor(JITTER_COLOR);
                    leftSprite.setSize((float) (m.size*2.2),(float) (m.size*2.2));
                    leftSprite.setAlphaMult(viewport.getAlphaMult());
                    SpriteAPI rightSprite = Global.getSettings().getSprite("fs_beamsystem", "fs_bs_1");
                    MagicRenderPlugin.addSingleframe(rightSprite, rightPonit, CombatEngineLayers.ABOVE_SHIPS_LAYER);
                    rightSprite.setColor(JITTER_COLOR);
                    rightSprite.setSize((float) (m.size*2.2),(float) (m.size*2.2));
                    rightSprite.setAlphaMult(viewport.getAlphaMult());
                    if (m.size < size_max) {
                        m.size = m.size + size_max * amount;
                    }
                    if (m.deviation < deviation_max) {
                        m.deviation = m.deviation + deviation_max * amount;
                    }

                    List<DamagingProjectileAPI> damagingProjectiles = engine.getProjectiles();
                    List<MissileAPI> destroyList = new ArrayList<>();
                    for (DamagingProjectileAPI damagingProjectile : damagingProjectiles) {
                        if (MathUtils.getDistance(leftPonit, damagingProjectile.getLocation()) <= m.deviation||MathUtils.getDistance(rightPonit, damagingProjectile.getLocation()) <= m.deviation) {
                            if(damagingProjectile instanceof MissileAPI) {
                                MissileAPI missile = (MissileAPI) damagingProjectile;
                                if(missile.getOwner()!=0&&missile.isGuided()) {
                                    missile.setFlightTime(missile.getMaxFlightTime());
                                    missile.setArmingTime(missile.getMaxFlightTime());
                                    missile.setFacing(s.getFacing());
                                    if (missileData.get(missile) == null) {
                                        missileData.put(missile, new fs_Util.MissileData(missile.getMaxSpeed(), missile.getMaxSpeed()));
                                    } else {
                                        fs_Util.MissileData missileDataNow = missileData.get(missile);
                                        float max = missileDataNow.max;
                                        float now = missileDataNow.now;
                                        if (now > 0) {
                                            now -= max * amount;
                                            missile.getVelocity().set(MathUtils.getPoint(new Vector2f(0, 0), now, missile.getFacing() + 180));
                                            missileData.put(missile, new fs_Util.MissileData(missile.getMaxSpeed(), now));
                                        } else {
                                            destroyList.add(missile);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    for (MissileAPI missileAPI:destroyList){
                        Vector2f oldLocation = missileAPI.getLocation();
                        String oldId =  missileAPI.getWeapon().getSpec().getWeaponId();
                        DamagingProjectileAPI newProjectile = (DamagingProjectileAPI)engine.spawnProjectile(s, null,
                                oldId,
                                oldLocation,
                                s.getFacing(),
                                null);
                        MissileAPI newMissile = (MissileAPI) newProjectile;
                        Global.getCombatEngine().applyDamageModifiersToSpawnedProjectileWithNullWeapon(
                                s, WeaponAPI.WeaponType.MISSILE, false, newMissile.getDamage());
                        engine.getProjectiles().remove(missileAPI);
                    }
                }
            }
        }
    }

    public void renderInUICoords(ViewportAPI viewport) {
    }

    public void init(CombatEngineAPI engine) {
        engine.getCustomData().put(ID, new fs_Util.LocalData());
        engine.getCustomData().put(MISSILE_DATA_ID, new fs_Util.MissilesData());
    }

    public void renderInWorldCoords(ViewportAPI viewport) {

    }

    public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {

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
    }
}
