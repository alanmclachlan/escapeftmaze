/*
 * Copyright (c) 2011 Alan McLachlan
 *
 * This file is part of Escape From The Maze.
 *
 * Escape From The Maze is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mclachlan.maze.stat.magic;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Log;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.ActorActionResolver;
import mclachlan.maze.stat.combat.AttackAction;
import mclachlan.maze.stat.combat.AttackType;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.event.FumblesEvent;
import mclachlan.maze.ui.diygui.animation.AnimationContext;

/**
 * A spell result that involves an attack with the character's equipped
 * weapon(s).
 */
public class AttackWithWeaponSpellResult extends SpellResult
{
	private Value nrStrikes;
	private StatModifier modifiers;
	private AttackType attackType;
	private MagicSys.SpellEffectType damageType;
	private String attackScript;
	private boolean requiresBackstabWeapon, requiresSnipeWeapon;
	private int requiredWeaponType;

	/*-------------------------------------------------------------------------*/

	public AttackWithWeaponSpellResult(
		Value nrStrikes,
		StatModifier modifiers,
		AttackType attackType,
		MagicSys.SpellEffectType damageType,
		String attackScript,
		boolean requiresBackstabWeapon,
		boolean requiresSnipeWeapon,
		int requiredWeaponType)
	{
		this.nrStrikes = nrStrikes;
		this.modifiers = modifiers;
		this.attackType = attackType;
		this.damageType = damageType;
		this.attackScript = attackScript;
		this.requiresBackstabWeapon = requiresBackstabWeapon;
		this.requiresSnipeWeapon = requiresSnipeWeapon;
		this.requiredWeaponType = requiredWeaponType;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> apply(
		UnifiedActor source,
		UnifiedActor target,
		int castingLevel,
		SpellEffect parent)
	{
		Combat combat = Maze.getInstance().getCurrentCombat();
		if (combat == null)
		{
			return null;
		}

		List<MazeEvent> result = new ArrayList<MazeEvent>();

		// decide the weapon to attack with
		AttackWith weapon = getAttackWith(source);

		// meets requirements? If not, it's a fumble.
		if (!meetsRequirements(source, weapon))
		{
			result.add(new FumblesEvent(source));
			return result;
		}

		// apply the damage type
		MagicSys.SpellEffectType actionDamageType = getDamageType(source, weapon);

		// apply the nr of strikes
		int actionNrStrikes = nrStrikes.compute(source);

		// select the attack script
		MazeScript actionAttackScript = getAttackScript(weapon);

		// create the attack action
		AttackAction action = getAttackAction(
			source,
			target,
			combat,
			weapon,
			actionDamageType,
			actionNrStrikes,
			actionAttackScript);

		// resolve the attack
		ActorActionResolver.attack(
			combat,
			source,
			target,
			action,
			result,
			new AnimationContext(source));

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private boolean meetsRequirements(UnifiedActor source, AttackWith weapon)
	{
		// backstab requirement?
		if (requiresBackstabWeapon && !weapon.isBackstabCapable() ||
			requiresSnipeWeapon && !weapon.isSnipeCapable())
		{
			Maze.log(Log.DEBUG, source.getName()+" - weapon is not backstab/snipe capable");
			return false;
		}

		// ammo requirement?
		Item secondaryWeapon = source.getSecondaryWeapon();
		if (weapon.isRanged() && weapon.getAmmoRequired() != null &&
			(secondaryWeapon == null || !weapon.getAmmoRequired().contains(secondaryWeapon.isAmmoType())))
		{
			Maze.log(Log.DEBUG, source.getName()+" - no ammo to snipe");
			return false;
		}

		// weapon type requirement?
		if (requiredWeaponType != ItemTemplate.WeaponSubType.NONE &&
			requiredWeaponType != weapon.getWeaponType())
		{
			Maze.log(Log.DEBUG, source.getName()+" - not the right weapon type");
			return false;
		}

		return true;
	}

	/*-------------------------------------------------------------------------*/
	private AttackAction getAttackAction(
		UnifiedActor source,
		UnifiedActor target,
		Combat combat,
		AttackWith weapon,
		MagicSys.SpellEffectType actionDamageType,
		int actionNrStrikes,
		MazeScript actionAttackScript)
	{
		AttackType actionAttackType = this.attackType;

		if (actionAttackType == null)
		{
			actionAttackType = GameSys.getInstance().getAttackType(weapon);
		}

		AttackAction action = new AttackAction(
			combat.getActorGroup(target),
			weapon,
			actionNrStrikes,
			actionAttackScript,
			true,
			false,
			actionAttackType,
			actionDamageType);
		action.setActor(source);

		// apply any modifiers to this attack;
		if (modifiers != null)
		{
			action.setModifiers(new StatModifier(modifiers));
		}
		return action;
	}

	/*-------------------------------------------------------------------------*/
	private MazeScript getAttackScript(AttackWith weapon)
	{
		MazeScript actionAttackScript;
		if (attackScript == null)
		{
			actionAttackScript = weapon.getAttackScript();
		}
		else
		{
			actionAttackScript = Database.getInstance().getScript(attackScript);
		}
		return actionAttackScript;
	}

	/*-------------------------------------------------------------------------*/
	private MagicSys.SpellEffectType getDamageType(UnifiedActor source, AttackWith weapon)
	{
		MagicSys.SpellEffectType actionDamageType = damageType;

		// if this spell result does not have a damage type set, use the default
		if (actionDamageType == null)
		{
			return GameSys.getInstance().getAttackWithDamageType(source, weapon);
		}
		return actionDamageType;
	}

	/*-------------------------------------------------------------------------*/
	private AttackWith getAttackWith(UnifiedActor source)
	{
		AttackWith weapon = source.getPrimaryWeapon();
		if (weapon == null)
		{
			weapon = GameSys.getInstance().getUnarmedWeapon(source, true);
		}
		return weapon;
	}

	/*-------------------------------------------------------------------------*/

	public Value getNrStrikes()
	{
		return nrStrikes;
	}

	public void setNrStrikes(Value nrStrikes)
	{
		this.nrStrikes = nrStrikes;
	}

	public StatModifier getModifiers()
	{
		return modifiers;
	}

	public void setModifiers(StatModifier modifiers)
	{
		this.modifiers = modifiers;
	}

	public MagicSys.SpellEffectType getDamageType()
	{
		return damageType;
	}

	public void setDamageType(MagicSys.SpellEffectType damageType)
	{
		this.damageType = damageType;
	}

	public String getAttackScript()
	{
		return attackScript;
	}

	public void setAttackScript(String attackScript)
	{
		this.attackScript = attackScript;
	}

	public boolean isRequiresBackstabWeapon()
	{
		return requiresBackstabWeapon;
	}

	public void setRequiresBackstabWeapon(boolean requiresBackstabWeapon)
	{
		this.requiresBackstabWeapon = requiresBackstabWeapon;
	}

	public boolean isRequiresSnipeWeapon()
	{
		return requiresSnipeWeapon;
	}

	public void setRequiresSnipeWeapon(boolean requiresSnipeWeapon)
	{
		this.requiresSnipeWeapon = requiresSnipeWeapon;
	}

	public AttackType getAttackType()
	{
		return attackType;
	}

	public void setAttackType(AttackType attackType)
	{
		this.attackType = attackType;
	}

	public int getRequiredWeaponType()
	{
		return requiredWeaponType;
	}

	public void setRequiredWeaponType(int requiredWeaponType)
	{
		this.requiredWeaponType = requiredWeaponType;
	}
}
