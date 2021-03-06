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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.stat.npc.NpcFaction;
import mclachlan.maze.stat.npc.NpcFactionTemplate;

/**
 *
 */
public class NpcFactionTemplatePanel extends EditorPanel
{
	private JComboBox<NpcFaction.Attitude> startingAttitude;

	/*-------------------------------------------------------------------------*/
	public NpcFactionTemplatePanel()
	{
		super(SwingEditor.Tab.NPC_FACTION_TEMPLATES);
	}

	/*-------------------------------------------------------------------------*/
	public JPanel getEditControls()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;

		startingAttitude = new JComboBox<NpcFaction.Attitude>(NpcFaction.Attitude.values());
		startingAttitude.addActionListener(this);

		result.add(new JLabel("Starting Attitude:"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		result.add(startingAttitude, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Vector<DataObject> loadData()
	{
		return new Vector<>(Database.getInstance().getNpcFactionTemplates().values());
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		NpcFactionTemplate nft = Database.getInstance().getNpcFactionTemplates().get(name);

		startingAttitude.removeActionListener(this);
		startingAttitude.setSelectedItem(nft.getStartingAttitude());
		startingAttitude.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject newItem(String name)
	{
		NpcFactionTemplate nft = new NpcFactionTemplate(name, NpcFaction.Attitude.NEUTRAL);
		Database.getInstance().getNpcFactionTemplates().put(name, nft);
		return nft;
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		NpcFactionTemplate nft = Database.getInstance().getNpcFactionTemplates().remove(currentName);
		nft.setName(newName);
		Database.getInstance().getNpcFactionTemplates().put(newName, nft);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject copyItem(String newName)
	{
		NpcFactionTemplate current = Database.getInstance().getNpcFactionTemplates().get(currentName);
		NpcFactionTemplate nft = new NpcFactionTemplate(newName, current.getStartingAttitude());
		Database.getInstance().getNpcFactionTemplates().put(newName, nft);
		return nft;
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getNpcFactionTemplates().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject commit(String name)
	{
		NpcFactionTemplate nft = Database.getInstance().getNpcFactionTemplates().get(name);
		nft.setStartingAttitude((NpcFaction.Attitude)startingAttitude.getSelectedItem());
		return nft;
	}
}
