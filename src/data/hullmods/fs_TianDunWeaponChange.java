package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.loading.VariantSource;
import org.lazywizard.lazylib.MathUtils;

import java.util.HashMap;
import java.util.Map;

public class fs_TianDunWeaponChange extends BaseHullMod {
    public Map<Integer, String> WEAPON_SWITCH;
    private final Map<String, Integer> WEAPON_TARGET;
    private final Map<Integer, String> HULLMOD_SWITCH;
    private final String LEFT_SLOT = "WS0014";

    private final String RIGHT_SLOT = "WS0015";

    public fs_TianDunWeaponChange() {
        this.WEAPON_SWITCH = new HashMap<>();

        this.WEAPON_SWITCH.put(0, "fs_TianDun_TuJi");
        this.WEAPON_SWITCH.put(1, "fs_TianDunXianQuan");

        this.WEAPON_TARGET = new HashMap<>();

        this.WEAPON_TARGET.put("fs_TianDun_TuJi", 1);
        this.WEAPON_TARGET.put("fs_TianDunXianQuan", 0);

        this.HULLMOD_SWITCH = new HashMap<>();

        this.HULLMOD_SWITCH.put(0, "fs_TianDunTuJi");
        this.HULLMOD_SWITCH.put(1, "fs_TianDunXianQuan");
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id)
    {
        stats.getVariant().addWeapon(RIGHT_SLOT, "fs_TianDun_TuJi_left");
        //判断是否已经装上
        boolean toWSwitchLeft = true;
        int index = 0;
        for (int i = 0; i < this.WEAPON_TARGET.size(); i++) {
            if (stats.getVariant().getHullMods().contains(this.HULLMOD_SWITCH.get(i))) {
                toWSwitchLeft = false;
                index = i;
            }
        }

        if (toWSwitchLeft)
        {
            boolean random = false;
            int selected;
            if (stats.getVariant().getWeaponSpec(LEFT_SLOT) != null) {
                selected =  this.WEAPON_TARGET.get(stats.getVariant().getWeaponSpec(LEFT_SLOT).getWeaponId().replace("_left",""));
            }
            else
            {
                selected = MathUtils.getRandomNumberInRange(0, this.WEAPON_TARGET.size() - 1);
                random = true;
            }

            stats.getVariant().addMod(this.HULLMOD_SWITCH.get(selected));

            stats.getVariant().clearSlot(LEFT_SLOT);
            String toWInstallLeft = this.WEAPON_SWITCH.get(index)+"_left";
            stats.getVariant().addWeapon(LEFT_SLOT, toWInstallLeft);

            stats.getVariant().clearSlot(RIGHT_SLOT);
            toWInstallLeft = this.WEAPON_SWITCH.get(index)+"_right";
            stats.getVariant().addWeapon(RIGHT_SLOT, toWInstallLeft);
            if (random) {
                stats.getVariant().autoGenerateWeaponGroups();
            }
        }
        else {
            stats.getVariant().clearSlot(LEFT_SLOT);
            String toWInstallLeft = this.WEAPON_SWITCH.get(index)+"_left";
            stats.getVariant().addWeapon(LEFT_SLOT, toWInstallLeft);

            stats.getVariant().clearSlot(RIGHT_SLOT);
            toWInstallLeft = this.WEAPON_SWITCH.get(index)+"_right";
            stats.getVariant().addWeapon(RIGHT_SLOT, toWInstallLeft);
            Global.getLogger(this.getClass()).info(stats.getVariant().getWeaponId(RIGHT_SLOT)+stats.getVariant().getSource());
        }
    }
}
