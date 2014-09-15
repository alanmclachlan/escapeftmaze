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

import mclachlan.maze.game.MazeEvent;


/**
 *
 */
public class FlavourTextEvent extends MazeEvent
{
	String text;
	int delay;
	boolean shouldClearText;

	/*-------------------------------------------------------------------------*/
	public FlavourTextEvent(String text)
	{
		this(text, Delay.WAIT_ON_CLICK, false);
	}

	/*-------------------------------------------------------------------------*/
	public FlavourTextEvent(String text, int delay, boolean shouldClearText)
	{
		this.text = text;
		this.delay = delay;
		this.shouldClearText = shouldClearText;
	}
	
	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return text;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return delay;
	}

	/*-------------------------------------------------------------------------*/
	public boolean shouldClearText()
	{
		return shouldClearText;
	}
}
