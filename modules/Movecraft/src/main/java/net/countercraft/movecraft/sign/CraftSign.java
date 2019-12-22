package net.countercraft.movecraft.sign;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftType;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.ICraft;
import net.countercraft.movecraft.events.CraftPilotEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

public final class CraftSign implements Listener{

    @EventHandler
    public void onSignChange(SignChangeEvent event){
        if(CraftManager.getInstance().getCraftTypeFromString(event.getLine(0)) == null) {
            return;
        }
        if(Settings.RequireCreatePerm && !event.getPlayer().hasPermission("movecraft." + ChatColor.stripColor(event.getLine(0)) + ".create")) {
            event.getPlayer().sendMessage(I18nSupport.getInternationalisedString("Insufficient Permissions"));
            event.setCancelled(true);
            return;
        }

        @Nullable BlockFace currentlyfacing;
        try {
            currentlyfacing = BlockFace.valueOf(event.getLine(1));
        } catch(IllegalArgumentException | NullPointerException e) {
            currentlyfacing = BlockFace.SELF;
        }
        if(currentlyfacing != BlockFace.NORTH && currentlyfacing != BlockFace.EAST && currentlyfacing != BlockFace.SOUTH && currentlyfacing != BlockFace.WEST) {
            BlockFace newfacing = null;
            if(event.getBlock().getType().name().endsWith("_WALL_SIGN")) {
                newfacing = ((WallSign) event.getBlock().getBlockData()).getFacing().getOppositeFace();
            }
            else if(event.getBlock().getType().name().endsWith("_SIGN")) {
                newfacing = ((org.bukkit.block.data.type.Sign) event.getBlock().getBlockData()).getRotation().getOppositeFace();
                switch(newfacing) {
                    case NORTH_NORTH_WEST:
                    case NORTH_NORTH_EAST:
                    case NORTH_EAST:
                        newfacing = BlockFace.NORTH;
                        break;
                    case EAST_NORTH_EAST:
                    case EAST_SOUTH_EAST:
                    case SOUTH_EAST:
                        newfacing = BlockFace.EAST;
                        break;
                    case SOUTH_SOUTH_EAST:
                    case SOUTH_SOUTH_WEST:
                    case SOUTH_WEST:
                        newfacing = BlockFace.SOUTH;
                        break;
                    case WEST_SOUTH_WEST:
                    case WEST_NORTH_WEST:
                    case NORTH_WEST:
                        newfacing = BlockFace.WEST;
                    case NORTH:
                    case EAST:
                    case SOUTH:
                    case WEST:
                        break;
                    default: // Should never happen
                        newfacing = BlockFace.NORTH;
                        break;
                }
            }
            if(newfacing != null) {
                event.setLine(1, newfacing.name());
            }
            else {
                event.setLine(1, BlockFace.NORTH.name());
            }
        }
    }

    @EventHandler
    public final void onSignClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (!(block.getState() instanceof Sign)) {
            return;
        }
        Sign sign = (Sign) event.getClickedBlock().getState();
        CraftType type = CraftManager.getInstance().getCraftTypeFromString(ChatColor.stripColor(sign.getLine(0)));
        if (type == null) {
            return;
        }
        // Valid sign prompt for ship command.
        if (!event.getPlayer().hasPermission("movecraft." + ChatColor.stripColor(sign.getLine(0)) + ".pilot")) {
            event.getPlayer().sendMessage(I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return;
        }
        // Attempt to run detection
        Location loc = event.getClickedBlock().getLocation();
        MovecraftLocation startPoint = new MovecraftLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        final Craft c = new ICraft(type, loc.getWorld());

        if (c.getType().getCruiseOnPilot()) {
            c.detect(null, event.getPlayer(), startPoint);
            final BlockFace direction;
            if (Settings.IsLegacy){
                if (sign.getData() instanceof org.bukkit.material.Sign){
                    org.bukkit.material.Sign signData = (org.bukkit.material.Sign) sign.getData();
                    if (signData.isWallSign()) {
                        direction = signData.getAttachedFace();
                    } else {
                        direction = signData.getFacing().getOppositeFace();
                    }
                }else {
                    direction = null;
                }
            } else {
                if (sign.getBlockData() instanceof org.bukkit.block.data.type.Sign){
                    org.bukkit.block.data.type.Sign signData = (org.bukkit.block.data.type.Sign)sign.getBlockData();
                    direction = signData.getRotation().getOppositeFace();
                }else if (sign.getBlockData() instanceof org.bukkit.block.data.type.WallSign){
                    WallSign signData = (WallSign)sign.getBlockData();
                    direction = signData.getFacing().getOppositeFace();
                } else {
                    direction = null;
                }
            }
            c.setCruiseDirection(direction);
            c.setLastCruiseUpdate(System.currentTimeMillis());
            c.setCruising(true);
            new BukkitRunnable() {
                @Override
                public void run() {
                    c.sink();
                }
            }.runTaskLater(Movecraft.getInstance(), (20 * 15));
        } else {
            if (CraftManager.getInstance().getCraftByPlayer(event.getPlayer()) == null) {
                c.detect(event.getPlayer(), event.getPlayer(), startPoint);
            } else {
                Craft oldCraft = CraftManager.getInstance().getCraftByPlayer(event.getPlayer());
                if (oldCraft.isNotProcessing()) {
                    CraftManager.getInstance().removeCraft(oldCraft);
                    c.detect(event.getPlayer(), event.getPlayer(), startPoint);
                }
            }
        }
        Bukkit.getServer().getPluginManager().callEvent(new CraftPilotEvent(c, CraftPilotEvent.Reason.PLAYER));
        event.setCancelled(true);

    }
}
