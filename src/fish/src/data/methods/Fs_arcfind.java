package data.methods;

import org.lwjgl.util.vector.Vector2f;

public class Fs_arcfind {
     public float Findarc(Vector2f source, Vector2f target){
        float arg;
        if (target.getY() > source.getY()) {
            arg = 90f - (float) Math.toDegrees(Math.atan((target.getX() - source.getX()) / (target.getY() - source.getY())));
        } else {
            arg = 270f - (float) Math.toDegrees(Math.atan((target.getX() - source.getX()) / (target.getY() - source.getY())));
        }
        return arg;
     }//source为源头，target为目标位置，这是一个通用的计算在source看来target与其之间角度关系的方法。
}
