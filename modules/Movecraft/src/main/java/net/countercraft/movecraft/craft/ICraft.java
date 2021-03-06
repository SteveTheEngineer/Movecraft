package net.countercraft.movecraft.craft;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.Rotation;
import net.countercraft.movecraft.async.detection.DetectionTask;
import net.countercraft.movecraft.async.rotation.RotationTask;
import net.countercraft.movecraft.async.translation.TranslationTask;
import net.countercraft.movecraft.localisation.I18nSupport;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static net.countercraft.movecraft.utils.SignUtils.getFacing;

public class ICraft extends Craft {
    private final UUID id = UUID.randomUUID();

    public ICraft(@NotNull CraftType type, @NotNull World world) {
        super(type, world);
    }


    @Override
    public void detect(Player player, Player notificationPlayer, MovecraftLocation startPoint) {
        this.setNotificationPlayer(notificationPlayer);
        Block block = startPoint.toBukkit(w).getBlock();
        if(block.getType().name().endsWith("_SIGN") && block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            if(sign.getLine(0).equalsIgnoreCase(type.getCraftName())) {
                final BlockFace currentlyfacing = BlockFace.valueOf(sign.getLine(1));
                if(currentlyfacing == BlockFace.NORTH || currentlyfacing == BlockFace.EAST || currentlyfacing == BlockFace.SOUTH || currentlyfacing == BlockFace.WEST) {
                    setDirection(currentlyfacing);
                } else {
                    setDirection(player.getFacing());
                }
            }
        } else {
            setDirection(player.getFacing());
        }
        Movecraft.getInstance().getAsyncManager().submitTask(new DetectionTask(this, startPoint, player), this);
    }

    @Override
    public void translate(int dx, int dy, int dz) {
        // check to see if the craft is trying to move in a direction not permitted by the type
        if (!this.getType().allowHorizontalMovement() && !this.getSinking()) {
            dx = 0;
            dz = 0;
        }
        if (!this.getType().allowVerticalMovement() && !this.getSinking()) {
            dy = 0;
        }
        if (dx == 0 && dy == 0 && dz == 0) {
            return;
        }

        if (!this.getType().allowVerticalTakeoffAndLanding() && dy != 0 && !this.getSinking()) {
            if (dx == 0 && dz == 0) {
                return;
            }
        }
        boolean skipChecks = false;
        if(this.getType().getAllowMovementDiagonal()) { // Experimental
            if(dx != 0 && dz != 0) {
                skipChecks = true;
            }
        }
        if(!skipChecks && !this.getType().getAllowMovementForward()) {
            if (this.getDirection() == BlockFace.NORTH && dz < 0) {
                dz = 0;
            } else if (this.getDirection() == BlockFace.EAST && dx > 0) {
                dx = 0;
            } else if (this.getDirection() == BlockFace.SOUTH && dz > 0) {
                dz = 0;
            } else if (this.getDirection() == BlockFace.WEST && dx < 0) {
                dx = 0;
            }
        }
        if(!skipChecks && !this.getType().getAllowMovementBackward()) {
            if(this.getDirection() == BlockFace.NORTH && dz > 0) {
                dz = 0;
            } else if(this.getDirection() == BlockFace.EAST && dx < 0) {
                dx = 0;
            } else if(this.getDirection() == BlockFace.SOUTH && dz < 0) {
                dz = 0;
            } else if(this.getDirection() == BlockFace.WEST && dx > 0) {
                dx = 0;
            }
        }
        if(!skipChecks && !this.getType().getAllowMovementLeft()) {
            if(this.getDirection() == BlockFace.NORTH && dx < 0) {
                dx = 0;
            } else if(this.getDirection() == BlockFace.EAST && dz < 0) {
                dz = 0;
            } else if(this.getDirection() == BlockFace.SOUTH && dx > 0) {
                dx = 0;
            } else if(this.getDirection() == BlockFace.WEST && dz > 0) {
                dz = 0;
            }
        }
        if(!skipChecks && !this.getType().getAllowMovementRight()) {
            if(this.getDirection() == BlockFace.NORTH && dx > 0) {
                dx = 0;
            } else if(this.getDirection() == BlockFace.EAST && dz > 0) {
                dz = 0;
            } else if(this.getDirection() == BlockFace.SOUTH && dx < 0) {
                dx = 0;
            } else if(this.getDirection() == BlockFace.WEST && dz < 0) {
                dz = 0;
            }
        }

        // find region that will need to be loaded to translate this craft
        /*int cminX = minX;
        int cmaxX = minX;
        if (dx < 0)
            cminX = cminX + dx;
        int cminZ = minZ;
        int cmaxZ = minZ;
        if (dz < 0)
            cminZ = cminZ + dz;
        for (MovecraftLocation m : blockList) {
            if (m.getX() > cmaxX)
                cmaxX = m.getX();
            if (m.getZ() > cmaxZ)
                cmaxZ = m.getZ();
        }
        if (dx > 0)
            cmaxX = cmaxX + dx;
        if (dz > 0)
            cmaxZ = cmaxZ + dz;
        cminX = cminX >> 4;
        cminZ = cminZ >> 4;
        cmaxX = cmaxX >> 4;
        cmaxZ = cmaxZ >> 4;


        // load all chunks that will be needed to translate this craft
        for (int posX = cminX - 1; posX <= cmaxX + 1; posX++) {
            for (int posZ = cminZ - 1; posZ <= cmaxZ + 1; posZ++) {
                if (!this.getW().isChunkLoaded(posX, posZ)) {
                    this.getW().loadChunk(posX, posZ);
                }
            }
        }*/
        Movecraft.getInstance().getAsyncManager().submitTask(new TranslationTask(this, dx, dy, dz), this);
    }

