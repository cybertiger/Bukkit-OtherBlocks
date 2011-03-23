// OtherBlocks - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.sargant.bukkit.otherblocks;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.block.*;
import org.bukkit.inventory.ItemStack;

import com.sargant.bukkit.common.CommonMaterial;

public class OtherBlocksBlockListener extends BlockListener
{
	private OtherBlocks parent;

	public OtherBlocksBlockListener(OtherBlocks instance) {
		parent = instance;
	}
	
	@Override
	public void onLeavesDecay(LeavesDecayEvent event) {
		
		boolean successfulConversion = false;
		Block target = event.getBlock();
		
		if(event.isCancelled()) return;
		
		for(OtherBlocksContainer obc : parent.transformList) {
			
			// Check it is leaf decay
			if(!OtherBlocks.isLeafDecay(obc.original)) continue;
			
			// Check worlds match
			if(!obc.worlds.contains(null) && !obc.worlds.contains(target.getWorld().getName())) continue;
			
			// Get the leaf's data value
			// Beware of the 0x4 bit being set - use a bitmask of 0x3
			Short leafData = (short) ((0x3) & event.getBlock().getData());
			
			// Check leaf species matches
			if (obc.originalData != null
                    && (leafData < obc.originalData 
                            || leafData > obc.originalDataRangeMax))
			    continue;
			
			// Check RNG is OK
			if(parent.rng.nextDouble() > (obc.chance.doubleValue()/100)) continue;
            
            setQuantity(obc);
			
			// Now drop OK
			successfulConversion = true;
			OtherBlocks.performDrop(target.getLocation(), obc);
		}
		
		if(successfulConversion) {
			// Convert the target block
			event.setCancelled(true);
			target.setType(Material.AIR);
		}
		
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event)
	{

		if (event.isCancelled()) return;

		Block target  = event.getBlock();
		ItemStack tool = event.getPlayer().getItemInHand();
		Integer maxDamage = 0;
		boolean successfulConversion = false;

		for(OtherBlocksContainer obc : parent.transformList) {

			// Check target block is not a creature
			if(OtherBlocks.isCreature(obc.original)) continue;
			
			// Check isn't leaf decay
			if(OtherBlocks.isLeafDecay(obc.original)) continue;

			// Check worlds match
			if(!obc.worlds.contains(null) && !obc.worlds.contains(target.getWorld().getName())) continue;
			
			// Check held item matches
			if(!OtherBlocks.containsValidToolString(tool.getType().toString(), obc.tool)) continue;
			
			// Check target block matches
			if(CommonMaterial.isValidSynonym(obc.original)) {
				if(false == CommonMaterial.isSynonymFor(obc.original, event.getBlock().getType())) continue;
			} else {
				if(Material.valueOf(obc.original) != event.getBlock().getType()) continue;
			}
			
			// Check data value of block matches
            if (obc.originalData != null
                    && (event.getBlock().getData() < obc.originalData 
                            || event.getBlock().getData() > obc.originalDataRangeMax))
                continue;

			// Check probability is great than the RNG
			if(parent.rng.nextDouble() > (obc.chance.doubleValue()/100)) continue;
			
			setQuantity(obc);

			// At this point, the tool and the target block match
			successfulConversion = true;
			OtherBlocks.performDrop(target.getLocation(), obc);
			maxDamage = (maxDamage < obc.damage) ? obc.damage : maxDamage;
		}

		if(successfulConversion) {

			// Convert the target block
			event.setCancelled(true);
			target.setType(Material.AIR);

			// Check the tool can take wear and tear
			if(tool.getType().getMaxDurability() < 0 || tool.getType().isBlock()) return;

			// Now adjust the durability of the held tool
			tool.setDurability((short) (tool.getDurability() + maxDamage));

			// Manually check whether the tool has exceed its durability limit
			if(tool.getDurability() >= tool.getType().getMaxDurability()) {
				event.getPlayer().setItemInHand(null);
			}
		}

	}

    private void setQuantity(OtherBlocksContainer obc) {
        if(obc.min_quantity == null) obc.quantity = 1;
        else if(obc.max_quantity == null) obc.quantity = obc.min_quantity;
        else obc.quantity = obc.min_quantity + parent.rng.nextInt(obc.max_quantity - obc.min_quantity + 1);
    }
}

