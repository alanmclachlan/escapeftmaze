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

package mclachlan.maze.util;

import java.util.*;
import mclachlan.maze.balance.MockCombat;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.ILootEntry;
import mclachlan.maze.map.LootTable;
import mclachlan.maze.map.SingleItemLootEntry;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.stat.FoeTemplate;
import mclachlan.maze.stat.Item;

/**
 *
 */
public class VerifyFoeInventories
{
	private static V1Saver saver;

	public static void main(String[] args) throws Exception
	{
		System.out.println("...");

		V1Loader loader = new V1Loader();
		saver = new V1Saver();
		Database db = new Database(loader, saver);
		Campaign campaign = Maze.getStubCampaign();
		loader.init(campaign);
		saver.init(campaign);

		Maze mockMaze = MockCombat.getMockMaze(loader);

		Map<String,FoeTemplate> foeTemplates = db.getFoeTemplates();

		for (FoeTemplate ft : foeTemplates.values())
		{
			Foe foe = new Foe(ft);

			LootTable lt = ft.getLoot();

			// only consider single item items. Items from random loot entries
			// need not be usable

			for (ILootEntry le : lt.getLootEntries().getPossibilities())
			{
				if (le instanceof SingleItemLootEntry)
				{
					Item item = le.generate();

					if (!foe.meetsRequirements(item.getEquipRequirements()) ||
						!foe.meetsRequirements(item.getUseRequirements()))
					{
						System.out.println(foe.getName()+": "+item.getName());
					}
				}
			}
		}

	}
}