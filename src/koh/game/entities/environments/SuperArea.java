package koh.game.entities.environments;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Neo-Craft
 */
@Builder
public class SuperArea {

    @Getter
    private int id, worldMapId;
    //public String nameIdtype;
    @Getter
    private boolean hasWorldMaptype;
    @Getter @Setter
    private Area[] areas = new Area[0];

}
