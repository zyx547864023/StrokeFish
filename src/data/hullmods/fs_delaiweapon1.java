package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
public class fs_delaiweapon1 extends BaseHullMod
{
    public int getDisplaySortOrder()
    {
        return 2000;
    }

    public int getDisplayCategoryIndex()
    {
        return 3;
    }
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "德莱盖特";
        return null;
    }
}
