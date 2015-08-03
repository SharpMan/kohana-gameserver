package koh.game.entities.environments.cells;

import java.util.ArrayList;
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
    private DofusMap Map;

    public Zone(SpellShapeEnum shape, byte radius, DofusMap Map) {
        this.SetRadius(radius);
        this.setShapeType(shape);
        this.Map = Map;
    }

    public Zone(SpellShapeEnum shape, byte radius, byte direction, DofusMap Map) {
        this.SetRadius(radius);
        this.SetDirection(direction);
        this.setShapeType(shape);
        this.Map = Map;
    }

    public SpellShapeEnum ShapeType() {
        return this.m_shapeType;
    }

    public void setShapeType(SpellShapeEnum value) {
        this.m_shapeType = value;
        this.InitializeShape();
    }

    @Override
    public int Surface() {
        return this.m_shape.Surface();
    }

    @Override
    public byte MinRadius() {
        return this.m_shape.MinRadius();
    }

    @Override
    public byte Direction() {
        return this.m_direction;
    }

    @Override
    public byte Radius() {
        return this.m_radius;
    }

    @Override
    public Short[] GetCells(short centerCell) {
        return this.m_shape.GetCells(centerCell);
    }

    @Override
    public void SetDirection(byte Direction) {
        this.m_direction = Direction;
        if (this.m_shape == null) {
            return;
        }
        this.m_shape.SetDirection(Direction);
    }

    @Override
    public void SetRadius(byte Radius) {
        this.m_radius = Radius;
        if (this.m_shape == null) {
            return;
        }
        this.m_shape.SetRadius(Radius);
    }

    private void InitializeShape() {
        switch (this.ShapeType()) {
            case slash:
                this.m_shape = new Line(this.Radius());
                break;
            case a:
            case A:
                this.m_shape = new Lozenge((byte) 0, (byte) 63,this.Map);
                break;
            case C:
                this.m_shape = new Lozenge((byte) 0, this.Radius(),this.Map);
                break;
            case D:
                this.m_shape = new CrossZone((byte) 0, this.Radius());
                break;
            case I:
                this.m_shape = new Lozenge(this.Radius(), (byte) 63,this.Map);
                break;
            case L:
                this.m_shape = new Line(this.Radius());
                break;
            case O:
                this.m_shape = new CrossZone((byte) 1, this.Radius());
                break;
            case G:
                this.m_shape = new Square((byte) 0, this.Radius());

                break;
            case P:
                this.m_shape = new Single();
                break;
            case Q:
                this.m_shape = new CrossZone((byte) 1, this.Radius());
                break;
            case T:
                this.m_shape = new CrossZone((byte) 0, this.Radius()) {
                    {
                        OnlyPerpendicular = true;
                    }
                };
                break;
            case U:
                this.m_shape = new HalfLozenge((byte) 0, this.Radius());
                break;
            case V:
                this.m_shape = new Cone((byte) 0, this.Radius());
                break;
            case W:
                this.m_shape = new Square((byte) 0, this.Radius()) {
                    {
                        this.diagonalFree = true;
                    }
                };
                break;
            case X:
                this.m_shape = new CrossZone((byte) 0, this.Radius());
                break;
            case Hammer:
                this.m_shape = new CrossZone((byte) 0, this.Radius()) {
                    {
                        /*disabledDirection = new ArrayList<Byte>()
                         {{ 
                         this.add(this.Direction() - 4 > DirectionsEnum.DIRECTION_EAST ? this.Direction - 4 : this.Direction + 4);
                         }};*/
                    }
                };
                ((CrossZone) this.m_shape).disabledDirection.add((byte) (this.Direction() - 4 > DirectionsEnum.RIGHT ? this.Direction() - 4 : this.Direction() + 4));
                break;
            case sharp:
                this.m_shape = new CrossZone((byte) 1, this.Radius()) {
                    {
                        Diagonal = true;
                    }
                };
                break;
            case minus:
                this.m_shape = new CrossZone((byte) 0, this.Radius()) {
                    {
                        this.OnlyPerpendicular = true;
                        this.Diagonal = true;
                    }
                };
                break;
            case star:
                this.m_shape = new CrossZone((byte) 0, this.Radius()) {
                    {
                        AllDirections = true;
                    }
                };
                break;
            case plus:
                this.m_shape = new CrossZone((byte) 0, this.Radius()) {
                    {
                        Diagonal = true;
                    }
                };
                break;
            default:
                this.m_shape = new CrossZone((byte) 0, (byte) 0);
                break;
        }

        this.m_shape.SetDirection(this.m_direction);
    }
}
