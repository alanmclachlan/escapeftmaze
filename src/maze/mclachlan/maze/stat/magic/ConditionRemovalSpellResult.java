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
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.Tile;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.CuringEvent;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionBearer;
import mclachlan.maze.stat.condition.ConditionEffect;

/**
 * A spell for curing a variety of ailments.
 */
public class ConditionRemovalSpellResult extends SpellResult
{
	private List<ConditionEffect> effects;
	private Value strength;
	
	/*-------------------------------------------------------------------------*/
	public ConditionRemovalSpellResult(List<ConditionEffect> effects, Value strength)
	{
		this.strength = strength;
		this.effects = effects;
	}
	
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(UnifiedActor source, UnifiedActor target, int castingLevel, SpellEffect parent)
	{
		return removeConditions(target, source, castingLevel);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(UnifiedActor source, Tile tile, int castingLevel, SpellEffect parent)
	{
		return removeConditions(tile, source, castingLevel);
	}

	/*-------------------------------------------------------------------------*/
	private List<MazeEvent> removeConditions(ConditionBearer target, UnifiedActor source, int castingLevel)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		List<Condition> list = target.getConditions();
		if (list != null)
		{
			List<Condition> conditions = new ArrayList<Condition>(list);
			for (Condition c : conditions)
			{
				if (c.isIdentified() && this.effects.contains(c.getEffect()))
				{
					// this condition is a candidate for removal
					int curing = strength.compute(source, castingLevel);
					result.add(new CuringEvent(target, c, curing));
				}
			}
		}

		return result;
	}
	
	/*-------------------------------------------------------------------------*/

	public List<ConditionEffect> getEffects()
	{
		return effects;
	}

	public void setEffects(List<ConditionEffect> effects)
	{
		this.effects = effects;
	}

	public Value getStrength()
	{
		return strength;
	}

	public void setStrength(Value strength)
	{
		this.strength = strength;
	}
}
