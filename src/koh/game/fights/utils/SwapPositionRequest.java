package koh.game.fights.utils;

/**
 *
 * @author Neo-Craft
 */
public class SwapPositionRequest {

    public int requestId;
    public int requesterId;
    public int requesterCellId;
    public int requestedId;
    public int requestedCellId;

    public SwapPositionRequest(int requestId, int requesterId, int requesterCellId, int requestedId, int requestedCellId) {
        this.requestId = requestId;
        this.requesterId = requesterId;
        this.requesterCellId = requesterCellId;
        this.requestedId = requestedId;
        this.requestedCellId = requestedCellId;
    }

}
