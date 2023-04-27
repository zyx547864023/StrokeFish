package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.awt.*;
import java.util.EnumSet;

public class fs_fighterboom extends BaseShipSystemScript {
    private final float pro = 0.5f;
    private boolean init = false;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        for (ShipAPI target : ship.getWing().getWingMembers()) {
            target.getMutableStats().getArmorDamageTakenMult().modifyMult(id, pro);
            target.getMutableStats().getHullDamageTakenMult().modifyMult(id, pro);
            target.getMutableStats().getShieldDamageTakenMult().modifyMult(id, pro);
            target.getMutableStats().getEnergyWeaponDamageMult().modifyMult(id, 1f + pro);
            target.getMutableStats().getBallisticWeaponDamageMult().modifyMult(id, 1f + pro);
            target.getMutableStats().getMissileWeaponDamageMult().modifyMult(id, 1f + pro);
            target.getMutableStats().getMaxSpeed().modifyMult(id, 1f + pro);
            target.getMutableStats().getAcceleration().modifyMult(id, 1f + pro);
            target.getMutableStats().getTurnAcceleration().modifyMult(id, 1f + pro);
            target.setWeaponGlow(effectLevel, new Color(86, 1, 1, 255), EnumSet.allOf(WeaponAPI.WeaponType.class));
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        if (!init) {
            init = true;
        } else {
            for (ShipAPI target : ship.getWing().getWingMembers()) {
                target.getMutableStats().getArmorDamageTakenMult().unmodifyMult(id);
                target.getMutableStats().getHullDamageTakenMult().unmodifyMult(id);
                target.getMutableStats().getShieldDamageTakenMult().unmodifyMult(id);
                target.getMutableStats().getEnergyWeaponDamageMult().unmodifyMult(id);
                target.getMutableStats().getBallisticWeaponDamageMult().unmodifyMult(id);
                target.getMutableStats().getMissileWeaponDamageMult().unmodifyMult(id);
                target.getMutableStats().getMaxSpeed().unmodifyMult(id);
                target.getMutableStats().getAcceleration().unmodifyMult(id);
                target.getMutableStats().getTurnAcceleration().unmodifyMult(id);
                Global.getCombatEngine().applyDamage(target, target.getLocation(), 100000f, DamageType.ENERGY, 0f, true, false, null);
            }
        }
    }
}
