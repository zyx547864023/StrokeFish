package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class fs_Phasepushfield extends BaseShipSystemScript {
    private boolean init1 = false;
    private boolean step = true;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        if (!init1) {
            init1 = true;

            if (step) {
                step = false;
                RippleDistortion ripple1 = new RippleDistortion(ship.getLocation(), new Vector2f());
                ripple1.setSize(700f);
                ripple1.setIntensity(100f);
                ripple1.fadeInSize(0.4f);
                ripple1.fadeInIntensity(0.3f);
                ripple1.setFrameRate(90f);
                DistortionShader.addDistortion(ripple1);//生成扭曲效果
                for (ShipAPI target : Global.getCombatEngine().getShips()) {
                    float arg;
                    if (target != null && target.getOwner() != ship.getOwner()) {
                        float d = Vector2f.sub(target.getLocation(), ship.getLocation(), new Vector2f()).length();//判定距离
                        if (d <= 700f) {
                            if (target.getLocation().getY() > ship.getLocation().getY()) {
                                arg = 90f - (float) Math.toDegrees(Math.atan((target.getLocation().getX() - ship.getLocation().getX()) / (target.getLocation().getY() - ship.getLocation().getY())));
                            } else {
                                arg = 270f - (float) Math.toDegrees(Math.atan((target.getLocation().getX() - ship.getLocation().getX()) / (target.getLocation().getY() - ship.getLocation().getY())));
                            }//获取目标与舰船的位置角度关系
                            CombatUtils.applyForce(target, arg, 300f + target.getMass() * 2f);//产生推力，最后一个变量为推力大小。
                        }
                    }
                }
                for (DamagingProjectileAPI target : Global.getCombatEngine().getProjectiles()) {
                    float arg;
                    if (target != null && target.getOwner() != ship.getOwner()) {
                        float d = Vector2f.sub(target.getLocation(), ship.getLocation(), new Vector2f()).length();//判定距离。
                        if (d <= 700f) {
                            if (target.getLocation().getY() > ship.getLocation().getY()) {
                                arg = 90f - (float) Math.toDegrees(Math.atan((target.getLocation().getX() - ship.getLocation().getX()) / (target.getLocation().getY() - ship.getLocation().getY())));
                            } else {
                                arg = 270f - (float) Math.toDegrees(Math.atan((target.getLocation().getX() - ship.getLocation().getX()) / (target.getLocation().getY() - ship.getLocation().getY())));
                            }//获取目标与舰船的位置角度关系。
                            CombatUtils.applyForce(target, arg, 500f);//产生推力，最后一个变量为推力大小。
                        }
                    }
                }
            } else {
                step = true;
                RippleDistortion ripple1 = new RippleDistortion(ship.getLocation(), new Vector2f());
                ripple1.setSize(700f);
                ripple1.setIntensity(100f);
                ripple1.fadeOutSize(0.4f);
                ripple1.fadeOutIntensity(0.3f);
                ripple1.setFrameRate(90f);
                DistortionShader.addDistortion(ripple1);//生成扭曲效果。
                for (ShipAPI target : Global.getCombatEngine().getShips()) {
                    float arg;
                    if (target != null && target.getOwner() != ship.getOwner()) {
                        float d = Vector2f.sub(target.getLocation(), ship.getLocation(), new Vector2f()).length();//判定距离
                        if (d <= 700f) {
                            if (target.getLocation().getY() > ship.getLocation().getY()) {
                                arg = 90f - (float) Math.toDegrees(Math.atan((target.getLocation().getX() - ship.getLocation().getX()) / (target.getLocation().getY() - ship.getLocation().getY())));
                            } else {
                                arg = 270f - (float) Math.toDegrees(Math.atan((target.getLocation().getX() - ship.getLocation().getX()) / (target.getLocation().getY() - ship.getLocation().getY())));
                            }//获取目标与舰船的位置角度关系。
                            CombatUtils.applyForce(target, arg + 180f, 300f + target.getMass() * 2f);//产生推力，最后一个变量为推力大小
                        }
                    }
                }
                for (DamagingProjectileAPI target : Global.getCombatEngine().getProjectiles()) {
                    float arg;
                    if (target != null && target.getOwner() != ship.getOwner()) {
                        float d = Vector2f.sub(target.getLocation(), ship.getLocation(), new Vector2f()).length();//判定距离
                        if (d <= 700f) {
                            if (target.getLocation().getY() > ship.getLocation().getY()) {
                                arg = 90f - (float) Math.toDegrees(Math.atan((target.getLocation().getX() - ship.getLocation().getX()) / (target.getLocation().getY() - ship.getLocation().getY())));
                            } else {
                                arg = 270f - (float) Math.toDegrees(Math.atan((target.getLocation().getX() - ship.getLocation().getX()) / (target.getLocation().getY() - ship.getLocation().getY())));
                            }//获取目标与舰船的位置角度关系。
                            CombatUtils.applyForce(target, arg + 180f, 500f);//产生牵引力，最后一个变量为推力大小
                        }
                    }
                }
            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        init1 = false;
    }
}
