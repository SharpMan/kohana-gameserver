package koh.game.conditions;

import org.apache.commons.lang3.NotImplementedException;

/**
 *
 * @author Neo-Craft
 */
public abstract class Criterion extends ConditionExpression {

    public ComparaisonOperatorEnum operator;

    public String literal;

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
        if (StatsCriterion.isStatsIdentifier(name)) {
           return new StatsCriterion(name);
        }
        switch (name) {
            case "PX":
                return  new AdminRightsCriterion();
            case "Pa":
                return new AlignementLevelCriterion();
            case "Ps":
               return new AlignmentCriterion();
            case "PU":
               return new BonesCriterion();
            case "PG":
               return new BreedCriterion();
            case "PE":
               return new EmoteCriterion();
            case "Pb":
               return new FriendListCriterion();
            case "Pg":
               return new GiftCriterion();
            case "PO":
               return new HasItemCriterion();
            case "PJ":
               return new JobCriterion();
            case "Pk":
               return new PanoplieBonusCriterion();
            case "PK":
               return new KamaCriterion();
            case "PL":
               return new LevelCriterion();
            case "MK":
               return new MapCharactersCriterion();
            case "PR":
               return new MariedCriterion();
            case "P¨Q":
               return new MaxRankCriterion();
           /* case "SG":
               return new MonthCriterion();*/
            case "PN":
               return new NameCriterion();
            case "Pe":
               return new PreniumAccountCriterion();
            case "PP":
            case "Pp":
               return new PvpRankCriterion();
            case "Qa":
               return new QuestActiveCriterion();
            case "Qf":
               return new QuestDoneCriterion();
            case "Qc":
               return new QuestStartableCriterion();
            case "Pq":
               return new RankCriterion();
            case "Pf":
               return new RideCriterion();
            case "SI":
               return new ServerCriterion();
            case "PS":
               return new SexCriterion();
            case "Pi":
            case "PI":
               return new SkillCriterion();
            case "PA":
               return new SoulStoneCriterion();
            case "Pr":
               return new SpecializationCriterion();
            case "Sc":
               return new StaticCriterion();
            case "Sd":
                return new DayItemCriterion();
            case "SG":
                return new MonthItemCriterion();
            case "PB":
               return new SubAreaCriterion();
            case "PZ":
               return new SubscribeCriterion();
            case "BI":
               return new UnusableCriterion();
            case "PW":
               return new WeightCriterion();
            default:
                throw new Error(String.format("Criterion %s doesn't not exist or not handled", name));
        }
    }

    public abstract void Build();

    protected boolean Compare(Object obj, Object comparand) {
        switch (this.operator) {
            case EQUALS:
                return obj.equals(comparand);
            case INEQUALS:
                return !obj.equals(comparand);
            default:
                throw new NotImplementedException(String.format("Cannot use {0} comparator on objects {1} and {2}", this.operator, obj, comparand));
        }
    }

    protected <T> boolean Compare(Comparable<T> obj, T comparand) {
        final int num = obj.compareTo(comparand);
        switch (this.operator) {
            case EQUALS:
                return num == 0;
            case INEQUALS:
                return num != 0;
            case SUPERIOR:
                return num > 0;
            case INFERIOR:
                return num < 0;
            default:
                throw new NotImplementedException(String.format("Cannot use {0} comparator on IComparable {1} and {2}", this.operator, obj, comparand));
        }
    }

    protected boolean Compare(String str, String comparand) {
        switch (this.operator) {
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
                throw new NotImplementedException(String.format("Cannot use {0} comparator on strings '{1}' and '{2}'", this.operator, str, comparand));
        }
    }

    protected String FormatToString(String identifier) {
        return String.format("{0}{1}{2}", identifier, Criterion.GetOperatorChar(this.operator), this.literal);
    }

}
