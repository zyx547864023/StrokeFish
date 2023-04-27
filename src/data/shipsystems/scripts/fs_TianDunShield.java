package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.sun.javafx.image.BytePixelSetter;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * 开盾
 * 第一阶段贴图放大
 * 第二阶段召唤盾船
 * 第三阶段盾船旋转
 * 关闭 降低透明度 透明度 为零移除
 * 盾face 到 coverToShip 小于 盾 arc 在 cover里
 * 盾face 到 coverToShip 大于 盾 arc
 *  coverToShip 到 双边的角 小于 到 盾 双边的角 不在 cover 里
 *  在 cover 里 且距离 phase
 *  开盾完成前 拉
 *  大于船盾 小于 外船盾
 *  如果船里有船 谁大谁解除相位 移出cover
 */
public class fs_TianDunShield extends BaseShipSystemScript {
    private CombatEngineAPI engine = Global.getCombatEngine();

    private final static String ID = "fs_TianDunShield";
    private final static String SHIP_ID = "fs_TianDunShield";
    private boolean isSet = false;
    public static enum STAGE {
        SIZE_UP,
        CALL,
        TURN,
        ALPHA_DOWN
    }
    boolean init = false;
    private ShipAPI shieldShip = null;
    private STAGE stage = null;
    private SpriteAPI shield = Global.getSettings().getSprite("graphics/ships/fs_TianDun/fs_TianDun_Shield.png");
    private float thisEffectLevel = 0;
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if(Global.getCombatEngine().isPaused()) return;
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        if (!init) {
            init = true;
            Global.getCombatEngine().addLayeredRenderingPlugin(new fs_TianDunShieldPlugin(ship));
        }

