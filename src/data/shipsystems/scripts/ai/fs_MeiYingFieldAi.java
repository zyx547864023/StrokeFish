package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class fs_MeiYingFieldAi implements ShipSystemAIScript {

    private CombatEngineAPI engine;
    private ShipAPI ship;
    float timer;
    private ShipAPI lastship;
    private ShipwideAIFlags flags;
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine)
    {
        this.ship = ship;
        this.engine = engine;
        timer=0f;
        this.flags=flags;
    }
    private void use(ShipAPI ship) {
        if (ship.getPhaseCloak() != null && ship.getPhaseCloak().getId().contentEquals("fs_MeiYingField")) {
            ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
        } else {
            ship.useSystem();
        }
    }
    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        boolean wantActive = (flags.hasFlag(ShipwideAIFlags.AIFlags.IN_CRITICAL_DPS_DANGER)
                || flags.hasFlag(ShipwideAIFlags.AIFlags.NEEDS_HELP)
                || flags.hasFlag(ShipwideAIFlags.AIFlags.BIGGEST_THREAT));
        MissileAPI missile=AIUtils.getNearestMissile(ship);
        if (wantActive) {
            float range = 0f;
            if (missile != null) {
                range = Math.min(range,  MathUtils.getDistance(ship, missile));
                if(range<=300f&&!ship.getSystem().isActive()){
                    ship.useSystem();
                }
            }
        }
        java.util.List<DamagingProjectileAPI> damagingProjectiles = engine.getProjectiles();
        int count = 0;
        for (DamagingProjectileAPI damagingProjectile : damagingProjectiles) {
            if (damagingProjectile instanceof MissileAPI) {
                float range = MathUtils.getDistance(ship, damagingProjectile);
                if(range<=500f&&damagingProjectile.getOwner()!=ship.getOwner()) {
                    count++;
                }
            }
        }
        if(count>3&&!ship.getSystem().isActive())
        {
            ship.useSystem();
        }
    }
}
