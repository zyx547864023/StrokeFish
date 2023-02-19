package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class fs_armorremoved extends BaseShipSystemScript {
    private final String id1 = "fs_armorremovedsystem";
    private final String id2 = "fs_armorremovedsystem1";
    private final Map<Integer, String> FS_ARMORREMOVELEFT;
    private final Map<Integer, String> FS_ARMORREMOVERIGHT;
    private final Map<Integer, String> FS_MISSILEWEAPON;
    private int step = 0;
    private float timer = 0f;
    private boolean init1 = false;
    private boolean init2 = false;
    private boolean init3 = false;
    private Vector2f[] FS_LEFTORGLOC = new Vector2f[7];

    private Vector2f[] FS_RIGHTORGLOC = new Vector2f[7];

    private final Vector2f[] FS_LEFTCENT = new Vector2f[7];

    private final Vector2f[] FS_RIGHTCENT = new Vector2f[7];
    private Vector2f[] FS_ORGLEFTCENT = new Vector2f[7];
    private Vector2f[] FS_ORGRIGHTCENT = new Vector2f[7];
    private float arg = 0f;


    public fs_armorremoved() {
        this.FS_MISSILEWEAPON = new HashMap<>();
        this.FS_MISSILEWEAPON.put(0, "fs_GeMing_shoulder_missile");
        this.FS_MISSILEWEAPON.put(1, "fs_GeMing_dick_missile");
        this.FS_MISSILEWEAPON.put(2, "fs_GeMing_3_missile");
        this.FS_MISSILEWEAPON.put(3, "fs_GeMing_4_missile");
        this.FS_MISSILEWEAPON.put(4, "fs_GeMing_5_missile");
        this.FS_ARMORREMOVELEFT = new HashMap<>();
        this.FS_ARMORREMOVELEFT.put(0, "fs_GeMing_left_shoulder_cover");
        this.FS_ARMORREMOVELEFT.put(1, "fs_GeMing_left_arm_cover");
        this.FS_ARMORREMOVELEFT.put(2, "fs_GeMing_left_hand_cover");
        this.FS_ARMORREMOVELEFT.put(3, "fs_GeMing_left_chest_cover");
        this.FS_ARMORREMOVELEFT.put(4, "fs_GeMing_left_waist_cover");
        this.FS_ARMORREMOVELEFT.put(5, "fs_GeMing_left_leg_cover");
        this.FS_ARMORREMOVELEFT.put(6, "fs_GeMing_left_egg_cover");
        this.FS_ARMORREMOVERIGHT = new HashMap<>();
        this.FS_ARMORREMOVERIGHT.put(0, "fs_GeMing_right_shoulder_cover");
        this.FS_ARMORREMOVERIGHT.put(1, "fs_GeMing_right_arm_cover");
        this.FS_ARMORREMOVERIGHT.put(2, "fs_GeMing_right_hand_cover");
        this.FS_ARMORREMOVERIGHT.put(3, "fs_GeMing_right_chest_cover");
        this.FS_ARMORREMOVERIGHT.put(4, "fs_GeMing_right_waist_cover");
        this.FS_ARMORREMOVERIGHT.put(5, "fs_GeMing_right_leg_cover");
        this.FS_ARMORREMOVERIGHT.put(6, "fs_GeMing_right_egg_cover");

    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        timer += Global.getCombatEngine().getElapsedInLastFrame();
        if (timer >= 1f) {
            if (!init3) {
                init3 = true;
                for (WeaponAPI a : ship.getAllWeapons()) {
                    for (int i = 0; i < this.FS_MISSILEWEAPON.size(); i++) {
                        if (Objects.equals(a.getSpec().getWeaponId(), this.FS_MISSILEWEAPON.get(i))) {
                            Global.getCombatEngine().spawnProjectile(ship, a, a.getId(), a.getFirePoint(0), a.getCurrAngle(), ship.getVelocity());
                        }
                    }
                }
            }
        }
        if (step == 1) {
            if (timer >= 1f) {

                if (!init1) {
                    init1 = true;

                    arg = ship.getFacing();
                    for (WeaponAPI a : ship.getAllWeapons()) {
                        for (int i = 0; i < this.FS_ARMORREMOVELEFT.size(); i++) {
                            if (Objects.equals(a.getSpec().getWeaponId(), this.FS_ARMORREMOVELEFT.get(i))) {
                                Vector2f orgleftloc = new Vector2f(a.getLocation().getX(), a.getLocation().getY());
                                FS_LEFTORGLOC[i] = orgleftloc;
                                Vector2f centleftloc = new Vector2f(a.getSprite().getCenterX(), a.getSprite().getCenterY());
                                FS_LEFTCENT[i] = centleftloc;
                            }
                        }
                        for (int i = 0; i < this.FS_ARMORREMOVERIGHT.size(); i++) {
                            if (Objects.equals(a.getSpec().getWeaponId(), this.FS_ARMORREMOVERIGHT.get(i))) {
                                Vector2f orgrightloc = new Vector2f(a.getLocation().getX(), a.getLocation().getY());
                                FS_RIGHTORGLOC[i] = orgrightloc;
                                Vector2f centrightloc = new Vector2f(a.getSprite().getCenterX(), a.getSprite().getCenterY());
                                FS_RIGHTCENT[i] = centrightloc;
                            }
                        }
                    }
                }
            } else {
                if (!init2) {
                    init2 = true;
                    arg = ship.getFacing();
                    for (WeaponAPI a : ship.getAllWeapons()) {
                        for (int i = 0; i < this.FS_ARMORREMOVELEFT.size(); i++) {
                            if (Objects.equals(a.getSpec().getWeaponId(), this.FS_ARMORREMOVELEFT.get(i))) {
                                FS_ORGLEFTCENT[i] = new Vector2f(a.getSprite().getCenterX(), a.getSprite().getCenterY());
                                Global.getCombatEngine().addNegativeNebulaParticle(MathUtils.getRandomPointInCircle(a.getLocation(), 10f), new Vector2f(15f * (float) Math.cos(Math.toRadians(arg + 90f)), 10f * (float) Math.sin(Math.toRadians(arg + 90f))), MathUtils.getRandomNumberInRange(30.0F, 60.0F), 6F, 0.1F, 0.2F, MathUtils.getRandomNumberInRange(1.5F, 2.0F), new Color(116, 255, 100, 100));
                                Global.getCombatEngine().addNebulaParticle(MathUtils.getRandomPointInCircle(a.getLocation(), 10f), new Vector2f(15f * (float) Math.cos(Math.toRadians(arg + 90f)), 10f * (float) Math.sin(Math.toRadians(arg + 90f))), MathUtils.getRandomNumberInRange(30.0F, 60.0F), 5F, 0.1F, 0.2F, MathUtils.getRandomNumberInRange(1.5F, 2.0F), new Color(255, 255, 255, 100));

                            }
                        }
                        for (int i = 0; i < this.FS_ARMORREMOVERIGHT.size(); i++) {
                            if (Objects.equals(a.getSpec().getWeaponId(), this.FS_ARMORREMOVERIGHT.get(i))) {
                                FS_ORGRIGHTCENT[i] = new Vector2f(a.getSprite().getCenterX(), a.getSprite().getCenterY());
                                Global.getCombatEngine().addNegativeNebulaParticle(MathUtils.getRandomPointInCircle(a.getLocation(), 10f), new Vector2f(15f * (float) Math.cos(Math.toRadians(arg - 90f)), 10f * (float) Math.sin(Math.toRadians(arg - 90f))), MathUtils.getRandomNumberInRange(30.0F, 60.0F), 6F, 0.1F, 0.2F, MathUtils.getRandomNumberInRange(1.5F, 2.0F), new Color(116, 255, 100, 100));
                                Global.getCombatEngine().addNebulaParticle(MathUtils.getRandomPointInCircle(a.getLocation(), 10f), new Vector2f(15f * (float) Math.cos(Math.toRadians(arg + 90f)), 10f * (float) Math.sin(Math.toRadians(arg + 90f))), MathUtils.getRandomNumberInRange(30.0F, 60.0F), 5F, 0.1F, 0.2F, MathUtils.getRandomNumberInRange(1.5F, 2.0F), new Color(255, 255, 255, 100));
                            }
                        }
                    }
                }
                for (WeaponAPI a : ship.getAllWeapons()) {
                    for (int i = 0; i < this.FS_ARMORREMOVELEFT.size(); i++) {
                        if (Objects.equals(a.getSpec().getWeaponId(), this.FS_ARMORREMOVELEFT.get(i))) {
                            a.getSprite().setCenter(FS_ORGLEFTCENT[i].getX() + (10f + i * 2f) * timer, FS_ORGLEFTCENT[i].getY());
                        }
                    }
                    for (int i = 0; i < this.FS_ARMORREMOVERIGHT.size(); i++) {
                        if (Objects.equals(a.getSpec().getWeaponId(), this.FS_ARMORREMOVERIGHT.get(i))) {
                            a.getSprite().setCenter(FS_ORGRIGHTCENT[i].getX() - (10f + i * 2f) * timer, FS_ORGRIGHTCENT[i].getY());
                        }
                    }
                }
            }
        }
        if (timer > 1f) {
            if (step == 1) {
                if (state == State.OUT) {
                    stats.getMaxSpeed().unmodifyFlat(id1); // to slow down ship to its regular top speed while powering drive down
                } else {
                    ship.addAfterimage(new Color(73, 210, 128, 153), 0f, 0f, -ship.getVelocity().x, -ship.getVelocity().y, 0f, 0f, 0.03f, 0.5f, true, true, false);
                    stats.getMaxSpeed().modifyFlat(id1, 200f * effectLevel);
                    stats.getAcceleration().modifyFlat(id1, 400f * effectLevel);//调整最大速度与加速度
                }
            }
            stats.getMaxSpeed().modifyMult(id2, 1.5f);
            stats.getAcceleration().modifyMult(id2, 1.5f);
            stats.getMaxTurnRate().modifyMult(id2, 1.7f);
            stats.getTurnAcceleration().modifyMult(id2, 1.7f);
            stats.getArmorBonus().modifyMult(id2, 0.8f);
            if (step == 1) {
                for (WeaponAPI a : ship.getAllWeapons()) {
                    for (int i = 0; i < this.FS_ARMORREMOVELEFT.size(); i++) {
                        if (Objects.equals(a.getSpec().getWeaponId(), this.FS_ARMORREMOVELEFT.get(i))) {
                            float range = 50f;
                            Vector2f targetloc = new Vector2f(FS_LEFTORGLOC[i].getX() + (timer - 1f) * (range + i * 10f) * (float) Math.cos(Math.toRadians(arg + 90f)), FS_LEFTORGLOC[i].getY() + (timer - 1f) * (range + i * 10f) * (float) Math.sin(Math.toRadians(arg + 90f)));
                            a.getSprite().setCenter(FS_LEFTCENT[i].getX() - (targetloc.getX() - a.getLocation().getX()), FS_LEFTCENT[i].getY() - (targetloc.getY() - a.getLocation().getY()));
                            a.setCurrAngle(arg);
                            if (timer >= 2f && timer <= 3f) {
                                a.getSprite().setColor(new Color(255, 255, 255, Math.round((3f - timer) * 255)));
                            }
                        }
                    }
                    for (int i = 0; i < this.FS_ARMORREMOVERIGHT.size(); i++) {
                        if (Objects.equals(a.getSpec().getWeaponId(), this.FS_ARMORREMOVERIGHT.get(i))) {
                            float range = 50f;
                            Vector2f targetloc = new Vector2f(FS_RIGHTORGLOC[i].getX() + (timer - 1f) * (range + i * 10f) * (float) Math.cos(Math.toRadians(arg - 90f)), FS_RIGHTORGLOC[i].getY() + (timer - 1f) * (range + i * 10f) * (float) Math.sin(Math.toRadians(arg - 90f)));
                            a.getSprite().setCenter(FS_RIGHTCENT[i].getX() - (targetloc.getX() - a.getLocation().getX()), FS_RIGHTCENT[i].getY() - (targetloc.getY() - a.getLocation().getY()));
                            a.setCurrAngle(arg);
                            if (timer >= 2f && timer <= 3f) {
                                a.getSprite().setColor(new Color(255, 255, 255, Math.round((3f - timer) * 255)));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        stats.getMaxSpeed().unmodifyFlat(id1);
        stats.getAcceleration().unmodifyFlat(id1);
        init1 = false;
        init2 = false;
        init3 = false;
        timer = 0f;
        FS_LEFTORGLOC = new Vector2f[7];
        FS_RIGHTORGLOC = new Vector2f[7];
        FS_ORGLEFTCENT = new Vector2f[7];
        FS_ORGRIGHTCENT = new Vector2f[7];
        arg = 0f;
        step++;
    }

}
