package net.countercraft.movecraft.mapUpdater.update;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;

import java.util.*;

public class EntityDamageCommand extends UpdateCommand {
    private final Craft craft;

    public EntityDamageCommand(Craft craft) {
        this.craft = craft;
    }

    public Craft getCraft() {
        return craft;
    }

    @Override
    public void doUpdate() {
        final Set<Damageable> foundEntities = new HashSet<>();
        for(MovecraftLocation location : craft.getHitBox()) {
            if(!location.toBukkit(craft.getW()).getBlock().getType().isSolid()) {
                continue;
            }
            craft.getW().getNearbyEntities(location.toBukkit(craft.getW()), 0.5, 0.5, 0.5, e -> e instanceof Damageable).forEach(e -> {
                if(!(e instanceof Damageable)) { // Double check for whatever reason
                    return;
                }
                if(!craft.getType().getDamageEntitiesAtLegs()) {
                    return;
                }
                if(!craft.getHitBox().contains(e.getLocation().getBlockX(), e.getLocation().getBlockY(), e.getLocation().getBlockZ())) { // Another check
                    return;
                }
                if(!e.getLocation().getBlock().getType().isSolid()) {
                    return;
                }
                foundEntities.add((Damageable) e);
            });
            craft.getW().getNearbyEntities(location.translate(0, -1, 0).toBukkit(craft.getW()), 0.5, 0.5, 0.5, e -> e instanceof Damageable).forEach(e -> {
                if(!(e instanceof Damageable)) {
                    return;
                }
                if(!craft.getType().getDamageEntitiesAtHead()) {
                    return;
                }
                if(!craft.getHitBox().contains(e.getLocation().getBlockX(), e.getLocation().getBlockY() + 1, e.getLocation().getBlockZ())) { // Another check
                    return;
                }
                if(!e.getLocation().getBlock().getRelative(BlockFace.UP).getType().isSolid()) {
                    return;
                }
                if(!foundEntities.contains(e)) {
                    foundEntities.add((Damageable) e);
                }
            });
        }
        foundEntities.forEach(ent -> {
            ent.damage(craft.getMass() / 100.0F);
            Bukkit.broadcastMessage(ent.getLocation().getBlockX() + ", " + ent.getLocation().getBlockY() + ", " + ent.getLocation().getBlockZ());
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityDamageCommand that = (EntityDamageCommand) o;
        return Objects.equals(craft, that.craft);
    }

    @Override
    public int hashCode() {
        return Objects.hash(craft);
    }
}