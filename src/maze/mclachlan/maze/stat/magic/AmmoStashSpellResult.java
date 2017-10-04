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

package mclachlan.maze.stat.magic;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.ItemTemplate;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.NoEffectEvent;
import mclachlan.maze.util.MazeException;

/**
 * Create ammo for the currently equipped ranged weapon
 */
public class AmmoStashSpellResult extends SpellResult
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(
		UnifiedActor source,
		UnifiedActor target,
		int castingLevel,
		SpellEffect parent)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();
		List<Item> dropped = new ArrayList<Item>();

		Item primaryWeapon = source.getPrimaryWeapon();

		if (primaryWeapon != null && primaryWeapon.getType() == ItemTemplate.Type.RANGED_WEAPON)
		{
			List<ItemTemplate.AmmoType> ammoRequired = primaryWeapon.getAmmoRequired();

			if (ammoRequired != null &&
				!ammoRequired.isEmpty())
			{
				ItemTemplate.AmmoType ammoType = ammoRequired.get(Dice.nextInt(ammoRequired.size()));

				ItemTemplate ammoTemplate = null;

				switch (ammoType)
				{
					case ARROW: ammoTemplate = Database.getInstance().getItemTemplate("Willow Arrow");
						break;
					case BOLT: ammoTemplate = Database.getInstance().getItemTemplate("Quarrel");
						break;
					case STONE: ammoTemplate = Database.getInstance().getItemTemplate("Bullet Stone");
						break;
					case SELF: // can't do anything here
						break;
					case SHOT: ammoTemplate = Database.getInstance().getItemTemplate("Musket Ball");
						break;
					case STAR: ammoTemplate = Database.getInstance().getItemTemplate("Shuriken");
						break;
					case DART: ammoTemplate = Database.getInstance().getItemTemplate("Feather Dart");
						break;
					case JAVELIN: ammoTemplate = Database.getInstance().getItemTemplate("Javelin");
						break;
					case HAMMER: ammoTemplate = Database.getInstance().getItemTemplate("Throwing Hammer");
						break;
					case AXE: ammoTemplate = Database.getInstance().getItemTemplate("Throwing Axe");
						break;
					case KNIFE: ammoTemplate = Database.getInstance().getItemTemplate("Throwing Knife");
						break;
					default:
						throw new MazeException(""+ammoType);
				}

				if (ammoTemplate != null)
				{
					int quantity = getQuantity(castingLevel);

					Item item = ammoTemplate.create(quantity);
					item.setIdentificationState(Item.IdentificationState.IDENTIFIED);
					item.setCursedState(Item.CursedState.DISCOVERED);

					if (!target.addItemSmartly(item))
					{
						dropped.add(item);
					}

					result.add(new FlavourTextEvent(
						StringUtil.getEventText("msg.ammo.stash",
							source.getDisplayName(),
							""+item.getStack().getCurrent(),
							item.getDisplayName())));
				}
			}
		}

		Maze.getInstance().dropItemsOnCurrentTile(dropped);

		// failed for some reason
		if (result.isEmpty())
		{
			result.add(new NoEffectEvent());
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	protected int getQuantity(int castingLevel)
	{
		int quantity = new Dice(castingLevel, 2, -5).roll();
		quantity = Math.max(2, quantity);
		return quantity;
	}
}
