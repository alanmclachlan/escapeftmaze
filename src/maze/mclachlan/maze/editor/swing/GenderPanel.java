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

package mclachlan.maze.editor.swing;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.Gender;
import mclachlan.maze.stat.StatModifier;

/**
 *
 */
public class GenderPanel extends EditorPanel
{
	StatModifierComponent startingModifiers;
	StatModifierComponent constantModifiers;
	StatModifierComponent bannerModifiers;

	/*-------------------------------------------------------------------------*/
	public GenderPanel()
	{
		super(SwingEditor.Tab.GENDER);
	}

	/*-------------------------------------------------------------------------*/
	protected JPanel getEditControls()
	{
		startingModifiers = new StatModifierComponent(SwingEditor.Tab.GENDER);
		constantModifiers = new StatModifierComponent(SwingEditor.Tab.GENDER);
		bannerModifiers = new StatModifierComponent(SwingEditor.Tab.GENDER);

		JPanel editControls = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5,5,5,5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx++;
		editControls.add(new JLabel("Starting Modifiers:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(startingModifiers, gbc);

		gbc.gridy++;
		gbc.gridx=1;
		gbc.weightx = 0.0;
		editControls.add(new JLabel("Constant Modifiers:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(constantModifiers, gbc);

		gbc.gridy++;
		gbc.gridx=1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		editControls.add(new JLabel("Banner Modifiers:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(bannerModifiers, gbc);
		return editControls;
	}

	/*-------------------------------------------------------------------------*/
	public Vector loadData()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getGenderList());
		Collections.sort(vec);
		return vec;
	}

	/*-------------------------------------------------------------------------*/
	public void newItem(String name)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.GENDER);
		Gender g = new Gender(name, new StatModifier(), new StatModifier(), new StatModifier());
		Database.getInstance().getGenders().put(name, g);
		refreshNames(name);
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.GENDER);
		Gender current = Database.getInstance().getGender((String)names.getSelectedValue());
		Database.getInstance().getGenders().remove(current.getName());
		current.setName(newName);
		Database.getInstance().getGenders().put(current.getName(), current);
		refreshNames(newName);
	}

	/*-------------------------------------------------------------------------*/
	public void copyItem(String newName)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.GENDER);

		Gender current = Database.getInstance().getGender((String)names.getSelectedValue());

		Gender g = new Gender(
			newName,
			new StatModifier(current.getStartingModifiers()),
			new StatModifier(current.getConstantModifiers()),
			new StatModifier(current.getBannerModifiers()));
		Database.getInstance().getGenders().put(newName, g);
		refreshNames(newName);
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.GENDER);
		String name = (String)names.getSelectedValue();
		Database.getInstance().getGenders().remove(name);
		refreshNames(null);
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		Gender gender = Database.getInstance().getGender(name);
		gender.setStartingModifiers(startingModifiers.getModifier());
		gender.setConstantModifiers(constantModifiers.getModifier());
		gender.setBannerModifiers(bannerModifiers.getModifier());
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		if (name == null)
		{
			return;
		}

		Gender g = Database.getInstance().getGender(name);

		startingModifiers.setModifier(g.getStartingModifiers());
		constantModifiers.setModifier(g.getConstantModifiers());
		bannerModifiers.setModifier(g.getBannerModifiers());
	}
}
