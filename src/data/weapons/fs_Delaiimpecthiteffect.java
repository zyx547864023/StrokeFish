package data.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.EnumSet;

public class fs_Delaiimpecthiteffect implements OnHitEffectPlugin {
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        CombatEngineAPI engine1 = Global.getCombatEngine();
        if (target instanceof ShipAPI&&!shieldHit) {
            engine1.addLayeredRenderingPlugin(new fs_Delaiimpecthiteffect.fs_Delaiimpecthitplugin((ShipAPI) target));
        }
    }



    public static class fs_Delaiimpecthitplugin implements CombatLayeredRenderingPlugin {
        private float timer = 0f;
        private final ShipAPI target;

        public fs_Delaiimpecthitplugin(ShipAPI targets) {
            this.target = targets;
        }

        public void init(CombatEntityAPI entity) {

        }

        @Override
        public void cleanup() {

        }

        @Override
        public boolean isExpired() {
            return timer >= 5f;
        }

        @Override
        public void advance(float amount) {
            timer += amount;
            String id = "Delaiimpactid";
            if (timer <= 3f) {
                if (this.target != null) {
                    this.target.getMutableStats().getMaxArmorDamageReduction().modifyMult(id, 0.8f);
                    this.target.setJitter(this.target, new Color(255, 42, 0, 255), 3f, 1, 3f);
                }
            } else {
                if (this.target != null) {
                    this.target.getMutableStats().getMaxArmorDamageReduction().unmodifyMult(id);
                }
            }
        }

        @Override
        public EnumSet<CombatEngineLayers> getActiveLayers() {
            return null;
        }

        @Override
        public float getRenderRadius() {
            return 0;
        }

        @Override
        public void render(CombatEngineLayers layer, ViewportAPI viewport) {

        }

    }
}
