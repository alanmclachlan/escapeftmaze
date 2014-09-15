
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

package mclachlan.maze.stat;

/**
 *
 */
public class ItemEnchantment
{
	private String name;
	private StatModifier modifiers;
	private String prefix, suffix;
	private int costModifier;

	/*-------------------------------------------------------------------------*/
	public ItemEnchantment(
		String name,
		String prefix,
		String suffix,
		StatModifier modifiers,
		int costModifier)
	{
		this.modifiers = modifiers;
		this.name = name;
		this.prefix = prefix;
		this.suffix = suffix;
		this.costModifier = costModifier;
	}

	/*-------------------------------------------------------------------------*/
	public StatModifier getModifiers()
	{
		return modifiers;
	}

	public void setModifiers(StatModifier modifiers)
	{
		this.modifiers = modifiers;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getSuffix()
	{
		return suffix;
	}

	public void setSuffix(String suffix)
	{
		this.suffix = suffix;
	}

	public String getPrefix()
	{
		return prefix;
	}

	public void setPrefix(String prefix)
	{
		this.prefix = prefix;
	}

	public int getCostModifier()
	{
		return costModifier;
	}

	public void setCostModifier(int costModifier)
	{
		this.costModifier = costModifier;
	}
}
