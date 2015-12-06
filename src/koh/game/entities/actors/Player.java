package koh.game.entities.actors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import koh.d2o.Couple;
import koh.game.Main;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;

import koh.game.dao.api.AccountDataDAO;
import koh.game.dao.mysql.PlayerDAOImpl;
import koh.game.entities.Account;
import koh.game.entities.ExpLevel;
import koh.game.entities.actors.character.CharacterInventory;
import koh.game.entities.actors.character.FieldNotification;
import koh.game.entities.environments.DofusMap;
import koh.game.network.WorldClient;
import koh.game.network.handlers.game.approach.CharacterHandler;
import koh.game.entities.actors.character.*;
import koh.game.entities.guilds.Guild;
import koh.game.entities.guilds.GuildMember;
import koh.game.entities.spells.LearnableSpell;
import koh.game.fights.Fight;
import koh.game.fights.FightState;
import koh.game.fights.Fighter;
import koh.game.fights.fighters.CharacterFighter;
import koh.game.network.ChatChannel;
import koh.protocol.messages.game.context.roleplay.TeleportOnSameMapMessage;
import koh.game.utils.Observable;
import koh.game.utils.Observer;
import koh.protocol.client.Message;
import koh.protocol.client.enums.AggressableStatusEnum;
import koh.protocol.client.enums.AlignmentSideEnum;
import koh.protocol.client.enums.CompassTypeEnum;
import koh.protocol.client.enums.PlayerStateEnum;
import koh.protocol.client.enums.PlayerStatusEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.atlas.compass.CompassUpdatePartyMemberMessage;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.character.stats.CharacterLevelUpInformationMessage;
import koh.protocol.messages.game.character.stats.CharacterLevelUpMessage;
import koh.protocol.messages.game.character.status.PlayerStatus;
import koh.protocol.messages.game.context.GameContextRefreshEntityLookMessage;
import koh.protocol.messages.game.initialization.CharacterLoadingCompleteMessage;
import koh.protocol.messages.game.context.roleplay.CurrentMapMessage;
import koh.protocol.messages.game.context.roleplay.GameRolePlayShowActorMessage;
import koh.protocol.messages.game.pvp.AlignmentRankUpdateMessage;
import koh.protocol.types.game.context.GameContextActorInformations;
import koh.protocol.types.game.character.ActorRestrictionsInformations;
import koh.protocol.types.game.character.alignment.ActorAlignmentInformations;
import koh.protocol.types.game.character.alignment.ActorExtendedAlignmentInformations;
import koh.protocol.types.game.choice.CharacterBaseInformations;
import koh.protocol.types.game.context.roleplay.BasicGuildInformations;
import koh.protocol.types.game.context.roleplay.GameRolePlayActorInformations;
import koh.protocol.types.game.context.roleplay.GameRolePlayCharacterInformations;
import koh.protocol.types.game.context.roleplay.HumanInformations;
import koh.protocol.types.game.context.roleplay.HumanOption;
import koh.protocol.types.game.context.roleplay.HumanOptionGuild;
import koh.protocol.types.game.context.roleplay.HumanOptionOrnament;
import koh.protocol.types.game.context.roleplay.HumanOptionTitle;
import koh.protocol.types.game.look.EntityLook;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class Player extends IGameActor implements Observer {

    private static final Logger logger = LogManager.getLogger(Player.class);

    public int owner;
    public String nickName;
    public int sexe;
    public byte breed;
    public ArrayList<Short> skins;
    public ArrayList<Integer> indexedColors = new ArrayList<>(5);
    public ArrayList<Short> scales;
    public Account account;
    public int achievementPoints;
    public int level;
    public WorldClient client;
    public long regenStartTime;
    public volatile DofusMap currentMap;
    public ArrayList<Byte> ennabledChannels = new ArrayList<>(20), DisabledChannels;
    public ShortcutBook shortcuts;
    public volatile MountInformations mountInfo;
    public int savedMap;
    public short savedCell;
    public volatile SpellBook mySpells;
    public volatile JobBook myJobs;
    public CharacterInventory inventoryCache;
    public PlayerStatusEnum status = PlayerStatusEnum.PLAYER_STATUS_AVAILABLE;
    public HashMap<ScoreType, Integer> scores = new HashMap<>(7);
    //GenericStats
    /*public int getAP;
     public int getMP;*/
    public int vitality;
    public int wisdom;
    public int strength;
    public int intell;
    public int agility;
    public int chance, life;
    public short activableTitle, activableOrnament;
    public byte regenRate;
    public byte[] emotes;
    public int[] ornaments, titles;

    public GenericStats stats;

    //stats
    public long experience;
    public int kamas, statPoints, spellPoints;
    public byte alignmentValue, alignmentGrade, PvPEnabled;
    public AlignmentSideEnum alignmentSide = AlignmentSideEnum.ALIGNMENT_NEUTRAL;
    public int honor, dishonor, energy;

    public CopyOnWriteArrayList<Player> followers;

    public boolean isInWorld;
    protected boolean myInitialized = false;
    private HumanInformations cachedHumanInformations = null;

    //Other
    public byte moodSmiley = -1;
    public Guild guild;

    private Fight myFight;
    private Fighter myFighter;

    public synchronized void initialize() {
        if (myInitialized) {
            return;
        }

        this.DisabledChannels = new ArrayList<>(14);
        for (byte i = 0; i < 14; i++) {
            if (!this.ennabledChannels.contains(i)) {
                this.DisabledChannels.add(i);
            }
        }
        if (this.mySpells == null || this.mySpells.haventSpell()) {
            this.mySpells = SpellBook.generateForBreed(this.breed, this.level);
        }
        if (this.myJobs == null) {
            this.myJobs = new JobBook() {
                {
                    this.deserializeEffects(new byte[0]);
                }
            };
        }

        this.guild = DAO.getGuilds().getForPlayer(this.ID);

        this.stats = new GenericStats(this);

        this.inventoryCache = new CharacterInventory(this);

        this.inventoryCache.itemsCache.values().stream().filter(x -> x.getPosition() != 63).forEach(Item -> {
            this.stats.merge(Item.getStats());
            //this.life += item.getStats().getTotal(StatsEnum.vitality);
        });

        this.inventoryCache.generalItemSetApply();

        this.myInitialized = true;
        if (life == 0) {
            life++;
        }
    }

    public void send(Message m) {
        if (client != null) {
            client.send(m);
        }
    }

    @Override
    public boolean canBeSee(IGameActor Actor) {
        if (this.account == null) {
            logger.error("NulledGameContext {} {}",this.nickName,this.ID);
            return false;
        }
        return true;
    }

    @Override
    public GameContextActorInformations getGameContextActorInformations(Player character) {
        if (this.account == null) {
            logger.error("NulledGameContext {}" , this.nickName);
        }
        return new GameRolePlayCharacterInformations(this.ID, this.getEntityLook(), this.getEntityDispositionInformations(character), this.nickName, this.getHumanInformations(), this.account.id, this.getActorAlignmentInformations());
    }

    public HumanInformations getHumanInformations() {
        if (cachedHumanInformations == null) {
            HumanOption[] Options = new HumanOption[0];
            if (this.activableTitle != 0) {
                Options = ArrayUtils.add(Options, new HumanOptionTitle(this.activableTitle, ""));
            }
            if (this.activableOrnament != 0) {
                Options = ArrayUtils.add(Options, new HumanOptionOrnament(this.activableOrnament));
            }
            if (this.guild != null) {
                Options = ArrayUtils.add(Options, new HumanOptionGuild(this.guild.toGuildInformations()));
            }
            // Options = ArrayUtils.add(Options, new HumanOptionAlliance(this.PvPEnabled, new AllianceInformations(1191, "a", "ta race", this.guild.getGuildEmblem())));
            this.cachedHumanInformations = new HumanInformations(new ActorRestrictionsInformations(), this.sexe == 1, Options);
        }
        return this.cachedHumanInformations;
    }

    public void refreshEntitie() {
        if (getFighter() != null) {
            getFight().sendToField(new GameContextRefreshEntityLookMessage(this.ID, this.getEntityLook()));
        } else {
            this.currentMap.sendToField(new GameContextRefreshEntityLookMessage(this.ID, this.getEntityLook()));
        }
    }

    public void refreshActor() {
        this.cachedHumanInformations = null;
        if (this.client != null) {
            currentMap.sendToField(new GameRolePlayShowActorMessage((GameRolePlayActorInformations) client.character.getGameContextActorInformations(null)));
        }
    }

    public GuildMember getGuildMember() {
        return this.guild.getMember(ID);
    }

    public synchronized void teleport(int newMapID, int newCellID) {
        if (this.currentMap.id == newMapID) {
            this.cell = newCellID == -1 ? currentMap.getAnyCellWalakable() : currentMap.getCell((short) newCellID) != null ? currentMap.getCell((short) newCellID) : cell;
            this.currentMap.sendToField(new TeleportOnSameMapMessage(ID, cell.id));
            return;
        }
        DofusMap NextMap = DAO.getMaps().findTemplate(newMapID);
        if (NextMap == null) {
            PlayerController.sendServerMessage(client, "Signal on the bugTracker nulled map -> " + newMapID);
            //client.sendPacket(new ErrorMapNotFoundMessage());
            return;
        }
        NextMap.Init();

        client.sequenceMessage();
        this.currentMap.destroyActor(this);
        this.currentMap = NextMap;
        if (NextMap.getCell((short) newCellID) == null || newCellID < 0 || newCellID > 559) {
            this.cell = NextMap.getAnyCellWalakable();
        } else {
            this.cell = NextMap.getCell((short) newCellID);
        }
        this.currentMap.spawnActor(this);
        client.send(new CurrentMapMessage(currentMap.id, "649ae451ca33ec53bbcbcc33becf15f4"));
        if (this.followers != null) {
            this.followers.parallelStream().forEach(e -> e.send(new CompassUpdatePartyMemberMessage(CompassTypeEnum.COMPASS_TYPE_PARTY, this.currentMap.coordinates(), this.ID)));
        }
    }

    public void addScore(ScoreType Type) {
        this.scores.put(Type, this.scores.get(Type) + 1);
    }

    public synchronized void onLogged() {
        try {
            if (!this.isInWorld) {
                this.isInWorld = true;
                this.account.CurrentIP = client.getIP();
                if (this.getFighter() == null) {
                    this.spawnToMap();
                    client.send(this.currentMap.getAgressableActorsStatus(this));
                }
                client.send(new CurrentMapMessage(currentMap.id, "649ae451ca33ec53bbcbcc33becf15f4")); //kdpelrkdpaielcmspekdprcvkdparkdb
                client.send(new CharacterLoadingCompleteMessage());
                ChatChannel.register(client);
                PlayerController.sendServerMessage(client, DAO.getSettings().getStringElement("World.onLogged"), DAO.getSettings().getStringElement("World.onLoggedColor"));
                // client.send(new BasicNoOperationMessage());
                client.send(new AlignmentRankUpdateMessage(this.alignmentGrade, false));
                client.sequenceMessage();
                if (this.guild != null) {
                    this.guild.registerPlayer(this);
                }

                //GuildWarn
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void onDisconnect() {
        try {
            if (!this.isInWorld) {
                return;
            }
            if (this.guild != null) {
                this.guild.unregisterPlayer(this);
            }
            if (this.followers != null) {
                this.followers.clear();
                this.followers = null;
            }
            if (client != null && currentMap != null) {
                currentMap.destroyActor(this);
            }
            this.client = null;
            if (this.account != null) {
                if (this.account.characters == null) {
                    logger.error("NulledAccountCharacters {} ", this.nickName); //pas sence arriver
                    logger.error(this.toString());
                }
                for (Player p : this.account.characters) { //TODO: ALleos
                    if(PlayerDAOImpl.myCharacterByTime.stream().anyMatch(x -> x.second.nickName.equalsIgnoreCase(p.nickName))){
                        logger.debug(p.nickName + " already aded");
                    }
                    PlayerDAOImpl.myCharacterByTime.add(new Couple<>(System.currentTimeMillis() + DAO.getSettings().getIntElement("account.DeleteMemoryTime") * 60 * 1000, p));
                    logger.debug(p.nickName + " aded" + this.account.characters.size());
                }
            } else {
                logger.error(nickName + " Nulled account on disconnection");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.isInWorld = false;
        }
    }

    public void refreshStats() {
        refreshStats(true);
    }

    public void refreshStats(boolean Logged) {
        if (this.regenStartTime != 0) {
            this.updateRegenedLife();
        }
        if (client != null) {
            if (client.getParty() != null) {
                client.getParty().updateMember(this);
            }
            if (Logged && getFighter() != null && getFight().fightState == FightState.STATE_PLACE) {
                getFighter().stats.reset();
                getFighter().stats.merge(this.stats);
                client.send(((CharacterFighter) getFighter()).FighterStatsListMessagePacket());
            } else {
                CharacterHandler.SendCharacterStatsListMessage(this.client);
            }
        }
    }

    public void stopRegen() {
        //TODo
    }

    public int getInitiative(boolean Base) {

        return 1 + (int) Math.floor((this.stats.getTotal(StatsEnum.Strength)
                + this.stats.getTotal(StatsEnum.Chance)
                + this.stats.getTotal(StatsEnum.Intelligence)
                + this.stats.getTotal(StatsEnum.Agility)
                + (Base ? this.stats.getTotal(StatsEnum.Initiative) : this.stats.getTotal(StatsEnum.Initiative)))
                * ((double) life / getMaxLife())
        );

    }

    /*public int getInitiative() {
     int FORCE = 142, EAU = 0, FEU = 808, AGI = 30;
     int BONUSINI = 365;
     int TOTALCARAC = FORCE + EAU + FEU + AGI;
     double PDVACTUEL = 4260, PDVMAX = 4260;
     double INITIATIVE_TOTAL = (TOTALCARAC + BONUSINI) * (PDVACTUEL / PDVMAX);

     int fact = 4;
     int pvmax = this.getMaxLife() - D2oDaoImpl.getBreed(this.breed).getHealPoint();
     int pv = life - D2oDaoImpl.getBreed(this.breed).getHealPoint();
     if (pv < 0) {
     pv = 1;
     }
     if (this.breed == BreedEnum.Sacrieur) {
     fact = 8;
     }
     double coef = pvmax / fact;

     coef += this.stats.getTotal(StatsEnum.getInitiative);
     coef += this.stats.getTotal(StatsEnum.agility);
     coef += this.stats.getTotal(StatsEnum.chance);
     coef += this.stats.getTotal(StatsEnum.Intelligence);
     coef += this.stats.getTotal(StatsEnum.strength);

     int init = 1;
     if (pvmax != 0) {
     init = (int) (coef * ((double) pv / (double) pvmax));
     }
     if (init < 0) {
     init = 0;
     }
     return init;
     }*/
    public int getProspection() {
        return (int) Math.floor((double) (this.stats.getTotal(StatsEnum.Chance) / 10)) + this.stats.getTotal(StatsEnum.Prospecting);

    }

    public void spawnToMap() {
        if (this.currentMap != null) {
            this.currentMap.spawnActor(this);
        }
    }

    public int getMaxLife() {
        return this.stats.getTotal(StatsEnum.Vitality) + ((int) level * 5) + DAO.getD2oTemplates().getBreed(this.breed).getHealPoint();
    }

    @Override
    public EntityLook getEntityLook() {
        if (entityLook == null) {
            this.entityLook = new EntityLook((short) 1, new ArrayList<Short>() {
                {
                    this.addAll(skins);
                }
            }, new ArrayList<Integer>() {
                {
                    this.addAll(indexedColors);
                }
            }, new ArrayList<Short>() {
                {
                    this.addAll(scales);
                }
            }, new ArrayList<>());
        }
        return entityLook;
    }
    /*byte alignmentSide, byte alignmentValue, byte alignmentGrade, int characterPower, int honor, int honorGradeFloor, int honorNextGradeFloor, byte aggressable*/

    public void changeAlignementSide(AlignmentSideEnum side) {
        if (this.myFight != null) {
            return;
        }
        this.alignmentSide = side;
        this.alignmentValue = 1;
        this.PvPEnabled = AggressableStatusEnum.PvP_ENABLED_AGGRESSABLE;
        this.onAligmenentSideChanged();
        this.setHonor(0, true);
    }

    public void setEnabldPvp(byte stat) {
        this.PvPEnabled = stat;
        this.onAligmenentSideChanged();
    }

    public void addHonor(int point, boolean notice) {
        setHonor(this.honor + point, notice);
        if (notice) {
            this.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, point > 0 ? 80 : 81, new String[]{Integer.toString(point)}));
        }
    }

    public void addDishonor(int point, boolean notice) {
        this.dishonor += point;
        if (notice) {
            this.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 75, new String[]{Integer.toString(point)}));
        }
    }

    public void setHonor(int point, boolean notice) {
        this.honor = point < 0 ? 0 : point;

        byte oldGrade = this.alignmentGrade;
        if (honor >= 17500) {
            this.alignmentGrade = 10;
        } else {
            for (byte n = 1; n <= 10; n++) {
                if (honor < DAO.getExps().getLevel(n).PvP) {
                    this.alignmentGrade = (byte) (n - 1);

                    break;
                }
            }
        }
        if (notice && this.alignmentGrade != oldGrade) {
            this.onAligmenentSideChanged();
            this.send(new AlignmentRankUpdateMessage(this.alignmentGrade, true));
        }
    }

    private void onAligmenentSideChanged() {
        this.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 82));
        this.currentMap.sendToField(new GameRolePlayShowActorMessage((GameRolePlayActorInformations) getGameContextActorInformations(null)));
        this.currentMap.sendToField(Player -> this.currentMap.getAgressableActorsStatus(Player));
        this.refreshStats();
    }

    public byte getPlayerState() {
        if (client == null) {
            return PlayerStateEnum.NOT_CONNECTED;
        } else {
            return PlayerStateEnum.UNKNOWN_STATE;
        }
    }

    public BasicGuildInformations getBasicGuildInformations() {
        return new BasicGuildInformations(0, "");
    }

    public int getCharacterPower() {
        return this.ID + this.level;
    }

    public ActorAlignmentInformations getActorAlignmentInformations() {
        return new ActorAlignmentInformations(this.PvPEnabled == AggressableStatusEnum.NON_AGGRESSABLE ? 0 : this.alignmentSide.value, this.alignmentValue, this.PvPEnabled == AggressableStatusEnum.NON_AGGRESSABLE ? 0 : this.alignmentGrade, this.getCharacterPower());
    }

    public ActorExtendedAlignmentInformations getActorAlignmentExtendInformations() {
        return new ActorExtendedAlignmentInformations(this.alignmentSide.value, this.alignmentValue, this.PvPEnabled == AggressableStatusEnum.NON_AGGRESSABLE ? 0 : this.alignmentGrade, this.getCharacterPower(), this.honor, DAO.getExps().getLevel(this.alignmentGrade).PvP, DAO.getExps().getLevel(this.alignmentGrade == 10 ? 10 : this.alignmentGrade + 1).PvP, this.PvPEnabled);
    }

    public CharacterBaseInformations toBaseInformations() {
        return new CharacterBaseInformations(ID, (byte) level, nickName, getEntityLook(), breed, sexe == 1);
    }

    public void addFollower(Player gay) {
        if (this.followers == null) {
            this.followers = new CopyOnWriteArrayList<>();
        }
        this.followers.addIfAbsent(gay);
        gay.send(new CompassUpdatePartyMemberMessage(CompassTypeEnum.COMPASS_TYPE_PARTY, this.currentMap.coordinates(), this.ID));
    }

    public void addExperience(long Value) {
        addExperience(Value, true);
    }

    public void addExperience(long value, boolean notice) {
        if (!this.myInitialized) {
            this.initialize();
        }

        this.experience += value;

        if (this.level != DAO.getExps().getMaxLevel()) {

            ExpLevel Floor;

            Integer lastLevel = this.level;
            do {
                Floor = DAO.getExps().getLevel(this.level + 1);
                if (Floor.player < this.experience) {
                    this.level++;
                    this.statPoints += 5;
                    this.spellPoints++;

                    if (this.level == 100) {
                        this.stats.addBase(StatsEnum.ActionPoints, 1);
                    }
                    // Apprend des nouveaux sorts
                    for (LearnableSpell learnableSpell : DAO.getSpells().findLearnableSpell(this.breed)) {
                        if ((int) learnableSpell.obtainLevel > (int) level && this.mySpells.hasSpell(learnableSpell.spell)) {
                            this.mySpells.removeSpell(this, learnableSpell.spell);
                        } else if ((int) learnableSpell.obtainLevel <= (int) level && !this.mySpells.hasSpell(learnableSpell.spell)) {
                            this.mySpells.addSpell(learnableSpell.spell, (byte) 1, this.mySpells.getFreeSlot(), this.client);
                        }
                    }

                }
            } while (Floor.player < this.experience && this.level != 200);

            if (this.level != lastLevel) {
                this.life = this.getMaxLife();
                this.send(new CharacterLevelUpMessage((byte) this.level));
                //friends
                this.currentMap.sendToField(new CharacterLevelUpInformationMessage((byte) this.level, this.nickName, this.ID));

            }

            if (this.client != null && notice) {
                this.refreshStats();
            }
        }
    }

    private void updateRegenedLife() {
        //Todo Pdv+=
    }

    public void destroyFromMap() {
        if (this.currentMap != null) {
            this.currentMap.destroyActor(this);
        }
    }

    @Override
    public void Observer$update(Observable o) {
    }

    @Override
    public void Observer$update(Observable o, Object arg) {
        if (arg instanceof Message) {
            if (o instanceof DofusMap && (getFight() != null || getFighter() != null)) {
                return;
            }
            if (client != null && isInWorld) {
                client.send((Message) arg);
            }
        } else if (arg instanceof FieldNotification) {
            FieldNotification task = (FieldNotification) arg;
            if (task.can(this)) {
                if (client != null && isInWorld) {
                    client.send((Message) task.packet);
                }
            }
        } else if (arg instanceof FieldOperation) {
            FieldOperation op = (FieldOperation) arg;
            op.execute(this);
        }
    }

    public boolean hasSexe() {
        return this.sexe == 1;
    }

    public int getAccountId() {
        return this.account.id;
    }

    @Override
    public void Observer$update(Observable o, Object... args) {
    }

    @Override
    public void Observer$reset(Observable o) {
    }

    public void save(boolean Clear) {
        if (this.myInitialized) {
            if (this.inventoryCache != null) {
                this.inventoryCache.save(Clear);
            }
            DAO.getPlayers().update(this, Clear);
            if (!Clear && this.account != null && this.account.accountData != null) {
                this.account.accountData.save(false);
            }
        }
    }

    public Object $FighterLook = new Object();

    public void setFight(Fight Fight) {
        synchronized ($FighterLook) {
            this.myFight = Fight;
        }
    }

    public void setFighter(Fighter Fighter) {
        synchronized ($FighterLook) {
            this.myFighter = Fighter;
        }
    }

    public Fight getFight() {
        return this.myFight;
    }

    public Fighter getFighter() {
        return this.myFighter;
    }

    public PlayerStatus getPlayerStatus() {
        return new PlayerStatus(this.status.value());
    }

    public void totalClear() {
        owner = 0;
        nickName = null;
        breed = 0;
        sexe = 0;
        skins.clear();
        skins = null;
        indexedColors.clear();
        indexedColors = null;
        scales.clear();
        scales = null;
        level = 0;
        client = null;
        regenStartTime = 0;
        currentMap = null;
        $FighterLook = null;
        if (ennabledChannels != null) {
            ennabledChannels.clear();
        }
        ennabledChannels = null;
        if (DisabledChannels != null) {
            DisabledChannels.clear();
        }
        DisabledChannels = null;
        if (shortcuts != null) {
            shortcuts.totalClear();
        }
        shortcuts = null;
        mapid = 0;
        if (mySpells != null) {
            mySpells.totalClear();
        }
        if (myJobs != null) {
            myJobs.totalClear();
        }
        if (this.mountInfo != null) {
            this.mountInfo.totalClear();
            this.mountInfo = null;
        }
        myJobs = null;
        mySpells = null;
        inventoryCache = null;
        status = null;
        vitality = 0;
        wisdom = 0;
        strength = 0;
        intell = 0;
        agility = 0;
        chance = 0;
        life = 0;
        if (stats != null) {
            stats.totalClear();
        }
        stats = null;
        experience = 0;
        kamas = 0;
        statPoints = 0;
        spellPoints = 0;
        alignmentSide = null;
        alignmentValue = 0;
        alignmentGrade = 0;
        PvPEnabled = 0;
        honor = 0;
        dishonor = 0;
        energy = 0;
        if (account != null) {
            account.totalClear();
        }
        account = null;
        isInWorld = false;
        myInitialized = false;

        myFight = null;
        myFighter = null;
        try {
            this.finalize();
        } catch (Throwable tr) {
        }
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
