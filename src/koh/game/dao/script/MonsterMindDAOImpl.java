package koh.game.dao.script;

import koh.game.dao.api.MonsterMindDAO;
import koh.game.entities.mob.IAMind;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Melancholia on 1/13/16.
 */
@Log4j2
public class MonsterMindDAOImpl extends MonsterMindDAO {

    private final Map<Integer,IAMind> AI_MINDS = new HashMap<>(20);

    private int loadAll(){
        try {
            Files.walk(Paths.get("data/script/AI"))
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".py"))
                    .forEach((Path file) ->
                    {
                        this.AI_MINDS.put(Integer.parseInt(file.getFileName()
                                        .toString()
                                        .substring(6, file.getFileName().toString().length() - 3)
                                        .toLowerCase())
                                , PythonUtils.getJythonObject(IAMind.class, file.toString()));

                    });

        } catch (Exception e) {
            log.error(e);
            log.warn(e.getMessage());
        }
        return AI_MINDS.size();
    }

    @Override
    public IAMind find(int id) {
        return this.AI_MINDS.get(id);
    }

    @Override
    public void start() {
        log.info("Loaded {} AI Minds", this.loadAll());
    }

    @Override
    public void stop() {

    }
}
