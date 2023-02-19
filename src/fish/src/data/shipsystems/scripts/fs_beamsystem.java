package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.methods.Fs_arcfind;
import data.scripts.plugins.MagicRenderPlugin;
import data.scripts.util.MagicFakeBeam;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;
import java.util.*;

import static data.scripts.util.MagicFakeBeam.getCollisionPointOnCircumference;
import static data.scripts.util.MagicFakeBeam.getShipCollisionPoint;

public class fs_beamsystem extends BaseShipSystemScript {
    List<ShipAPI> targets1 = null;
    boolean init = false;
    private String KEY = "FS_bspluginkey";

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        if (targets1 == null) {
            targets1 = new ArrayList();
        }
        if (!init) {
            init = true;
            for (ShipAPI target : Global.getCombatEngine().getShips()) {
                if (target.getOwner() == ship.getOwner() && target != ship && Objects.equals(target.getHullSpec().getHullId(), ship.getHullSpec().getHullId())) {
                    float d = Vector2f.sub(target.getLocation(), ship.getLocation(), new Vector2f()).length() - ship.getShieldRadiusEvenIfNoShield() - target.getShieldRadiusEvenIfNoShield();//判定距离
                    if (d <= 1200f && targets1.size() < 5) {
                        targets1.add(target);
                    }
                }
            }
            for (ShipAPI target : this.targets1) {
                if (!target.getCustomData().containsKey(KEY)) {
                    DataContainer data = new DataContainer();
                    target.setCustomData(KEY, data);
                }
                DataContainer data = (DataContainer) target.getCustomData().get(KEY);
                data.targets2 = null;
            }
            Global.getCombatEngine().addLayeredRenderingPlugin(new Fs_bs_plugin(ship, targets1, KEY));
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        KEY = "FS_bspluginkey" + ship.getFleetMemberId();
        targets1 = null;
        init = false;
    }

    private static class DataContainer {
        List<ShipAPI> targets2;

    }

    public static class Fs_bs_plugin extends BaseCombatLayeredRenderingPlugin {
        private final ShipAPI ship;
        private final String KEY;
        boolean init1 = false;
        Color paramColor = new Color(12, 122, 190, 255);
        Color beamColor = new Color(0, 255, 255, 255);
        List<ShipAPI> targets;
        float totalamount = 0f;
        float arg2;
        private float timer1 = 0f;

        public Fs_bs_plugin(ShipAPI ship, List<ShipAPI> targets, String str) {
            this.ship = ship;
            this.targets = targets;
            KEY = str;
        }

        public static Vector2f findbeamend(Vector2f from, float range, float angle, float smoothIn, float smoothOut, ShipAPI source) {
            Vector2f end = MathUtils.getPoint(from, range, angle);
            List<CombatEntityAPI> entity = CombatUtils.getEntitiesWithinRange(from, range + 500.0F);
            if (!entity.isEmpty()) {
                Iterator<CombatEntityAPI> i$ = entity.iterator();

                while (true) {
                    CombatEntityAPI e;
                    do {
                        if (!i$.hasNext()) {
                            if (MathUtils.isWithinRange(from, end, smoothIn + smoothOut)) {
                                end = MathUtils.getPoint(from, smoothIn + smoothOut + 2.0F, angle);
                            }
                            return end;
                        }

                        e = i$.next();
                    } while (e.getCollisionClass() == CollisionClass.NONE);


                    Vector2f col = new Vector2f(1000000.0F, 1000000.0F);
                    if (e instanceof ShipAPI) {
                        if (e != source && ((ShipAPI) e).getParentStation() != e && e.getCollisionClass() != CollisionClass.NONE && (e.getCollisionClass() != CollisionClass.FIGHTER || e.getOwner() != source.getOwner() || ((ShipAPI) e).getEngineController().isFlamedOut()) && CollisionUtils.getCollides(from, end, e.getLocation(), e.getCollisionRadius())) {
                            ShipAPI s = (ShipAPI) e;
                            Vector2f hitPoint = getShipCollisionPoint(from, end, s, angle);
                            if (hitPoint != null) {
                                col = hitPoint;
                            }

                        }
                    } else if ((e instanceof CombatAsteroidAPI || e instanceof MissileAPI && e.getOwner() != source.getOwner()) && CollisionUtils.getCollides(from, end, e.getLocation(), e.getCollisionRadius())) {
                        Vector2f cAst = getCollisionPointOnCircumference(from, end, e.getLocation(), e.getCollisionRadius());
                        if (cAst != null) {
                            col = cAst;
                        }
                    }

                    if (col.x != 1000000.0F && MathUtils.getDistanceSquared(from, col) < MathUtils.getDistanceSquared(from, end)) {
                        end = col;
                    }

                }
            }
            return end;
        }

