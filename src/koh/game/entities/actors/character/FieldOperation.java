
package koh.game.entities.actors.character;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */

@FunctionalInterface
public interface FieldOperation {
    void execute(Player player);
}
