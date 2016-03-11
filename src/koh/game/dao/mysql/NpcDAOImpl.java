package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.game.dao.DAO;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.NpcDAO;
import koh.game.entities.actors.Npc;
import koh.game.entities.actors.pnj.NpcItem;
import koh.game.entities.actors.pnj.NpcMessage;
import koh.game.entities.actors.pnj.NpcReply;
import koh.game.entities.actors.pnj.NpcTemplate;
import koh.game.entities.actors.pnj.replies.BankReply;
import koh.game.entities.actors.pnj.replies.CloseReply;
import koh.game.entities.actors.pnj.replies.TalkReply;
import koh.game.entities.actors.pnj.replies.TeleportReply;
import koh.game.entities.item.ItemTemplate;
import koh.game.utils.sql.ConnectionResult;
import koh.utils.Enumerable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Neo-Craft
 */
public class NpcDAOImpl extends NpcDAO {

    private final Logger logger = LogManager.getLogger(NpcDAO.class);
    private final Map<Integer, NpcTemplate> templates = new HashMap<>(2500);
    private final Map<Integer, ArrayList<Npc>> npcs = new HashMap<>(1000);
    private final Map<Integer, NpcMessage> messages = new HashMap<>(20000);
    private final List<NpcReply> replies = new ArrayList<>(100);

    @Inject
    private DatabaseSource dbSource;

    @Override
    public ArrayList<Npc> forMap(int mapid) {
        return this.npcs.get(mapid);
    }

    @Override
    public NpcTemplate findTemplate(int id) {
        return this.templates.get(id);
    }

    @Override
    public NpcMessage findMessage(int id) {
        return this.messages.get(id);
    }

    @Override
    public Stream<NpcReply> repliesAsStream() {
        return this.replies.stream();
    }

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
                npcReply.setReplyID(result.getInt("reply_id"));
                npcReply.setCriteria(result.getString("criteria"));
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
                npcReply.setParameters(params.toArray(new String[params.size()]));
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
                npcs.get(result.getInt("map")).add(new Npc(result));
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
                if (templates.get(result.getInt("npc_shop_id")).getItems() == null) {
                    templates.get(result.getInt("npc_shop_id")).setItems(new HashMap<>());
                }

                templates.get(result.getInt("npc_shop_id")).getItems().put(result.getInt("item_id"),
                        NpcItem.builder()
                                .customPrice(result.getFloat("custom_price"))
                                .buyCriterion(result.getString("buy_criterion"))
                                .maximiseStats(result.getBoolean("max_stats"))
                                .item(result.getInt("item_id"))
                                .token(result.getInt("token_id"))
                                .build());
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                messages.put(result.getInt("id"), new NpcMessage(result));
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

                templates.put(result.getInt("id"), NpcTemplate.builder()
                        .id(result.getInt("id"))
                        .dialogMessages(Enumerable.stringToMultiArray(result.getString("dialog_messages")))
                        .dialogReplies(Enumerable.stringToMultiArray(result.getString("dialog_replies")))
                        .actions(Enumerable.stringToIntArray(result.getString("actions")))
                        .gender(result.getInt("gender"))
                        .look(result.getString("look"))
                        .fastAnimsFun(result.getBoolean("fast_anims_fun"))
                        .orderItemsByLevel(result.getBoolean("order_items_level"))
                        .orderItemsByPrice(result.getBoolean("order_items_price"))
                        .build());
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
        logger.info("loaded {} npc templates", this.loadAll());
        logger.info("loaded {} npc spawns", this.loadAllSpawns());
        logger.info("loaded {} npc items", this.loadAllItems());
        logger.info("loaded {} npc messages", this.loadAllMessages());
        logger.info("loaded {} npc replies", this.loadAllReplies());
    }

    @Override
    public void stop() {

    }
}
