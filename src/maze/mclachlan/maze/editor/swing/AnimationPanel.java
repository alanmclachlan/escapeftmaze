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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import static mclachlan.maze.data.v1.V1Animation.*;
import mclachlan.maze.util.MazeException;
import mclachlan.maze.ui.diygui.Animation;
import mclachlan.maze.ui.diygui.Constants;
import mclachlan.maze.ui.diygui.animation.ProjectileAnimation;
import mclachlan.maze.ui.diygui.animation.ColourMagicPortraitAnimation;

/**
 *
 */
public class AnimationPanel extends JPanel implements ActionListener
{
	private int[] dialogLookup = new int[MAX];
	
	CardLayout cards;
	JPanel controls;
	JComboBox type;
	
	JTextField impl;
	JTextArea projectileImages;
	JSpinner projectileFrameDelay;
	JRadioButton blue, red, black, purple, gold, white, green, custom;
	JButton customColourButton;

	/*-------------------------------------------------------------------------*/
	public AnimationPanel()
	{
		JPanel top = new JPanel();
		Vector<String> vec = new Vector<String>();
		for (int i=0; i<MAX; i++)
		{
			String str = describeType(i);
			if (str != null)
			{
				int index = vec.size();
				dialogLookup[index] = i;
				vec.addElement(str);
			}
		}
		type = new JComboBox(vec);
		type.addActionListener(this);
		top.add(new JLabel("Type"));
		top.add(type);
		
		this.cards = new CardLayout();
		controls = new JPanel(cards);
		for (int i=0; i<MAX; i++)
		{
			JPanel c = getControls(i);
			if (c != null)
			{
				controls.add(c, String.valueOf(i));
			}
		}
		
		this.setLayout(new BorderLayout(3,3));
		this.add(top, BorderLayout.NORTH);
		this.add(controls, BorderLayout.CENTER);
	}

	/*-------------------------------------------------------------------------*/
	public void setState(Animation a)
	{
		int animType;
		if (types.containsKey(a.getClass()))
		{
			animType = types.get(a.getClass());
		}
		else
		{
			animType = CUSTOM;
		}

		for (int i = 0; i < dialogLookup.length; i++)
		{
			if (dialogLookup[i] == animType)
			{
				type.setSelectedIndex(i);
				break;
			}
		}
		
		switch (animType)
		{
			case CUSTOM: 
				impl.setText(a.getClass().getName());
				break;
			case PROJECTILE:
				ProjectileAnimation pa = (ProjectileAnimation)a;
				List<String> names = pa.getProjectileImages();
				StringBuilder sb = new StringBuilder();
				for (String s : names)
				{
					sb.append(s).append('\n');
				}
				projectileImages.setText(sb.toString());
				projectileFrameDelay.setValue(pa.getFrameDelay());
				break;
			case COLOUR_PORTRAIT:
				ColourMagicPortraitAnimation cmpa = (ColourMagicPortraitAnimation)a;
				Color c = cmpa.getColour();
				
				if (c.equals(Color.BLUE))
				{
					blue.setSelected(true);
				}
				else if (c.equals(Color.RED))
				{
					red.setSelected(true);
				}
				else if (c.equals(Color.BLACK))
				{
					black.setSelected(true);
				}
				else if (c.equals(Constants.Colour.PURPLE))
				{
					purple.setSelected(true);
				}
				else if (c.equals(Constants.Colour.GOLD))
				{
					gold.setSelected(true);
				}
				else if (c.equals(Color.WHITE))
				{
					white.setSelected(true);
				}
				else if (c.equals(Constants.Colour.STEALTH_GREEN))
				{
					green.setSelected(true);
				}
				else
				{
					custom.setSelected(true);
					customColourButton.setBackground(c);
				}

				break;
			default: throw new MazeException("Invalid animation type: "+animType);
		}
	}

