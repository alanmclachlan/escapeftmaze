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
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.game.Launcher;
import mclachlan.maze.game.Maze;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class SwingEditor extends JFrame implements WindowListener
{
	public static SwingEditor instance;
	JLabel status;
	JTabbedPane tabs, staticDataTabs, dynamicDataTabs;
	BitSet dirty = new BitSet();
	List<IEditorPanel> editorPanels = new ArrayList<IEditorPanel>();

	private Map<String, String> config;
	private List<Campaign> campaigns;
	private Campaign currentCampaign;

	// editor panels
	private GenderPanel genderPanel;
	private BodyPartPanel bodyPartPanel;
	private ExperienceTablePanel experienceTablePanel;
	private CharacterClassPanel characterClassesPanel;
	private AttackTypePanel attackTypePanel;
	private ConditionEffectPanel conditionEffectPanel;
	private ConditionTemplatePanel conditionTemplatePanel;
	private SpellEffectPanel spellEffectPanel;
	private LootEntryPanel lootEntryPanel;
	private LootTablePanel lootTablePanel;
	private MazeScriptPanel mazeScriptPanel;
	private SpellPanel spellPanel;
	private RacePanel racePanel;
	private MazeTexturePanel mazeTexturePanel;
	private FoeAttackPanel foeAttackPanel;
	private NaturalWeaponsPanel naturalWeaponsPanel;
	private FoeTemplatePanel foeTemplatePanel;
	private TrapsPanel trapsPanel;
	private FoeEntryPanel foeEntryPanel;
	private EncounterTablePanel encounterTablePanel;
	private NpcFactionTemplatePanel npcFactionTemplatePanel;
	private WieldingComboPanel wieldingComboPanel;
	private ItemTemplatePanel itemTemplatePanel;
	private CampaignEditorPanel campaignEditorPanel;
	private DifficultyLevelPanel difficultyLevelPanel;
	private NpcTemplatePanel npcTemplatePanel;
	private CraftRecipePanel craftRecipePanel;
	private ItemEnchantmentsPanel itemEnchantmentsPanel;
	private StartingKitsPanel startingKitsPanel;
	private ZonePanel zonePanel;

	private GuildPanel guildPanel;
	private List<SaveGamePanel> saveGamePanels;

	/*-------------------------------------------------------------------------*/
	public SwingEditor() throws Exception
	{
		super("Mazemaster");
		instance = this;
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setIconImage(ImageIO.read(new File("maze.png")));

		config = Launcher.getConfig();
		campaigns = Launcher.loadCampaigns();

		//
		// if the maze.campaign config property is set, launch straight into
		// that one, otherwise load the default campaign
		//

		if (config.get(Maze.AppConfig.CAMPAIGN) != null)
		{
			String campaign = config.get(Maze.AppConfig.CAMPAIGN);
			for (Campaign c : campaigns)
			{
				if (c.getName().equals(campaign))
				{
					currentCampaign = c;
					break;
				}
			}
		}
		else
		{
			currentCampaign = Maze.getStubCampaign();
		}

		initDatabase(currentCampaign);

		EditingControls c = new EditingControls(this);
		JMenuBar menuBar = c.buildMenuBar();
		JPanel bottom = c.getBottomPanel();
		status = c.getStatus();

		staticDataTabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);

		addStaticDataTab("Campaign", getCampaignPanel());
		addStaticDataTab("Difficulty Levels", getDifficultyLevelPanel());
		addStaticDataTab("Genders", getGenderPanel());
		addStaticDataTab("Body Parts", getBodyPartPanel());
		addStaticDataTab("Experience Tables", getExperienceTablePanel());
		addStaticDataTab("Starting Kits", getStartingKitsPanel());
		addStaticDataTab("Character Classes", getCharacterClassesPanel());
		addStaticDataTab("Personalities", getPersonalitiesPanel());
		addStaticDataTab("Attack Types", getAttackTypesPanel());
		addStaticDataTab("Condition Effects", getConditionEffectsPanel());
		addStaticDataTab("Condition Templates", getConditionTemplatesPanel());
		addStaticDataTab("Spell Effects", getSpellEffectsPanel());
		addStaticDataTab("Loot Entries", getLootEntriesPanel());
		addStaticDataTab("Loot Tables", getLootTablesPanel());
		addStaticDataTab("Maze Scripts", getMazeScriptPanel());
		addStaticDataTab("Spells", getSpellsPanel());
		addStaticDataTab("Player Spell Books", getPlayerSpellBooksPanel());
		addStaticDataTab("Races", getRacesPanel());
		addStaticDataTab("Maze Textures", getMazeTexturesPanel());
		addStaticDataTab("Foe Attacks", getFoeAttacksPanel());
		addStaticDataTab("Natural Weapons", getNaturalWeaponsPanel());
		addStaticDataTab("Foe Templates", getFoeTemplatesPanel());
		addStaticDataTab("Traps", getTrapsPanel());
		addStaticDataTab("Foe Entries", getFoeEntriesPanel());
		addStaticDataTab("Encounter Tables", getEncounterTablesPanel());
		addStaticDataTab("Npc Faction Templates", getNpcFactionTemplatesPanel());
		addStaticDataTab("Npc Templates", getNpcTemplatesPanel());
		addStaticDataTab("Wielding Combos", getWieldingCombosPanel());
		addStaticDataTab("Item Templates", getItemTemplatesPanel());
		addStaticDataTab("Craft Recipes", getCraftRecipePanel());
		addStaticDataTab("Item Enchantments", getItemEnchantmentsPanel());
		addStaticDataTab("Zones", getZonesPanel());

		campaignEditorPanel.initForeignKeys();
		campaignEditorPanel.refresh(currentCampaign);
		
		dynamicDataTabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);
		
		addDynamicDataTab("Guild", getGuildPanel());
		
		saveGamePanels = new ArrayList<SaveGamePanel>();
		List<String> saves = Database.getInstance().getLoader().getSaveGames();
		for (String s : saves)
		{
			SaveGamePanel sgp = new SaveGamePanel(s);
			addDynamicDataTab(s, sgp);
			sgp.initForeignKeys();
			sgp.refresh();
			saveGamePanels.add(sgp);
		}

		this.setJMenuBar(menuBar);

		this.setLayout(new BorderLayout(5,5));
		
		tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		
		tabs.add("Static Data", staticDataTabs);
		tabs.add("Save Games and Guild Files", dynamicDataTabs);

		this.add(tabs, BorderLayout.CENTER);
		this.add(bottom, BorderLayout.SOUTH);

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)(d.getWidth()/2);
		int centerY = (int)(d.getHeight()/2);
		int width = (int)(d.getWidth()-200);
		int height = (int)(d.getHeight()-200);

		addWindowListener(this);
		this.setBounds(centerX-width/2, centerY-height/2, width, height);
		this.setVisible(true);
	}

	/*-------------------------------------------------------------------------*/
	private Component getStartingKitsPanel()
	{
		startingKitsPanel = new StartingKitsPanel();
		return startingKitsPanel;
	}

	/*-------------------------------------------------------------------------*/
	private Component getGuildPanel()
	{
		guildPanel = new GuildPanel();
		return guildPanel;
	}

	/*-------------------------------------------------------------------------*/
	private void addStaticDataTab(String title, Component panel)
	{
		staticDataTabs.addTab(title, panel);
		if (panel instanceof IEditorPanel)
		{
			this.editorPanels.add((IEditorPanel)panel);
		}
	}
	
	/*-------------------------------------------------------------------------*/
	private void addDynamicDataTab(String title, Component panel)
	{
		dynamicDataTabs.addTab(title, panel);
		if (panel instanceof IEditorPanel)
		{
			this.editorPanels.add((EditorPanel)panel);
		}
		else if (panel instanceof SaveGamePanel)
		{
			SaveGamePanel sgp = (SaveGamePanel)panel;
			this.editorPanels.add(sgp.getSaveGamePlayerCharacterPanel());
			this.editorPanels.add(sgp.getNpcFactionPanel());
		}
	}

	/*-------------------------------------------------------------------------*/
	private Component getCampaignPanel()
	{
		campaignEditorPanel = new CampaignEditorPanel();
		return campaignEditorPanel;
	}

	public Component getDifficultyLevelPanel()
	{
		difficultyLevelPanel = new DifficultyLevelPanel();
		return difficultyLevelPanel;
	}

	private EditorPanel getZonesPanel()
	{
		zonePanel = new ZonePanel();
		return zonePanel;
	}

	private EditorPanel getItemTemplatesPanel()
	{
		itemTemplatePanel = new ItemTemplatePanel();
		return itemTemplatePanel;
	}

	private EditorPanel getWieldingCombosPanel()
	{
		wieldingComboPanel = new WieldingComboPanel();
		return wieldingComboPanel;
	}

	private EditorPanel getNpcTemplatesPanel()
	{
		npcTemplatePanel = new NpcTemplatePanel();
		return npcTemplatePanel;
	}

	private EditorPanel getNpcFactionTemplatesPanel()
	{
		npcFactionTemplatePanel = new NpcFactionTemplatePanel();
		return npcFactionTemplatePanel;
	}

	private EditorPanel getEncounterTablesPanel()
	{
		encounterTablePanel = new EncounterTablePanel();
		return encounterTablePanel;
	}

	private EditorPanel getCraftRecipePanel()
	{
		craftRecipePanel = new CraftRecipePanel();
		return craftRecipePanel;
	}

	private EditorPanel getItemEnchantmentsPanel()
	{
		itemEnchantmentsPanel = new ItemEnchantmentsPanel();
		return itemEnchantmentsPanel;
	}

	private EditorPanel getPersonalitiesPanel()
	{
		return new PersonalitiesPanel();
	}

	private EditorPanel getFoeEntriesPanel()
	{
		foeEntryPanel = new FoeEntryPanel();
		return foeEntryPanel;
	}

	private EditorPanel getTrapsPanel()
	{
		trapsPanel = new TrapsPanel();
		return trapsPanel;
	}

	private EditorPanel getFoeTemplatesPanel()
	{
		foeTemplatePanel = new FoeTemplatePanel();
		return foeTemplatePanel;
	}

	private EditorPanel getFoeAttacksPanel()
	{
		foeAttackPanel = new FoeAttackPanel();
		return foeAttackPanel;
	}

	private EditorPanel getNaturalWeaponsPanel()
	{
		naturalWeaponsPanel = new NaturalWeaponsPanel();
		return naturalWeaponsPanel;
	}

	private EditorPanel getMazeTexturesPanel()
	{
		mazeTexturePanel = new MazeTexturePanel();
		return mazeTexturePanel;
	}

	private EditorPanel getRacesPanel()
	{
		racePanel = new RacePanel();
		return racePanel;
	}

	private EditorPanel getSpellsPanel()
	{
		spellPanel = new SpellPanel();
		return spellPanel;
	}

	private EditorPanel getPlayerSpellBooksPanel()
	{
		return new PlayerSpellBooksPanel();
	}

	private EditorPanel getMazeScriptPanel()
	{
		mazeScriptPanel = new MazeScriptPanel();
		return mazeScriptPanel;
	}

	private EditorPanel getLootTablesPanel()
	{
		lootTablePanel = new LootTablePanel();
		return lootTablePanel;
	}

	private EditorPanel getLootEntriesPanel()
	{
		lootEntryPanel = new LootEntryPanel();
		return lootEntryPanel;
	}

	private EditorPanel getSpellEffectsPanel()
	{
		spellEffectPanel = new SpellEffectPanel();
		return spellEffectPanel;
	}

	private EditorPanel getConditionTemplatesPanel()
	{
		conditionTemplatePanel = new ConditionTemplatePanel();
		return conditionTemplatePanel;
	}

	private EditorPanel getConditionEffectsPanel()
	{
		conditionEffectPanel = new ConditionEffectPanel();
		return conditionEffectPanel;
	}

	private EditorPanel getAttackTypesPanel()
	{
		attackTypePanel = new AttackTypePanel();
		return attackTypePanel;
	}

	private EditorPanel getCharacterClassesPanel()
	{
		characterClassesPanel = new CharacterClassPanel();
		return characterClassesPanel;
	}

	private EditorPanel getExperienceTablePanel()
	{
		experienceTablePanel = new ExperienceTablePanel();
		return experienceTablePanel;
	}

	EditorPanel getBodyPartPanel()
	{
		bodyPartPanel = new BodyPartPanel();
		return bodyPartPanel;
	}

	EditorPanel getGenderPanel()
	{
		genderPanel = new GenderPanel();
		return genderPanel;
	}

	/*-------------------------------------------------------------------------*/
	static void initDatabase(Campaign campaign)
		throws Exception
	{
		V1Loader loader = new V1Loader();
		V1Saver saver = new V1Saver();
		new Database(loader, saver);
		loader.init(campaign);
		saver.init(campaign);
	}

	/*-------------------------------------------------------------------------*/
	public void setDirty(int tab)
	{
		if (tab < 0)
		{
			return;
		}
		
		dirty.set(tab);

		setDirtyStatusMessage();
	}

	/*-------------------------------------------------------------------------*/
	private void setDirtyStatusMessage()
	{
		if (dirty.isEmpty())
		{
			status.setText("");
			return;
		}

		String message = "Dirty: ";
		for (int i=0; i<dirty.size(); i++)
		{
			if (dirty.get(i))
			{
				message += Tab.valueOf(i);

				if (i < dirty.size()-1)
				{
					message += ", ";
				}
			}
		}

		status.setText(message);
	}

	/*-------------------------------------------------------------------------*/
	public void clearDirty(int tab)
	{
		dirty.clear(tab);
		setDirtyStatusMessage();
	}
	
	/*-------------------------------------------------------------------------*/
	public boolean isDirty(int tab)
	{
		return dirty.get(tab);
	}

	/*-------------------------------------------------------------------------*/
	private void changeCampaign(Campaign c)
	{
		if (!dirty.isEmpty())
		{
			int option = JOptionPane.showConfirmDialog(
				this, "Lose unsaved changes?", "Change Campaign", JOptionPane.YES_NO_OPTION);
			if (option == JOptionPane.NO_OPTION)
			{
				return;
			}
		}

		try
		{
			discardChanges();
			currentCampaign = c;
			initDatabase(c);
			reloadAll();
			dirty.clear();
			setDirtyStatusMessage();

			campaignEditorPanel.initForeignKeys();
			campaignEditorPanel.refresh(currentCampaign);
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void saveChanges() throws Exception
	{
		commitAll();

		// save dirty changes to the database
		
		// static data
		if (dirty.get(Tab.GENDER)) Database.getInstance().getSaver().saveGenders(Database.getInstance().getGenders());
		if (dirty.get(Tab.BODY_PART)) Database.getInstance().getSaver().saveBodyParts(Database.getInstance().getBodyParts());
		if (dirty.get(Tab.EXPERIENCE_TABLE)) Database.getInstance().getSaver().saveExperienceTables(Database.getInstance().getExperienceTables());
		if (dirty.get(Tab.CHARACTER_CLASSES)) Database.getInstance().getSaver().saveCharacterClasses(Database.getInstance().getCharacterClasses());
		if (dirty.get(Tab.ATTACK_TYPES)) Database.getInstance().getSaver().saveAttackTypes(Database.getInstance().getAttackTypes());
		if (dirty.get(Tab.CONDITION_EFFECTS)) Database.getInstance().getSaver().saveConditionEffects(Database.getInstance().getConditionEffects());
		if (dirty.get(Tab.CONDITION_TEMPLATES)) Database.getInstance().getSaver().saveConditionTemplates(Database.getInstance().getConditionTemplates());
		if (dirty.get(Tab.SPELL_EFFECTS)) Database.getInstance().getSaver().saveSpellEffects(Database.getInstance().getSpellEffects());
		if (dirty.get(Tab.LOOT_ENTRIES)) Database.getInstance().getSaver().saveLootEntries(Database.getInstance().getLootEntries());
		if (dirty.get(Tab.LOOT_TABLES)) Database.getInstance().getSaver().saveLootTables(Database.getInstance().getLootTables());
		if (dirty.get(Tab.SCRIPTS)) Database.getInstance().getSaver().saveMazeScripts(Database.getInstance().getMazeScripts());
		if (dirty.get(Tab.SPELLS)) Database.getInstance().getSaver().saveSpells(Database.getInstance().getSpells());
		if (dirty.get(Tab.PLAYER_SPELL_BOOKS)) Database.getInstance().getSaver().savePlayerSpellBooks(Database.getInstance().getPlayerSpellBooks());
		if (dirty.get(Tab.RACES)) Database.getInstance().getSaver().saveRaces(Database.getInstance().getRaces());
		if (dirty.get(Tab.TEXTURES)) Database.getInstance().getSaver().saveMazeTextures(Database.getInstance().getMazeTextures());
		if (dirty.get(Tab.FOE_ATTACKS)) Database.getInstance().getSaver().saveFoeAttacks(Database.getInstance().getFoeAttacks());
		if (dirty.get(Tab.FOE_TEMPLATES)) Database.getInstance().getSaver().saveFoeTemplates(Database.getInstance().getFoeTemplates());
		if (dirty.get(Tab.TRAPS)) Database.getInstance().getSaver().saveTraps(Database.getInstance().getTraps());
		if (dirty.get(Tab.FOE_ENTRIES)) Database.getInstance().getSaver().saveFoeEntries(Database.getInstance().getFoeEntries());
		if (dirty.get(Tab.ENCOUNTER_TABLES)) Database.getInstance().getSaver().saveEncounterTables(Database.getInstance().getEncounterTables());
		if (dirty.get(Tab.NPC_FACTION_TEMPLATES)) Database.getInstance().getSaver().saveNpcFactionTemplates(Database.getInstance().getNpcFactionTemplates());
		if (dirty.get(Tab.NPC_TEMPLATES)) Database.getInstance().getSaver().saveNpcTemplates(Database.getInstance().getNpcTemplates());
		if (dirty.get(Tab.WIELDING_COMBOS)) Database.getInstance().getSaver().saveWieldingCombos(Database.getInstance().getWieldingCombos());
		if (dirty.get(Tab.ITEM_TEMPLATES)) Database.getInstance().getSaver().saveItemTemplates(Database.getInstance().getItemTemplates());
		if (dirty.get(Tab.DIFFICULTY_LEVELS)) Database.getInstance().getSaver().saveDifficultyLevels(Database.getInstance().getDifficultyLevels());
		if (dirty.get(Tab.CRAFT_RECIPES)) Database.getInstance().getSaver().saveCraftRecipes(Database.getInstance().getCraftRecipes());
		if (dirty.get(Tab.ITEM_ENCHANTMENTS)) Database.getInstance().getSaver().saveItemEnchantments(Database.getInstance().getItemEnchantments());
		if (dirty.get(Tab.NATURAL_WEAPONS)) Database.getInstance().getSaver().saveNaturalWeapons(Database.getInstance().getNaturalWeapons());
		if (dirty.get(Tab.STARTING_KITS)) Database.getInstance().getSaver().saveStartingKits(Database.getInstance().getStartingKits());
		if (dirty.get(Tab.PERSONALITIES)) Database.getInstance().getSaver().savePersonalities(Database.getInstance().getPersonalities());
		// zones are not cached
		
		// dynamic data
		if (dirty.get(Tab.GUILD)) Database.getInstance().getSaver().saveCharacterGuild(
			Database.getInstance().getCharacterGuild());

		if (dirty.get(Tab.SAVE_GAMES))
		{
			for (SaveGamePanel sgp : saveGamePanels)
			{
				sgp.save();
			}
		}
		
		// update foreign keys
		for (IEditorPanel editor : editorPanels)
		{
			editor.initForeignKeys();
			// that will have reset all the combo boxes, so refresh the view
			editor.refresh(editor.getCurrentName());

			if (editor == campaignEditorPanel)
			{
				campaignEditorPanel.refresh(currentCampaign);
			}
		}

		for (SaveGamePanel sgp : saveGamePanels)
		{
			sgp.initForeignKeys();
			sgp.refresh();
		}
//		campaignEditorPanel.commit();
//		campaignEditorPanel.initForeignKeys();
//		campaignEditorPanel.refresh(campaignEditorPanel.currentCampaign);
	}

	/*-------------------------------------------------------------------------*/
	public void saveAllChanges() throws Exception
	{
		commitAll();

		// save all changes to the database

		// static data
		Database.getInstance().getSaver().saveGenders(Database.getInstance().getGenders());
		Database.getInstance().getSaver().saveBodyParts(Database.getInstance().getBodyParts());
		Database.getInstance().getSaver().saveExperienceTables(Database.getInstance().getExperienceTables());
		Database.getInstance().getSaver().saveCharacterClasses(Database.getInstance().getCharacterClasses());
		Database.getInstance().getSaver().saveAttackTypes(Database.getInstance().getAttackTypes());
		Database.getInstance().getSaver().saveConditionEffects(Database.getInstance().getConditionEffects());
		Database.getInstance().getSaver().saveConditionTemplates(Database.getInstance().getConditionTemplates());
		Database.getInstance().getSaver().saveSpellEffects(Database.getInstance().getSpellEffects());
		Database.getInstance().getSaver().saveLootEntries(Database.getInstance().getLootEntries());
		Database.getInstance().getSaver().saveLootTables(Database.getInstance().getLootTables());
		Database.getInstance().getSaver().saveMazeScripts(Database.getInstance().getMazeScripts());
		Database.getInstance().getSaver().saveSpells(Database.getInstance().getSpells());
		Database.getInstance().getSaver().savePlayerSpellBooks(Database.getInstance().getPlayerSpellBooks());
		Database.getInstance().getSaver().saveRaces(Database.getInstance().getRaces());
		Database.getInstance().getSaver().saveMazeTextures(Database.getInstance().getMazeTextures());
		Database.getInstance().getSaver().saveFoeAttacks(Database.getInstance().getFoeAttacks());
		Database.getInstance().getSaver().saveFoeTemplates(Database.getInstance().getFoeTemplates());
		Database.getInstance().getSaver().saveTraps(Database.getInstance().getTraps());
		Database.getInstance().getSaver().saveFoeEntries(Database.getInstance().getFoeEntries());
		Database.getInstance().getSaver().saveEncounterTables(Database.getInstance().getEncounterTables());
		Database.getInstance().getSaver().saveNpcFactionTemplates(Database.getInstance().getNpcFactionTemplates());
		Database.getInstance().getSaver().saveNpcTemplates(Database.getInstance().getNpcTemplates());
		Database.getInstance().getSaver().saveWieldingCombos(Database.getInstance().getWieldingCombos());
		Database.getInstance().getSaver().saveItemTemplates(Database.getInstance().getItemTemplates());
		Database.getInstance().getSaver().saveDifficultyLevels(Database.getInstance().getDifficultyLevels());
		Database.getInstance().getSaver().saveCraftRecipes(Database.getInstance().getCraftRecipes());
		Database.getInstance().getSaver().saveItemEnchantments(Database.getInstance().getItemEnchantments());
		Database.getInstance().getSaver().savePersonalities(Database.getInstance().getPersonalities());
		Database.getInstance().getSaver().saveNaturalWeapons(Database.getInstance().getNaturalWeapons());
		Database.getInstance().getSaver().saveStartingKits(Database.getInstance().getStartingKits());
		// zones are not cached

		// dynamic data
		Database.getInstance().getSaver().saveCharacterGuild(
			Database.getInstance().getCharacterGuild());

		for (SaveGamePanel sgp : saveGamePanels)
		{
			sgp.save();
		}

		// update foreign keys
		for (IEditorPanel editor : editorPanels)
		{
			editor.initForeignKeys();
			// that will have reset all the combo boxes, so refresh the view
			editor.refresh(editor.getCurrentName());
			if (editor == campaignEditorPanel)
			{
				campaignEditorPanel.refresh(currentCampaign);
			}
		}

		for (SaveGamePanel sgp : saveGamePanels)
		{
			sgp.initForeignKeys();
			sgp.refresh();
		}
//		campaignEditorPanel.commit();
//		campaignEditorPanel.initForeignKeys();
//		campaignEditorPanel.refresh(campaignEditorPanel.currentCampaign);
	}

	/*-------------------------------------------------------------------------*/
	public void discardChanges() throws Exception
	{
		initDatabase(currentCampaign);
		reloadAll();
	}

	/*-------------------------------------------------------------------------*/
	private void commitAll()
	{
		for (IEditorPanel editor : editorPanels)
		{
			editor.commit(editor.getCurrentName());
		}
		
		campaignEditorPanel.commit();
	}

	/*-------------------------------------------------------------------------*/
	private void reloadAll()
	{
		for (IEditorPanel editor : editorPanels)
		{
			editor.reload();
		}
	}

	/*-------------------------------------------------------------------------*/
	private void clearDirtyStatus()
	{
		dirty.clear();
		setDirtyStatusMessage();
	}

	/*-------------------------------------------------------------------------*/
	private void exit()
	{
		if (dirty.isEmpty())
		{
			// don't ask
			System.exit(0);
		}

		int option = JOptionPane.showConfirmDialog(
			this, "Exit without saving?", "Exit", JOptionPane.YES_NO_OPTION);
		if (option == JOptionPane.YES_OPTION)
		{
			System.exit(0);
		}
	}

	/*-------------------------------------------------------------------------*/
	public IEditorPanel getEditorPanel()
	{
		return (IEditorPanel)staticDataTabs.getSelectedComponent();
	}

	/*-------------------------------------------------------------------------*/
	public void refreshEditorPanel(IEditorPanel other)
	{
		for (IEditorPanel p : editorPanels)
		{
			if (p.getClass() == other.getClass())
			{
				p.refreshNames(p.getCurrentName());
			}
		}
	}

	public void windowOpened(WindowEvent e)
	{
	}

	public void windowClosing(WindowEvent e)
	{
		exit();
	}

	public void windowClosed(WindowEvent e)
	{
	}

	public void windowIconified(WindowEvent e)
	{
	}

	public void windowDeiconified(WindowEvent e)
	{
	}

	public void windowActivated(WindowEvent e)
	{
	}

	public void windowDeactivated(WindowEvent e)
	{
	}

	/*-------------------------------------------------------------------------*/
	public static class Tab
	{
		public static final int CAMPAIGN = 0;
		public static final int GENDER = 1;
		public static final int BODY_PART = 2;
		public static final int EXPERIENCE_TABLE = 3;
		public static final int CHARACTER_CLASSES = 4;
		// retainer classes removed
		public static final int ATTACK_TYPES = 6;
		public static final int CONDITION_EFFECTS = 7;
		public static final int CONDITION_TEMPLATES = 8;
		public static final int SPELL_EFFECTS = 9;
		public static final int LOOT_ENTRIES = 10;
		public static final int LOOT_TABLES = 11;
		public static final int SCRIPTS = 12;
		public static final int SPELLS = 13;
		public static final int PLAYER_SPELL_BOOKS = 14;
		public static final int RACES = 15;
		public static final int TEXTURES = 16;
		public static final int FOE_ATTACKS = 17;
		public static final int FOE_TEMPLATES = 18;
		public static final int TRAPS = 19;
		public static final int FOE_ENTRIES = 20;
		public static final int ENCOUNTER_TABLES = 21;
		public static final int NPC_FACTION_TEMPLATES = 22;
		public static final int NPC_TEMPLATES = 23;
		public static final int WIELDING_COMBOS = 24;
		public static final int ITEM_TEMPLATES = 25;
		public static final int ZONES = 26;
		public static final int GUILD = 27;
		// retainer guild removed
		public static final int SAVE_GAMES = 29;

		public static final int DIFFICULTY_LEVELS = 30;
		public static final int CRAFT_RECIPES = 31;

		public static final int ITEM_ENCHANTMENTS = 32;
		public static final int PERSONALITIES = 33;
		public static final int NATURAL_WEAPONS = 34;
		public static final int STARTING_KITS = 35;

		public static String valueOf(int tab)
		{
			switch (tab)
			{
				case CAMPAIGN: return "campaign";
				case GENDER: return "gender";
				case BODY_PART: return "body parts";
				case EXPERIENCE_TABLE: return "experience table";
				case CHARACTER_CLASSES: return "character classes";
				case ATTACK_TYPES: return "attack types";
				case CONDITION_EFFECTS: return "condition effects";
				case CONDITION_TEMPLATES: return "condition templates";
				case SPELL_EFFECTS: return "spell effects";
				case LOOT_ENTRIES: return "loot entries";
				case LOOT_TABLES: return "loot tables";
				case SCRIPTS: return "scripts";
				case SPELLS: return "spells";
				case PLAYER_SPELL_BOOKS: return "player spell books";
				case RACES: return "races";
				case TEXTURES: return "textures";
				case FOE_ATTACKS: return "foe attacks";
				case FOE_TEMPLATES: return "foe templates";
				case TRAPS: return "traps";
				case FOE_ENTRIES: return "foe entries";
				case ENCOUNTER_TABLES: return "encounter tables";
				case NPC_FACTION_TEMPLATES: return "npc faction templates";
				case NPC_TEMPLATES: return "npc templates";
				case WIELDING_COMBOS: return "wielding combos";
				case ITEM_TEMPLATES: return "item templates";
				case ZONES: return "zones";
				case GUILD: return "character guild";
				case SAVE_GAMES: return "save games";
				case DIFFICULTY_LEVELS: return "difficulty levels";
				case CRAFT_RECIPES: return "craft recipes";
				case ITEM_ENCHANTMENTS: return "item enchantments";
				case PERSONALITIES: return "personalities";
				case NATURAL_WEAPONS: return "natural weapons";
				case STARTING_KITS: return "starting kits";
				default: throw new MazeException("invalid tab "+tab);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public static class EditingControls implements ActionListener
	{
		SwingEditor parent;

		JMenuBar menuBar;
		JLabel status;
		JButton apply, applyAll, discard, exit, newItem,
			copyItem, renameItem, deleteItem;
		JMenuItem newMenuItem, copyMenuItem, renameMenuItem, deleteMenuItem,
			applyMenuItem, applyAllMenuItem, discardMenuItem, exitMenuItem,
			changeCampaignMenuItem, aboutMenuItem;

		/*----------------------------------------------------------------------*/
		public EditingControls(SwingEditor parent)
		{
			this.parent = parent;
		}

		/*----------------------------------------------------------------------*/
		public JPanel getBottomPanel()
		{
			apply = new JButton("Apply");
			apply.addActionListener(this);
			apply.setMnemonic(KeyEvent.VK_A);
			applyAll = new JButton("Apply All");
			applyAll.addActionListener(this);
			discard = new JButton("Discard");
			discard.addActionListener(this);
			discard.setMnemonic(KeyEvent.VK_I);
			exit = new JButton("Exit");
			exit.addActionListener(this);
			exit.setMnemonic(KeyEvent.VK_E);

			newItem = new JButton("New");
			newItem.addActionListener(this);
			newItem.setMnemonic(KeyEvent.VK_N);
			copyItem = new JButton("Copy");
			copyItem.addActionListener(this);
			copyItem.setMnemonic(KeyEvent.VK_C);
			renameItem = new JButton("Rename");
			renameItem.addActionListener(this);
			renameItem.setMnemonic(KeyEvent.VK_R);
			deleteItem = new JButton("Delete");
			deleteItem.addActionListener(this);
			deleteItem.setMnemonic(KeyEvent.VK_D);

			JPanel bottom = new JPanel(new GridLayout(2, 1));

			status = new JLabel();
			bottom.add(status);

			JPanel buttonPanel = new JPanel(new FlowLayout());
			buttonPanel.add(newItem);
			buttonPanel.add(copyItem);
			buttonPanel.add(renameItem);
			buttonPanel.add(deleteItem);
			buttonPanel.add(apply);
			buttonPanel.add(applyAll);
			buttonPanel.add(discard);
			buttonPanel.add(exit);
			bottom.add(buttonPanel);
			return bottom;
		}

		/*----------------------------------------------------------------------*/
		public JMenuBar buildMenuBar()
		{
			JMenuBar menuBar = new JMenuBar();
			JMenu fileMenu = new JMenu("File");
			fileMenu.setMnemonic(KeyEvent.VK_F);
			newMenuItem = new JMenuItem("New...");
			newMenuItem.addActionListener(this);
			newMenuItem.setMnemonic(KeyEvent.VK_N);
			copyMenuItem = new JMenuItem("Copy...");
			copyMenuItem.addActionListener(this);
			copyMenuItem.setMnemonic(KeyEvent.VK_C);
			renameMenuItem = new JMenuItem("Rename...");
			renameMenuItem.addActionListener(this);
			renameMenuItem.setMnemonic(KeyEvent.VK_R);
			deleteMenuItem = new JMenuItem("Delete");
			deleteMenuItem.addActionListener(this);
			deleteMenuItem.setMnemonic(KeyEvent.VK_D);
			applyMenuItem = new JMenuItem("Apply");
			applyMenuItem.addActionListener(this);
			applyMenuItem.setMnemonic(KeyEvent.VK_A);
			applyAllMenuItem = new JMenuItem("Apply All");
			applyAllMenuItem.addActionListener(this);
			discardMenuItem = new JMenuItem("Discard");
			discardMenuItem.addActionListener(this);
			discardMenuItem.setMnemonic(KeyEvent.VK_I);
			exitMenuItem = new JMenuItem("Exit");
			exitMenuItem.addActionListener(this);
			exitMenuItem.setMnemonic(KeyEvent.VK_E);

			fileMenu.add(newMenuItem);
			fileMenu.add(copyMenuItem);
			fileMenu.add(renameMenuItem);
			fileMenu.add(deleteMenuItem);
			fileMenu.addSeparator();
			fileMenu.add(applyMenuItem);
			fileMenu.add(applyAllMenuItem);
			fileMenu.add(discardMenuItem);
			fileMenu.addSeparator();
			fileMenu.add(exitMenuItem);

			JMenu campaignMenu = new JMenu("Campaign");
			campaignMenu.setMnemonic(KeyEvent.VK_M);
			changeCampaignMenuItem = new JMenuItem("Change Campaign...");
			changeCampaignMenuItem.addActionListener(this);
			changeCampaignMenuItem.setMnemonic(KeyEvent.VK_C);
			campaignMenu.add(changeCampaignMenuItem);

			JMenu helpMenu = new JMenu("Help");
			helpMenu.setMnemonic(KeyEvent.VK_H);
			aboutMenuItem = new JMenuItem("About");
			aboutMenuItem.addActionListener(this);
			aboutMenuItem.setMnemonic(KeyEvent.VK_A);
			helpMenu.add(aboutMenuItem);

			menuBar.add(fileMenu);
			menuBar.add(campaignMenu);
			menuBar.add(helpMenu);

			this.menuBar = menuBar;
			return menuBar;
		}

		/*-------------------------------------------------------------------------*/
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() == apply || e.getSource() == applyMenuItem)
			{
				IEditorPanel panel = getEditorPanel();
				panel.commit(panel.getCurrentName());

				int option = JOptionPane.showConfirmDialog(
					parent, "Save changes?", "Apply", JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION)
				{
					try
					{
						parent.saveChanges();
					}
					catch (Exception x)
					{
						throw new MazeException(x);
					}
					parent.clearDirtyStatus();
				}
			}
			if (e.getSource() == applyAll || e.getSource() == applyAllMenuItem)
			{
				IEditorPanel panel = getEditorPanel();
				panel.commit(panel.getCurrentName());
				
				int option = JOptionPane.showConfirmDialog(
					parent, "Save ALL files?", "Apply", JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION)
				{
					try
					{
						parent.saveAllChanges();
					}
					catch (Exception x)
					{
						throw new MazeException(x);
					}
					parent.clearDirtyStatus();
				}
			}
			else if (e.getSource() == discard || e.getSource() == discardMenuItem)
			{
				int option = JOptionPane.showConfirmDialog(
					parent, "Discard all changes?", "Discard", JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION)
				{
					try
					{
						parent.discardChanges();
					}
					catch (Exception e1)
					{
						throw new MazeException(e1);
					}
					parent.clearDirtyStatus();
				}
			}
			else if (e.getSource() == exit || e.getSource() == exitMenuItem)
			{
				exit();
			}
			else if (e.getSource() == newItem || e.getSource() == newMenuItem)
			{
				String name = JOptionPane.showInputDialog(
					parent, "Name:", "New Item", JOptionPane.QUESTION_MESSAGE);

				if (name != null)
				{
					IEditorPanel panel = getEditorPanel();
					if (panel.getCurrentName() != null)
					{
						panel.commit(panel.getCurrentName());
					}
					panel.newItem(name);
					panel.refreshNames(name);
					panel.refresh(name);
					parent.setDirty(panel.getDirtyFlag());
				}
			}
			else if (e.getSource() == renameItem || e.getSource() == renameMenuItem)
			{
				IEditorPanel panel = getEditorPanel();

				String name = (String)JOptionPane.showInputDialog(
					parent, "New Name:", "Rename Item", JOptionPane.QUESTION_MESSAGE,
					null, null, panel.getCurrentName());

				if (name != null)
				{
					panel.commit(panel.getCurrentName());
					panel.renameItem(name);
					panel.refreshNames(name);
					panel.refresh(name);
					parent.setDirty(panel.getDirtyFlag());
				}
			}
			else if (e.getSource() == copyItem || e.getSource() == copyMenuItem)
			{
				IEditorPanel panel = getEditorPanel();

				String name = (String)JOptionPane.showInputDialog(
					parent, "New Name:", "Copy Item", JOptionPane.QUESTION_MESSAGE,
					null, null, panel.getCurrentName());

				if (name != null)
				{
					panel.commit(panel.getCurrentName());
					panel.copyItem(name);
					panel.refreshNames(name);
					panel.refresh(name);
					parent.setDirty(panel.getDirtyFlag());
				}
			}
			else if (e.getSource() == deleteItem || e.getSource() == deleteMenuItem)
			{
				IEditorPanel panel = getEditorPanel();

				panel.commit(panel.getCurrentName());

				int option = JOptionPane.showConfirmDialog(
					parent, "Are you sure?", "Delete Item", JOptionPane.YES_NO_OPTION);

				if (option == JOptionPane.YES_OPTION)
				{
					panel.deleteItem();
					panel.refreshNames(null);
					parent.setDirty(panel.getDirtyFlag());
				}
			}
			else if (e.getSource() == aboutMenuItem)
			{
				JOptionPane.showMessageDialog(
					parent,
					"Mazemaster Editor\n" +
						"version "+parent.config.get(Maze.AppConfig.VERSION) + "\n\n" +
						"Current Campaign: "+parent.currentCampaign.getDisplayName(),
					"Mazemaster",
					JOptionPane.INFORMATION_MESSAGE);
			}
			else if (e.getSource() == changeCampaignMenuItem)
			{
				final JDialog dialog = new JDialog(parent, "Change Campaign", true);
				dialog.setLayout(new BorderLayout(3,3));
				JButton ok = new JButton("OK");
				JButton cancel = new JButton("Cancel");
				JPanel buttonPanel = new JPanel();
				buttonPanel.add(ok);
				buttonPanel.add(cancel);
				final CampaignPanel campaignPanel = new CampaignPanel(parent.campaigns);
				dialog.add(campaignPanel, BorderLayout.CENTER);
				dialog.add(buttonPanel, BorderLayout.SOUTH);
				ok.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						parent.changeCampaign(campaignPanel.getCampaign());
						dialog.setVisible(false);
					}
				});
				cancel.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						dialog.setVisible(false);
					}
				});
				dialog.pack();
				dialog.setLocationRelativeTo(parent);
				dialog.setVisible(true);
			}
		}

		/*----------------------------------------------------------------------*/
		public IEditorPanel getEditorPanel()
		{
			return parent.getEditorPanel();
		}

		/*----------------------------------------------------------------------*/
		public JLabel getStatus()
		{
			return status;
		}

		/*----------------------------------------------------------------------*/
		public void exit()
		{
			parent.exit();
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		new SwingEditor();
	}
}
