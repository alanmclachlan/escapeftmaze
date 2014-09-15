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

package mclachlan.maze.game.event;

import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.PlayerParty;
import mclachlan.maze.stat.UnifiedActor;

/**
 *
 */
public class RemoveItemEvent extends MazeEvent
{
	String item;

	public RemoveItemEvent(String item)
	{
		this.item = item;
	}

	public List<MazeEvent> resolve()
	{
		// removes all items with the given name from all characters, lost forever
		PlayerParty party = Maze.getInstance().getParty();

		for (UnifiedActor actor : party.getActors())
		{
			actor.removeItem(item, true);
		}

		return null;
	}
}