package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.NpcDAO;
import koh.game.entities.actors.Npc;
import koh.game.entities.actors.npc.NpcItem;
import koh.game.entities.actors.npc.NpcMessage;
import koh.game.entities.actors.npc.NpcReply;
import koh.game.entities.actors.npc.NpcTemplate;
import koh.game.entities.actors.npc.replies.BankReply;
import koh.game.entities.actors.npc.replies.CloseReply;
import koh.game.entities.actors.npc.replies.TalkReply;
import koh.game.entities.actors.npc.replies.TeleportReply;
import koh.game.utils.sql.ConnectionResult;
import koh.utils.Enumerable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.util.*;

/**
 * @author Neo-Craft
 */
public class NpcDAOImpl extends NpcDAO {

    private static final Logger logger = LogManager.getLogger(NpcDAOImpl.class);
    public static Map<Integer, NpcTemplate> templates = new HashMap<>(2500);
    public static Map<Integer, ArrayList<Npc>> npcs = new HashMap<>(1000);
    public static Map<Integer, NpcMessage> messages = new HashMap<>(20000);
    public static List<NpcReply> replies = new ArrayList<>(100);
    @Inject
    private DatabaseSource dbSource;

    private int loadAllReplies() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from npcs_replies", 0)) {
            ResultSet result = conn.getResult();
            NpcReply npcReply = null;
            ArrayList<String> params = new ArrayList<>();
            while (result.next()) {
                switch (result.getString("type")) {
                    case "go":
                        npcReply = new TeleportReply();
                        break;
                    case "close":
                        npcReply = new CloseReply();
                        break;
                    case "bank":
                        npcReply = new BankReply();
                        break;
                    case "continue":
                        npcReply = new TalkReply();
                        break;
                    default:
                        continue;
                }
                npcReply.ReplyID = result.getInt("reply_id");
                npcReply.Criteria = result.getString("criteria");
                if (result.getString("parameter0") != null) {
                    params.add(result.getString("parameter0"));
                }
                if (result.getString("parameter1") != null) {
                    params.add(result.getString("parameter1"));
                }
                if (result.getString("parameter2") != null) {
                    params.add(result.getString("parameter2"));
                }
                if (result.getString("parameter3") != null) {
                    params.add(result.getString("parameter2"));
                }
                if (result.getString("parameter4") != null) {
                    params.add(result.getString("parameter2"));
                }
                if (result.getString("additional_parameters") != null) {
                    params.addAll(Arrays.asList(result.getString("additional_parameters").split("\\|")));
                }
                npcReply.Parameters = params.toArray(new String[params.size()]);
                params.clear();
                replies.add(npcReply);

                i++;
            }
            npcReply = null;
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;

    }

    private int loadAllSpawns() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from maps_npcs", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                if (!npcs.containsKey(result.getInt("map"))) {
                    npcs.put(result.getInt("map"), new ArrayList<>());
                }
                npcs.get(result.getInt("map")).add(new Npc() {
                    {
                        this.Mapid = result.getInt("map");
                        this.CellID = result.getShort("cell");
                        this.Direction = result.getByte("direction");
                        this.Sex = result.getBoolean("sex");
                        this.NpcId = result.getInt("id");
                        this.Artwork = result.getInt("artwork");
                        this.QuestsToStart = Enumerable.StringToIntArray(result.getString("quests_to_start"));
                        this.QuestsToValid = Enumerable.StringToIntArray(result.getString("quests_to_valid"));
                    }
                });
                i++;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    private int loadAllItems() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from npc_items", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                if (templates.get(result.getInt("npc_shop_id")).Items == null) {
                    templates.get(result.getInt("npc_shop_id")).Items = new HashMap<>();
                }
                templates.get(result.getInt("npc_shop_id")).Items.put(result.getInt("item_id"), new NpcItem() {
                    {
                        this.CustomPrice = result.getFloat("custom_price");
                        this.BuyCriterion = result.getString("buy_criterion");
                        this.MaximiseStats = result.getBoolean("max_stats");
                        this.Item = result.getInt("item_id");
                        this.Token = result.getInt("token_id");
                    }
                });
                i++;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    private int loadAllMessages() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from npc_messages", 0)) {
            ResultSet result = conn.getResult();


            while (result.next()) {
                messages.put(result.getInt("id"), new NpcMessage() {
                    {
                        this.Id = result.getInt("id");
                        this.MessageId = result.getInt("message_id");
                        this.Parameters = result.getString("parameters").split("\\|");
                        this.Criteria = result.getString("criteria");
                        this.FalseQuestion = result.getInt("if_false");
                        if (result.getString("replies") != null) {
                            if (result.getString("replies").isEmpty()) {
                                this.Replies = new int[0];
                            } else {
                                this.Replies = Enumerable.StringToIntArray(result.getString("replies"));
                            }
                        }
                    }
                });
                i++;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    public int loadAll() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from npc_templates", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                templates.put(result.getInt("id"), new NpcTemplate() {
                    {
                        this.Id = result.getInt("id");
                        this.dialogMessages = Enumerable.StringToMultiArray(result.getString("dialog_messages"));
                        this.dialogReplies = Enumerable.StringToMultiArray(result.getString("dialog_replies"));
                        this.actions = Enumerable.StringToIntArray(result.getString("actions"));
                        this.gender = result.getInt("gender");
                        this.look = result.getString("look");
                        this.fastAnimsFun = result.getBoolean("fast_anims_fun");
                        this.OrderItemsByLevel = result.getBoolean("order_items_level");
                        this.OrderItemsByPrice = result.getBoolean("order_items_price");
                    }
                });
                i++;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;

    }

    @Override
    public void start() {
        logger.info("loaded {} npc templates",this.loadAll());
        logger.info("loaded {} npc spawns",this.loadAllSpawns());
        logger.info("loaded {} npc items",this.loadAllItems());
        logger.info("loaded {} npc messages",this.loadAllMessages());
        logger.info("loaded {} npc replies",this.loadAllReplies());
    }

    @Override
    public void stop() {

    }
}
