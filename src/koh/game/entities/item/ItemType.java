package koh.game.entities.item;

import com.mysql.jdbc.StringUtils;
import koh.game.Main;

/**
 *
 * @author Neo-Craft
 */
public class ItemType {

    public int superType;
    public boolean plural;
    public int gender;
    public String rawZone;
    public boolean needUseConfirm, mimickable;

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
                Main.Logs().writeError(("Zone incorrect (" + this.rawZone) + ")");
            };
        } catch (java.lang.NumberFormatException e) {
            Main.Logs().writeError(String.format("Error with getItemType %s", this.superType));
        }
    }

}
