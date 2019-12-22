/*
 * This file is part of Movecraft.
 *
 *     Movecraft is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Movecraft is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Movecraft.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.countercraft.movecraft;

import org.bukkit.block.BlockFace;

public enum Rotation {
    CLOCKWISE, NONE, ANTICLOCKWISE;

    public BlockFace rotate4d(BlockFace in) {
        if(this == CLOCKWISE) {
            switch(in) {
                case NORTH:
                    return BlockFace.EAST;
                case EAST:
                    return BlockFace.SOUTH;
                case SOUTH:
                    return BlockFace.WEST;
                case WEST:
                    return BlockFace.NORTH;
                default:
                    return in;
            }
        } else if(this == ANTICLOCKWISE) {
            switch(in) {
                case NORTH:
                    return BlockFace.WEST;
                case WEST:
                    return BlockFace.SOUTH;
                case SOUTH:
                    return BlockFace.EAST;
                case EAST:
                    return BlockFace.NORTH;
                default:
                    return in;
            }
        } else {
            return in;
        }
    }
}
