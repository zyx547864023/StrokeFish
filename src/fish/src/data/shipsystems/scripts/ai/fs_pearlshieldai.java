package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
public class fs_pearlshieldai implements ShipSystemAIScript {

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
        if (ship.getPhaseCloak() != null && ship.getPhaseCloak().getId().contentEquals("fs_pearlshield")) {
            ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
        } else {
            ship.useSystem();
        }
    }
    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        boolean wantActive = (flags.hasFlag(AIFlags.IN_CRITICAL_DPS_DANGER)
                || flags.hasFlag(AIFlags.NEEDS_HELP)
                || flags.hasFlag(AIFlags.BIGGEST_THREAT)
                  ||ship.getFluxTracker().getFluxLevel()>=0.5f);

        if (wantActive) {
            float range = 0f;
            if (target != null) {
                range = Math.min(range,  MathUtils.getDistance(ship, target));
                if(range<=1200f&&!ship.getSystem().isActive()){
                    ship.useSystem();
                }
            }


        }

    }
}
