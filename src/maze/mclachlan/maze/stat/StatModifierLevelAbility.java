/*
 * Copyright (c) 2014 Alan McLachlan
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

package mclachlan.maze.stat;

import java.util.*;
import mclachlan.maze.data.StringUtil;

/**
 * A level ability that grants some modifiers to the character
 */
public class StatModifierLevelAbility extends LevelAbility
{
	private final StatModifier modifier;

	/*-------------------------------------------------------------------------*/
	public StatModifierLevelAbility(String key, String displayName,
		String description, StatModifier modifier)
	{
		super(key, displayName, description);
		this.modifier = modifier;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public StatModifier getModifier()
	{
		return modifier;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Object[] getDisplayArgs()
	{
		List result = new ArrayList();

		for (String s : modifier.getModifiers().keySet())
		{
			result.add(StringUtil.descModifier(s, modifier.getModifier(s)));
		}

		return result.toArray();
	}
}
