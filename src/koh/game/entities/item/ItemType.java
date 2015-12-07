package koh.game.entities.item;

import com.mysql.jdbc.StringUtils;
import lombok.Builder;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
@Builder
public class ItemType {

    @Getter
    private int superType;
    @Getter
    private boolean plural;
    @Getter
    private int gender;
    @Getter
    private String rawZone;
    @Getter
    private boolean needUseConfirm, mimickable;

    private static final Logger logger = LogManager.getLogger(ItemType.class);

    private int _zoneSize = Integer.MAX_VALUE;
    private int _zoneShape = Integer.MAX_VALUE;
    private int _zoneMinSize = Integer.MAX_VALUE;

    public byte zoneSize() {
        if (this._zoneSize == Integer.MAX_VALUE) {
            this.parseZone();
        };
        return (byte) (this._zoneSize);
    }

    public int zoneShape() {
        if (this._zoneShape == Integer.MAX_VALUE) {
            this.parseZone();
        };
        return (this._zoneShape);
    }

    public int zoneMinSize() {
        if (this._zoneMinSize == Integer.MAX_VALUE) {
            this.parseZone();
        };
        return (this._zoneMinSize);
    }

    private void parseZone() {
        try {
            String[] params;
            if (!StringUtils.isNullOrEmpty(this.rawZone)) {
                this._zoneShape = this.rawZone.charAt(0);
                if (!this.rawZone.contains(",")) {
                    return;
                }
                params = this.rawZone.substring(1).split(",");
                if (params.length > 0) {
                    this._zoneSize = Integer.parseInt(params[0]);
                } else {
                    this._zoneSize = 0;
                };
                if (params.length > 1) {
                    this._zoneMinSize = Integer.parseInt(params[1]);
                } else {
                    this._zoneMinSize = 0;
                };
            } else {
                logger.error("Zone incorrect ({})",this.rawZone);
            };
        } catch (java.lang.NumberFormatException e) {
            logger.error("Error with getItemType {}", this.superType);
        }
    }

}
