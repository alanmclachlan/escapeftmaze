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

import mclachlan.crusader.EngineObject;
import java.awt.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.Trap;
import mclachlan.maze.stat.PercentageTable;

/**
 * Initiates player interaction with a chest.
 */
public class Chest extends TileScript
{
	private TileScript chestContents;
	private PercentageTable<Trap> traps;
	private String mazeVariable;
	private String northTexture, southTexture, eastTexture, westTexture;
	private MazeScript preScript;
	
	private EngineObject engineObject;
	private Trap currentTrap;

	/*-------------------------------------------------------------------------*/
	/**
	 * @param chestContents
	 * 	What happens when this chest is opened
	 * @param traps
	 * @param mazeVariable
* 	The maze variable storing the state of this chest
	 * @param preScript
	 */
	public Chest(
		TileScript chestContents,
		PercentageTable<Trap> traps,
		String mazeVariable,
		String northTexture,
		String southTexture,
		String eastTexture,
		String westTexture, 
		MazeScript preScript)
	{
		this.preScript = preScript;
		this.chestContents = chestContents;
		this.traps = traps;
		this.mazeVariable = mazeVariable;
		this.northTexture = northTexture;
		this.southTexture = southTexture;
		this.eastTexture = eastTexture;
		this.westTexture = westTexture;
		
		this.engineObject = new EngineObject(
			null,
			Database.getInstance().getMazeTexture(northTexture).getTexture(), 
			Database.getInstance().getMazeTexture(southTexture).getTexture(), 
			Database.getInstance().getMazeTexture(eastTexture).getTexture(), 
			Database.getInstance().getMazeTexture(westTexture).getTexture(),
			0,
			false,
			null,
			null);
	}
	
	/*-------------------------------------------------------------------------*/
	public void initialise(Maze maze, Point tile, int tileIndex)
	{
		if (!State.EMPTY.equalsIgnoreCase(MazeVariables.get(this.mazeVariable)))
		{
			engineObject.setTileIndex(tileIndex);
			maze.addObject(engineObject);
		}
	}

	/*-------------------------------------------------------------------------*/
	public java.util.List<MazeEvent> handlePlayerAction(
		Maze maze, Point tile, int facing, int playerAction)
	{
		if (playerAction != PlayerAction.LOCKS)
		{
			return null;
		}

		if (State.EMPTY.equalsIgnoreCase(MazeVariables.get(this.mazeVariable)))
		{
			return null;
		}

		maze.encounterChest(this);
		return null;
	}
	
	/*-------------------------------------------------------------------------*/
	public java.util.List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		if (preScript != null &&
			!State.EMPTY.equalsIgnoreCase(MazeVariables.get(this.mazeVariable)))
		{
			return preScript.getEvents();
		}
		else
		{
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public TileScript getChestContents()
	{
		return chestContents;
	}

	public String getMazeVariable()
	{
		return mazeVariable;
	}

	public PercentageTable<Trap> getTraps()
	{
		return traps;
	}

	public Trap getCurrentTrap()
	{
		return currentTrap;
	}

	public void refreshCurrentTrap()
	{
		this.currentTrap = traps.getRandomItem();
	}

	public EngineObject getEngineObject()
	{
		return engineObject;
	}

	public String getEastTexture()
	{
		return eastTexture;
	}

	public String getNorthTexture()
	{
		return northTexture;
	}

	public String getSouthTexture()
	{
		return southTexture;
	}

	public String getWestTexture()
	{
		return westTexture;
	}

	public MazeScript getPreScript()
	{
		return preScript;
	}

	public void setPreScript(MazeScript preScript)
	{
		this.preScript = preScript;
	}
	
	/*-------------------------------------------------------------------------*/
	public void setState(String chestState)
	{
		MazeVariables.set(this.mazeVariable, chestState);
	}

	/*-------------------------------------------------------------------------*/
	public static class State
	{
		public static final String UNTOUCHED = "untouched";
		public static final String EMPTY = "empty";
	}
}
