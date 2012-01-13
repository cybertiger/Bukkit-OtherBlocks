// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.

package com.gmail.zariust.otherdrops.event;

import static java.lang.Math.max;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.drakespirit.plugins.moneydrop.MoneyDrop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.gmail.zariust.common.Verbosity.*;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.PlayerWrapper;
import com.gmail.zariust.otherdrops.ProfilerEntry;
import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.options.Action;
import com.gmail.zariust.otherdrops.options.SoundEffect;
import com.gmail.zariust.otherdrops.options.ToolDamage;
import com.gmail.zariust.otherdrops.subject.Agent;
import com.gmail.zariust.otherdrops.subject.BlockTarget;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.gmail.zariust.otherdrops.subject.Target;
import com.gmail.zariust.otherdrops.drop.DropType;
import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.drop.ItemDrop;
import com.gmail.zariust.otherdrops.special.SpecialResult;

public class SimpleDropEvent extends CustomDropEvent
{
	// Actions
	private DropType dropped;
	private DoubleRange quantity;
	private IntRange attackerDamage;
	private ToolDamage toolDamage;
	private double dropSpread;
	private BlockTarget replacementBlock;
	private List<SpecialResult> events;
	private List<String> commands;
	private List<String> messages;
	private Set<SoundEffect> effects;

	private Location randomize;

	private Location offset;
	
	// Constructors
	// TODO: Expand!? Probably not necessary though...
	public SimpleDropEvent(Target targ, Action act) {
		super(targ, act);
	}
	
	public void setRandomLocMult(Location loc) {
		randomize = loc;		
	}
	
	public void setLocationOffset(Location loc) {
		offset = loc;
	}
	
	// Tool Damage
	public ToolDamage getToolDamage() {
		return toolDamage;
	}

	public void setToolDamage(ToolDamage val) {
		toolDamage = val;
	}
	
	// Quantity getters and setters
	public DoubleRange getQuantityRange() {
		return quantity;
	}
	
	public void setQuantity(double val) {
		quantity = new DoubleRange(val, val);
	}
	
	public void setQuantity(DoubleRange val) {
		quantity = val;
	}
	
	public void setQuantity(double low, double high) {
		quantity = new DoubleRange(low, high);
	}

	// The drop
	public void setDropped(DropType drop) {
		this.dropped = drop;
	}

	public DropType getDropped() {
		return dropped;
	}

	@Override
	public boolean isDefault() {
		return dropped == null;
	}
	
	@Override
	public String getDropName() {
		if(dropped == null) return "DEFAULT";
		else if(dropped instanceof ItemDrop && ((ItemDrop)dropped).getMaterial() == Material.AIR
			&& (replacementBlock == null || replacementBlock.getMaterial() == null)) return "DENY";
		return dropped.toString();
	}

	// The drop spread chance
	public void setDropSpread(double spread) {
		this.dropSpread = spread;
	}
	
	public void setDropSpread(boolean spread) {
		this.dropSpread = spread ? 100.0 : 0.0;
	}

	public double getDropSpreadChance() {
		return dropSpread;
	}

	public boolean getDropSpread() {
		if(dropSpread >= 100.0) return true;
		else if(dropSpread <= 0.0) return false;
		return rng.nextDouble() > dropSpread / 100.0;
	}

	// Attacker Damage
	public IntRange getAttackerDamageRange() {
		return attackerDamage;
	}

	public void setAttackerDamage(int val) {
		attackerDamage = new IntRange(val, val);
	}

	public void setAttackerDamage(IntRange val) {
		attackerDamage = val;
	}
	
	public void setAttackerDamage(int low, int high) {
		attackerDamage = new IntRange(low, high);
	}
	
	// Replacement
	public BlockTarget getReplacement() {
		return replacementBlock;
	}
	
	public void setReplacement(BlockTarget block) {
		replacementBlock = block;
	}

	// Events
	public void setEvents(List<SpecialResult> evt) {
		this.events = evt;
	}

	public List<SpecialResult> getEvents() {
		return events;
	}

	// Commands
	public void setCommands(List<String> cmd) {
		this.commands = cmd;
	}

