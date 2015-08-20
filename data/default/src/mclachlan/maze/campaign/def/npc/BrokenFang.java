
package mclachlan.maze.campaign.def.npc;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.map.script.SetMazeVariableEvent;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.combat.event.SoundEffectEvent;
import mclachlan.maze.stat.npc.ActorsLeaveEvent;
import mclachlan.maze.stat.npc.ChangeNpcLocationEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;

/**
 * Gnoll chieftain, Gnoll Village.
 */
public class BrokenFang extends NpcScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("The hut is clean and dry, hung with " +
				"colourful tapestries.\n"),
			new FlavourTextEvent("From behind one of the hangings appears " +
				"a tall, muscular gnoll. He approaches you..."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new SoundEffectEvent(RedEar.SOUND_GROWL),
			new NpcSpeechEvent("Hrpf. Broken Fang greets you. Broken Fang is " +
				"chieftain here."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> subsequentGreeting()
	{
		return getList(
			new NpcSpeechEvent("Hrpf."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> neutralGreeting()
	{
		return getList(
			new NpcSpeechEvent("Grrr."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesNeutral()
	{
		return getList(
			new NpcSpeechEvent("Hrpf."),
			new ActorsLeaveEvent());
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(
			new NpcSpeechEvent("Grrrr."),
			new ActorsLeaveEvent());
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> parsePartySpeech(PlayerCharacter pc, String speech)
	{
		if (speech.toLowerCase().indexOf("washing of the spears is come") >= 0)
		{
			return getList(
				new NpcSpeechEvent("Grrrrarrrrr! Long waited day is here!"),
				new NpcSpeechEvent("Long gnolls have waited, patient hunters. " +
					"Today war band runs! Blood and kills!"),
				new NpcSpeechEvent("Grrraarrrrrgh!"),
				new FlavourTextEvent("Brushing you aside, the gnoll chieftain " +
					"stides out of the tent, barking orders loudly...."),
				new SetMazeVariableEvent(Imogen.QUEST_3_COMPLETE, "true"),
				// move him out the way
				new ChangeNpcLocationEvent(npc, new Point(1,1), "Gnoll Village"),
				new ActorsLeaveEvent());
		}
		else
		{
			return super.parsePartySpeech(pc, speech);
		}
	}
}