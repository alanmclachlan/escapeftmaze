/*
 * Copyright (c) 2012 Alan McLachlan
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
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.condition.ConditionTemplate;

/**
 *
 */
public class UpdateConditionTemplates
{
	public static void main(String[] args) throws Exception
	{
		System.out.println("...");

		V1Loader loader = new V1Loader();
		V1Saver saver = new V1Saver();
		Database db = new Database(loader, saver, Maze.getStubCampaign());

		int count = 0;

		Map<String,ConditionTemplate> list = db.getConditionTemplates();

		for (String s : list.keySet())
		{
			ConditionTemplate ct = list.get(s);
			StatModifier mods = ct.getStatModifier();

			//todo: banner modifiers

//			if (mods != null)
//			{
//				int damRes = mods.getModifier("reserved14");
//				if (damRes > 0)
//				{
//					System.out.println(ct.getName()+": "+damRes);
//
//					mods.setModifier("reserved14", 0);
//					mods.setModifier(Stats.Modifiers.RESIST_BLUDGEONING, damRes);
//					mods.setModifier(Stats.Modifiers.RESIST_PIERCING, damRes);
//					mods.setModifier(Stats.Modifiers.RESIST_SLASHING, damRes);
//
//					ct.setStatModifier(mods);
//				}
//			}
		}
		
		saver.saveConditionTemplates(list);
		System.out.println("count = [" + count + "]");
	}
}