	/*-------------------------------------------------------------------------*/
	private String describeType(int i)
	{
		switch (i)
		{
			case CUSTOM: return "Custom";
			case PROJECTILE: return "Projectile";
			case COLOUR_PORTRAIT: return "Colour Magic Portrait";
			default: throw new MazeException("Invalid animation type: "+i);
		}
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getControls(int i)
	{
		switch (i)
		{
			case CUSTOM: return getCustomPanel();
			case PROJECTILE: return getProjectilePanel();
			case COLOUR_PORTRAIT: return getColourMagicPanel();
			default: throw new MazeException("Invalid animation type: "+i);
		}
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getColourMagicPanel()
	{
		blue = new JRadioButton("Blue");
		red = new JRadioButton("Red");
		black = new JRadioButton("Black");
		purple = new JRadioButton("Purple");
		gold = new JRadioButton("Gold");
		white = new JRadioButton("White");
		green = new JRadioButton("Green");
		custom = new JRadioButton();
		customColourButton = new JButton(" ... ");
		customColourButton.addActionListener(this);
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(blue);
		bg.add(red);
		bg.add(black);
		bg.add(purple);
		bg.add(gold);
		bg.add(white);
		bg.add(green);
		bg.add(custom);
		
		JPanel result = new JPanel();
		dirtyGridLayoutCrap(result, 
			blue, new JLabel(),
			red, new JLabel(),
			black, new JLabel(),
			purple, new JLabel(),
			gold, new JLabel(),
			white, new JLabel(),
			green, new JLabel(),
			custom, customColourButton);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getProjectilePanel()
	{
		projectileImages = new JTextArea(20, 30);
		projectileFrameDelay = new JSpinner(new SpinnerNumberModel(0, 0, 99999, 1));
		JPanel result = new JPanel();
		dirtyGridLayoutCrap(result, 
			new JLabel("Frame Delay:"), projectileFrameDelay,
			new JLabel("Projectile Image: "), projectileImages);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getCustomPanel()
	{
		impl = new JTextField(20);
		JPanel result = new JPanel();
		dirtyGridLayoutCrap(result, new JLabel("Custom Impl: "), impl);
		return result;
	}
	
	/*-------------------------------------------------------------------------*/
	private void dirtyGridLayoutCrap(JPanel panel, Component... comps)
	{
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		for (int i=0; i<comps.length; i+=2)
		{
			if (i == comps.length-2)
			{
				gbc.weighty = 1;
			}
			gbc.gridx = 0;
			gbc.weightx = 0;
			if (comps[i+1] == null)
			{
				gbc.gridwidth = 2;
				gbc.weightx = 1;
			}
			panel.add(comps[i], gbc);
			gbc.gridx = 1;
			gbc.weightx = 1;
			if (comps[i+1] == null)
			{
				gbc.gridwidth = 1;
			}
			else
			{
				panel.add(comps[i+1], gbc);
			}
			gbc.gridy++;
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public Animation getAnimation()
	{
		int index = type.getSelectedIndex();
		switch (index)
		{
			case CUSTOM: 
				try
				{
					Class clazz = Class.forName(impl.getText());
					return (Animation)clazz.newInstance();
				}
				catch (Exception x)
				{
					throw new MazeException(x);
				}
			case PROJECTILE:
				List<String> list = Arrays.asList(projectileImages.getText().split("\\n"));
				return new ProjectileAnimation(list, (Integer)projectileFrameDelay.getValue());
			case COLOUR_PORTRAIT:
				Color c = null;
				if (blue.isSelected())
				{
					c = Color.BLUE;
				}
				else if (red.isSelected())
				{
					c = Color.RED;
				}
				else if (black.isSelected())
				{
					c = Color.BLACK;
				}
				else if (purple.isSelected())
				{
					c = Constants.Colour.PURPLE;
				}
				else if (gold.isSelected())
				{
					c = Constants.Colour.GOLD;
				}
				else if (white.isSelected())
				{
					c = Color.WHITE;
				}
				else if (green.isSelected())
				{
					c = Constants.Colour.STEALTH_GREEN;
				}
				else
				{
					c = customColourButton.getBackground();
				}
				return new ColourMagicPortraitAnimation(c);
			default: throw new MazeException("Invalid animation type: "+index);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == type)
		{
			int index = type.getSelectedIndex();
			if (index > -1)
			{
				cards.show(controls, String.valueOf(dialogLookup[index]));
			}
		}
		else if (e.getSource() == customColourButton)
		{
			custom.setSelected(true);
			Color c = JColorChooser.showDialog(
				SwingEditor.instance,
				"Custom Colour",
				Color.BLACK);
			
			customColourButton.setBackground(c);
		}
	}
}
