package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

public class fs_armorremoved extends BaseShipSystemScript {
    private int step=0;
    private boolean init1=false;
    private boolean init2=false;
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if(!init1){
            init1=true;
            step++;
        }

        if(step==1) {
            if(!init2) {
                init2=true;
                for (WeaponAPI a : ship.getAllWeapons()) {
                        ;
                }
            }
            }
        }
    }
