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

package mclachlan.maze.stat.combat.event;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.npc.Npc;

/**
 *
 */
public class NpcMindreadEvent extends MazeEvent
{
	Npc npc;
	int strength;

	/*-------------------------------------------------------------------------*/
	public NpcMindreadEvent(Npc npc, int strength)
	{
		this.npc = npc;
		this.strength = strength;
	}

	/*-------------------------------------------------------------------------*/
	public Npc getNpc()
	{
		return npc;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		return npc.getScript().mindRead(this.strength);
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return 0;
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return "";
	}
}
