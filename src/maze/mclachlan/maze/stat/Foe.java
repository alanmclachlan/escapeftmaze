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
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.game.*;
import mclachlan.maze.map.ILootEntry;
import mclachlan.maze.map.LootEntry;
import mclachlan.maze.map.LootTable;
import mclachlan.maze.stat.combat.*;
import mclachlan.maze.stat.combat.event.AttackEvent;
import mclachlan.maze.stat.magic.MagicSys;
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
			template.getNaturalWeapons(),
			null,
			new Stats(template.getStats()),
			template.getSpellBook(),
			new Inventory(MAX_PACK_ITEMS));

		this.template = template;

		HashMap<String, Integer> levels = new HashMap<String, Integer>();
		levels.put(getName(), template.levelRange.roll());
		this.setLevels(levels);

		// roll up this foes vitals
		int maxHP = template.hitPointsRange.roll();
		int maxStealth = template.actionPointsRange.roll();
		int maxMagic = template.magicPointsRange.roll();

		getStats().setHitPoints(new CurMaxSub(maxHP));
		getStats().setActionPoints(new CurMax(maxStealth));
		getStats().setMagicPoints(new CurMax(maxMagic));

		if (template.identificationDifficulty == 0)
		{
			identificationState = Item.IdentificationState.IDENTIFIED;
		}

		// apply difficulty levels
		DifficultyLevel dl = Maze.getInstance().getDifficultyLevel();
		dl.foeIsSpawned(this);

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
		if (ambushStatus == Combat.AmbushStatus.PARTY_AMBUSHES_FOES ||
			ambushStatus == Combat.AmbushStatus.PARTY_MAY_EVADE_FOES)
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
	public ActorActionIntention getFoeAttackIntention(int engagementRange,
		Dice possDice, Combat combat)
	{
		FoeAttack fa = null;
		FoeAttack.FoeAttackSpell spell = null;

		while (fa == null)
		{
			fa = this.getAttacks().getRandomItem();
			if (!isLegalAttack(fa, engagementRange))
			{
				fa = null;
				continue;
			}

			switch (fa.getType())
			{
				case MELEE_ATTACK:
				case RANGED_ATTACK:
					ActorGroup target = combat.getFoesOf(this).get(possDice.roll());
					return new AttackIntention(target, combat, fa);

				case CAST_SPELL:
					spell = fa.getSpells().getRandomItem();
					SpellTarget spellTarget = chooseTarget(this, spell, combat);
					return new SpellIntention(spellTarget, spell.getSpell(), spell.getCastingLevel().roll());

				case SPECIAL_ABILITY:
					spell = fa.getSpecialAbility();
					SpellTarget abilityTarget = chooseTarget(this, spell, combat);
					return new SpecialAbilityIntention(abilityTarget, spell.getSpell(), spell.getCastingLevel().roll());

				default:
					throw new MazeException("Invalid type: "+fa.getType());
			}
		}

		throw new MazeException("could not find valid foe attack for ["+this.getName()+"]");
	}

	/*-------------------------------------------------------------------------*/
	public SpellTarget chooseTarget(Foe foe, FoeAttack.FoeAttackSpell spell, Combat combat)
	{
		SpellTarget target;
		switch (spell.getSpell().getTargetType())
		{
			case MagicSys.SpellTargetType.ALL_FOES:
			case MagicSys.SpellTargetType.CLOUD_ALL_GROUPS:
				target = null;
				break;

			case MagicSys.SpellTargetType.PARTY:
				target = combat.getActorGroup(foe);
				break;

			case MagicSys.SpellTargetType.TILE:
				target = null;
				break;

			case MagicSys.SpellTargetType.CASTER:
				target = foe;
				break;

			case MagicSys.SpellTargetType.ALLY:
				List<UnifiedActor> allies = combat.getAllAlliesOf(foe);
				Dice d = new Dice(1, allies.size(), -1);
				target = allies.get(d.roll());
				break;

			case MagicSys.SpellTargetType.FOE:
				List<UnifiedActor> enemies = combat.getAllFoesOf(foe);
				d = new Dice(1, enemies.size(), -1);
				target = enemies.get(d.roll());
				break;

			case MagicSys.SpellTargetType.FOE_GROUP:
			case MagicSys.SpellTargetType.CLOUD_ONE_GROUP:
				List<ActorGroup> groups = combat.getFoesOf(foe);
				d = new Dice(1, groups.size(), -1);
				target = groups.get(d.roll());
				break;

			// these should never really be cast be foes
			case MagicSys.SpellTargetType.ITEM:
			case MagicSys.SpellTargetType.NPC:
			case MagicSys.SpellTargetType.LOCK_OR_TRAP:
				target = null;
				break;

			default: throw new MazeException("Invalid target type: "+
				spell.getSpell().getTargetType());
		}
		return target;
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
		switch (this.template.evasionBehaviour)
		{
			case EvasionBehaviour.ALWAYS_EVADE:
				return true;
			case EvasionBehaviour.NEVER_EVADE:
				return false;
			case EvasionBehaviour.RANDOM_EVADE:
				return Dice.d2.roll() == 1;
			case EvasionBehaviour.CLEVER_EVADE:
				//
				// some heuristics to decide if they should attack
				//
				int foeStrength = 0;
				for (FoeGroup fg : groups)
				{
					for (UnifiedActor a : fg.getActors())
					{
						foeStrength += a.getLevel();
					}
				}

				int partyStrength = 0;
				for (UnifiedActor a : party.getActors())
				{
					partyStrength += a.getLevel();
				}

				return foeStrength >= partyStrength;
			default:
				throw new MazeException("Invalid evasion behaviour: "+
					this.template.evasionBehaviour);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<CombatAction> getCombatActions(ActorActionIntention intention)
	{
		Maze.log(Log.DEBUG, "getting combat actions for "+this.getName());
		if (intention instanceof AttackIntention)
		{
			List<CombatAction> result = new ArrayList<CombatAction>();

			if (intention instanceof AttackIntention)
			{
				FoeAttack fa = (FoeAttack)((AttackIntention)intention).getAttackWith();

				int[] attacks = fa.getAttacks();
				int initiativePenalty = 0;
				for (int attack : attacks)
				{
					CombatAction action = new AttackAction(
						((AttackIntention)intention).getActorGroup(),
						fa,
						attack,
						fa.getAttackScript(),
						true,
						GameSys.getInstance().isLightningStrike(this, fa),
						fa.getDefaultDamageType());

					action.setModifier(Stats.Modifiers.INITIATIVE, initiativePenalty);
					initiativePenalty -= 5;
					result.add(action);
					Maze.log(Log.DEBUG, action.toString());
				}

				return result;
			}
			else if (intention instanceof SpellIntention)
			{
				SpellIntention si = (SpellIntention)intention;
				CombatAction action = new SpellAction(
					si.getTarget(),
					si.getSpell(),
					si.getCastingLevel());
				result.add(action);
				Maze.log(Log.DEBUG, action.toString());
				return result;
			}
			else if (intention instanceof SpecialAbilityIntention)
			{
				SpecialAbilityIntention sai = (SpecialAbilityIntention)intention;
				CombatAction action = new SpecialAbilityAction(
					sai.getDisplayName(),
					sai.getTarget(),
					sai.getSpell(),
					sai.getCastingLevel());
				Maze.log(Log.DEBUG, action.toString());
				result.add(action);
				return result;
			}
			else
			{
				throw new MazeException("Invalid type: "+intention);
			}
		}
		else if (intention instanceof DefendIntention)
		{
			List<CombatAction> result = new ArrayList<CombatAction>();
			DefendAction action = new DefendAction();
			Maze.log(Log.DEBUG, action.toString());
			result.add(action);
			return result;
		}
		else if (intention instanceof RunAwayIntention)
		{
			List<CombatAction> result = new ArrayList<CombatAction>();
			RunAwayAction action = new RunAwayAction();
			Maze.log(Log.DEBUG, action.toString());
			result.add(action);
			return result;
		}
		else if (intention instanceof HideIntention)
		{
			List<CombatAction> result = new ArrayList<CombatAction>();
			HideAction action = new HideAction();
			Maze.log(Log.DEBUG, action.toString());
			result.add(action);
			return result;
		}
		else
		{
			// todo: probably this masks a few bugs
			List<CombatAction> result = new ArrayList<CombatAction>();
			CombatAction action = CombatAction.DO_NOTHING;
			Maze.log(Log.DEBUG, "!possible do-nothing bug here");
			Maze.log(Log.DEBUG, action.toString());
			result.add(action);
			return result;
//			throw new MazeException("Unrecognised combat intention: "+intention);
		}
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
	public void addAllies(List<FoeGroup> foeGroups)
	{
		Maze.getInstance().addFoeAllies(foeGroups);
		if (this.combatantData != null)
		{
			this.combatantData.setSummonedGroup(foeGroups);
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean isActiveModifier(String modifier)
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<AttackWith> getAttackWithOptions()
	{
		ArrayList<AttackWith> result = new ArrayList<AttackWith>();
		result.addAll(template.getNaturalWeapons());
		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<SpellLikeAbility> getSpellLikeAbilities()
	{
		return template.getSpellLikeAbilities();
	}

	/*-------------------------------------------------------------------------*/
	public PercentageTable<FoeAttack> getAttacks()
	{
		return this.template.attacks;
	}
	
	/*-------------------------------------------------------------------------*/
	public PercentageTable<BodyPart> getBodyParts()
	{
		return this.template.bodyParts;
	}
	
	/*-------------------------------------------------------------------------*/
	public PercentageTable<String> getPlayerBodyParts()
	{
		return this.template.playerBodyParts;
	}

	/*-------------------------------------------------------------------------*/
	public Item getArmour(BodyPart bodyPart)
	{
		// foes are only treated as having natural armour, on their BodyParts
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public void deductAmmo(AttackEvent event)
	{
		// Foes have no ammo restrictions ;-)
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return template.name;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isNpc()
	{
		return template.isNpc;
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
		return this.template.baseTexture;
	}
	
	/*-------------------------------------------------------------------------*/
	public MazeTexture getMeleeAttackTexture()
	{
		return this.template.meleeAttackTexture;
	}

	/*-------------------------------------------------------------------------*/
	public MazeTexture getRangedAttackTexture()
	{
		return this.template.rangedAttackTexture;
	}

	/*-------------------------------------------------------------------------*/
	public MazeTexture getCastSpellTexture()
	{
		return this.template.castSpellTexture;
	}

	/*-------------------------------------------------------------------------*/
	public MazeTexture getSpecialAbilityTexture()
	{
		return this.template.specialAbilityTexture;
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
		return template.pluralName;
	}

	public String getUnidentifiedName()
	{
		return template.unidentifiedName;
	}

	public String getUnidentifiedPluralName()
	{
		return template.unidentifiedPluralName;
	}

	public LootTable getLootTable()
	{
		return template.loot;
	}

	public int getExperience()
	{
		return template.experience;
	}

	public boolean cannotBeEvaded()
	{
		return template.cannotBeEvaded;
	}

	public int getIdentificationDifficulty()
	{
		return template.identificationDifficulty;
	}

	public String getType()
	{
		return template.type;
	}

	public StatModifier getAllFoesBannerModifiers()
	{
		return template.allFoesBannerModifiers;
	}

	public StatModifier getFoeGroupBannerModifiers()
	{
		return template.foeGroupBannerModifiers;
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
		return template.faction;
	}
	
	public MazeScript getAppearanceScript()
	{
		return template.appearanceScript;
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
		List<FoeAttack> items = getAttacks().getItems();

		for (FoeAttack fa : items)
		{
			if (isLegalAttack(fa, engagementRange))
			{
				return true;
			}
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isLegalAttack(FoeAttack fa, int engagementRange)
	{
		return fa.getMinRange() <= engagementRange &&
			fa.getMaxRange() >= engagementRange;
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