	public List<String> getCommands() {
		return commands;
	}

	// Messages
	public void setMessages(List<String> msg) {
		this.messages = msg;
	}
	
	public List<String> getMessages() {
		return messages;
	}

	public String getRandomMessage(double amount) {
		if(messages == null || messages.isEmpty()) return null;
		String msg = messages.get(rng.nextInt(messages.size()));
		msg = msg.replace("%Q", "%q");
		if(dropped != null && dropped.isQuantityInteger())
			msg = msg.replace("%q", String.valueOf(Math.round(amount)));
		else msg = msg.replace("%q", Double.toString(amount));
		msg = msg.replace("%d", getDropName().toLowerCase());
		msg = msg.replace("%D", getDropName().toUpperCase());
		// TODO: this doesn't work - just returns "PLAYER" rather than the tool they used
		msg = msg.replace("%t", currentEvent.getTool().toString().toLowerCase());
		msg = msg.replace("%T", currentEvent.getTool().toString().toUpperCase());
		msg = msg.replaceAll("&([0-9a-fA-F])", "§$1"); // replace color codes
		msg = msg.replace("&&", "&"); // replace "escaped" ampersand
		return msg;
	}
	
	public String getMessagesString() {
		if(messages.size() == 0) return "(none)";
		else if(messages.size() == 1) return quoted(messages.get(0));
		List<String> msg = new ArrayList<String>();
		for(String message : messages) msg.add(quoted(message));
		return msg.toString();
	}

	private String quoted(String string) {
		if(!string.contains("\"")) return '"' + string + '"';
		else if(!string.contains("'")) return "'" + string + "'";
		return '"' + string.replace("\"", "\\\"") + '"';
	}

	// Effects
	public void setEffects(Set<SoundEffect> set) {
		this.effects = set;
	}

	public Set<SoundEffect> getEffects() {
		return effects;
	}
	
	public String getEffectsString() {
		if(effects == null) return null;
		if(effects.size() > 1) return effects.toString();
		if(effects.isEmpty()) return "(none)";
		List<Object> list = new ArrayList<Object>();
		list.addAll(effects);
		return list.get(0).toString();
	}

	private Location randomiseLocation(Location location, Location maxOffset) {
		double x = maxOffset.getX();
		double y = maxOffset.getY();
		double z = maxOffset.getZ();
		return location.add(
				OtherDrops.rng.nextDouble()*x*(OtherDrops.rng.nextInt() > 0.5 ? 1:-1),
				OtherDrops.rng.nextDouble()*y*(OtherDrops.rng.nextInt() > 0.5 ? 1:-1),
				OtherDrops.rng.nextDouble()*z*(OtherDrops.rng.nextInt() > 0.5 ? 1:-1)
		);
	}