        public void init(CombatEntityAPI entity) {
            super.init(entity);
            advance(0f);
        }

        @Override
        public boolean isExpired() {
            return timer1 >= 5f;//返回值为true时，Plugin删除，因此当计时器超过三秒后删除。
        }

        @Override
        public void advance(float amount) {
            boolean player = false;
            player = this.ship == Global.getCombatEngine().getPlayerShip();
            if (!init1) {
                init1 = true;

                for (ShipAPI target1 : this.targets) {
                    if (!target1.getCustomData().containsKey(KEY)) {
                        DataContainer data = new DataContainer();
                        target1.setCustomData(KEY, data);
                    }
                    DataContainer data = (DataContainer) target1.getCustomData().get(KEY);
                    if (data.targets2 == null) {
                        data.targets2 = new ArrayList();
                    }
                    for (ShipAPI target : Global.getCombatEngine().getShips()) {
                        if (target.getOwner() == this.ship.getOwner() && target != this.ship && Objects.equals(target.getHullSpec().getHullId(), this.ship.getHullSpec().getHullId()) && !this.targets.contains(target)) {
                            float d = Vector2f.sub(target.getLocation(), target1.getLocation(), new Vector2f()).length() - target1.getShieldRadiusEvenIfNoShield() - target.getShieldRadiusEvenIfNoShield();//判定距离
                            if (d <= 1200f && totalamount + this.targets.size() < 5) {
                                data.targets2.add(target);
                                totalamount++;
                            }
                        }
                    }
                }
            }
            if (player) {
                if (timer1 <= 3f) {
                    arg2 = Fs_arcfind.Findarc(this.ship.getLocation(), this.ship.getMouseTarget());
                } else {
                    float arg1 = Fs_arcfind.Findarc(this.ship.getLocation(), this.ship.getMouseTarget());
                    if (Math.abs(arg1 - arg2) <= 180f) {
                        if (arg2 <= arg1) {
                            arg2 = Math.min(arg2 + 30f * amount, arg1);
                        } else {
                            arg2 = Math.max(arg2 - 30f * amount, arg1);
                        }
                    } else {
                        if (arg2 <= arg1) {
                            arg2 = Math.max(arg2 - 30f * amount, arg1 - 360f);
                        } else {
                            arg2 = Math.min(arg2 + 30f * amount, arg1 + 360f);
                        }
                    }
                }
            } else {
                if (this.ship.getShipTarget() != null) {
                    arg2 = Fs_arcfind.Findarc(this.ship.getLocation(), this.ship.getShipTarget().getLocation());
                } else {
                    arg2 = this.ship.getFacing();
                }
            }
            timer1 += amount;
            SpriteAPI sprite = Global.getSettings().getSprite("fs_beamsystem", "fs_bs_1");
            MagicRenderPlugin.addSingleframe(sprite, this.ship.getLocation(), CombatEngineLayers.UNDER_SHIPS_LAYER);
            sprite.setAngle(30f * timer1);
            sprite.setColor(paramColor);
            if (timer1 <= 0.5f) {
                sprite.setAlphaMult(timer1 * 2f);
            } else if (timer1 >= 4.5f) {
                sprite.setAlphaMult((5f - timer1) * 2f);
            }
            if (timer1 >= 3f && timer1 <= 5f) {
                float r = 74f;
                Vector2f p1 = new Vector2f(this.ship.getLocation().getX() + (74f) * (float) Math.cos(Math.toRadians(arg2)), this.ship.getLocation().getY() + (74f) * (float) Math.sin(Math.toRadians(arg2)));
                float alp = 1f;
                if (timer1 <= 0.5f) {
                    alp = timer1 * 2f;
                } else if (timer1 >= 4.5f) {
                    alp = (5f - timer1) * 2f;
                }
                SpriteAPI sprite1 = Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow");
                MagicRenderPlugin.addSingleframe(sprite1, p1, CombatEngineLayers.ABOVE_SHIPS_LAYER);
                sprite1.setAngle(30f * timer1);
                sprite1.setColor(beamColor);
                sprite1.setSize(Math.min(20f + (timer1 - 3f) * 20f, 20f * (1f + (totalamount + this.targets.size()) * 0.5f)), Math.min(20f + (timer1 - 3f) * 20f, 20f * (1f + (totalamount + this.targets.size()) * 0.5f)));
                sprite1.setAlphaMult(alp);

                MagicFakeBeam.spawnAdvancedFakeBeam(Global.getCombatEngine(),
                        new Vector2f(this.ship.getLocation().getX() + (r - 7f) * (float) Math.cos(Math.toRadians(arg2)), this.ship.getLocation().getY() + (r - 7f) * (float) Math.sin(Math.toRadians(arg2))),
                        Math.min(4000f * (timer1 - 3f), 1000f), arg2, 10f * (1f + (totalamount + this.targets.size()) * 0.5f), 10f * (1f + (totalamount + this.targets.size()) * 0.5f), 0f, "fs_nocover",
                        "fs_nocover", 512,
                        500f * timer1,
                        20f,
                        20f,
                        amount, 0f, 50f * (1 + (totalamount + this.targets.size()) * 0.5f), paramColor, paramColor, 1000f * amount * (1 + (totalamount + this.targets.size()) * 0.5f), DamageType.ENERGY, 0f, this.ship);
            }
            if (this.targets != null && this.targets.size() > 0) {
                for (ShipAPI target : this.targets) {
                    if (!target.getCustomData().containsKey(KEY)) {
                        DataContainer data = new DataContainer();
                        target.setCustomData(KEY, data);
                    }
                    DataContainer data = (DataContainer) target.getCustomData().get(KEY);
                    if (data.targets2 != null && data.targets2.size() > 0) {
                        for (ShipAPI target2 : data.targets2) {
                            float arg = Fs_arcfind.Findarc(target2.getLocation(), target.getLocation());
                            float d = Vector2f.sub(target2.getLocation(), target.getLocation(), new Vector2f()).length();//判定距离
                            float r = 74f;
                            SpriteAPI sprite1 = Global.getSettings().getSprite("fs_beamsystem", "fs_bs_1");
                            MagicRenderPlugin.addSingleframe(sprite1, target2.getLocation(), CombatEngineLayers.UNDER_SHIPS_LAYER);
                            SpriteAPI sprite2 = Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow");
                            sprite1.setAngle(30f * timer1);
                            sprite1.setColor(paramColor);
                            sprite2.setAngle(90f * timer1);
                            sprite2.setColor(paramColor);
                            sprite2.setSize(20f, 20f);
                            if (timer1 <= 0.5f) {
                                sprite1.setAlphaMult(timer1 * 2f);
                                sprite2.setAlphaMult(timer1 * 2f);
                            } else if (timer1 >= 4.5f) {
                                sprite1.setAlphaMult((5f - timer1) * 2f);
                                sprite2.setAlphaMult((5f - timer1) * 2f);
                            }
                            if (timer1 <= 1f) {
                                MagicRenderPlugin.addSingleframe(sprite2, new Vector2f(target2.getLocation().getX() + r * (float) Math.cos(Math.toRadians(arg - 180f + 180f * timer1)), target2.getLocation().getY() + r * (float) Math.sin(Math.toRadians(arg - 180f + 180f * timer1))), CombatEngineLayers.UNDER_SHIPS_LAYER);
                                MagicRenderPlugin.addSingleframe(sprite2, new Vector2f(target2.getLocation().getX() + r * (float) Math.cos(Math.toRadians(arg - 180f - 180f * timer1)), target2.getLocation().getY() + r * (float) Math.sin(Math.toRadians(arg - 180f - 180f * timer1))), CombatEngineLayers.UNDER_SHIPS_LAYER);
                            }
                            if (timer1 <= 1f) {
                                MagicRenderPlugin.addSingleframe(sprite2, new Vector2f(target2.getLocation().getX() + r * (float) Math.cos(Math.toRadians(arg - 180f + 180f * timer1)), target2.getLocation().getY() + r * (float) Math.sin(Math.toRadians(arg - 180f + 180f * timer1))), CombatEngineLayers.UNDER_SHIPS_LAYER);
                                MagicRenderPlugin.addSingleframe(sprite2, new Vector2f(target2.getLocation().getX() + r * (float) Math.cos(Math.toRadians(arg - 180f - 180f * timer1)), target2.getLocation().getY() + r * (float) Math.sin(Math.toRadians(arg - 180f - 180f * timer1))), CombatEngineLayers.UNDER_SHIPS_LAYER);
                            } else if (timer1 <= 2f) {
                                MagicRenderPlugin.addSingleframe(sprite2, new Vector2f(target2.getLocation().getX() + r * (float) Math.cos(Math.toRadians(arg)) + (d - 2 * r) * (timer1 - 1f) * (float) Math.cos(Math.toRadians(arg)), target2.getLocation().getY() + r * (float) Math.sin(Math.toRadians(arg)) + (d - 2 * r) * (timer1 - 1f) * (float) Math.sin(Math.toRadians(arg))), CombatEngineLayers.UNDER_SHIPS_LAYER);
                            }
                        }
                    }
                    float arg = Fs_arcfind.Findarc(target.getLocation(), this.ship.getLocation());
                    float d = Vector2f.sub(target.getLocation(), this.ship.getLocation(), new Vector2f()).length();//判定距离
                    float r = 74f;
                    SpriteAPI sprite1 = Global.getSettings().getSprite("fs_beamsystem", "fs_bs_1");
                    MagicRenderPlugin.addSingleframe(sprite1, target.getLocation(), CombatEngineLayers.UNDER_SHIPS_LAYER);
                    SpriteAPI sprite2 = Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow");
                    sprite1.setAngle(30f * timer1);
                    sprite1.setColor(paramColor);
                    sprite2.setAngle(90f * timer1);
                    sprite2.setColor(paramColor);
                    sprite2.setSize(20f, 20f);
                    if (timer1 <= 0.5f) {
                        sprite1.setAlphaMult(timer1 * 2f);
                        sprite2.setAlphaMult(timer1 * 2f);
                    } else if (timer1 >= 4.5f) {
                        sprite1.setAlphaMult((5f - timer1) * 2f);
                        sprite2.setAlphaMult((5f - timer1) * 2f);
                    }
                    if (timer1 <= 1f) {
                        MagicRenderPlugin.addSingleframe(sprite2, new Vector2f(target.getLocation().getX() + r * (float) Math.cos(Math.toRadians(arg - 180f + 180f * timer1)), target.getLocation().getY() + r * (float) Math.sin(Math.toRadians(arg - 180f + 180f * timer1))), CombatEngineLayers.UNDER_SHIPS_LAYER);
                        MagicRenderPlugin.addSingleframe(sprite2, new Vector2f(target.getLocation().getX() + r * (float) Math.cos(Math.toRadians(arg - 180f - 180f * timer1)), target.getLocation().getY() + r * (float) Math.sin(Math.toRadians(arg - 180f - 180f * timer1))), CombatEngineLayers.UNDER_SHIPS_LAYER);
                    }
                    if (timer1 <= 1f) {
                        MagicRenderPlugin.addSingleframe(sprite2, new Vector2f(target.getLocation().getX() + r * (float) Math.cos(Math.toRadians(arg - 180f + 180f * timer1)), target.getLocation().getY() + r * (float) Math.sin(Math.toRadians(arg - 180f + 180f * timer1))), CombatEngineLayers.UNDER_SHIPS_LAYER);
                        MagicRenderPlugin.addSingleframe(sprite2, new Vector2f(target.getLocation().getX() + r * (float) Math.cos(Math.toRadians(arg - 180f - 180f * timer1)), target.getLocation().getY() + r * (float) Math.sin(Math.toRadians(arg - 180f - 180f * timer1))), CombatEngineLayers.UNDER_SHIPS_LAYER);
                    } else if (timer1 <= 2f) {
                        MagicRenderPlugin.addSingleframe(sprite2, new Vector2f(target.getLocation().getX() + r * (float) Math.cos(Math.toRadians(arg)) + (d - 2 * r) * (timer1 - 1f) * (float) Math.cos(Math.toRadians(arg)), target.getLocation().getY() + r * (float) Math.sin(Math.toRadians(arg)) + (d - 2 * r) * (timer1 - 1f) * (float) Math.sin(Math.toRadians(arg))), CombatEngineLayers.UNDER_SHIPS_LAYER);
                    } else if (timer1 <= 3f) {
                        MagicRenderPlugin.addSingleframe(sprite2, new Vector2f(this.ship.getLocation().getX() + r * (float) Math.cos(Math.toRadians(arg - 180f + (arg2 - arg + 180f) * (timer1 - 2f))), this.ship.getLocation().getY() + r * (float) Math.sin(Math.toRadians(arg - 180f + (arg2 - arg + 180f) * (timer1 - 2f)))), CombatEngineLayers.UNDER_SHIPS_LAYER);
                        MagicRenderPlugin.addSingleframe(sprite2, new Vector2f(this.ship.getLocation().getX() + r * (float) Math.cos(Math.toRadians(arg - 180f - (180 - arg2 + arg) * (timer1 - 2f))), this.ship.getLocation().getY() + r * (float) Math.sin(Math.toRadians(arg - 180f - (180 - arg2 + arg) * (timer1 - 2f)))), CombatEngineLayers.UNDER_SHIPS_LAYER);
                    }
                }
            }
        }

