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

package mclachlan.maze.stat;

import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.combat.event.AttackEvent;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.SpellEffect;
import java.util.List;

/**
 * 
 */
public interface AttackWith
{
	String getDisplayName();

	int getToHit();
	int getToPenetrate();
	int getToCritical();
	Dice getDamage();
	MagicSys.SpellEffectType getDefaultDamageType();

	String describe(AttackEvent e);

	String[] getAttackTypes();

	int getMaxRange();
	int getMinRange();
	boolean isRanged();
	boolean isBackstabCapable();
	boolean isSnipeCapable();

	GroupOfPossibilities<SpellEffect> getSpellEffects();
	int getSpellEffectLevel();

	String slaysFoeType();

	MazeScript getAttackScript();

	ItemTemplate.AmmoType isAmmoType();
	List<ItemTemplate.AmmoType> getAmmoRequired();

	int getToInitiative();
}
