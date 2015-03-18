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

package mclachlan.maze.stat.condition;

import java.util.*;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.GameCache;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ConditionManager implements GameCache
{
	/**
	 * Map of conditions by condition bearer
	 */
	private Map<ConditionBearer, List<Condition>> conditions = new HashMap<ConditionBearer, List<Condition>>();
	private static ConditionManager instance = new ConditionManager();
	private final Object mutex = new Object();
	private static List<Condition> emptyList = Collections.unmodifiableList(new ArrayList<Condition>());

	/*-------------------------------------------------------------------------*/
	public static ConditionManager getInstance()
	{
		return instance;
	}

	/*-------------------------------------------------------------------------*/
	public void loadGame(String name, Loader loader) throws Exception
	{
		this.conditions = loader.loadConditions(name);
	}

	/*-------------------------------------------------------------------------*/
	public void saveGame(String saveGameName, Saver saver) throws Exception
	{
		saver.saveConditions(saveGameName, conditions);
	}

	/*-------------------------------------------------------------------------*/
	public void endOfTurn(long turnNr)
	{
		updateConditions(turnNr);
	}

	/*-------------------------------------------------------------------------*/
	private void updateConditions(long turnNr)
	{
		Maze.log("updating conditions...");
		synchronized (mutex)
		{
			ArrayList<MazeEvent> conditionEvents = new ArrayList<MazeEvent>();
			for (ConditionBearer bearer : conditions.keySet())
			{
				ListIterator<Condition> li = conditions.get(bearer).listIterator();
				while (li.hasNext())
				{
					Condition c = li.next();
					Maze.log("condition "+c.getName()+" on "+c.getTarget().getName());

					// Any end of turn condition effects:
					List<MazeEvent> list = c.endOfTurn(turnNr);
					if (list != null)
					{
						conditionEvents.addAll(list);
					}

					// Expire conditions
					if (c.getDuration() < 0)
					{
						Maze.log("condition expired");
						li.remove();
						c.expire();
					}
					else
					{
						Maze.log("duration "+c.getDuration());
						Maze.log("strength "+c.getStrength());
					}
				}
			}
			Maze.getInstance().resolveEvents(conditionEvents, false);
		}

		Maze.log("finished updating conditions");
	}

	/*-------------------------------------------------------------------------*/
	public void addCondition(ConditionBearer bearer, Condition c)
	{
		c.setTarget(bearer);
		synchronized (mutex)
		{
			List<Condition> list = conditions.get(bearer);
			if (list == null)
			{
				list = new ArrayList<Condition>();
				conditions.put(bearer, list);
			}
			list.add(c);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void removeCondition(ConditionBearer bearer, Condition c)
	{
		synchronized (mutex)
		{
			List<Condition> list = conditions.get(bearer);
			if (list == null)
			{
				// todo: rather do nothing?
				throw new MazeException("invalid attempt to remove condition");
			}
			list.remove(c);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<Condition> getConditions(ConditionBearer bearer)
	{
		synchronized(mutex)
		{
			if (conditions.containsKey(bearer))
			{
				return conditions.get(bearer);
			}
			else
			{
				return emptyList;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Removes all conditions from the given bearer
	 */
	public void removeConditions(ConditionBearer bearer)
	{
		synchronized(mutex)
		{
			conditions.remove(bearer);
		}
	}
}
