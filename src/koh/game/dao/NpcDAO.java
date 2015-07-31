package koh.game.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import koh.game.MySQL;
import koh.game.entities.actors.Npc;
import koh.game.entities.actors.npc.*;
import koh.game.entities.actors.npc.replies.*;
import koh.game.utils.Settings;
import koh.utils.Enumerable;

/**
 *
 * @author Neo-Craft
 */
public class NpcDAO {

    public static Map<Integer, NpcTemplate> Cache = new HashMap<>();
    public static Map<Integer, ArrayList<Npc>> Npcs = new HashMap<>();
    public static Map<Integer, NpcMessage> Messages = new HashMap<>();
    public static List<NpcReply> Replies = new ArrayList<>();

    public static int FindReplies() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from npcs_replies", Settings.GetStringElement("Database.Name"), 0);
            NpcReply FuckingReply = null;
            ArrayList<String> params = new ArrayList<>();
            while (RS.next()) {
                switch (RS.getString("type")) {
                    case "go":
                        FuckingReply = new TeleportReply();
                        break;
                    case "close":
                        FuckingReply = new CloseReply();
                        break;
                    case "bank":
                        FuckingReply = new BankReply();
                        break;
                    case "continue":
                        FuckingReply = new TalkReply();
                        break;
                    default:
                        continue;
                }
                FuckingReply.ReplyID = RS.getInt("reply_id");
                FuckingReply.Criteria = RS.getString("criteria");
                if (RS.getString("parameter0") != null) {
                    params.add(RS.getString("parameter0"));
                }
                if (RS.getString("parameter1") != null) {
                    params.add(RS.getString("parameter1"));
                }
                if (RS.getString("parameter2") != null) {
                    params.add(RS.getString("parameter2"));
                }
                if (RS.getString("parameter3") != null) {
                    params.add(RS.getString("parameter2"));
                }
                if (RS.getString("parameter4") != null) {
                    params.add(RS.getString("parameter2"));
                }
                if (RS.getString("additional_parameters") != null) {
                    params.addAll(Arrays.asList(RS.getString("additional_parameters").split("\\|")));
                }
                FuckingReply.Parameters = params.toArray(new String[params.size()]);
                params.clear();
                Replies.add(FuckingReply);

                i++;
            }
            FuckingReply = null;
            MySQL.closeResultSet(RS);
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

    }

    public static int FindSpawns() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from maps_npcs", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                if (!Npcs.containsKey(RS.getInt("map"))) {
                    Npcs.put(RS.getInt("map"), new ArrayList<>());
                }
                Npcs.get(RS.getInt("map")).add(new Npc() {
                    {
                        this.Mapid = RS.getInt("map");
                        this.CellID = RS.getShort("cell");
                        this.Direction = RS.getByte("direction");
                        this.Sex = RS.getBoolean("sex");
                        this.NpcId = RS.getInt("id");
                        this.Artwork = RS.getInt("artwork");
                        this.QuestsToStart = Enumerable.StringToIntArray(RS.getString("quests_to_start"));
                        this.QuestsToValid = Enumerable.StringToIntArray(RS.getString("quests_to_valid"));
                    }
                });
                i++;
            }
            MySQL.closeResultSet(RS);
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

    }

    public static int FindItems() {
        try {
            int i = 0;

            ResultSet RS = MySQL.executeQuery("SELECT * from npc_items", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                if (Cache.get(RS.getInt("npc_shop_id")).Items == null) {
                    Cache.get(RS.getInt("npc_shop_id")).Items = new HashMap<>();
                }
                Cache.get(RS.getInt("npc_shop_id")).Items.put(RS.getInt("item_id"), new NpcItem() {
                    {
                        this.CustomPrice = RS.getFloat("custom_price");
                        this.BuyCriterion = RS.getString("buy_criterion");
                        this.MaximiseStats = RS.getBoolean("max_stats");
                        this.Item = RS.getInt("item_id");
                        this.Token = RS.getInt("token_id");
                    }
                });
                i++;
            }
            MySQL.closeResultSet(RS);
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

    }

    public static int FindMessages() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from npc_messages", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                Messages.put(RS.getInt("id"), new NpcMessage() {
                    {
                        this.Id = RS.getInt("id");
                        this.MessageId = RS.getInt("message_id");
                        this.Parameters = RS.getString("parameters").split("\\|");
                        this.Criteria = RS.getString("criteria");
                        this.FalseQuestion = RS.getInt("if_false");
                        if (RS.getString("replies") != null) {
                            if (RS.getString("replies").isEmpty()) {
                                this.Replies = new int[0];
                            } else {
                                this.Replies = Enumerable.StringToIntArray(RS.getString("replies"));
                            }
                        }
                    }
                });
                i++;
            }
            MySQL.closeResultSet(RS);
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

    }

    public static int FindAll() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from npc_templates", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                Cache.put(RS.getInt("id"), new NpcTemplate() {
                    {
                        this.Id = RS.getInt("id");
                        this.dialogMessages = Enumerable.StringToMultiArray(RS.getString("dialog_messages"));
                        this.dialogReplies = Enumerable.StringToMultiArray(RS.getString("dialog_replies"));
                        this.actions = Enumerable.StringToIntArray(RS.getString("actions"));
                        this.gender = RS.getInt("gender");
                        this.look = RS.getString("look");
                        this.fastAnimsFun = RS.getBoolean("fast_anims_fun");
                        this.OrderItemsByLevel = RS.getBoolean("order_items_level");
                        this.OrderItemsByPrice = RS.getBoolean("order_items_price");
                    }
                });
                i++;
            }
            MySQL.closeResultSet(RS);
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

    }

}
