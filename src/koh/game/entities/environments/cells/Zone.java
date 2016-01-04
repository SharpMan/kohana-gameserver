package koh.game.entities.environments.cells;

import koh.game.entities.environments.CrossZone;
import koh.game.entities.environments.DofusMap;
import koh.protocol.client.enums.DirectionsEnum;
import koh.protocol.client.enums.SpellShapeEnum;

/**
 *
 * @author Neo-Craft
 */
public class Zone implements IZone {

    private IZone m_shape;
    private SpellShapeEnum m_shapeType;
    private byte m_radius;
    private byte m_direction;
    private DofusMap map;

    public Zone(SpellShapeEnum shape, byte radius, DofusMap Map) {
        this.map = Map;
        this.setRadius(radius);
        this.setShapeType(shape);
    }

    public Zone(SpellShapeEnum shape, byte radius, byte direction, DofusMap Map) {
        this.map = Map;
        this.setRadius(radius);
        this.setDirection(direction);
        this.setShapeType(shape);
        
        
    }

    public SpellShapeEnum getShapeType() {
        return this.m_shapeType;
    }

    public void setShapeType(SpellShapeEnum value) {
        this.m_shapeType = value;
        this.initializeShape();
    }

    @Override
    public int getSurface() {
        return this.m_shape.getSurface();
    }

    @Override
    public byte getMinRadius() {
        return this.m_shape.getMinRadius();
    }

    @Override
    public byte getDirection() {
        return this.m_direction;
    }

    @Override
    public byte getRadius() {
        return this.m_radius;
    }

    @Override
    public Short[] getCells(short centerCell) {
        return this.m_shape.getCells(centerCell);
    }

    @Override
    public void setDirection(byte direction) {
        this.m_direction = direction;
        if (this.m_shape == null) {
            return;
        }
        this.m_shape.setDirection(direction);
    }

    @Override
    public void setRadius(byte radius) {
        this.m_radius = radius;
        if (this.m_shape == null) {
            return;
        }
        this.m_shape.setRadius(radius);
    }

    private void initializeShape() {

        switch (this.getShapeType()) {
            case slash:
                this.m_shape = new Line(this.getRadius());
                break;
            case a:
            case A:
                this.m_shape = new Lozenge((byte) 0, (byte) 63, this.map);
                break;
            case C:
                this.m_shape = new Lozenge((byte) 0, this.getRadius(), this.map);
                break;
            case D:
                this.m_shape = new CrossZone((byte) 0, this.getRadius());
                break;
            case I:
                this.m_shape = new Lozenge(this.getRadius(), (byte) 63, this.map);
                break;
            case L:
                this.m_shape = new Line(this.getRadius());
                break;
            case O:
                //this.m_shape = new CrossZone((byte) 1, this.getRadius());
                this.m_shape =  new Lozenge(this.getRadius(),this.getRadius(),this.map);
                break;
            case G:
                this.m_shape = new Square((byte) 0, this.getRadius(), this.map);

                break;
            case P:
                this.m_shape = new Single();
                break;
            case Q:
                this.m_shape = new CrossZone((byte) 1, this.getRadius());
                break;
            case T:
                this.m_shape = new CrossZone((byte) 0, this.getRadius()) {
                    {
                        onlyPerpendicular = true;
                    }
                };
                break;
            case U:
                this.m_shape = new HalfLozenge((byte) 0, this.getRadius());
                break;
            case V:
                this.m_shape = new Cone((byte) 0, this.getRadius());
                break;
            case W:
                this.m_shape = new Square((byte) 0, this.getRadius(), this.map) {
                    {
                        this.diagonalFree = true;
                    }
                };
                break;
            case X:
                this.m_shape = new CrossZone((byte) 0, this.getRadius());
                break;
            case Hammer:
                this.m_shape = new CrossZone((byte) 0, this.getRadius()) {
                    {
                        /*disabledDirection = new ArrayList<Byte>()
                         {{ 
                         this.add(this.direction() - 4 > DirectionsEnum.DIRECTION_EAST ? this.direction - 4 : this.direction + 4);
                         }};*/
                    }
                };
                ((CrossZone) this.m_shape).disabledDirection.add((byte) (this.getDirection() - 4 > DirectionsEnum.RIGHT ? this.getDirection() - 4 : this.getDirection() + 4));
                break;
            case sharp:
                this.m_shape = new CrossZone((byte) 1, this.getRadius()) {
                    {
                        diagonal = true;
                    }
                };
                break;
            case minus:
                this.m_shape = new CrossZone((byte) 0, this.getRadius()) {
                    {
                        this.onlyPerpendicular = true;
                        this.diagonal = true;
                    }
                };
                break;
            case star:
                this.m_shape = new CrossZone((byte) 0, this.getRadius()) {
                    {
                        allDirections = true;
                    }
                };
                break;
            case plus:
                this.m_shape = new CrossZone((byte) 0, this.getRadius()) {
                    {
                        diagonal = true;
                    }
                };
                break;
            default:
                this.m_shape = new CrossZone((byte) 0, (byte) 0);
                break;
        }
        this.m_shape.setDirection(this.m_direction);
    }
}
