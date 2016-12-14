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
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.ILootEntry;
import mclachlan.maze.map.LootTable;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.UnifiedActor;

/**
 * Creates an item in the targets possession
 */
public class CreateItemSpellResult extends SpellResult
{
	/**
	 * Loot table from which to calculate the item
	 */
	private String lootTable;

	/*-------------------------------------------------------------------------*/
	public CreateItemSpellResult(String lootTable)
	{
		this.lootTable = lootTable;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(
		UnifiedActor source,
		UnifiedActor target,
		int castingLevel,
		SpellEffect parent)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		LootTable table = Database.getInstance().getLootTable(lootTable);

		List<ILootEntry> items = table.getLootEntries().getRandom();
		List<Item> dropped = new ArrayList<Item>();
		for (ILootEntry lootEntry : items)
		{
			Item item = lootEntry.generate();
			item.setIdentificationState(Item.IdentificationState.IDENTIFIED);
			item.setCursedState(Item.CursedState.DISCOVERED);

			if (!target.addItemSmartly(item))
			{
				dropped.add(item);
			}
		}

		Maze.getInstance().dropItemsOnCurrentTile(dropped);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public String getLootTable()
	{
		return lootTable;
	}
}
