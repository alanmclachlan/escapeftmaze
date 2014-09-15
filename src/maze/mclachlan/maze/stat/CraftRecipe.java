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
public class CraftRecipe
{
	private String name;
	private StatModifier requirements;
	private String item1;
	private String item2;
	private String resultingItem;

	/*-------------------------------------------------------------------------*/
	public CraftRecipe(
		String name, StatModifier requirements,
		String item1,
		String item2,
		String resultingItem)
	{
		this.name = name;
		this.requirements = requirements;
		this.item1 = item1;
		this.item2 = item2;
		this.resultingItem = resultingItem;
	}

	/*-------------------------------------------------------------------------*/

	public String getName()
	{
		return name;
	}

	public StatModifier getRequirements()
	{
		return requirements;
	}

	public String getItem1()
	{
		return item1;
	}

	public String getItem2()
	{
		return item2;
	}

	public String getResultingItem()
	{
		return resultingItem;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setRequirements(StatModifier requirements)
	{
		this.requirements = requirements;
	}

	public void setItem1(String item1)
	{
		this.item1 = item1;
	}

	public void setItem2(String item2)
	{
		this.item2 = item2;
	}

	public void setResultingItem(String resultingItem)
	{
		this.resultingItem = resultingItem;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isMatch(String name1, String name2)
	{
		return (name1.equals(item1) && name2.equals(item2)) ||
			(name1.equals(item2) && name2.equals(item1));
	}
}