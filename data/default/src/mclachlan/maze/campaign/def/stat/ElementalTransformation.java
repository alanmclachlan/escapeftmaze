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

package mclachlan.maze.campaign.def.stat;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.Value;
import mclachlan.maze.stat.magic.ValueList;

/**
 *
 */
public class ElementalTransformation extends LevelAbility
{
	private final StatModifier statModifier;
	private final SpellLikeAbility ally;
	private List<TypeDescriptor> types;

	// todo: gains Elemental creature type?

	/*-------------------------------------------------------------------------*/
	public ElementalTransformation()
	{
		statModifier = new StatModifier();
		statModifier.setModifier(Stats.Modifier.RESIST_FIRE, 100);
		statModifier.setModifier(Stats.Modifier.RESIST_WATER, 100);
		statModifier.setModifier(Stats.Modifier.RESIST_EARTH, 100);
		statModifier.setModifier(Stats.Modifier.RESIST_AIR, 100);
		statModifier.setModifier(Stats.Modifier.IMMUNE_TO_BLIND, 100);
		statModifier.setModifier(Stats.Modifier.IMMUNE_TO_DISEASE, 100);
		statModifier.setModifier(Stats.Modifier.IMMUNE_TO_STONE, 100);
		statModifier.setModifier(Stats.Modifier.IMMUNE_TO_LIGHTNING, 100);
		statModifier.setModifier(Stats.Modifier.BLUE_MAGIC_GEN, 1);

		Spell spell = Database.getInstance().getSpell("Elemental Ally");
		Value value = new Value(1, Value.SCALE.SCALE_WITH_CLASS_LEVEL);
		value.setReference("Elemental");
		ValueList castingLevel = new ValueList(value);
		ally = new SpellLikeAbility(spell, castingLevel);
	}

	/*-------------------------------------------------------------------------*/
	private void initTypes()
	{
		FoeType elemental = Database.getInstance().getFoeTypes().get("Elemental");
		types = new ArrayList<TypeDescriptor>();
		types.add(elemental);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public SpellLikeAbility getAbility()
	{
		return ally;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public StatModifier getModifier()
	{
		return statModifier;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Collection<TypeDescriptor> getTypeDescriptors()
	{
		if (types == null)
		{
			initTypes();
		}
		return types;
	}
}
