package data.methods;

import com.fs.starfarer.api.combat.ShipAPI;

public class Fs_hullsizeint {
    public int Getsize(ShipAPI target){
        if(target.isCapital()){
            return 4;
        }
        else if(target.isCruiser()){
            return 3;
        }
        else if(target.isDestroyer()){
            return 2;
        }
        else if(target.isFrigate()){
            return 1;
        }
        else return 0;
    }
}