        @Override
        public float getRenderRadius() {
            return 1000000f;
        }

        @Override
        public EnumSet<CombatEngineLayers> getActiveLayers() {
            return EnumSet.of(CombatEngineLayers.UNDER_SHIPS_LAYER);
        }

        @Override
        public void render(CombatEngineLayers layer, ViewportAPI viewport) {
            if (this.targets != null && this.targets.size() > 0) {
                for (ShipAPI target : this.targets) {
                    if (!target.getCustomData().containsKey(KEY)) {
                        DataContainer data = new DataContainer();
                        target.setCustomData(KEY, data);
                    }
                    DataContainer data = (DataContainer) target.getCustomData().get(KEY);
                    if (data.targets2 != null && data.targets2.size() > 0) {
                        for (ShipAPI target2 : data.targets2) {
                            float arg = Fs_arcfind.Findarc(target2.getLocation(), target.getLocation());
                            float r = 74f;
                            float alp = 1f;
                            Vector2f p1 = new Vector2f(target2.getLocation().getX() + r * (float) Math.cos(Math.toRadians(arg)), target2.getLocation().getY() + r * (float) Math.sin(Math.toRadians(arg)));
                            Vector2f p2 = new Vector2f(target.getLocation().getX() - r * (float) Math.cos(Math.toRadians(arg)), target.getLocation().getY() - r * (float) Math.sin(Math.toRadians(arg)));
                            SpriteAPI sprite = Global.getSettings().getSprite("fs_beamsystem", "fs_bs_2");
                            if (timer1 <= 0.5f) {
                                alp = timer1 * 2f;
                            } else if (timer1 >= 4.5f) {
                                alp = (5f - timer1) * 2f;
                            }
                            renderLine(paramColor, p1, sprite, p2, 0.1f, alp, 3f * timer1, 14f);
                        }
                    }
                    float arg = Fs_arcfind.Findarc(target.getLocation(), this.ship.getLocation());
                    float r = 74f;
                    float alp = 1f;
                    Vector2f p1 = new Vector2f(target.getLocation().getX() + r * (float) Math.cos(Math.toRadians(arg)), target.getLocation().getY() + r * (float) Math.sin(Math.toRadians(arg)));
                    Vector2f p2 = new Vector2f(this.ship.getLocation().getX() - r * (float) Math.cos(Math.toRadians(arg)), this.ship.getLocation().getY() - r * (float) Math.sin(Math.toRadians(arg)));
                    SpriteAPI sprite = Global.getSettings().getSprite("fs_beamsystem", "fs_bs_2");
                    if (timer1 <= 0.5f) {
                        alp = timer1 * 2f;
                    } else if (timer1 >= 4.5f) {
                        alp = (5f - timer1) * 2f;
                    }
                    renderLine(paramColor, p1, sprite, p2, 0.1f, alp, 3f * timer1, 14f);

                }
            }
            if (timer1 >= 3f) {
                SpriteAPI sprite = Global.getSettings().getSprite("fx", "fs_bs_beam1");
                SpriteAPI sprite2 = Global.getSettings().getSprite("fx", "fs_bs_beam2");
                Vector2f p1 = new Vector2f(this.ship.getLocation().getX() + (74f) * (float) Math.cos(Math.toRadians(arg2)), this.ship.getLocation().getY() + (74f) * (float) Math.sin(Math.toRadians(arg2)));
                Vector2f p2 = findbeamend(p1, 1000f, arg2, 20f,
                        20f, this.ship);
                float alp = 1f;
                if (timer1 <= 3.5f) {
                    alp = (timer1 - 3f) * 2f;
                } else if (timer1 >= 4.5f) {
                    alp = (5f - timer1) * 2f;
                }
                renderLine(beamColor, p1, sprite, p2, 0.1f, alp, 3f * timer1, 10f * (1f + (totalamount + this.targets.size()) * 0.5f));
                renderLine(beamColor, p1, sprite, p2, 0.1f, alp, 3f * timer1, 10f * (1f + (totalamount + this.targets.size()) * 0.5f));
            }
        }

