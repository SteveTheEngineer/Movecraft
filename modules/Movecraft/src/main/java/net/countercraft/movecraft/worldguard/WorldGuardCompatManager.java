package net.countercraft.movecraft.worldguard;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WorldGuardCompatManager implements Listener {
    @EventHandler
    public void onCraftTranslateEvent(CraftTranslateEvent event){
        if(!Settings.WorldGuardBlockMoveOnBuildPerm)
            return;
        if(event.getCraft().getNotificationPlayer() == null)
            return;
        for(MovecraftLocation location : event.getNewHitBox()){
            if(!Movecraft.getInstance().getWorldGuardPlugin().canBuild(event.getCraft().getNotificationPlayer(),location.toBukkit(event.getCraft().getW()))){
                event.setCancelled(true);
                event.setFailMessage(String.format( I18nSupport.getInternationalisedString( "Translation - Failed Player is not permitted to build in this WorldGuard region" )+" @ %d,%d,%d", location.getX(), location.getY(), location.getZ() ) );
                return;
            }

        }
    }

    @EventHandler
    public void onCraftRotateEvent(CraftRotateEvent event){
        if(!Settings.WorldGuardBlockMoveOnBuildPerm)
            return;
        if(event.getCraft().getNotificationPlayer() == null)
            return;
        for(MovecraftLocation location : event.getNewHitBox()){
            if(!pilotHasAccessToRegion(event.getCraft().getNotificationPlayer(), location, event.getCraft().getW())){
                event.setCancelled(true);
                event.setFailMessage(String.format( I18nSupport.getInternationalisedString("Rotation - Player is not permitted to build in this WorldGuard region" )+" @ %d,%d,%d", location.getX(), location.getY(), location.getZ()));
                return;
            }
        }
    }
    private boolean pilotHasAccessToRegion(Player player, MovecraftLocation location, World world){
        if (Settings.IsLegacy){
            return Movecraft.getInstance().getWorldGuardPlugin().canBuild(player, location.toBukkit(world));
        } else {
            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            LocalPlayer lPlayer;
            Location wgLoc;

            try {
                lPlayer = WorldGuard.getInstance().checkPlayer((Actor) player);
            } catch (CommandException e) {
                e.printStackTrace();
                lPlayer = null;
            }
            query.getApplicableRegions(location.toBukkit(world))
            return true;
        }
    }

}
