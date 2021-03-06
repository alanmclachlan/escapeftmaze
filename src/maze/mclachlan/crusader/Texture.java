
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

package mclachlan.crusader;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 *
 */
public class Texture implements Comparable<Texture>
{
	/** The number of different images in this texture */ 
	int nrFrames;
	
	/** The delay between changing images, in millis. */ 
	int animationDelay;
	
	/** The images involved. */ 
	BufferedImage[] images;
	
	/** Used by the engine */
	int[][] imageData;

	private String name;

	/** Width of all the images, in pixels */
	public int imageWidth;
	
	/** Height of all the images, in pixels */
	public int imageHeight;

	/** Any scrolling behaviour on this texture, null if none */
	ScrollBehaviour scrollBehaviour;

	/** how fast the texture scrolls */
	int scrollSpeed;

	//
	// Dodgy hack alert.  EngineObjects keep their own versions of these two so
	// that their frames can change independently.  These texture-wide counters
	// are used by walls floors and ceilings
	//

	/** The current image */
	int currentFrame;

	/** When this texture last changed */
	long lastChanged = System.currentTimeMillis();

	/*-------------------------------------------------------------------------*/
	public Texture(
		String name,
		int imageWidth,
		int imageHeight,
		BufferedImage[] frames,
		int animationDelay,
		ScrollBehaviour scrollBehaviour,
		int textureScrollSpeed)
	{
		this.name = name;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.animationDelay = animationDelay;
		this.images = frames;
		this.scrollBehaviour = scrollBehaviour;
		this.scrollSpeed = textureScrollSpeed;
		if (frames != null)
		{
			this.nrFrames = frames.length;
		}
	}

	/*-------------------------------------------------------------------------*/
	public int getAnimationDelay()
	{
		return animationDelay;
	}

	public int getCurrentFrame()
	{
		return currentFrame;
	}

	public void setCurrentFrame(int currentFrame)
	{
		this.currentFrame = currentFrame;
	}

	public int[][] getImageData()
	{
		return imageData;
	}

	public int getImageHeight()
	{
		return imageHeight;
	}

	public BufferedImage[] getImages()
	{
		return images;
	}

	public int getImageWidth()
	{
		return imageWidth;
	}

	public long getLastChanged()
	{
		return lastChanged;
	}

	public String getName()
	{
		return name;
	}

	public int getNrFrames()
	{
		return nrFrames;
	}

	public ScrollBehaviour getScrollBehaviour()
	{
		return scrollBehaviour;
	}

	public int getScrollSpeed()
	{
		return scrollSpeed;
	}

	public void setScrollBehaviour(
		ScrollBehaviour scrollBehaviour)
	{
		this.scrollBehaviour = scrollBehaviour;
	}

	public void setScrollSpeed(int scrollSpeed)
	{
		this.scrollSpeed = scrollSpeed;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	Current image data, taking all frames and transformations into account
	 */
	public int getCurrentImageData(int textureX, int textureY, long timeNow)
	{
		if (scrollBehaviour != null)
		{
			switch (scrollBehaviour)
			{
				case LEFT:
					textureX = Math.abs((int)((textureX - (timeNow / scrollSpeed)) % imageWidth));
					textureY = Math.abs(textureY);
					break;
				case RIGHT:
					textureX = Math.abs((int)((textureX + (timeNow / scrollSpeed)) % imageWidth));
					textureY = Math.abs(textureY);
					break;
				case DOWN:
					textureY = Math.abs((int)((textureY - (timeNow / scrollSpeed)) % imageHeight));
					textureX = Math.abs(textureX);
					break;
				case UP:
					textureY = Math.abs((int)((textureY + (timeNow / scrollSpeed)) % imageHeight));
					textureX = Math.abs(textureX);
					break;
				default:
					throw new CrusaderException("invalid scroll behaviour: " + scrollBehaviour);
			}
		}

		if (textureX < 0 || textureX > imageWidth-1)
		{
			textureX = Math.min(Math.abs(textureX % imageWidth), imageWidth-1);
		}

		if (textureY < 0 || textureY > imageHeight-1)
		{
			textureY = Math.min(Math.abs(textureY % imageHeight), imageHeight-1);
		}

		return imageData[currentFrame][textureX + textureY * imageWidth];
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String toString()
	{
		return "Texture{" +
			"name='" + name + '\'' +
			", nrFrames=" + nrFrames +
			", currentFrame=" + currentFrame +
			'}';
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int compareTo(Texture other)
	{
		return this.getName().compareTo(other.getName());
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof Texture))
		{
			return false;
		}
		Texture texture = (Texture)o;
		return name.equals(texture.name);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(name);
	}

	/*-------------------------------------------------------------------------*/
	public enum ScrollBehaviour
	{
		NONE, LEFT, RIGHT, UP, DOWN
	}
}