        public void renderLine(Color paramColor, Vector2f anchor, SpriteAPI lineTex, Vector2f target, double t, float alphaMult, float textElapsed, float width) {
            float arg = Fs_arcfind.Findarc(anchor, target);

            GL11.glPushMatrix();
            GL11.glTranslatef(0f, 0f, 0f);
            GL11.glRotatef(0f, 0f, 0f, 1f);

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            lineTex.bindTexture();

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

            GL11.glColor4ub((byte) paramColor.getRed(), (byte) paramColor.getGreen(), (byte) paramColor.getBlue(), (byte) (paramColor.getAlpha() * alphaMult));

            float base = 0f;
            float maxTex = lineTex.getTextureHeight();
            float maxWidth=lineTex.getWidth();
            GL11.glBegin(GL11.GL_QUAD_STRIP);

            double x = anchor.getX() + 0.5f * width * Math.cos(Math.toRadians(arg + 90f));
            double y = anchor.getY() + 0.5f * width * Math.sin(Math.toRadians(arg + 90f));
            double leftEdgeOfShowingTex = 0d;
            for (double k = 0; k <= 1.1d; k += t) {

                double nextX = anchor.getX() + (target.getX() - anchor.getX()) * (k + t) + 0.5f * width * Math.cos(Math.toRadians(arg + 90f));
                double nextY = anchor.getY() + (target.getY() - anchor.getY()) * (k + t) + 0.5f * width * Math.sin(Math.toRadians(arg + 90f));
                double distance = Math.hypot(nextX - x, nextY - y);

                GL11.glTexCoord2f((float) (leftEdgeOfShowingTex + textElapsed), base);
                GL11.glVertex2f((float) x, (float) y);

                x = anchor.getX() + (target.getX() - anchor.getX()) * k + 0.5f * width * Math.cos(Math.toRadians(arg - 90f));
                y = anchor.getY() + (target.getY() - anchor.getY()) * k + 0.5f * width * Math.sin(Math.toRadians(arg - 90f));
                GL11.glTexCoord2f((float) (leftEdgeOfShowingTex + textElapsed), maxTex);
                GL11.glVertex2f((float) x, (float) y);

                x = nextX;
                y = nextY;
                leftEdgeOfShowingTex += distance/maxWidth;
            }

            GL11.glEnd();
            GL11.glPopMatrix();
        }
    }
}
