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
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.DrainEvent;

/**
 *
 */
public class DrainSpellResult extends SpellResult
{
	private Value drain;
	private String modifier;

	/*-------------------------------------------------------------------------*/	
	public DrainSpellResult(Value drain, String modifier)
	{
		this.drain = drain;
		this.modifier = modifier;
	}
	
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(UnifiedActor source, UnifiedActor target, int castingLevel, SpellEffect parent)
	{
		int damage = this.drain.compute(source, castingLevel);
		
		return getList(
			new DrainEvent(
				target,
				source,
				damage,
				parent.getType(),
				modifier));
	}

	/*-------------------------------------------------------------------------*/
	public Value getDrain()
	{
		return drain;
	}
	
	/*-------------------------------------------------------------------------*/
	public String getModifier()
	{
		return modifier;
	}
}
