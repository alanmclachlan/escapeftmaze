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

/**
 * Represents spell effects that are repeatedly applied to the bearer of
 * this condition.
 */
public class RepeatedSpellEffect
{
	/** turn this effect starts */
	private int startTurn;

	/** turn this effect ends */
	private int endTurn;

	/** effect occurs every n-th turn. e.g. turnMod=2 for every second turn */
	private int turnMod;

	/** probability of spell effect occurring */
	private int probability;

	/** the spell effect to be applied to the bearer of this condition */
	private String spellEffect;

	/*-------------------------------------------------------------------------*/
	public RepeatedSpellEffect(
		int startTurn, int endTurn, int turnMod, int probability,
		String spellEffect)
	{
		this.startTurn = startTurn;
		this.endTurn = endTurn;
		this.turnMod = turnMod;
		this.probability = probability;
		this.spellEffect = spellEffect;
	}

	/*-------------------------------------------------------------------------*/
	public int getStartTurn()
	{
		return startTurn;
	}

	public void setStartTurn(int startTurn)
	{
		this.startTurn = startTurn;
	}

	public int getEndTurn()
	{
		return endTurn;
	}

	public void setEndTurn(int endTurn)
	{
		this.endTurn = endTurn;
	}

	public int getTurnMod()
	{
		return turnMod;
	}

	public void setTurnMod(int turnMod)
	{
		this.turnMod = turnMod;
	}

	public String getSpellEffect()
	{
		return spellEffect;
	}

	public void setSpellEffect(String spellEffect)
	{
		this.spellEffect = spellEffect;
	}

	public int getProbability()
	{
		return probability;
	}

	public void setProbability(int probability)
	{
		this.probability = probability;
	}
}
