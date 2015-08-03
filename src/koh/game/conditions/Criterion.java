package koh.game.conditions;

import static javafx.scene.input.KeyCode.T;
import org.apache.commons.lang3.NotImplementedException;

/**
 *
 * @author Neo-Craft
 */
public abstract class Criterion extends ConditionExpression {

    public ComparaisonOperatorEnum Operator;

    public String Literal;

    public static ComparaisonOperatorEnum TryGetOperator(char c) {
        switch (c) {
            case '!':
                return ComparaisonOperatorEnum.INEQUALS;
            case '<':
                return ComparaisonOperatorEnum.INFERIOR;
            case '=':
                return ComparaisonOperatorEnum.EQUALS;
            case '>':
                return ComparaisonOperatorEnum.SUPERIOR;
            case '~':
                return ComparaisonOperatorEnum.LIKE;
            default:
                return null;
        }
    }

    public static char GetOperatorChar(ComparaisonOperatorEnum op) {
        switch (op) {
            case EQUALS:
                return '=';
            case INEQUALS:
                return '!';
            case SUPERIOR:
                return '>';
            case INFERIOR:
                return '<';
            case LIKE:
                return '~';
            case STARTWITH:
                return 's';
            case STARTWITHLIKE:
                return 'S';
            case ENDWITH:
                return 'e';
            case ENDWITHLIKE:
                return 'E';
            case VALID:
                return 'v';
            case INVALID:
                return 'i';
            case UNKNOWN_1:
                return '#';
            case UNKNOWN_2:
                return '/';
            case UNKNOWN_3:
                return 'X';
            default:
                throw new Error(String.format("{0} is not a valid comparaison operator", op));
        }
    }

    public static Criterion CreateCriterionByName(String name) {
        if (StatsCriterion.IsStatsIdentifier(name)) {
            return (Criterion) new StatsCriterion(name);
        }
        switch (name) {
            case "PX":
                return (Criterion) new AdminRightsCriterion();
            case "Pa":
                return (Criterion) new AlignementLevelCriterion();
            case "Ps":
                return (Criterion) new AlignmentCriterion();
            case "PU":
                return (Criterion) new BonesCriterion();
            case "PG":
                return (Criterion) new BreedCriterion();
            case "PE":
                return (Criterion) new EmoteCriterion();
            case "Pb":
                return (Criterion) new FriendListCriterion();
            case "Pg":
                return (Criterion) new GiftCriterion();
            case "PO":
                return (Criterion) new HasItemCriterion();
            case "PJ":
                return (Criterion) new JobCriterion();
            case "Pk":
                return (Criterion) new PanoplieBonusCriterion();
            case "PK":
                return (Criterion) new KamaCriterion();
            case "PL":
                return (Criterion) new LevelCriterion();
            case "MK":
                return (Criterion) new MapCharactersCriterion();
            case "PR":
                return (Criterion) new MariedCriterion();
            case "P¨Q":
                return (Criterion) new MaxRankCriterion();
            case "SG":
                return (Criterion) new MonthCriterion();
            case "PN":
                return (Criterion) new NameCriterion();
            case "Pe":
                return (Criterion) new PreniumAccountCriterion();
            case "PP":
            case "Pp":
                return (Criterion) new PvpRankCriterion();
            case "Qa":
                return (Criterion) new QuestActiveCriterion();
            case "Qf":
                return (Criterion) new QuestDoneCriterion();
            case "Qc":
                return (Criterion) new QuestStartableCriterion();
            case "Pq":
                return (Criterion) new RankCriterion();
            case "Pf":
                return (Criterion) new RideCriterion();
            case "SI":
                return (Criterion) new ServerCriterion();
            case "PS":
                return (Criterion) new SexCriterion();
            case "Pi":
            case "PI":
                return (Criterion) new SkillCriterion();
            case "PA":
                return (Criterion) new SoulStoneCriterion();
            case "Pr":
                return (Criterion) new SpecializationCriterion();
            case "Sc":
                return (Criterion) new StaticCriterion();
            case "PB":
                return (Criterion) new SubAreaCriterion();
            case "PZ":
                return (Criterion) new SubscribeCriterion();
            case "BI":
                return (Criterion) new UnusableCriterion();
            case "PW":
                return (Criterion) new WeightCriterion();
            default:
                throw new Error(String.format("Criterion %s doesn't not exist or not handled", name));
        }
    }

    public abstract void Build();

    protected boolean Compare(Object obj, Object comparand) {
        switch (this.Operator) {
            case EQUALS:
                return obj.equals(comparand);
            case INEQUALS:
                return !obj.equals(comparand);
            default:
                throw new NotImplementedException(String.format("Cannot use {0} comparator on objects {1} and {2}", this.Operator, obj, comparand));
        }
    }

    protected <T> boolean Compare(Comparable<T> obj, T comparand) {
        int num = obj.compareTo(comparand);
        switch (this.Operator) {
            case EQUALS:
                return num == 0;
            case INEQUALS:
                return num != 0;
            case SUPERIOR:
                return num > 0;
            case INFERIOR:
                return num < 0;
            default:
                throw new NotImplementedException(String.format("Cannot use {0} comparator on IComparable {1} and {2}", this.Operator, obj, comparand));
        }
    }

    protected boolean Compare(String str, String comparand) {
        switch (this.Operator) {
            case EQUALS:
                return str.equals(comparand);
            case INEQUALS:
                return !str.equals(comparand);
            case LIKE:
                //return str.Equals(comparand, StringComparison.InvariantCultureIgnoreCase);
                return str.equalsIgnoreCase(comparand);
            case STARTWITH:
                return str.startsWith(comparand);
            case STARTWITHLIKE:
                return str.toLowerCase().startsWith(comparand.toLowerCase());
            case ENDWITH:
                return str.endsWith(comparand);
            case ENDWITHLIKE:
                return str.toLowerCase().endsWith(comparand.toLowerCase());
            default:
                throw new NotImplementedException(String.format("Cannot use {0} comparator on strings '{1}' and '{2}'", this.Operator, str, comparand));
        }
    }

    protected String FormatToString(String identifier) {
        return String.format("{0}{1}{2}", identifier, Criterion.GetOperatorChar(this.Operator), this.Literal);
    }

}
