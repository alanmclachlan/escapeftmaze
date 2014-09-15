
package mclachlan.maze.campaign.def.npc;

import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.npc.NpcLeavesEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;
import java.util.*;

/**
 * Vendor+guildmaster at Danaos castle, leonal.
 */
public class Agenor extends NpcScript
{
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		return null;
	}

	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("The commissary appears to be well stocked " +
				"and ordered. Shelves of supplies, equipment, weapons and " +
				"armour line the walls. A tall leonal with a clipboard " +
				"approaches you."));
	}

	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new NpcSpeechEvent("Grrreetings, strangers."),
			new NpcSpeechEvent("I am Agenor, quartermaster here. For you, I " +
				"can buy and sell supplies."),
			new NpcSpeechEvent("Off the record, I am also quite well connected " +
				"in the community, and if you're looking to be put in touch " +
				"with other adventurers, I can probably arrange something."));
	}

	public List<MazeEvent> subsequentGreeting()
	{
		return getList(
			new NpcSpeechEvent("Grrreetings again, strangers. How can I help?"));
	}

	public List<MazeEvent> neutralGreeting()
	{
		return getList(
			new NpcSpeechEvent("Hrrrrrr. Yes?"));
	}

	/*
	 public List<MazeEvent> attacksParty()
	 {
		 throw new RuntimeException("Unimplemented auto generated method!");
	 }

	 public List<MazeEvent> attackedByParty()
	 {
		 throw new RuntimeException("Unimplemented auto generated method!");
	 }

 */
	public List<MazeEvent> partyLeavesNeutral()
	{
		return getList(
			new NpcSpeechEvent("Hrrrrrr."),
			new NpcLeavesEvent());
	}

	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(
			new NpcSpeechEvent("Farrrrewell friends"),
			new NpcLeavesEvent());
	}

/*
	public List<MazeEvent> mindRead(int strength)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> mindReadFails(int strength)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> successfulThreat(int total)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> failedThreat(int total)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> successfulBribe(int total)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> failedBribe(int total)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> successfulCharm()
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> failedCharm()
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> successfulTheft(PlayerCharacter pc, Item item)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> failedUndetectedTheft(PlayerCharacter pc, Item item)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> failedDetectedTheft(PlayerCharacter pc, Item item)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> grabAndAttack(PlayerCharacter pc, Item item)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> givenItemByParty(PlayerCharacter owner, Item item)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> partyWantsToTalk(PlayerCharacter pc)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	public List<MazeEvent> parsePartySpeech(PlayerCharacter pc, String speech)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}*/
}