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

package mclachlan.maze.stat.condition.impl;

import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.*;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionBearer;
import mclachlan.maze.stat.condition.ConditionEffect;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.ui.diygui.Constants;

/**
 * A custom condition impl for when an actor goes berserk.
 */
public class Berserk extends Condition
{
	private static ConditionEffect effect = new BerserkEffect();

	private static final StatModifier baseModifiers = new StatModifier();

	/*-------------------------------------------------------------------------*/
	static
	{
		baseModifiers.setModifier(Stats.Modifier.BRAWN, +3);
		baseModifiers.setModifier(Stats.Modifier.SKILL, -2);
		baseModifiers.setModifier(Stats.Modifier.BRAINS, -5);
		baseModifiers.setModifier(Stats.Modifier.SNEAKING, -10);
		baseModifiers.setModifier(Stats.Modifier.BONUS_ATTACKS, 1);
		baseModifiers.setModifier(Stats.Modifier.DAMAGE, +1);
		baseModifiers.setModifier(Stats.Modifier.DEFENCE, -4);
		baseModifiers.setModifier(Stats.Modifier.INITIATIVE, +2);
		baseModifiers.setModifier(Stats.Modifier.RESIST_BLUDGEONING, +15);
		baseModifiers.setModifier(Stats.Modifier.RESIST_SLASHING, +15);
		baseModifiers.setModifier(Stats.Modifier.RESIST_PIERCING, +15);
		baseModifiers.setModifier(Stats.Modifier.RESIST_MENTAL, +10);
		baseModifiers.setModifier(Stats.Modifier.STAMINA_REGEN, +20);
		baseModifiers.setModifier(Stats.Modifier.ACTION_POINT_REGEN, -20);
		baseModifiers.setModifier(Stats.Modifier.IMMUNE_TO_FEAR, 1);
		baseModifiers.setModifier(Stats.Modifier.IMMUNE_TO_IRRITATE, 1);
		baseModifiers.setModifier(Stats.Modifier.DAMAGE_MULTIPLIER, 1);
		baseModifiers.setModifier(Stats.Modifier.LIGHTNING_STRIKE_UNARMED, 1);
	}

	/*-------------------------------------------------------------------------*/
	public Berserk()
	{
		setDuration(Integer.MAX_VALUE);
		setStrength(Integer.MAX_VALUE);
		setIdentified(true);
		setStrengthIdentified(false);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getName()
	{
		return Constants.Conditions.BERSERK;
	}

	@Override
	public String getDisplayName()
	{
		return "Berserk";
	}

	@Override
	public String getIcon()
	{
		return "condition/berserk";
	}

	@Override
	public String getAdjective()
	{
		return "berserk";
	}

	@Override
	public int getModifier(Stats.Modifier modifier, ConditionBearer bearer)
	{
		UnifiedActor actor = (UnifiedActor)bearer;
		int result = baseModifiers.getModifier(modifier);

		// Blunting of Blades
		if (Stats.Modifier.VS_PENETRATE.equals(modifier) &&
			actor.getModifier(Stats.Modifier.BERSERK_POWERS) >= 2)
		{
			result += 25;
		}

		// Staying of Shafts
		if (Stats.Modifier.ARROW_CUTTING.equals(modifier) &&
			actor.getModifier(Stats.Modifier.BERSERK_POWERS) >= 3)
		{
			result += 25;
		}

		// Bewildering of Witches
		if ((Stats.Modifier.RESIST_EARTH.equals(modifier) ||
			Stats.Modifier.RESIST_WATER.equals(modifier) ||
			Stats.Modifier.RESIST_ENERGY.equals(modifier) ||
			Stats.Modifier.RESIST_MENTAL.equals(modifier))
			&&
			actor.getModifier(Stats.Modifier.BERSERK_POWERS) >= 4)
		{
			result += 25;
		}

		// Quenching of Flames
		if (Stats.Modifier.IMMUNE_TO_HEAT.equals(modifier) &&
			actor.getModifier(Stats.Modifier.BERSERK_POWERS) >= 5)
		{
			result += 1;
		}

		return result;
	}

	@Override
	public Map<Stats.Modifier, Integer> getModifiers()
	{
		return baseModifiers.getModifiers();
	}

	@Override
	public ConditionEffect getEffect()
	{
		return effect;
	}

	@Override
	public boolean isStrengthWanes()
	{
		return false;
	}

	@Override
	public void setCastingLevel(int castingLevel)
	{
		super.setCastingLevel(castingLevel);
	}

	@Override
	public MagicSys.SpellEffectType getType()
	{
		return MagicSys.SpellEffectType.NONE;
	}

	@Override
	public MagicSys.SpellEffectSubType getSubtype()
	{
		return MagicSys.SpellEffectSubType.NONE;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		// only end berserk once combat is over or the bearer passes out

		if (Maze.getInstance().getState() != Maze.State.COMBAT)
		{
			// todo: stick around a few turns?
			setDuration(-1);
			return null;
		}

		ConditionBearer target = getTarget();
		if (target instanceof UnifiedActor)
		{
			if (!GameSys.getInstance().isActorAware((UnifiedActor)target))
			{
				setDuration(-1);
				return null;
			}
		}

		setDuration(Integer.MAX_VALUE);
		return null;
	}

	@Override
	public boolean isAffliction()
	{
		return false;
	}

	@Override
	public boolean isIdentified()
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	static class BerserkEffect extends ConditionEffect
	{
		/*----------------------------------------------------------------------*/
		public ActorActionIntention checkIntention(
			UnifiedActor actor,
			Combat combat,
			ActorActionIntention intention,
			Condition condition)
		{
			//
			// Berserk characters always attack if possible
			// todo: with melee weapons, drop anything else.
			//

			List<ActorGroup> foeGroups = combat.getFoesOf(actor);
			ActorGroup actorGroup = foeGroups.get(0);
			return new AttackIntention(actorGroup, combat, actor.getAttackWithOptions().get(0));
		}
	}
}