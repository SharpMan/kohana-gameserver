package koh.game.entities.actors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import koh.d2o.Couple;
import koh.game.Main;
import koh.game.actions.GameFight;
import koh.game.controllers.PlayerController;
import koh.game.dao.D2oDao;
import koh.game.dao.ExpDAO;
import static koh.game.dao.GuildDAO.HasGuild;
import koh.game.dao.MapDAO;
import koh.game.dao.PlayerDAO;
import koh.game.dao.SpellDAO;
import koh.game.entities.Account;
import koh.game.entities.ExpLevel;
import koh.game.entities.actors.character.CharacterInventory;
import koh.game.entities.actors.character.FieldNotification;
import koh.game.entities.environments.DofusMap;
import koh.game.network.WorldClient;
import koh.game.network.handlers.game.approach.CharacterHandler;
import koh.game.utils.Settings;
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
import koh.protocol.messages.connection.BasicNoOperationMessage;
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
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class Player extends IGameActor implements Observer {

    public int Owner;
    public String NickName;
    public int Sexe;
    public byte Breed;
    public ArrayList<Short> Skins;
    public ArrayList<Integer> IndexedColors = new ArrayList<>(5);
    public ArrayList<Short> Scales;
    public Account Account;
    public int achievementPoints;
    public int Level;
    public WorldClient Client;
    public long RegenStartTime;
    public volatile DofusMap CurrentMap;
    public ArrayList<Byte> EnnabledChannels = new ArrayList<>(20), DisabledChannels;
    public ShortcutBook Shortcuts;
    public volatile MountInformations MountInfo;
    public int SavedMap;
    public short SavedCell;
    public volatile SpellBook mySpells;
    public volatile JobBook myJobs;
    public CharacterInventory InventoryCache;
    public PlayerStatusEnum Status = PlayerStatusEnum.PLAYER_STATUS_AVAILABLE;
    public HashMap<ScoreType, Integer> Scores = new HashMap<>(7);
    //GenericStats
    /*public int AP;
     public int MP;*/
    public int Vitality;
    public int Wisdom;
    public int Strength;
    public int Intell;
    public int Agility;
    public int Chance, Life;
    public short activableTitle, activableOrnament;
    public byte RegenRate;
    public byte[] Emotes;
    public int[] Ornaments, Titles;

    public GenericStats Stats;

    //Stats
    public long Experience;
    public int Kamas, StatPoints, SpellPoints;
    public byte AlignmentValue, AlignmentGrade, PvPEnabled;
    public AlignmentSideEnum AlignmentSide = AlignmentSideEnum.ALIGNMENT_NEUTRAL;
    public int Honor, Dishonor, Energy;

    public CopyOnWriteArrayList<Player> Followers;

    public boolean IsInWorld;
    protected boolean myInitialized = false;
    private HumanInformations CachedHumanInformations = null;

    //Other
    public byte MoodSmiley = -1;
    public Guild Guild;

    private Fight myFight;
    private Fighter myFighter;

    public synchronized void Initialize() {
        if (myInitialized) {
            return;
        }

        this.DisabledChannels = new ArrayList<>(14);
        for (byte i = 0; i < 14; i++) {
            if (!this.EnnabledChannels.contains(i)) {
                this.DisabledChannels.add(i);
            }
        }
        if (this.mySpells == null || this.mySpells.HaventSpell()) {
            this.mySpells = SpellBook.GenerateForBreed(this.Breed, this.Level);
        }
        if (this.myJobs == null) {
            this.myJobs = new JobBook() {
                {
                    this.DeserializeEffects(new byte[0]);
                }
            };
        }

        this.Guild = HasGuild(this.ID);

        this.Stats = new GenericStats(this);

        this.InventoryCache = new CharacterInventory(this);

        this.InventoryCache.ItemsCache.values().stream().filter(x -> x.GetPosition() != 63).forEach(Item -> {
            this.Stats.Merge(Item.GetStats());
            //this.Life += Item.GetStats().GetTotal(StatsEnum.Vitality);
        });

        this.InventoryCache.GeneralItemSetApply();

        this.myInitialized = true;
        if (Life == 0) {
            Life++;
        }
    }

    public void Send(Message m) {
        if (Client != null) {
            Client.Send(m);
        }
    }

    @Override
    public GameContextActorInformations GetGameContextActorInformations(Player character) {
        return new GameRolePlayCharacterInformations(this.ID, this.GetEntityLook(), this.GetEntityDispositionInformations(character), this.NickName, this.GetHumanInformations(), this.Account.ID, this.GetActorAlignmentInformations());
    }

    public HumanInformations GetHumanInformations() {
        if (CachedHumanInformations == null) {
            HumanOption[] Options = new HumanOption[0];
            if (this.activableTitle != 0) {
                Options = ArrayUtils.add(Options, new HumanOptionTitle(this.activableTitle, ""));
            }
            if (this.activableOrnament != 0) {
                Options = ArrayUtils.add(Options, new HumanOptionOrnament(this.activableOrnament));
            }
            if (this.Guild != null) {
                Options = ArrayUtils.add(Options, new HumanOptionGuild(this.Guild.toGuildInformations()));
            }
            // Options = ArrayUtils.add(Options, new HumanOptionAlliance(this.PvPEnabled, new AllianceInformations(1191, "a", "ta race", this.Guild.GetGuildEmblem())));
            this.CachedHumanInformations = new HumanInformations(new ActorRestrictionsInformations(), this.Sexe == 1, Options);
        }
        return this.CachedHumanInformations;
    }

    public void RefreshEntitie() {
        if (GetFighter() != null) {
            GetFight().sendToField(new GameContextRefreshEntityLookMessage(this.ID, this.GetEntityLook()));
        } else {
            this.CurrentMap.sendToField(new GameContextRefreshEntityLookMessage(this.ID, this.GetEntityLook()));
        }
    }

    public void RefreshActor() {
        this.CachedHumanInformations = null;
        if (this.Client != null) {
            CurrentMap.sendToField(new GameRolePlayShowActorMessage((GameRolePlayActorInformations) Client.Character.GetGameContextActorInformations(null)));
        }
    }

    public GuildMember GuildMember() {
        return this.Guild.Members.get(ID);
    }

    public synchronized void teleport(int newMapID, int newCellID) {
        if (this.CurrentMap.Id == newMapID) {
            this.Cell = newCellID == -1 ? CurrentMap.GetAnyCellWalakable() : CurrentMap.getCell((short) newCellID) != null ? CurrentMap.getCell((short) newCellID) : Cell;
            this.CurrentMap.sendToField(new TeleportOnSameMapMessage(ID, Cell.Id));
            return;
        }
        DofusMap NextMap = MapDAO.Cache.get(newMapID);
        if (NextMap == null) {
            PlayerController.SendServerMessage(Client, "Nulled map");
            //Client.sendPacket(new ErrorMapNotFoundMessage());
            return;
        }
        NextMap.Init();

        Client.SequenceMessage();
        this.CurrentMap.DestroyActor(this);
        this.CurrentMap = NextMap;
        if (NextMap.getCell((short) newCellID) == null || newCellID < 0 || newCellID > 559) {
            this.Cell = NextMap.GetAnyCellWalakable();
        } else {
            this.Cell = NextMap.getCell((short) newCellID);
        }
        this.CurrentMap.SpawnActor(this);
        Client.Send(new CurrentMapMessage(CurrentMap.Id, "649ae451ca33ec53bbcbcc33becf15f4"));
        if (this.Followers != null) {
            this.Followers.parallelStream().forEach(e -> e.Send(new CompassUpdatePartyMemberMessage(CompassTypeEnum.COMPASS_TYPE_PARTY, this.CurrentMap.Cordinates(), this.ID)));
        }
    }

    public void addScore(ScoreType Type) {
        this.Scores.put(Type, this.Scores.get(Type) + 1);
    }

    public synchronized void onLogged() {
        try {
            if (!this.IsInWorld) {
                this.IsInWorld = true;
                this.Account.CurrentIP = Client.getIP();
                if (this.GetFighter() == null) {
                    this.SpawnToMap();
                    Client.Send(this.CurrentMap.GetAgressableActorsStatus(this));
                }
                Client.Send(new CurrentMapMessage(CurrentMap.Id, "649ae451ca33ec53bbcbcc33becf15f4")); //kdpelrkdpaielcmspekdprcvkdparkdb
                Client.Send(new CharacterLoadingCompleteMessage());
                ChatChannel.Register(Client);
                PlayerController.SendServerMessage(Client, Settings.GetStringElement("World.onLogged"), Settings.GetStringElement("World.onLoggedColor"));
                // Client.Send(new BasicNoOperationMessage());
                Client.Send(new AlignmentRankUpdateMessage(this.AlignmentGrade, false));
                Client.SequenceMessage();
                if (this.Guild != null) {
                    this.Guild.registerPlayer(this);
                }

                //GuildWarn
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void OnDisconnect() {
        try {
            this.IsInWorld = false;
            if (this.Guild != null) {
                this.Guild.unregisterPlayer(this);
            }
            if (this.Followers != null) {
                this.Followers.clear();
                this.Followers = null;
            }
            if (Client != null && CurrentMap != null) {
                CurrentMap.DestroyActor(this);
            }
            this.Client = null;
            for (Player p : this.Account.Characters) {
                PlayerDAO.myCharacterByTime.add(new Couple<>(System.currentTimeMillis() + Settings.GetIntElement("Account.DeleteMemoryTime") * 60 * 1000, p));
                Main.Logs().writeError(p.NickName + " aded");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void RefreshStats() {
        RefreshStats(true);
    }

    public void RefreshStats(boolean Logged) {
        if (this.RegenStartTime != 0) {
            this.UpdateRegenedLife();
        }
        if (Client != null) {
            if (Logged && GetFighter() != null && GetFight().FightState == FightState.STATE_PLACE) {
                GetFighter().Stats.Reset();
                GetFighter().Stats.Merge(this.Stats);
                Client.Send(((CharacterFighter) GetFighter()).FighterStatsListMessagePacket());
            } else {
                CharacterHandler.SendCharacterStatsListMessage(this.Client);
            }
            if (Client.GetParty() != null) {
                Client.GetParty().UpdateMember(this);
            }
        }
    }

    public void StopRegen() {
        //TODo
    }

    public int Initiative(boolean Base) {

        return 1 + (int) Math.floor((this.Stats.GetTotal(StatsEnum.Strength)
                + this.Stats.GetTotal(StatsEnum.Chance)
                + this.Stats.GetTotal(StatsEnum.Intelligence)
                + this.Stats.GetTotal(StatsEnum.Agility)
                + (Base ? this.Stats.GetTotal(StatsEnum.Initiative) : this.Stats.GetTotal(StatsEnum.Initiative)))
                * ((double) Life / MaxLife())
        );

    }

    /*public int Initiative() {
     int FORCE = 142, EAU = 0, FEU = 808, AGI = 30;
     int BONUSINI = 365;
     int TOTALCARAC = FORCE + EAU + FEU + AGI;
     double PDVACTUEL = 4260, PDVMAX = 4260;
     double INITIATIVE_TOTAL = (TOTALCARAC + BONUSINI) * (PDVACTUEL / PDVMAX);

     int fact = 4;
     int pvmax = this.MaxLife() - D2oDao.getBreed(this.Breed).getHealPoint();
     int pv = Life - D2oDao.getBreed(this.Breed).getHealPoint();
     if (pv < 0) {
     pv = 1;
     }
     if (this.Breed == BreedEnum.Sacrieur) {
     fact = 8;
     }
     double coef = pvmax / fact;

     coef += this.Stats.GetTotal(StatsEnum.Initiative);
     coef += this.Stats.GetTotal(StatsEnum.Agility);
     coef += this.Stats.GetTotal(StatsEnum.Chance);
     coef += this.Stats.GetTotal(StatsEnum.Intelligence);
     coef += this.Stats.GetTotal(StatsEnum.Strength);

     int init = 1;
     if (pvmax != 0) {
     init = (int) (coef * ((double) pv / (double) pvmax));
     }
     if (init < 0) {
     init = 0;
     }
     return init;
     }*/
    public int Prospection() {
        return (int) Math.floor((double) (this.Stats.GetTotal(StatsEnum.Chance) / 10)) + this.Stats.GetTotal(StatsEnum.Prospecting);

    }

    public void SpawnToMap() {
        if (this.CurrentMap != null) {
            this.CurrentMap.SpawnActor(this);
        }
    }

    public int MaxLife() {
        return this.Stats.GetTotal(StatsEnum.Vitality) + ((int) Level * 5) + D2oDao.getBreed(this.Breed).getHealPoint();
    }

    @Override
    public EntityLook GetEntityLook() {
        if (entityLook == null) {
            this.entityLook = new EntityLook((short) 1, new ArrayList<Short>() {
                {
                    this.addAll(Skins);
                }
            }, new ArrayList<Integer>() {
                {
                    this.addAll(IndexedColors);
                }
            }, new ArrayList<Short>() {
                {
                    this.addAll(Scales);
                }
            }, new ArrayList<>());
        }
        return entityLook;
    }
    /*byte alignmentSide, byte alignmentValue, byte alignmentGrade, int characterPower, int honor, int honorGradeFloor, int honorNextGradeFloor, byte aggressable*/

    public void ChangeAlignementSide(AlignmentSideEnum side) {
        if (this.myFight != null) {
            return;
        }
        this.AlignmentSide = side;
        this.AlignmentValue = 1;
        this.PvPEnabled = AggressableStatusEnum.PvP_ENABLED_AGGRESSABLE;
        this.OnAligmenentSideChanged();
        this.setHonor(0, true);
    }

    public void setEnabldPvp(byte Stat) {
        this.PvPEnabled = Stat;
        this.OnAligmenentSideChanged();
    }

    public void addHonor(int Point, boolean Notice) {
        setHonor(this.Honor + Point, Notice);
        if (Notice) {
            this.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, Point > 0 ? 80 : 81, new String[]{Integer.toString(Point)}));
        }
    }

    public void addDishonor(int Point, boolean Notice) {
        this.Dishonor += Point;
        if (Notice) {
            this.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 75, new String[]{Integer.toString(Point)}));
        }
    }

    public void setHonor(int Point, boolean Notice) {
        this.Honor = Point;
        byte oldGrade = this.AlignmentGrade;
        if (Honor >= 17500) {
            this.AlignmentGrade = 10;
        } else {
            for (byte n = 1; n <= 10; n++) {
                if (Honor < ExpDAO.GetFloorByLevel(n).PvP) {
                    this.AlignmentGrade = (byte) (n - 1);

                    break;
                }
            }
        }
        if (Notice && this.AlignmentGrade != oldGrade) {
            this.OnAligmenentSideChanged();
            this.Send(new AlignmentRankUpdateMessage(this.AlignmentGrade, true));
        }
    }

    private void OnAligmenentSideChanged() {
        this.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 82));
        this.CurrentMap.sendToField(new GameRolePlayShowActorMessage((GameRolePlayActorInformations) GetGameContextActorInformations(null)));
        this.CurrentMap.sendToField(Player -> this.CurrentMap.GetAgressableActorsStatus(Player));
        this.RefreshStats();
    }

    public byte GetPlayerState() {
        if (Client == null) {
            return PlayerStateEnum.NOT_CONNECTED;
        } else {
            return PlayerStateEnum.UNKNOWN_STATE;
        }
    }

    public BasicGuildInformations GetBasicGuildInformations() {
        return new BasicGuildInformations(0, "");
    }

    public int CharacterPower() {
        return this.ID + this.Level;
    }

    public ActorAlignmentInformations GetActorAlignmentInformations() {
        return new ActorAlignmentInformations(this.PvPEnabled == AggressableStatusEnum.NON_AGGRESSABLE ? 0 : this.AlignmentSide.value, this.AlignmentValue, this.PvPEnabled == AggressableStatusEnum.NON_AGGRESSABLE ? 0 : this.AlignmentGrade, this.CharacterPower());
    }

    public ActorExtendedAlignmentInformations GetActorAlignmentExtendInformations() {
        return new ActorExtendedAlignmentInformations(this.AlignmentSide.value, this.AlignmentValue, this.PvPEnabled == AggressableStatusEnum.NON_AGGRESSABLE ? 0 : this.AlignmentGrade, this.CharacterPower(), this.Honor, ExpDAO.GetFloorByLevel(this.AlignmentGrade).PvP, ExpDAO.GetFloorByLevel(this.AlignmentGrade == 10 ? 10 : this.AlignmentGrade + 1).PvP, this.PvPEnabled);
    }

    public CharacterBaseInformations toBaseInformations() {
        return new CharacterBaseInformations(ID, (byte) Level, NickName, GetEntityLook(), Breed, Sexe == 1);
    }

    public void addFollower(Player ch) {
        if (this.Followers == null) {
            this.Followers = new CopyOnWriteArrayList<>();
        }
        this.Followers.addIfAbsent(ch);
        ch.Send(new CompassUpdatePartyMemberMessage(CompassTypeEnum.COMPASS_TYPE_PARTY, this.CurrentMap.Cordinates(), this.ID));
    }
    
    public void AddExperience(long Value) {
        AddExperience(Value,true);
    }

    public void AddExperience(long Value,boolean notice) {
        if (!this.myInitialized) {
            this.Initialize();
        }

        this.Experience += Value;

        if (this.Level != ExpDAO.maxLEVEL) {

            ExpLevel Floor;

            Integer LastLevel = this.Level;
            do {
                Floor = ExpDAO.GetFloorByLevel(this.Level + 1);
                if (Floor.Player < this.Experience) {
                    this.Level++;
                    this.StatPoints += 5;
                    this.SpellPoints++;

                    if (this.Level == 100) {
                        this.Stats.AddBase(StatsEnum.ActionPoints, 1);
                    }
                    // Apprend des nouveaux sorts
                    for (LearnableSpell learnableSpell : SpellDAO.LearnableSpells.get((int) this.Breed)) {
                        if ((int) learnableSpell.ObtainLevel > (int) Level && this.mySpells.HasSpell(learnableSpell.Spell)) {
                            this.mySpells.RemoveSpell(this, learnableSpell.Spell);
                        } else if ((int) learnableSpell.ObtainLevel <= (int) Level && !this.mySpells.HasSpell(learnableSpell.Spell)) {
                            this.mySpells.AddSpell(learnableSpell.Spell, (byte) 1, this.mySpells.getFreeSlot(), this.Client);
                        }
                    }

                }
            } while (Floor.Player < this.Experience && this.Level != 200);

            if (this.Level != LastLevel) {
                this.Life = this.MaxLife();
                this.Send(new CharacterLevelUpMessage((byte) this.Level));
                //Friends
                this.CurrentMap.sendToField(new CharacterLevelUpInformationMessage((byte) this.Level, this.NickName, this.ID));

            }

            if (this.Client != null && notice) {
                this.RefreshStats();
            }
        }
    }

    private void UpdateRegenedLife() {
        //Todo Pdv+=
    }

    public void DestroyFromMap() {
        if (this.CurrentMap != null) {
            this.CurrentMap.DestroyActor(this);
        }
    }

    @Override
    public void Observer$update(Observable o) {
    }

    @Override
    public void Observer$update(Observable o, Object arg) {
        if (arg instanceof Message) {
            if (o instanceof DofusMap && this.Client != null && GetFight() != null) {
                return;
            }
            if (Client != null && IsInWorld) {
                Client.Send((Message) arg);
            }
        } else if (arg instanceof FieldNotification) {
            FieldNotification task = (FieldNotification) arg;
            if (task.can(this)) {
                if (Client != null && IsInWorld) {
                    Client.Send((Message) task.packet);
                }
            }
        } else if (arg instanceof FieldOperation) {
            FieldOperation op = (FieldOperation) arg;
            op.execute(this);
        }
    }

    public boolean Sexe() {
        return this.Sexe == 1;
    }

    public int AccountId() {
        return this.Account.ID;
    }

    @Override
    public void Observer$update(Observable o, Object... args) {
    }

    @Override
    public void Observer$reset(Observable o) {
    }

    public void Save(boolean Clear) {
        if (this.myInitialized) {
            if (this.InventoryCache != null) {
                this.InventoryCache.Save(Clear);
            }
            PlayerDAO.Update(this, Clear);
            if (!Clear && this.Account != null && this.Account.Data != null) {
                this.Account.Data.Save(false);
            }
        }
    }

    public Object $FighterLook = new Object();

    public void SetFight(Fight Fight) {
        synchronized ($FighterLook) {
            this.myFight = Fight;
        }
    }

    public void SetFighter(Fighter Fighter) {
        synchronized ($FighterLook) {
            this.myFighter = Fighter;
        }
    }

    public Fight GetFight() {
        return this.myFight;
    }

    public Fighter GetFighter() {
        return this.myFighter;
    }

    public PlayerStatus PlayerStatus() {
        return new PlayerStatus(this.Status.value());
    }

    public void totalClear() {
        Owner = 0;
        NickName = null;
        Breed = 0;
        Sexe = 0;
        Skins.clear();
        Skins = null;
        IndexedColors.clear();
        IndexedColors = null;
        Scales.clear();
        Scales = null;
        Level = 0;
        Client = null;
        RegenStartTime = 0;
        CurrentMap = null;
        $FighterLook = null;
        if (EnnabledChannels != null) {
            EnnabledChannels.clear();
        }
        EnnabledChannels = null;
        if (DisabledChannels != null) {
            DisabledChannels.clear();
        }
        DisabledChannels = null;
        if (Shortcuts != null) {
            Shortcuts.totalClear();
        }
        Shortcuts = null;
        Mapid = 0;
        if (mySpells != null) {
            mySpells.totalClear();
        }
        if (myJobs != null) {
            myJobs.totalClear();
        }
        if (this.MountInfo != null) {
            this.MountInfo.totalClear();
            this.MountInfo = null;
        }
        myJobs = null;
        mySpells = null;
        InventoryCache = null;
        Status = null;
        Vitality = 0;
        Wisdom = 0;
        Strength = 0;
        Intell = 0;
        Agility = 0;
        Chance = 0;
        Life = 0;
        if (Stats != null) {
            Stats.totalClear();
        }
        Stats = null;
        Experience = 0;
        Kamas = 0;
        StatPoints = 0;
        SpellPoints = 0;
        AlignmentSide = null;
        AlignmentValue = 0;
        AlignmentGrade = 0;
        PvPEnabled = 0;
        Honor = 0;
        Dishonor = 0;
        Energy = 0;
        if (Account != null) {
            Account.totalClear();
        }
        Account = null;
        IsInWorld = false;
        myInitialized = false;

        myFight = null;
        myFighter = null;
        try {
            this.finalize();
        } catch (Throwable tr) {
        }
    }

}
