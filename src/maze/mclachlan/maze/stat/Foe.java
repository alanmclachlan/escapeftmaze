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

import java.util.*;
import mclachlan.crusader.EngineObject;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.game.*;
import mclachlan.maze.map.ILootEntry;
import mclachlan.maze.map.LootEntry;
import mclachlan.maze.map.LootTable;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.CombatantData;
import mclachlan.maze.stat.combat.event.AttackEvent;
import mclachlan.maze.stat.npc.NpcFaction;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class Foe extends UnifiedActor
{
	private FoeTemplate template;

	/** The ActorGroup that this foe belongs to */
	private FoeGroup foeGroup;

	/** Temp data for each combat round */
	private CombatantData combatantData;
	
	/** A handle to this foe's CrusaderEngine object */
	private EngineObject sprite;

	/** Whether the PCs have figured out what the heck this is. A constant from
	 * {@link Item.IdentificationState} */
	private int identificationState = Item.IdentificationState.UNIDENTIFIED;

	/** whether this foes is around because of a summon spell */
	private boolean isSummoned;

	/*-------------------------------------------------------------------------*/

	/**
	 * Creates a new foe from the given foe template
	 */
	public Foe(FoeTemplate template)
	{
		// todo: gender, race and class for foes
		super(
			template.getName(),
			null,
			null,
			null,
			template.getBodyParts(),
			null,
			null,
			new Stats(template.getStats()),
			template.getSpellBook(),
			new Inventory(MAX_PACK_ITEMS));

		this.template = template;

		// set level
		HashMap<String, Integer> levels = new HashMap<String, Integer>();
		levels.put(getName(), template.getLevelRange().roll());
		this.setLevels(levels);

		// set natural weapons
		if (template.getNaturalWeapons() != null)
		{
			List<NaturalWeapon> naturalWeapons = new ArrayList<NaturalWeapon>();
			for (String nw : template.getNaturalWeapons())
			{
				naturalWeapons.add(Database.getInstance().getNaturalWeapons().get(nw));
			}
			setNaturalWeapons(naturalWeapons);
		}

		// roll up this foes vitals
		int maxHP = template.getHitPointsRange().roll();
		int maxStealth = template.getActionPointsRange().roll();
		int maxMagic = template.getMagicPointsRange().roll();

		getStats().setHitPoints(new CurMaxSub(maxHP));
		getStats().setActionPoints(new CurMax(maxStealth));
		getStats().setMagicPoints(new CurMax(maxMagic));

		if (template.getIdentificationDifficulty() == 0)
		{
			identificationState = Item.IdentificationState.IDENTIFIED;
		}

		// apply difficulty levels
		DifficultyLevel dl = Maze.getInstance().getDifficultyLevel();
		if (dl != null)
		{
			dl.foeIsSpawned(this);
		}

		generateInventory();

		Maze.log(Log.DEBUG, "Spawned ["+template.getName()+"] " +
			"hp=["+getStats().getHitPoints().getCurrent()+"] " +
			"sp=["+getStats().getActionPoints().getCurrent()+"] " +
			"mp=["+getStats().getMagicPoints().getCurrent()+"]");
	}

	/*-------------------------------------------------------------------------*/
	private void generateInventory()
	{
		Maze.log(Log.DEBUG, "generating inventory for "+getName());

		GroupOfPossibilities<ILootEntry> lootEntries = getLootTable().getLootEntries();
		if (lootEntries != null)
		{
			List<ILootEntry> entries = lootEntries.getRandom();
			List<Item> items = LootEntry.generate(entries);

			for (Item i : items)
			{
				Maze.log(Log.DEBUG, getName()+" carries "+i.getDisplayName());
				addInventoryItem(i);
			}
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * This foe gets a chance to look through what is in it's inventory and
	 * equip items in any available slots
	 */
	public void initialEquip()
	{
		Maze.log(Log.DEBUG, getName()+" organises inventory");
		Maze.log(Log.DEBUG, getName()+" slots: ["+this.getAllEquipableSlots()+"]");
		Maze.log(Log.DEBUG, getName()+" inventory: ["+this.getInventory()+"]");

		for (EquipableSlot slot : this.getAllEquipableSlots())
		{
			List<Item> inventory = this.getInventory().getItems();
			Item item = getBestItemForSlot(slot.getType(), inventory);
			if (item != null)
			{
				Maze.log(Log.DEBUG, getName()+" equips "+item.getName()+" in "+slot.getType());
				this.setEquippedItem(slot.getType(), item);
				this.getInventory().remove(item);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private Item getBestItemForSlot(EquipableSlot.Type slotType, List<Item> items)
	{
		Item bestItem = null;

		for (Item item : items)
		{
			boolean meetsRequirements = this.meetsRequirements(item.getEquipRequirements());
			boolean contains = item.getEquipableSlotTypes().contains(slotType);

			if (meetsRequirements &&
				contains)
			{
				if (bestItem == null)
				{
					bestItem = item;
				}
				else if (item.getBaseCost() > bestItem.getBaseCost())
				{
					// todo: better item scoring that just base cost
					bestItem = item;
				}
			}
		}

		return bestItem;
	}

	/*-------------------------------------------------------------------------*/
	public ActorActionIntention getCombatIntention()
	{
		Combat combat = this.combatantData.getCombat();

		// party gets a free round
		Combat.AmbushStatus ambushStatus = combat.getAmbushStatus();
		if (ambushStatus == Combat.AmbushStatus.PARTY_MAY_AMBUSH_FOES ||
			ambushStatus == Combat.AmbushStatus.PARTY_MAY_AMBUSH_OR_EVADE_FOES)
		{
			return ActorActionIntention.INTEND_NOTHING;
		}

		// dead or immobilised foes do nothing
		if (this.getHitPoints().getCurrent() <= 0 ||
			!GameSys.getInstance().askActorForCombatIntentions(this))
		{
			return ActorActionIntention.INTEND_NOTHING;
		}

		FoeCombatAi ai = Maze.getInstance().getDifficultyLevel().getFoeCombatAi();
		return ai.getCombatIntention(this, combat);
	}

	/*-------------------------------------------------------------------------*/
	public int getStealthBehaviour()
	{
		return template.getStealthBehaviour();
	}

	/*-------------------------------------------------------------------------*/
	public int getFleeChance()
	{
		return template.getFleeChance();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Asks this foe (who has been nominated leader of the bunch of foes), if
	 * they should evade the party.
	 */
	public boolean shouldEvade(List<FoeGroup> groups, PlayerParty party)
	{
		FoeCombatAi ai = Maze.getInstance().getDifficultyLevel().getFoeCombatAi();
		return ai.shouldEvade(this, groups, party);
	}

	public void removeItem(Item item, boolean removeWholeStack)
	{
	}

	public void removeItem(String itemName, boolean removeWholeStack)
	{
	}

	/*-------------------------------------------------------------------------*/
	public ActorGroup getActorGroup()
	{
		return foeGroup;
	}

	@Override
	public void inventoryItemAdded(Item item)
	{
	}

	/*-------------------------------------------------------------------------*/
	public void removeCurse(int strength)
	{
		// no effect on foes
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void addAllies(List<FoeGroup> foeGroups)
	{
		Maze.getInstance().addFoeAllies(foeGroups);
		if (this.combatantData != null)
		{
			this.combatantData.setSummonedGroup(foeGroups);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean isActiveModifier(String modifier)
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<SpellLikeAbility> getSpellLikeAbilities()
	{
		return template.getSpellLikeAbilities();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public CharacterClass.Focus getFocus()
	{
		return template.getFocus();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public PercentageTable<BodyPart> getBodyParts()
	{
		return this.template.getBodyParts();
	}
	
	/*-------------------------------------------------------------------------*/
	public PercentageTable<String> getPlayerBodyParts()
	{
		return this.template.getPlayerBodyParts();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Item getArmour(BodyPart bodyPart)
	{
		// foes are only treated as having natural armour, on their BodyParts
		return null;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void deductAmmo(AttackEvent event)
	{
		// Foes have no ammo restrictions ;-)
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return template.getName();
	}

	/*-------------------------------------------------------------------------*/
	public boolean isNpc()
	{
		return template.isNpc();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	A display name for this foe that depends on whether or not it has
	 * 	been identified.
	 */
	public String getDisplayName()
	{
		String result;
		switch(getIdentificationState())
		{
			case Item.IdentificationState.IDENTIFIED:
				result = getName();
				break;
			case Item.IdentificationState.UNIDENTIFIED:
				result = getUnidentifiedName();
				break;
			default: throw new MazeException("Invalid item identification state: "+
				getIdentificationState());
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	A plural display name for this foe that depends on whether or not
	 * 	it has been identified.
	 */
	public String getDisplayNamePlural()
	{
		String result;
		switch(getIdentificationState())
		{
			case Item.IdentificationState.IDENTIFIED:
				result = getPluralName();
				break;
			case Item.IdentificationState.UNIDENTIFIED:
				result = getUnidentifiedPluralName();
				break;
			default: throw new MazeException("Invalid item identification state: "+
				getIdentificationState());
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public int getBaseModifier(String modifier)
	{
		return this.template.getStats().getModifier(modifier);
	}

	/*-------------------------------------------------------------------------*/
	public void incModifier(String modifier, int amount)
	{
		this.getStats().incModifier(modifier, amount);
	}

	/*-------------------------------------------------------------------------*/
	public MazeTexture getBaseTexture()
	{
		return this.template.getBaseTexture();
	}
	
	/*-------------------------------------------------------------------------*/
	public MazeTexture getMeleeAttackTexture()
	{
		return this.template.getMeleeAttackTexture();
	}

	/*-------------------------------------------------------------------------*/
	public MazeTexture getRangedAttackTexture()
	{
		return this.template.getRangedAttackTexture();
	}

	/*-------------------------------------------------------------------------*/
	public MazeTexture getCastSpellTexture()
	{
		return this.template.getCastSpellTexture();
	}

	/*-------------------------------------------------------------------------*/
	public MazeTexture getSpecialAbilityTexture()
	{
		return this.template.getSpecialAbilityTexture();
	}
	
	/*-------------------------------------------------------------------------*/
	//
	// Data used by the engine in combat
	//
	public CombatantData getCombatantData()
	{
		return combatantData;
	}

	public void setCombatantData(CombatantData combatantData)
	{
		this.combatantData = combatantData;
	}

	public EngineObject getSprite()
	{
		return sprite;
	}

	public void setSprite(EngineObject sprite)
	{
		this.sprite = sprite;
	}

	/**
	 * @return
	 * 	A constant from {@link Item.IdentificationState}.
	 */
	public int getIdentificationState()
	{
		return identificationState;
	}

	/**
	 * @param state
	 * 	A constant from {@link Item.IdentificationState}.
	 */
	public void setIdentificationState(int state)
	{
		this.identificationState = state;
	}

	/*-------------------------------------------------------------------------*/
	public String getPluralName()
	{
		return template.getPluralName();
	}

	public String getUnidentifiedName()
	{
		return template.getUnidentifiedName();
	}

	public String getUnidentifiedPluralName()
	{
		return template.getUnidentifiedPluralName();
	}

	public LootTable getLootTable()
	{
		return template.getLoot();
	}

	public int getExperience()
	{
		return template.getExperience();
	}

	public boolean cannotBeEvaded()
	{
		return template.cannotBeEvaded();
	}

	public int getIdentificationDifficulty()
	{
		return template.getIdentificationDifficulty();
	}

	public String getType()
	{
		return template.getType();
	}

	public StatModifier getAllFoesBannerModifiers()
	{
		return template.getAllFoesBannerModifiers();
	}

	public StatModifier getFoeGroupBannerModifiers()
	{
		return template.getFoeGroupBannerModifiers();
	}

	public boolean isImmuneToCriticals()
	{
		return template.immuneToCriticals;
	}

	public boolean isSummoned()
	{
		return isSummoned;
	}

	public void setSummoned(boolean summoned)
	{
		isSummoned = summoned;
	}

	public String getFaction()
	{
		return template.getFaction();
	}
	
	public MazeScript getAppearanceScript()
	{
		return template.getAppearanceScript();
	}

	public FoeGroup getFoeGroup()
	{
		return this.foeGroup;
	}

	public void setFoeGroup(FoeGroup foeGroup)
	{
		this.foeGroup = foeGroup;
	}

	public MazeScript getDeathScript()
	{
		return this.template.getDeathScript();
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("Foe");
		sb.append("{name='").append(template.getName()).append('\'');
		sb.append(", level=").append(getLevel());
		sb.append('}');
		return sb.toString();
	}

	/*-------------------------------------------------------------------------*/
	public boolean canAttack(int engagementRange)
	{
		List<AttackWith> items = getAttackWithOptions();

		for (AttackWith aw : items)
		{
			if (isLegalAttack(aw, engagementRange))
			{
				return true;
			}
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isLegalAttack(AttackWith aw, int engagementRange)
	{
		return aw.getMinRange() <= engagementRange &&
			aw.getMaxRange() >= engagementRange;
	}

	/*-------------------------------------------------------------------------*/
	public int getEvasionBehaviour()
	{
		return template.getEvasionBehaviour();
	}

	/*-------------------------------------------------------------------------*/
	public NpcFaction.Attitude getDefaultAttitude()
	{
		return template.getDefaultAttitude();
	}

	/*-------------------------------------------------------------------------*/
	public static class EvasionBehaviour
	{
		/** the foe will always ambush the party if it can */
		public static final int NEVER_EVADE = 1;
		/** the foe will evade the party 50% of the time */
		public static final int RANDOM_EVADE = 2;
		/** the foe will always evade the party if it can */
		public static final int ALWAYS_EVADE = 3;
		/** the foe will make a judgement call based on party strength */
		public static final int CLEVER_EVADE = 4;

		public static String toString(int i)
		{
			switch (i)
			{
				case NEVER_EVADE: return "never evade";
				case RANDOM_EVADE: return "random evade";
				case ALWAYS_EVADE: return "always evade";
				case CLEVER_EVADE: return "clever evade";
				default: throw new MazeException("Invalid evasion behaviour: "+i);
			}
		}

		public static int valueOf(String s)
		{
			if (s.equals("never evade")) return NEVER_EVADE;
			else if (s.equals("random evade")) return RANDOM_EVADE;
			else if (s.equals("always evade")) return ALWAYS_EVADE;
			else if (s.equals("clever evade")) return CLEVER_EVADE;
			else throw new MazeException("Invalid evasion behaviour: ["+s+"]");
		}
	}

	/*-------------------------------------------------------------------------*/
	public static class StealthBehaviour
	{
		/** foe will never take a hide or ambush action */
		public static final int NOT_STEALTHY = 1;
		/** foe will not rely exclusively on stealth actions, but will ambush if
		 * possible and hide sometimes when out of action points */
		public static final int OPPORTUNISTIC = 2;
		/** foe relies on stealth and will always try to hide and ambush */
		public static final int STEALTH_RELIANT = 3;

		public static String toString(int i)
		{
			switch (i)
			{
				case NOT_STEALTHY: return "not stealthy";
				case OPPORTUNISTIC: return "opportunistic";
				case STEALTH_RELIANT: return "stealth reliant";
				default: throw new MazeException("Invalid stealth behaviour: "+i);
			}
		}

		public static int valueOf(String s)
		{
			if (s.equals("not stealthy")) return NOT_STEALTHY;
			else if (s.equals("opportunistic")) return OPPORTUNISTIC;
			else if (s.equals("stealth reliant")) return STEALTH_RELIANT;
			else throw new MazeException("Invalid stealth behaviour: ["+s+"]");
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Contains some common foe types, but not an exclusive set.
	 */
	public static class Type
	{
		public static final String NONE = "None";
		public static final String LEGENDARY = "Legendary";
	}

	/*-------------------------------------------------------------------------*/
	public static class Animation
	{
		public static final int BASE_TEXTURE = 0;
		public static final int MELEE_ATTACK = 1;
		public static final int RANGED_ATTACK = 2;
		public static final int CAST_SPELL = 3;
		public static final int SPECIAL_ABILITY = 4;
	}
}
