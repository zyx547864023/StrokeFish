package data.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;

public class fs_Delaigteffect implements EveryFrameWeaponEffectPlugin {
    float effectlevel=0f;
    float timer=0f;
    boolean init=false;
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        effectlevel=Math.max(0f,Math.min(effectlevel,1f));
        if(weapon.getChargeLevel()>0f){
            effectlevel+=amount;
        }
        else {
            effectlevel-=amount;
        }
        if(effectlevel>=0.5f){
            timer+=amount;
        }
        else {
            timer=0f;
        }
        if(timer>=5f&&timer<10f){
            weapon.setRefireDelay(0.1f);
            if(weapon.getChargeLevel()==1f){
                if(!init) {
                    init=true;
                    weapon.getShip().getFluxTracker().decreaseFlux(15f);
                }
            }
            else {
                init=false;
            }
            weapon.getMuzzleFlashSpec().setParticleColor(new Color(19, 222, 119, 218));
            weapon.getMuzzleFlashSpec().setLength(45f);
            weapon.getMuzzleFlashSpec().setParticleCount(15);
            weapon.getMuzzleFlashSpec().setSpread(20f);
        }
        else if(timer>=10f){
            weapon.setRefireDelay(0.05f);
            if(weapon.getChargeLevel()==1f){
                if(!init) {
                    init=true;
                    weapon.getShip().getFluxTracker().decreaseFlux(30f);
                }
            }
            else {
                init=false;
            }
            weapon.getMuzzleFlashSpec().setParticleColor(new Color(0, 255, 187, 218));
            weapon.getMuzzleFlashSpec().setLength(60f);
            weapon.getMuzzleFlashSpec().setParticleCount(20);
            weapon.getMuzzleFlashSpec().setSpread(30f);
        }
        else {
            weapon.getMuzzleFlashSpec().setParticleColor(new Color(72, 171, 88, 218));
            weapon.getMuzzleFlashSpec().setLength(30f);
            weapon.getMuzzleFlashSpec().setParticleCount(10);
            weapon.getMuzzleFlashSpec().setSpread(10f);
        }
    }
}
