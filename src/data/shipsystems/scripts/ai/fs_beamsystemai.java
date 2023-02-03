package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class fs_beamsystemai implements ShipSystemAIScript {
    float timer;
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipAPI lastship;
    private ShipwideAIFlags flags;

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
        timer = 0f;
        this.flags = flags;
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
        float range;

            if (target != null) {
                range = MathUtils.getDistance(ship, target);
                if (range <= 1000f && !ship.getSystem().isActive()) {
                    use(ship);
                }

        }
    }
}
