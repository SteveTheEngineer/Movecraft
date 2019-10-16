package net.countercraft.movecraft;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import javafx.util.Pair;
import net.countercraft.movecraft.craft.Craft;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.util.Vector;

import java.util.ArrayDeque;
import java.util.HashMap;

public abstract class MovecraftRepair {
    public abstract boolean saveCraftRepairState(Craft craft, Sign sign);

    public abstract boolean saveRegionRepairState(World world, ProtectedRegion region);
    public abstract Clipboard loadCraftRepairStateClipboard(Craft craft, Sign sign);

    public abstract Clipboard loadRegionRepairStateClipboard(String repairStateName, World bukkitWorld);

    public abstract HashMap<javafx.util.Pair<Material, Byte>, Double> getMissingBlocks(String repairName);

    public abstract ArrayDeque<Pair<Vector, Vector>> getMissingBlockLocations(String repairName);

    public abstract long getNumDiffBlocks(String repairName);
}
