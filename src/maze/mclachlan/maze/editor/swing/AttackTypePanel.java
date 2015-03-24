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
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.combat.AttackType;
import mclachlan.maze.stat.magic.MagicSys;

/**
 *
 */
public class AttackTypePanel extends EditorPanel
{
	private JTextField verb;
	private JComboBox damageType;
	private StatModifierComponent modifiers;

	/*-------------------------------------------------------------------------*/
	public AttackTypePanel()
	{
		super(SwingEditor.Tab.ATTACK_TYPES);
	}

	/*-------------------------------------------------------------------------*/
	protected JPanel getEditControls()
	{
		verb = new JTextField(30);
		verb.addActionListener(this);
		verb.addKeyListener(this);

		Vector<MagicSys.SpellEffectType> damageTypes = new Vector<MagicSys.SpellEffectType>();
		Collections.addAll(damageTypes, MagicSys.SpellEffectType.values());
		damageType = new JComboBox(damageTypes);
		damageType.addActionListener(this);

		modifiers = new StatModifierComponent(SwingEditor.Tab.ATTACK_TYPES);

		JPanel editControls = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = createGridBagConstraints();

		dodgyGridBagShite(editControls, new JLabel("Verb:"), verb, gbc);

		dodgyGridBagShite(editControls, new JLabel("Damage Type:"), damageType, gbc);

		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		editControls.add(new JLabel("Modifiers:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(modifiers, gbc);

		return editControls;
	}

	/*-------------------------------------------------------------------------*/
	public Vector loadData()
	{
		Vector vec = new Vector(Database.getInstance().getAttackTypes().keySet());
		Collections.sort(vec);
		return vec;
	}

	/*-------------------------------------------------------------------------*/
	public void newItem(String name)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.ATTACK_TYPES);
		AttackType g = new AttackType(
			name,
			"",
			MagicSys.SpellEffectType.NONE,
			new StatModifier());
		Database.getInstance().getAttackTypes().put(name, g);
		refreshNames(name);
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.ATTACK_TYPES);
		AttackType current = Database.getInstance().getAttackType((String)names.getSelectedValue());
		Database.getInstance().getAttackTypes().remove(current.getName());
		current.setName(newName);
		Database.getInstance().getAttackTypes().put(current.getName(), current);
		refreshNames(newName);
	}

	/*-------------------------------------------------------------------------*/
	public void copyItem(String newName)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.ATTACK_TYPES);

		AttackType current = Database.getInstance().getAttackType((String)names.getSelectedValue());

		AttackType g = new AttackType(
			newName,
			current.getVerb(),
			current.getDamageType(),
			new StatModifier(current.getModifiers()));
		Database.getInstance().getAttackTypes().put(newName, g);
		refreshNames(newName);
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.ATTACK_TYPES);
		String name = (String)names.getSelectedValue();
		Database.getInstance().getAttackTypes().remove(name);
		refreshNames(null);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		if (name == null)
		{
			return;
		}

		AttackType at = Database.getInstance().getAttackType(name);
		
		verb.removeActionListener(this);
		verb.removeKeyListener(this);
		damageType.removeActionListener(this);

		verb.setText(at.getVerb());
		damageType.setSelectedItem(at.getDamageType());
		modifiers.setModifier(at.getModifiers());
		
		verb.addActionListener(this);
		verb.addKeyListener(this);
		damageType.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		AttackType attackType = Database.getInstance().getAttackType(name);
		attackType.setVerb(verb.getText());
		attackType.setModifiers(modifiers.getModifier());
		attackType.setDamageType((MagicSys.SpellEffectType)damageType.getSelectedItem());
	}
}