        if (state == State.IN)
        {
            stage = STAGE.SIZE_UP;
            thisEffectLevel = effectLevel;
            if (shieldShip!=null){
                shieldShip.setPhased(false);
                shieldShip.setOwner(ship.getOwner());
                shieldShip.getFluxTracker().setCurrFlux(0);
                shieldShip.getFluxTracker().setHardFlux(0);
            }
        }
        else if (state == State.ACTIVE){
            CombatFleetManagerAPI manager = engine.getFleetManager(ship.getOwner());
            //召唤
            if (shieldShip==null) {
                FleetMemberAPI newFleetMember = Global.getFactory().createFleetMember
                        (FleetMemberType.SHIP, SHIP_ID+"_Hull");
                newFleetMember.setOwner(ship.getOwner());
                newFleetMember.getCrewComposition().addCrew(newFleetMember.getNeededCrew());
                shieldShip = manager.spawnFleetMember(newFleetMember, ship.getLocation(), ship.getFacing(), 0f);
                shieldShip.setAlphaMult(0);
                shieldShip.setControlsLocked(true);
                shieldShip.getMutableStats().getShieldUnfoldRateMult().modifyPercent(id,1000f);
                shieldShip.setShipAI(null);
            }
        }
    }
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        isSet = false;
        stage = STAGE.ALPHA_DOWN;
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) {return null;}
        return null;
    }

    public class fs_TianDunShieldPlugin extends BaseCombatLayeredRenderingPlugin {
        private final ShipAPI ship;
        private List<ShipAPI> coverShip = new ArrayList<>();

        public fs_TianDunShieldPlugin(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public void advance(float amount) {
            if (engine.isPaused()) return;
            if (!ship.isAlive()) return;
            try {

                if (shieldShip!=null) {
                    if (shieldShip.getFluxTracker().getCurrFlux()>shieldShip.getMaxFlux()*0.99) {
                        stage = STAGE.ALPHA_DOWN;
                        ship.getSystem().forceState(ShipSystemAPI.SystemState.COOLDOWN,0f);
                    }
                    shieldShip.setFacing(ship.getFacing());
                    shieldShip.getLocation().set(ship.getLocation());
                    if (ship.getSystem().isActive()) {
                        if (shieldShip.getShield().isOff()) {
                            shieldShip.getShield().toggleOn();
                        }
                        shieldShip.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
                        List<ShipAPI> removeShip = new ArrayList<>();
                        for (ShipAPI c : coverShip) {
                            if (c.isAlive() && !c.isFighter()) {
                                //turnShip(c, amount);
                                //算法
                                float cToShip = VectorUtils.getAngle(c.getLocation(), ship.getLocation());
                                float cToShipLeft = cToShip + 90;
                                float cToShipRight = cToShip - 90;
                                Vector2f leftPoint = MathUtils.getPointOnCircumference(c.getLocation(), c.getCollisionRadius(), cToShipLeft);
                                Vector2f rightPoint = MathUtils.getPointOnCircumference(c.getLocation(), c.getCollisionRadius(), cToShipRight);
                                float shipToLeftPoint = VectorUtils.getAngle(ship.getLocation(), leftPoint);
                                float shipToRightPoint = VectorUtils.getAngle(ship.getLocation(), rightPoint);
                                float shieldFacing = shieldShip.getShield().getFacing();
                                //
                                if (MathUtils.getDistance(ship, c) <= shieldShip.getShield().getRadius()) {
                                    c.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
                                    if (
                                            Math.abs(MathUtils.getShortestRotation(shipToLeftPoint,shieldFacing))<shieldShip.getShield().getActiveArc()+1
                                                    &&Math.abs(MathUtils.getShortestRotation(shipToRightPoint,shieldFacing))<shieldShip.getShield().getActiveArc()+1
                                                    || c.equals(ship) || shieldShip.getShield().getActiveArc() >=360
                                    ) {
                                        if (c.getShield() != null) {
                                            if (c.getShield().isOn()) {
                                                c.getShield().toggleOff();
                                            }
                                        }
                                        if (c.getPhaseCloak() != null) {
                                            if (c.getPhaseCloak().isActive()) {
                                                c.getPhaseCloak().forceState(ShipSystemAPI.SystemState.OUT, 0f);
                                                c.getPhaseCloak().deactivate();
                                            }
                                        }

                                        if (!CollisionClass.NONE.equals(c.getCollisionClass())) {
                                            c.setCustomData("COLLISION_CLASS", c.getCollisionClass());
                                        }
                                        c.setCollisionClass(CollisionClass.NONE);

                                        if (MathUtils.getDistance(ship, c) > ship.getCollisionRadius()) {
                                            CombatUtils.applyForce(c, VectorUtils.getAngle(c.getLocation(), ship.getLocation()), (MathUtils.getDistance(c.getLocation(), ship.getLocation()) + c.getMaxSpeed()) * amount);
                                        } else {
                                            //c.getVelocity().set(ship.getVelocity());
                                        }
                                    } else {
                                        if (c.getCustomData().get("COLLISION_CLASS") != null) {
                                            c.setCollisionClass((CollisionClass) c.getCustomData().get("COLLISION_CLASS"));
                                            c.getCustomData().remove(ID+"IS_CATCH");
                                        }
                                    }
                                }
                                else {
                                    if (c.getCustomData().get("COLLISION_CLASS") != null) {
                                        c.setCollisionClass((CollisionClass) c.getCustomData().get("COLLISION_CLASS"));
                                        c.getCustomData().remove(ID+"IS_CATCH");
                                    }
                                    removeShip.add(c);
                                }
                            }
                        }
                        coverShip.removeAll(removeShip);
                    }
                    if (stage == STAGE.SIZE_UP) {
                        //给敌人船一个往外推的力
                        for (ShipAPI s: engine.getShips()){
                            if (s.isAlive() && !s.isFighter()) {
                                //没被其他人拉
                                if ((MathUtils.getDistance(ship, s) > ship.getCollisionRadius() && MathUtils.getDistance(ship, s) <= shieldShip.getShield().getRadius() * 1.5 && ship.getOwner() == s.getOwner()
                                &&!s.equals(shieldShip) && coverShip.indexOf(s) == -1 && s.getCustomData().get(ID+"IS_CATCH") == null) || (s.equals(ship) && coverShip.indexOf(s) == -1 && s.getCustomData().get(ID+"IS_CATCH") == null)
                                ) {
                                    s.setCustomData(ID+"IS_CATCH",true);
                                    coverShip.add(s);
                                }
                                if (MathUtils.getDistance(ship, s) <= shieldShip.getShield().getRadius() && ship.getOwner() != s.getOwner()) {
                                    //CombatUtils.applyForce(s, VectorUtils.getAngle(ship.getLocation(), s.getLocation()), (MathUtils.getDistance(s.getLocation(), ship.getLocation()) + s.getMaxSpeed()) * amount);
                                }
                            }
                        }

                        //开盾
                        if (shieldShip.getShield().getActiveArc()>=360) {
                            stage = STAGE.TURN;
                        }
                    }
                    else if (stage == STAGE.TURN) {

                    }
                    else if (stage == STAGE.ALPHA_DOWN) {
                        shieldShip.getShield().toggleOff();
                        if (shield.getAlphaMult()>0) {
                            shield.setAlphaMult(shield.getAlphaMult() - amount);
                        }
                        else {
                            shield.setAlphaMult(0);
                            stage = null;
                        }
                    }
                    else {
                        int count = 0;
                        if (shieldShip.getShield().getActiveArc()<=0) {
                            for (ShipAPI c : coverShip) {
                                if (CollisionClass.NONE.equals(c.getCollisionClass())) {
                                    if (c.getCustomData().get("COLLISION_CLASS")!=null) {
                                        c.setCollisionClass((CollisionClass) c.getCustomData().get("COLLISION_CLASS"));
                                        c.getCustomData().remove(ID+"IS_CATCH");
                                    }
                                } else {
                                    count++;
                                }
                            }
                        }
                        if (count == coverShip.size())
                        {
                            coverShip.clear();
                        }
                        float height = engine.getMapHeight();
                        float width = engine.getMapWidth();
                        Vector2f newLocation = new Vector2f(ship.getOwner() == 0 ? -width * 4 : width * 4, ship.getOwner() == 0 ? -height * 4 : height * 4);
                        //shieldShip.getLocation().set(newLocation);
                        shieldShip.setPhased(true);
                        shieldShip.setOwner(100);
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
            if (shieldShip!=null) {
                if (stage == STAGE.SIZE_UP) {
                    //渲染贴图
                    shield = Global.getSettings().getSprite("graphics/ships/fs_TianDun/fs_TianDun_Shield.png");
                    shield.setSize(shieldShip.getShield().getRadius() * thisEffectLevel, shieldShip.getShield().getRadius() * thisEffectLevel);
                    shield.setAlphaMult(shield.getAlphaMult() * thisEffectLevel);
                    //shield.renderAtCenter(ship.getLocation().x,ship.getLocation().y);
                } else if (stage == STAGE.TURN) {
                    //渲染贴图
                    shield = Global.getSettings().getSprite("graphics/ships/fs_TianDun/fs_TianDun_Shield.png");
                    shield.setSize(shieldShip.getShield().getRadius(), shieldShip.getShield().getRadius());
                    //shield.renderAtCenter(ship.getLocation().x,ship.getLocation().y);
                } else if (stage == STAGE.ALPHA_DOWN) {
                    if (shield.getAlphaMult() > 0) {
                        //shield.renderAtCenter(ship.getLocation().x,ship.getLocation().y);
                    } else {
                        stage = null;
                    }
                }
            }
        }

        @Override
        public boolean isExpired() {
            return false;
        }

        private void turnShip(ShipAPI inShip,float amount)
        {
            float turn = MathUtils.getShortestRotation(inShip.getFacing(),ship.getFacing());
            float turnRate = inShip.getMaxTurnRate()*amount;
            if(Math.abs(turn)>5)//5
            {
                if(turn>0) {
                    inShip.setFacing(MathUtils.clampAngle(inShip.getFacing() + turnRate));
                }
                else {
                    inShip.setFacing(MathUtils.clampAngle(inShip.getFacing() - turnRate));
                }
            }
            else {
                inShip.setFacing(ship.getFacing());
            }
        }
    }
}