    @Override
    public void rotate(Rotation rotation, MovecraftLocation originPoint) {
        if(getLastRotateTime()+1e9>System.nanoTime()){
            if(getNotificationPlayer()!=null)
                getNotificationPlayer().sendMessage(I18nSupport.getInternationalisedString("Rotation - Turning Too Quickly"));
            return;
        }
        setLastRotateTime(System.nanoTime());
        RotationTask task = new RotationTask(this, originPoint, rotation, this.getW());
        Movecraft.getInstance().getAsyncManager().submitTask(task, this);
    }

    @Override
    public void rotate(Rotation rotation, MovecraftLocation originPoint, boolean isSubCraft) {
        RotationTask task = new RotationTask(this, originPoint, rotation, this.getW(), isSubCraft);
        Movecraft.getInstance().getAsyncManager().submitTask(task, this);
    }




    @Override
    public void resetSigns(@NotNull Sign clicked) {
        for (final MovecraftLocation ml : hitBox) {
            final Block b = ml.toBukkit(w).getBlock();
            if (!(b.getState() instanceof Sign)) {
                continue;
            }
            final Sign sign = (Sign) b.getState();
            if (sign.equals(clicked)) {
                continue;
            }
            if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("Cruise: ON")){
                sign.setLine(0, "Cruise: OFF");
            }
            else if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("Cruise: OFF")
                    && ChatColor.stripColor(clicked.getLine(0)).equalsIgnoreCase("Cruise: ON")
                    && getFacing(sign) == getFacing(clicked)) {
                    sign.setLine(0,"Cruise: ON");
            }
            else if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("Ascend: ON")){
                sign.setLine(0, "Ascend: OFF");
            }
            else if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("Ascend: OFF")
                    && ChatColor.stripColor(clicked.getLine(0)).equalsIgnoreCase("Ascend: ON")){
                sign.setLine(0, "Ascend: ON");
            }
            else if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("Descend: ON")){
                sign.setLine(0, "Descend: OFF");
            }
            else if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("Descend: OFF")
                    && ChatColor.stripColor(clicked.getLine(0)).equalsIgnoreCase("Descend: ON")){
                sign.setLine(0, "Descend: ON");
            }
            sign.update();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ICraft)){
            return false;
        }
        return this.id.equals(((ICraft)obj).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
