package data.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageListener;
import com.fs.starfarer.api.util.Misc;
import data.util.fs_Multi;
import data.util.fs_Util;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class fs_LightningGunOnFireEffect implements OnFireEffectPlugin {

    private static final float BASE_DAMAGE = 200f;
    private static final Color HIT_COLOR = new Color(211, 27, 217);
    private static final float SLOP_RANGE = 200f;
    private static final EnumSet<CollisionClass> ALLOWED_COLLISIONS
            = EnumSet.of(CollisionClass.ASTEROID, CollisionClass.FIGHTER, CollisionClass.MISSILE_FF, CollisionClass.MISSILE_NO_FF, CollisionClass.SHIP);
    private static final Vector2f ZERO = new Vector2f();

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if (projectile == null) {
            return;
        }

        if (!engine.getListenerManager().hasListenerOfClass(LightningGunListener.class)) {
            engine.getListenerManager().addListener(new LightningGunListener());
        }

        CombatEntityAPI target = null;
        Vector2f endpoint = new Vector2f(projectile.getWeapon().getRange() + SLOP_RANGE, 0f);
        VectorUtils.rotate(endpoint, projectile.getFacing(), endpoint);
        Vector2f.add(projectile.getSpawnLocation(), endpoint, endpoint);

        Vector2f point = new Vector2f(endpoint);
        Vector2f visualPoint = point;

        List<CombatEntityAPI> entitiesToCheck = CombatUtils.getEntitiesWithinRange(projectile.getSpawnLocation(), projectile.getWeapon().getRange() + SLOP_RANGE);

        /* Easy filters */
        Iterator<CombatEntityAPI> iter = entitiesToCheck.iterator();
        while (iter.hasNext()) {
            CombatEntityAPI entity = iter.next();
            if ((entity == projectile.getSource()) || (entity == projectile)) {
                iter.remove();
                continue;
            }
            if (!ALLOWED_COLLISIONS.contains(entity.getCollisionClass())) {
                iter.remove();
                continue;
            }
            if ((entity.getCollisionClass() != CollisionClass.SHIP) && (entity.getOwner() == projectile.getOwner())) {
                iter.remove();
                continue;
            }
            if ((projectile.getSource() != null) && (fs_Multi.getRoot(projectile.getSource()) == entity)) {
                iter.remove();
                continue;
            }
            if (entity instanceof ShipAPI) {
                ShipAPI ship = (ShipAPI) entity;
                if ((projectile.getSource() != null) && (fs_Multi.getRoot(ship) == projectile.getSource())) {
                    iter.remove();
                }
            }
        }

        /* Hard filters */
        Collections.sort(entitiesToCheck, new CollectionUtils.SortEntitiesByDistance(projectile.getSpawnLocation()));
        iter = entitiesToCheck.iterator();
        while (iter.hasNext()) {
            CombatEntityAPI entity = iter.next();
            if (CollisionUtils.getCollides(projectile.getSpawnLocation(), endpoint, entity.getLocation(), entity.getCollisionRadius())) {
                if (CollisionUtils.isPointWithinCollisionCircle(projectile.getSpawnLocation(), entity)) {
                    if (CollisionUtils.isPointWithinBounds(projectile.getSpawnLocation(), entity)) {
                        point = projectile.getSpawnLocation();
                        visualPoint = projectile.getSpawnLocation();
                        Vector2f extra = new Vector2f(25f, 0f);
                        VectorUtils.rotate(extra, projectile.getFacing(), extra);
                        Vector2f.add(visualPoint, extra, visualPoint);
                        target = entity;
                        break;
                    }
                }
                if (entity instanceof ShipAPI) {
                    /* Shield (near side) */
                    ShipAPI ship = (ShipAPI) entity;
                    if ((ship.getShield() != null) && ship.getShield().isOn()) {
                        Vector2f collision = fs_Util.getCollisionRayCircle(projectile.getSpawnLocation(), endpoint, ship.getShield().getLocation(), ship.getShield().getRadius(), true);
                        if ((collision != null) && Misc.isInArc(ship.getShield().getFacing(), ship.getShield().getActiveArc(), ship.getShield().getLocation(), collision)) {
                            point = collision;
                            visualPoint = collision;
                            target = entity;
                            break;
                        }
                    }
                    Vector2f collision = CollisionUtils.getCollisionPoint(projectile.getSpawnLocation(), endpoint, entity);
                    if (collision != null) {
                        point = collision;
                        visualPoint = collision;
                        target = entity;
                        break;
                    }
                    /* Shield (back side) */
                    if ((ship.getShield() != null) && ship.getShield().isOn()) {
                        collision = fs_Util.getCollisionRayCircle(projectile.getSpawnLocation(), endpoint, ship.getShield().getLocation(), ship.getShield().getRadius(), false);
                        if ((collision != null) && Misc.isInArc(ship.getShield().getFacing(), ship.getShield().getActiveArc(), ship.getShield().getLocation(), collision)) {
                            point = collision;
                            visualPoint = collision;
                            target = entity;
                            break;
                        }
                    }
                } else {
                    Vector2f collision = fs_Util.getCollisionRayCircle(projectile.getSpawnLocation(), endpoint, entity.getLocation(), entity.getCollisionRadius(), true);
                    if (collision != null) {
                        point = collision;
                        visualPoint = collision;
                        target = entity;
                        break;
                    }
                }
            }
        }

        /* Move back to sanity */
        Vector2f difference = Vector2f.sub(point, projectile.getSpawnLocation(), new Vector2f());
        float length = difference.length();
        if (length >= (projectile.getWeapon().getRange() + SLOP_RANGE)) {
            difference.scale((projectile.getWeapon().getRange() + SLOP_RANGE) / length);
            Vector2f.add(projectile.getSpawnLocation(), difference, point);
            visualPoint = point;
        }

        float distance = MathUtils.getDistance(projectile.getSpawnLocation(), point);
        if (distance < 0.1f) {
            distance = 10f;
        }
        float atten = 1f;
        if (distance > projectile.getWeapon().getRange()) {
            atten = 1f - ((distance - projectile.getWeapon().getRange()) / (SLOP_RANGE * 2f));
        }
        float thickness = 10f * atten;
        float coreWidth = thickness * 0.65f;
        int brightness = (int) (255f * atten);

        StandardLight light = new StandardLight(projectile.getSpawnLocation(), point, ZERO, ZERO, null);
        light.setIntensity(0.65f * atten);
        light.setSize(80f * atten);
        light.setColor(0.65f, 0.75f, 0.9f);
        light.fadeOut(0.35f);
        LightShader.addLight(light);

        EmpArcEntityAPI arc = engine.spawnEmpArcVisual(projectile.getSpawnLocation(), projectile.getSource(), visualPoint, target, thickness,
                new Color(211, 27, 217, fs_Util.clamp255(brightness)), new Color(240, 250, 255, fs_Util.clamp255(brightness)));
        arc.setCoreWidthOverride(coreWidth);
        arc.setSingleFlickerMode();

        EmpArcEntityAPI arc2 = engine.spawnEmpArcVisual(projectile.getSpawnLocation(), projectile.getSource(), visualPoint, target, thickness,
                new Color(211, 27, 217, fs_Util.clamp255(brightness)), new Color(240, 250, 255, fs_Util.clamp255(brightness)));
        arc2.setCoreWidthOverride(coreWidth);
        arc2.setSingleFlickerMode();

        EmpArcEntityAPI arc3 = engine.spawnEmpArcVisual(projectile.getSpawnLocation(), projectile.getSource(), visualPoint, target, thickness,
                new Color(211, 27, 217, fs_Util.clamp255(brightness)), new Color(240, 250, 255, fs_Util.clamp255(brightness)));
        arc3.setCoreWidthOverride(coreWidth);
        arc3.setSingleFlickerMode();

        if (target != null) {
            float emp = projectile.getEmpAmount() * atten;
            float dam = projectile.getDamageAmount() * atten;
            projectile.getLocation().set(point);
            engine.applyDamage(projectile, target, point, dam, DamageType.ENERGY, emp, false, atten < 1f, projectile, true);
            //Global.getSoundPlayer().playSound("swp_lightning_gun_arc", 1f, 0.55f * atten, visualPoint, ZERO);
        }

        projectile.setCollisionClass(CollisionClass.NONE);
    }

    private static final class LightningGunListener implements DamageListener {

        @Override
        public void reportDamageApplied(Object source, CombatEntityAPI target, ApplyDamageResultAPI result) {
            if ((source instanceof DamagingProjectileAPI) && (target != null)) {
                DamagingProjectileAPI proj = (DamagingProjectileAPI) source;
                if ((proj.getProjectileSpecId() != null) && proj.getProjectileSpecId().contentEquals("fs_MeiYing_lightninggun_shot")) {
                    float hitGlowSize = Misc.getHitGlowSize(100f, BASE_DAMAGE, result);
                    Global.getCombatEngine().addHitParticle(proj.getLocation(), target.getVelocity(), hitGlowSize, 1f, HIT_COLOR);
                }
            }
        }
    }
}
