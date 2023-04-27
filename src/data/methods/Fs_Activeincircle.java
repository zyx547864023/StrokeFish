package data.methods;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import org.lwjgl.util.vector.Vector2f;


public class Fs_Activeincircle {
    public static void Circlemove(CombatEntityAPI target, float rad, float vel, ShipAPI ship) {
        Vector2f cen = ship.getLocation();
        float arg = Fs_arcfind.Findarc(target.getLocation(), cen) - 90f;
        float arg1 = Fs_arcfind.Findarc(cen, target.getLocation());
        Vector2f loc1 = new Vector2f(cen.getX() + (float) (rad * Math.cos(Math.toRadians(arg1))), cen.getY() + (float) (rad * Math.sin(Math.toRadians(arg1))));
        target.getLocation().set(loc1);
        target.setFacing(arg);
        target.getVelocity().set((vel) * (float) Math.cos(Math.toRadians(arg)) + ship.getVelocity().getX(), (vel) * (float) Math.sin(Math.toRadians(arg)) + ship.getVelocity().getY());
    }//使目标绕舰船顺时针运动
}
