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
import koh.game.entities.actors.pnj.replies.*;
import koh.game.entities.item.ItemTemplate;
import koh.game.utils.sql.ConnectionResult;
import koh.game.utils.sql.ConnectionStatement;
import koh.protocol.client.enums.ItemSuperTypeEnum;
import koh.utils.Enumerable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
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
            final ResultSet result = conn.getResult();
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
                    case "restat":
                        npcReply = new RestatReply();
                        break;
                    case "dj":
                        npcReply = new DjReply();
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
        } catch (Exception e) {
            e.printStackTrace();
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

    @Override
    public void insert(Npc npc) {
        try {
            try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("INSERT INTO `maps_npcs` VALUES (?,?,?,?,?,?,?,?);")) {

                PreparedStatement pStatement = conn.getStatement();

                pStatement.setInt(1, npc.getNpcId());
                pStatement.setInt(2, npc.getMapid());
                pStatement.setShort(3,npc.getCellID());
                pStatement.setByte(4, npc.getDirection()); //10
                pStatement.setBoolean(5, npc.isSex());
                pStatement.setString(6, "");
                pStatement.setString(7, "");
                pStatement.setInt(8, 0);
                pStatement.execute();


                logger.info(pStatement.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int loadAllItems() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from npc_items", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                final NpcTemplate npc = templates.get(result.getInt("npc_shop_id"));
                if (npc.getItems() == null) {
                    npc.setItems(new HashMap<>());
                }

                //System.out.println(f.getMapid());

               /* boolean dispo = npcs.entrySet().stream()
                        .anyMatch(e-> e.getValue().stream().anyMatch(f -> f.getMapid() == 115082755 && f.getCellID() != 260))
                        && !result.getBoolean("max_stats");

                if(dispo && DAO.getItemTemplates().getTemplate(result.getInt("item_id")) != null && DAO.getItemTemplates().getTemplate(result.getInt("item_id")).getLevel() >= 100)
                 System.out.println("UPDATE `npc_items` set custom_price="+DAO.getItemTemplates().getTemplate(result.getInt("item_id")).getLevel() * 1000+" WHERE item_id="+result.getInt("item_id")+" AND npc_shop_id="+result.getInt("npc_shop_id")+";");
*/
                npc.getItems().put(result.getInt("item_id"),
                        NpcItem.builder()
                                .customPrice(/*dispo && DAO.getItemTemplates().getTemplate(result.getInt("item_id")) != null && DAO.getItemTemplates().getTemplate(result.getInt("item_id")).getLevel() >= 100 ? DAO.getItemTemplates().getTemplate(result.getInt("item_id")).getLevel() * 1000 :*/ result.getFloat("custom_price"))
                                .buyCriterion(result.getString("buy_criterion"))
                                .maximiseStats(result.getBoolean("max_stats"))
                                .item(result.getInt("item_id"))
                                .token(result.getInt("token_id"))
                                .build());
                i++;
            }
            if(DAO.getSettings().getIntElement("World.ID") == 1) {
                NpcTemplate npc = this.findTemplate(786);

                DAO.getItemTemplates().getItemTemplates().values()
                        .stream()
                        .filter(item -> item.getTypeId() == 84)
                        .forEach(it -> {
                            if (npc.getItems() == null) {
                                npc.setItems(new HashMap<>());
                            }
                            npc.getItems().put(it.getId(),
                                    NpcItem.builder()
                                            .customPrice(0)
                                            .buyCriterion("")
                                            .maximiseStats(false)
                                            .item(it.getId())
                                            .token(0)
                                            .build());
                        });
            }

            final NpcTemplate npc2 = this.findTemplate(1561)
                    ,npc3 = this.findTemplate(1560)
                    ,npc4 = this.findTemplate(1559)
                    ,npc5 = this.findTemplate(313)
                    ,npc6 = this.findTemplate(793)
                    ,npc7 = this.findTemplate(1564)
                    ,npc8 = this.findTemplate(1396);

            DAO.getItemTemplates().getItemTemplates().values()
                    .stream()
                    .filter(item -> item.getTypeId() == 1 && item.getLevel() < 100 && item.getLevel() > 20)
                    .forEach(it -> {
                        if (npc8.getItems() == null) {
                            npc8.setItems(new HashMap<>());
                        }
                        npc8.getItems().put(it.getId(),
                                NpcItem.builder()
                                        .customPrice(10)
                                        .buyCriterion("")
                                        .maximiseStats(true)
                                        .item(it.getId())
                                        .token(13470)
                                        .build());
                    });

            DAO.getItemTemplates().getItemTemplates().values()
                    .stream()
                    .filter(item -> item.getTypeId() == 9 && item.getLevel() < 100 && item.getLevel() > 20)
                    .forEach(it -> {
                        if (npc7.getItems() == null) {
                            npc7.setItems(new HashMap<>());
                        }
                        npc7.getItems().put(it.getId(),
                                NpcItem.builder()
                                        .customPrice(10)
                                        .buyCriterion("")
                                        .maximiseStats(true)
                                        .item(it.getId())
                                        .token(13470)
                                        .build());
                    });

            DAO.getItemTemplates().getItemTemplates().values()
                    .stream()
                    .filter(item -> item.getTypeId() == 17 && item.getLevel() < 100 && item.getLevel() > 20)
                    .forEach(it -> {
                        if (npc6.getItems() == null) {
                            npc6.setItems(new HashMap<>());
                        }
                        npc6.getItems().put(it.getId(),
                                NpcItem.builder()
                                        .customPrice(10)
                                        .buyCriterion("")
                                        .maximiseStats(true)
                                        .item(it.getId())
                                        .token(13470)
                                        .build());
                    });

            DAO.getItemTemplates().getItemTemplates().values()
                    .stream()
                    .filter(item -> item.getTypeId() == 16 && item.getLevel() < 100 && item.getLevel() > 20)
                    .forEach(it -> {
                        if (npc5.getItems() == null) {
                            npc5.setItems(new HashMap<>());
                        }
                        npc5.getItems().put(it.getId(),
                                NpcItem.builder()
                                        .customPrice(10)
                                        .buyCriterion("")
                                        .maximiseStats(true)
                                        .item(it.getId())
                                        .token(13470)
                                        .build());
                    });

            DAO.getItemTemplates().getItemTemplates().values()
                    .stream()
                    .filter(item -> item.getTypeId() == 10 && item.getLevel() < 100 && item.getLevel() > 20)
                    .forEach(it -> {
                        if (npc2.getItems() == null) {
                            npc2.setItems(new HashMap<>());
                        }
                        npc2.getItems().put(it.getId(),
                                NpcItem.builder()
                                        .customPrice(10)
                                        .buyCriterion("")
                                        .maximiseStats(true)
                                        .item(it.getId())
                                        .token(13470)
                                        .build());
                    });
            DAO.getItemTemplates().getItemTemplates().values()
                    .stream()
                    .filter(item -> item.getTypeId() == 11 && item.getLevel() < 100 && item.getLevel() > 20)
                    .forEach(it -> {
                        if (npc4.getItems() == null) {
                            npc4.setItems(new HashMap<>());
                        }
                        npc4.getItems().put(it.getId(),
                                NpcItem.builder()
                                        .customPrice(10)
                                        .buyCriterion("")
                                        .maximiseStats(true)
                                        .item(it.getId())
                                        .token(13470)
                                        .build());
                    });

           /* DAO.getItemTemplates().getItemTemplates().values()
                    .stream()
                    .filter(item -> item.getSuperType() == ItemSuperTypeEnum.SUPERTYPE_WEAPON && item.getLevel() < 100 && item.getLevel() > 20)
                    .forEach(it -> {
                        if (npc3.getItems() == null) {
                            npc3.setItems(new HashMap<>());
                        }
                        npc3.getItems().put(it.getId(),
                                NpcItem.builder()
                                        .customPrice(10)
                                        .buyCriterion("")
                                        .maximiseStats(true)
                                        .item(it.getId())
                                        .token(13470)
                                        .build());
                    });*/

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    final Random rand = new Random();

    public NpcItem randomEntry(Map<Integer, NpcItem> map) {
        ArrayList<Integer> keys = new ArrayList<>(map.keySet());
        final int start = rand.nextInt(keys.size());
        return map.get(keys.get(start));
    }

    private int loadExtraItems(){
        int i = 0;
        try{
            templates.get(1524).setItems(new HashMap<>());

            final int size = templates.get(1511).getItems().size();

            NpcItem item = randomEntry(templates.get(1511).getItems());

            templates.get(1524).getItems().put(item.getItem(), NpcItem.builder()
                    .customPrice((int) (item.getCustomPrice() * 0.69f ))
                    .buyCriterion(item.getBuyCriterion())
                    .maximiseStats(item.isMaximiseStats())
                    .item(item.getItem())
                    .token(13470)
                    .build());

            item = randomEntry(templates.get(1511).getItems());

            templates.get(1524).getItems().put(item.getItem(), NpcItem.builder()
                    .customPrice((int) (item.getCustomPrice() * 0.69f))
                    .buyCriterion(item.getBuyCriterion())
                    .maximiseStats(item.isMaximiseStats())
                    .item(item.getItem())
                    .token(13470)
                    .build());

            item = randomEntry(templates.get(1511).getItems());

            templates.get(1524).getItems().put(item.getItem(), NpcItem.builder()
                    .customPrice((int) (item.getCustomPrice()))
                    .buyCriterion(item.getBuyCriterion())
                    .maximiseStats(item.isMaximiseStats())
                    .item(item.getItem())
                    .token(13470)
                    .build());


            if(new Random().nextInt(3) == 0){
                templates.get(1524).getItems().put(14485, NpcItem.builder()
                        .customPrice(240 + (new Random().nextInt(35)))
                        .buyCriterion(item.getBuyCriterion())
                        .maximiseStats(item.isMaximiseStats())
                        .item(14485)
                        .token(13470)
                        .build());
            }

            //

            try (ConnectionResult conn = dbSource.executeQuery("SELECT * from npc_items WHERE token_id = 13470 AND custom_price > 90 ORDER BY RAND() LIMIT 10", 0)) {
                ResultSet result = conn.getResult();

                while (result.next()) {
                    templates.get(1524).getItems().put(result.getInt("item_id"),
                            NpcItem.builder()
                                    .customPrice((int) (result.getFloat("custom_price") * 0.68))
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



            i++;

        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return  i;
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
        logger.info("loaded {} npc extra items", this.loadExtraItems());

    }

    @Override
    public void stop() {

    }
}
