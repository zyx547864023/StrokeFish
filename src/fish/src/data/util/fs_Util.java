package data.util;

import org.lwjgl.util.vector.Vector2f;

public class fs_Util {

    public static int clamp255(int x) {
        return Math.max(0, Math.min(255, x));
    }

    public static Vector2f getCollisionRayCircle(Vector2f start, Vector2f end, Vector2f circle, float radius, boolean getNear) {
        float x1 = start.x - circle.x;
        float x2 = end.x - circle.x;
        float y1 = start.y - circle.y;
        float y2 = end.y - circle.y;
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dr_sqrd = (dx * dx) + (dy * dy);
        float D = (x1 * y2) - (x2 * y1);
        float delta = (radius * radius * dr_sqrd) - (D * D);
        if (delta < 0f) {
            return null;
        } else if (delta > 0f) {
            float x_sub = Math.signum(dy) * dx * (float) Math.sqrt(delta);
            float x_a = ((D * dy) + x_sub) / dr_sqrd;
            float x_b = ((D * dy) - x_sub) / dr_sqrd;
            float y_sub = Math.abs(dy) * (float) Math.sqrt(delta);
            float y_a = ((-D * dx) + y_sub) / dr_sqrd;
            float y_b = ((-D * dx) - y_sub) / dr_sqrd;
            float dax = x_a - x1;
            float dbx = x_b - x1;
            float day = y_a - y1;
            float dby = y_b - y1;
            float dist_a_sqrt = (dax * dax) + (day * day);
            float dist_b_sqrt = (dbx * dbx) + (dby * dby);
            if ((dist_a_sqrt < dist_b_sqrt) ^ !getNear) {
                return new Vector2f(x_a + circle.x, y_a + circle.y);
            } else {
                return new Vector2f(x_b + circle.x, y_b + circle.y);
            }
        } else {
            float x = (D * dy) / dr_sqrd;
            float y = (-D * dx) / dr_sqrd;
            return new Vector2f(x + circle.x, y + circle.y);
        }
    }
}
