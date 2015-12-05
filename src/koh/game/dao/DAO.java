package koh.game.dao;

import com.google.inject.Inject;
import koh.game.dao.api.*;
import koh.game.dao.mysql.MountDAO;
import koh.game.dao.mysql.PlayerDAOImpl;
import koh.game.utils.Settings;
import lombok.Getter;

public class DAO {

    @Inject @Getter
    private static Settings settings;

    @Inject @Getter
    private static AccountDataDAO accountDatas;

    @Inject @Getter
    private static AreaDAO areas;

    @Inject @Getter
    private static ExpDAO exps;

    @Inject @Getter
    private static JobDAO jobTemplates;

    @Inject @Getter
    private static MonsterDAO monsters;

    @Inject @Getter
    private static MountDAO mounts;

    @Inject @Getter
    private static PlayerDAO players;

    @Inject @Getter
    private static PaddockDAO paddocks;

    @Inject @Getter
    private static NpcDAO npcs;

    @Inject @Getter
    private static ItemDAO items;

    @Inject @Getter
    private static ItemTemplateDAO itemTemplates;

    @Inject @Getter
    private static MapDAO maps;

    @Inject @Getter
    private static SpellDAO spells;

    @Inject @Getter
    private static D2oDAO d2oTemplates;

}
