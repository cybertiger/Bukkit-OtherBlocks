package com.gmail.zariust.otherdrops.subject;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.zariust.otherdrops.event.AbstractDropEvent;

public class PlayerSubject extends LivingSubject {
	private ToolAgent tool;
	private String name;
	private Player agent;
	
	public PlayerSubject() {
		this((String) null);
	}

	public PlayerSubject(String attacker) {
		this(null, attacker);
	}
	
	public PlayerSubject(Player attacker) {
		this(attacker.getItemInHand(), attacker.getName(), attacker);
	}
	
	public PlayerSubject(ItemStack item, String attacker) {
		this(item, attacker, null);
	}

	public PlayerSubject(ItemStack item, String who, Player attacker) {
		super(attacker);
		tool = new ToolAgent(item);
		name = who;
		agent = attacker;
	}

	private PlayerSubject equalsHelper(Object other) {
		if(!(other instanceof PlayerSubject)) return null;
		return (PlayerSubject) other;
	}

	private boolean isEqual(PlayerSubject player) {
		if(player == null) return false;
		return tool.equals(player.tool) && name.equals(player.name);
	}

	@Override
	public boolean equals(Object other) {
		PlayerSubject player = equalsHelper(other);
		return isEqual(player);
	}

	@Override
	public boolean matches(Subject other) {
		PlayerSubject player = equalsHelper(other);
		if(name == null) return true;
		else return isEqual(player);
	}

	@Override
	public int hashCode() {
		return AbstractDropEvent.hashCode(ItemCategory.PLAYER, name.hashCode(), tool.hashCode());
	}
	
	public Material getMaterial() {
		return tool.getMaterial();
	}
	
	public Player getPlayer() {
		if(agent == null) agent = Bukkit.getServer().getPlayer(name);
		return agent;
	}
	
	@Override
	public void damageTool(short damage) {
		if(damage == 0) return;
		ItemStack stack = agent.getItemInHand();
		if(stack == null || stack.getAmount() == 1) {
			agent.setItemInHand(null);
			return;
		}
		// Either it's a tool and we damage it, or it's not and we take one
		short maxDurability = stack.getType().getMaxDurability();
		if(maxDurability > 0) { // a tool
			short durability = stack.getDurability();
			if(durability + damage >= maxDurability) agent.setItemInHand(null);
			else stack.setDurability((short) (durability + damage));
		} else { // not a tool
			int amount = stack.getAmount();
			if(amount <= damage) agent.setItemInHand(null);
			else stack.setAmount(amount - damage);
		}
		// TODO: Option of failure if damage is greater that the amount remaining?
	}
	
	@Override
	public void damageTool() { // Default tool damage; 1 for tools, 0 for normal items
		if(agent.getItemInHand().getType().getMaxDurability() > 0) damageTool((short) 1);
	}
	
	@Override
	public void damage(int amount) {
		agent.damage(amount);
	}

	public ToolAgent getTool() {
		return tool;
	}

	public int getData() {
		return tool.getData();
	}

	@Override
	public ItemCategory getType() {
		return ItemCategory.PLAYER;
	}

	@Override
	public boolean overrideOn100Percent() {
		return false;
	}

	@Override
	public Location getLocation() {
		if(agent != null) return agent.getLocation();
		return null;
	}

	@Override
	public List<Target> canMatch() {
		return Collections.singletonList((Target) this);
	}

	@Override
	public String getKey() {
		return "PLAYER";
	}

	@Override
	public String toString() {
		if(name == null) {
			if(tool == null) return "PLAYER";
			return tool.toString();
		}
		return "PLAYER@" + name + " with "+tool.toString(); // TODO: does adding the tool here break anything?
	}
}