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

package mclachlan.maze.map.script;

import java.util.List;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.EncounterTable;
import mclachlan.maze.map.FoeEntry;
import mclachlan.maze.stat.FoeGroup;
import mclachlan.maze.data.Database;

/**
 *
 */
public class EncounterEvent extends MazeEvent
{
	private String mazeVariable;
	private String encounterTable;

	/*-------------------------------------------------------------------------*/
	public EncounterEvent(
		String mazeVariable,
		String encounterTable)
	{
		this.mazeVariable = mazeVariable;
		this.encounterTable = encounterTable;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		if (this.mazeVariable != null)
		{
			if (MazeVariables.getBoolean(this.mazeVariable))
			{
				return null;
			}
		}

		EncounterTable table = Database.getInstance().getEncounterTable(encounterTable);
		FoeEntry foeEntry = table.getEncounterTable().getRandomItem();
		List<FoeGroup> allFoes = foeEntry.generate();

		if (Maze.getInstance().encounter(allFoes, mazeVariable))
		{
			if (mazeVariable != null)
			{
				MazeVariables.set(this.mazeVariable, "true");
			}
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/
	public String getEncounterTable()
	{
		return encounterTable;
	}

	/*-------------------------------------------------------------------------*/
	public String getMazeVariable()
	{
		return mazeVariable;
	}
}
