package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;

public class fs_delaiweaponchange extends BaseHullMod {
    public Map<Integer, String> FS_DELAIWEAPON_SWITCH;
    private final Map<String, Integer> FS_DELAIWEAPON_TARGET;
    private final Map<Integer, String> FS_DELAIHULLMOD_SWITCH;
    private final String fs_delaislot = "Fs_delaislot";

    public fs_delaiweaponchange() {
        this.FS_DELAIWEAPON_SWITCH = new HashMap<>();

        this.FS_DELAIWEAPON_SWITCH.put(0, "fs_DeLaiManNi_gt");
        this.FS_DELAIWEAPON_SWITCH.put(1, "fs_DeLaiManNi_impact");
        this.FS_DELAIWEAPON_SWITCH.put(2, "fs_DeLaiManNi_trebie");

        this.FS_DELAIWEAPON_TARGET = new HashMap<>();

        this.FS_DELAIWEAPON_TARGET.put("fs_DeLaiManNi_gt", 2);
        this.FS_DELAIWEAPON_TARGET.put("fs_DeLaiManNi_trebie", 1);
        this.FS_DELAIWEAPON_TARGET.put("fs_DeLaiManNi_impact", 0);

        this.FS_DELAIHULLMOD_SWITCH = new HashMap<>();

        this.FS_DELAIHULLMOD_SWITCH.put(0, "fs_delaiweapon1");
        this.FS_DELAIHULLMOD_SWITCH.put(1, "fs_delaiweapon2");
        this.FS_DELAIHULLMOD_SWITCH.put(2, "fs_delaiweapon3");

    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id)
    {

        boolean toWSwitchLeft = true;

        for (int i = 0; i < this.FS_DELAIWEAPON_TARGET.size(); i++) {
            if (stats.getVariant().getHullMods().contains(this.FS_DELAIHULLMOD_SWITCH.get(i))) {
                toWSwitchLeft = false;
            }
        }

        if (toWSwitchLeft)
        {
            boolean random = false;
            int selected;
            if (stats.getVariant().getWeaponSpec(fs_delaislot) != null) {
                selected =  this.FS_DELAIWEAPON_TARGET.get(stats.getVariant().getWeaponSpec(fs_delaislot).getWeaponId());
            }
            else
            {
                selected = MathUtils.getRandomNumberInRange(0, this.FS_DELAIWEAPON_TARGET.size() - 1);
                random = true;
            }

            stats.getVariant().addMod(this.FS_DELAIHULLMOD_SWITCH.get(selected));

            stats.getVariant().clearSlot(fs_delaislot);
            String toWInstallLeft = this.FS_DELAIWEAPON_SWITCH.get(selected);
            stats.getVariant().addWeapon(fs_delaislot, toWInstallLeft);
            if (random) {
                stats.getVariant().autoGenerateWeaponGroups();
            }
        }

    }



}
