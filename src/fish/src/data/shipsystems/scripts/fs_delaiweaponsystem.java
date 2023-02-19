package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.awt.*;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class fs_delaiweaponsystem extends BaseShipSystemScript {
    private final Map<Integer, String> FS_DELAIWEAPONSYSTEM;
    private boolean init=false;
    private float damage;

    public fs_delaiweaponsystem() {
        this.FS_DELAIWEAPONSYSTEM = new HashMap<>();

        this.FS_DELAIWEAPONSYSTEM.put(0, "fs_DeLaiManNi_gt");
        this.FS_DELAIWEAPONSYSTEM.put(1, "fs_DeLaiManNi_impact");
        this.FS_DELAIWEAPONSYSTEM.put(2, "fs_DeLaiManNi_trebie");
    }

        @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        for(WeaponAPI weapon:ship.getAllWeapons()){
           for (int i=0;i<3;i++){
               if(Objects.equals(weapon.getSpec().getWeaponId(), this.FS_DELAIWEAPONSYSTEM.get(i))){
                       weapon.getDamage().setDamage(damage * 1.5f);
                   ship.setWeaponGlow(effectLevel,new Color(84, 129, 24,255), EnumSet.of(WeaponAPI.WeaponType.BALLISTIC));
               }
           }
        }
        stats.getTurnAcceleration().modifyMult(id,1.5f);
        stats.getMaxTurnRate().modifyMult(id,1.5f);
        stats.getMaxSpeed().modifyMult(id,1.25f);
        stats.getAcceleration().modifyMult(id,1.25f);
        stats.getFluxDissipation().modifyMult(id,1.25f);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        for(WeaponAPI weapon:ship.getAllWeapons()){
            for (int i=0;i<3;i++){
                if(Objects.equals(weapon.getSpec().getWeaponId(), this.FS_DELAIWEAPONSYSTEM.get(i))){
                    if(!init) {
                        init=true;
                        damage = weapon.getDamage().getBaseDamage();
                    }
                    weapon.getDamage().setDamage(damage);
                }
            }
        }
        stats.getTurnAcceleration().unmodifyMult(id);
        stats.getMaxTurnRate().unmodifyMult(id);
        stats.getMaxSpeed().unmodifyMult(id);
        stats.getAcceleration().unmodifyMult(id);
        stats.getFluxDissipation().unmodifyMult(id);
    }
}
