package koh.game.dao.api;

import koh.game.entities.mob.MonsterGrade;
import koh.game.entities.mob.MonsterTemplate;
import koh.patterns.services.api.Service;
import lombok.Getter;

import java.util.HashMap;

/**
 * Created by Melancholia on 11/28/15.
 */
public abstract class MonsterDAO implements Service {

    @Getter
    protected final HashMap<Integer, MonsterTemplate> templates = new HashMap<>(3000);
    public abstract MonsterTemplate find(int id);

    public abstract void update(MonsterGrade gr, String column, int value);

    public abstract void update(MonsterGrade gr);
}