	@Override
	public void run() {
		OtherDrops.logInfo("Performing SimpleDrop...",HIGHEST);
		ProfilerEntry entry = new ProfilerEntry("DROP");
		OtherDrops.profiler.startProfiling(entry);
		// We need a player for some things.
		Player who = null;
		if(currentEvent.getTool() instanceof PlayerSubject) who = ((PlayerSubject) currentEvent.getTool()).getPlayer();
		// We also need the location
		Location location = currentEvent.getLocation();
		// Then the actual drop
		// May have unexpected effects when use with delay.
		double amount = 1;
		if(dropped != null) { // null means "default"
			Target target = currentEvent.getTarget();
			boolean dropNaturally = true; // TODO: How to make this specifiable in the config?
			boolean spreadDrop = getDropSpread();
			amount = quantity.getRandomIn(rng);
			DropFlags flags = DropType.flags(who, dropNaturally, spreadDrop, rng);
			int droppedQuantity = dropped.drop(target, offset, amount, flags);
			OtherDrops.logInfo("SimpleDrop: dropped "+dropped.toString()+" x "+amount,HIGHEST);
			if(droppedQuantity < 0) { // If the embedded chance roll fails, bail out!
				// Profiling info
				OtherDrops.profiler.stopProfiling(entry);
				return;
			}
			// If the drop chance was 100% and no replacement block is specified, make it air
			double chance = max(getChance(), dropped.getChance());
			if(replacementBlock == null && chance >= 100.0 && target.overrideOn100Percent()) {
				replacementBlock = new BlockTarget(Material.AIR);
			}
			amount *= dropped.getAmount();
		} else {
			// DEFAULT event - set cancelled to false
			currentEvent.setCancelled(false); 
			// TODO: some way of setting it so that if we've set false here we don't set true on the same occureddrop?
			// this could save us from checking the DEFAULT drop outside the loop in OtherDrops.performDrop()
		}
		// Send a message, if any
		if(who != null) {
			if (dropped instanceof com.gmail.zariust.otherdrops.drop.MoneyDrop) {
				amount = dropped.total;
			}
			String msg = getRandomMessage(amount);
			if(msg != null) who.sendMessage(msg);
		}
		// Run commands, if any
		if(commands != null) {
			for(String command : commands) {
				boolean suppress = false;
				Boolean override = false;
				// Five possible prefixes (slash is optional in all of them)
				//   "/" - Run the command as the player, and send them any result messages
				//  "/!" - Run the command as the player, but send result messages to the console
				//  "/*" - Run the command as the player with op override, and send them any result messages
				// "/!*" - Run the command as the player with op override, but send result messages to the console
				//  "/$" - Run the command as the console, but send the player any result messages
				// "/!$" - Run the command as the console, but send result messages to the console
				if(who != null) command = command.replaceAll("%p", who.getName());
				if(command.startsWith("/")) command = command.substring(1);
				if(command.startsWith("!")) {
					command = command.substring(1);
					suppress = true;
				}
				if(command.startsWith("*")) {
					command = command.substring(1);
					override = true;
				} else if(command.startsWith("$")) {
					command = command.substring(1);
					override = null;
				}
				CommandSender from;
				if(who == null || override == null) from = Bukkit.getConsoleSender();
				else from = new PlayerWrapper(who, override, suppress);
				Bukkit.getServer().dispatchCommand(from, command);
			}
		}
		// Replacement block
		if(replacementBlock != null) {
			Target toReplace = currentEvent.getTarget();
			if(replacementBlock.getMaterial() == null) {
				replacementBlock = new BlockTarget(toReplace.getLocation().getBlock());
			}
			toReplace.setTo(replacementBlock);
			currentEvent.setCancelled(true);
		}
		
		// Effects after replacement block
		// TODO: I don't think effect should account for randomize/offset.
		if (effects != null) for(SoundEffect effect : effects) 
			effect.play(randomiseLocation(location, randomize));

		Agent used = currentEvent.getTool();
		if (used != null) {  // there's no tool for leaf decay
			// Tool damage
			if(toolDamage != null) {
				used.damageTool(toolDamage, rng);
			} else {
				used.damageTool(new ToolDamage(1), rng);				
			}

			// Attacker damage
			if(attackerDamage != null) {
				int damage = attackerDamage.getRandomIn(rng);
				used.damage(damage);
			}
		}
		try {
			Location oldLocation = currentEvent.getLocation();
			randomiseLocation(currentEvent.getLocation(), randomize);
			// And finally, events
			if (events != null) {
				for(SpecialResult evt : events) {
					if(evt.canRunFor(currentEvent)) evt.executeAt(currentEvent);
				}
			}
			currentEvent.setLocation(oldLocation);
		} catch (Exception ex) {
			OtherDrops.logWarning("Exception while running special event results: " + ex.getMessage(), NORMAL);
			if(OtherDrops.plugin.config.getVerbosity().exceeds(HIGH)) ex.printStackTrace();
		}
		// Profiling info
		OtherDrops.profiler.stopProfiling(entry);
	}

	@Override
	public String getLogMessage() {
		StringBuilder log = new StringBuilder();
		log.append(quantity);
		log.append("x " + dropped);
		if(replacementBlock != null) log.append(", leaving " + replacementBlock.getMaterial() + ",");
		return super.getLogMessage().replace("%d", log.toString());
	}

}
