package Ozone.Commands.Task;

import Atom.Meth;
import Ozone.Commands.BotInterface;
import Ozone.Commands.Pathfinding;
import Ozone.Patch.DesktopInput;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.ai.Astar;
import mindustry.content.Blocks;
import mindustry.gen.Call;
import mindustry.world.Tile;

import static Ozone.Commands.Pathfinding.distanceTo;

//TODO relocate these method
public class Move extends Task {
    private final Vec2 destPos, destTilePos;
    private final float airTolerance = 1.2f, landTolerance = 0.04f;
    private Tile destTile = null;
    private Seq<Tile> pathfindingCache = new Seq<>();

    public Move(float x, float y) {
        this(new Vec2(x, y));
    }

    public Move(Vec2 dest) {
        destPos = new Vec2(dest.x * 8, dest.y * 8);
        destTilePos = dest;
        destTile = Vars.world.tile(Math.round(dest.x), Math.round(dest.y));
        if(destTile == null)
            tellUser("what, there is nothing in there");
        if (!Vars.player.unit().isFlying()) {

            pathfindingCache = Astar.pathfind(Vars.player.tileOn(), destTile, Pathfinding::isSafe, s -> {
                return  s != null&&s.passable() && s.floor() != Blocks.deepwater.asFloor() && s.build == null;
            });

        }
    }

    @Override
    public void taskCompleted() {
        Vars.player.reset();
        if (!pathfindingCache.isEmpty()) Call.sendChatMessage("/sync");
        setMov(new Vec2(0, 0));
        super.taskCompleted();
    }

    @Override
    public boolean isCompleted() {
        if (Vars.player.unit().isFlying())
            return distanceTo(BotInterface.getCurrentPos(), destPos) < airTolerance * 1.2f;
        else
            return distanceTo(BotInterface.getCurrentPos(), destPos) < landTolerance || pathfindingCache.isEmpty();
    }

    @Override
    public void update() {
        if (!Vars.player.unit().isFlying()) {
            if (pathfindingCache.isEmpty()) return;
            for (Tile t : pathfindingCache) {
                if (t.block() == null)
                    tellUser("Null block: " + t.toString());
                else if (t.block().isFloor())
                    t.setOverlay(Blocks.magmarock);
                else if (t.block().isStatic())
                    t.setOverlay(Blocks.dirtWall);
            }
            if (destTile != null) {
                if (distanceTo(BotInterface.getCurrentTilePos(), new Vec2(destTile.x, destTile.y)) <= landTolerance) {
                    pathfindingCache.remove(0).clearOverlay();
                }
            }
            if (pathfindingCache.isEmpty()) return;
            destTile = pathfindingCache.get(0);
            destTile.setOverlay(Blocks.dirt);
        }
        setMov(destTile);
    }

    public void setMov(Tile targetTile){
        Vec2 vec = new Vec2();
        vec.trns(Vars.player.unit().angleTo(targetTile), Vars.player.unit().type().speed * 2f);
        Log.debug("Ozone-AI @", "DriveX: " + vec.x);
        Log.debug("Ozone-AI @", "DriveY: " + vec.y);
        Vars.player.unit().moveAt(vec);
    }

    public void setMov(Vec2 mov) {
        if (Vars.control.input instanceof DesktopInput) ((DesktopInput) Vars.control.input).setMove(mov);
        else Log.infoTag("Ozone", "Can't control movement, DesktopInput not patched");
    }

    public float getCurrentDistance() {
        return (float) distanceTo(BotInterface.getCurrentTilePos(), destTilePos);
    }



}
