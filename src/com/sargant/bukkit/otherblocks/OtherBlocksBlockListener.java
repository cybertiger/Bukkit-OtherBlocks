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

public class OtherBlocksBlockListener extends BlockListener
{
	private OtherBlocks parent;

	public OtherBlocksBlockListener(OtherBlocks instance) {
		parent = instance;
	}
	
	@Override
	public void onLeavesDecay(LeavesDecayEvent event) {
		
		if(event.isCancelled()) return;

		boolean successfulComparison = false;
		boolean doDefaultDrop = false;
		Block target = event.getBlock();
		
		
		for(OtherBlocksContainer obc : parent.transformList) {
		    
		    // Get the leaf's data value
            // Beware of the 0x4 bit being set - use a bitmask of 0x3
            Short leafData = (short) ((0x3) & event.getBlock().getData());
		    
		    if(!obc.compareTo(
		            "SPECIAL_LEAFDECAY", 
		            leafData, 
		            Material.AIR.toString(), 
		            target.getWorld(),
		            null,
		            parent.permissionHandler)) {
		        continue;
		    }
			
			// Check RNG is OK
			if(parent.rng.nextDouble() > (obc.chance.doubleValue()/100)) continue;
			
			// Now drop OK
			if(obc.dropped.equalsIgnoreCase("DEFAULT")) doDefaultDrop = true;
			
			successfulComparison = true;
			OtherBlocks.performDrop(target.getLocation(), obc, null);
		}
		
		if(successfulComparison && !doDefaultDrop) {
			// Convert the target block
			event.setCancelled(true);
			target.setType(Material.AIR);
		}
		
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (event.isCancelled()) return;

		boolean otherblocksActive = true;

		if (parent.permissionsPlugin != null) {
			if (!(parent.permissionHandler.has(event.getPlayer(), "otherblocks.active"))) {
				otherblocksActive = false;
			}			
		}

		if (otherblocksActive) {

			Block target  = event.getBlock();
			ItemStack tool = event.getPlayer().getItemInHand();
			Integer maxDamage = 0;
			boolean successfulComparison = false;
			boolean doDefaultDrop = false;
			Integer maxAttackerDamage = 0;

			for(OtherBlocksContainer obc : parent.transformList) {

				if(!obc.compareTo(
						event.getBlock(),
						(short) event.getBlock().getData(),
						tool.getType().toString(), 
						target.getWorld(),
						event.getPlayer(),
						parent.permissionHandler)) {

					continue;
				}

				// Check probability is great than the RNG
				if(parent.rng.nextDouble() > (obc.chance.doubleValue()/100)) continue;

				// At this point, the tool and the target block match
				successfulComparison = true;
				if(obc.dropped.equalsIgnoreCase("DEFAULT")) doDefaultDrop = true;
				OtherBlocks.performDrop(target.getLocation(), obc, event.getPlayer());

				maxDamage = (maxDamage < obc.damage) ? obc.damage : maxDamage;
				
				Integer currentAttackerDamage = obc.getRandomAttackerDamage();
				maxAttackerDamage = (maxAttackerDamage < currentAttackerDamage) ? currentAttackerDamage : maxAttackerDamage;
			}

			if(successfulComparison && !doDefaultDrop) {

				// give a chance for logblock (if available) to log the block destruction
				OtherBlocks.queueBlockBreak(event.getPlayer().getName(), event.getBlock().getState());

				// Convert the target block
				event.setCancelled(true);
				target.setType(Material.AIR);

				// Deal player damage if set
				if (event.getPlayer() != null) {
					event.getPlayer().damage(maxAttackerDamage);
				}

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

	}
	
	@Override
	public void onBlockFromTo(BlockFromToEvent event) {

		if (event.isCancelled()) return;
		if(event.getBlock().getType() != Material.WATER && event.getBlock().getType() != Material.STATIONARY_WATER)
			return;
		if(event.getToBlock().getType() == Material.AIR) return;

		Block target  = event.getToBlock();
		Integer maxDamage = 0;
		boolean successfulComparison = false;
		boolean doDefaultDrop = false;

		for(OtherBlocksContainer obc : parent.transformList) {
		    
		    if(!obc.compareTo(
		            event.getBlock().getType().toString(),
		            (short) event.getBlock().getData(),
		            "DAMAGE_WATER", 
		            target.getWorld(),
		            null,
		            parent.permissionHandler)) {
		        
		        continue;
		    }

		    // Check probability is great than the RNG
			if(parent.rng.nextDouble() > (obc.chance.doubleValue()/100)) continue;

			// At this point, the tool and the target block match
			successfulComparison = true;
			if(obc.dropped.equalsIgnoreCase("DEFAULT")) doDefaultDrop = true;
			OtherBlocks.performDrop(target.getLocation(), obc, null);
			maxDamage = (maxDamage < obc.damage) ? obc.damage : maxDamage;
		}

		if(successfulComparison && !doDefaultDrop) {

			// Convert the target block
			event.setCancelled(true);
			target.setType(Material.AIR);
		}
	}
}

