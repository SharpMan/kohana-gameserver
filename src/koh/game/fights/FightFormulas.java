package koh.game.fights;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import koh.game.dao.ExpDAO;
import koh.game.entities.actors.character.ScoreType;
import koh.game.entities.guild.GuildMember;
import koh.game.entities.item.EffectHelper;
import koh.game.fights.fighters.CharacterFighter;
import koh.game.utils.Settings;
import koh.protocol.client.enums.AlignmentSideEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.context.mount.MountSetMessage;

/**
 *
 * @author Neo-Craft
 */
public class FightFormulas {

    public static short CalculateEarnedDishonor(Fighter Character) {
        return Character.Fight.GetEnnemyTeam(Character.Team).AlignmentSide != AlignmentSideEnum.ALIGNMENT_NEUTRAL ? (short) 0 : (short) 1;
    }

    public static short HonorPoint(Fighter Fighter, Stream<Fighter> Winners, Stream<Fighter> Lossers, boolean isLosser) {
        return HonorPoint(Fighter, Winners, Lossers, isLosser, true);
    }

    public static short HonorPoint(Fighter Fighter, Stream<Fighter> Winners, Stream<Fighter> Lossers, boolean isLosser, boolean End) {

        if (Fighter.Fight.GetEnnemyTeam(Fighter.Team).AlignmentSide == AlignmentSideEnum.ALIGNMENT_NEUTRAL) {
            return (short) 0;
        }

        if (System.currentTimeMillis() - Fighter.Fight.FightTime > 2 * 60 * 1000) {
            ((CharacterFighter) Fighter).Character.addScore(isLosser ? ScoreType.PVP_LOOSE : ScoreType.PVP_WIN);
        }

        if (End && Fighter.Fight.GetWinners().GetFighters().count() == 1L && Fighter.Fight.GetWinners().GetFighters().count() == Fighter.Fight.GetEnnemyTeam(Fighter.Fight.GetWinners()).GetFighters().count()) {
            return isLosser ? CalculLooseHonor(Winners, Lossers) : CalculWinHonor(Winners, Lossers);
        }

        double num1 = (double) Winners.mapToInt(x -> x.Level()).sum();
        double num2 = (double) Lossers.mapToInt(x -> x.Level()).sum();
        double num3 = Math.floor(Math.sqrt((double) Fighter.Level()) * 10.0 * (num2 / num1));
        if (isLosser) {
            if (num3 > ((CharacterFighter) Fighter).Character.Honor) {
                num3 = -(short) ((CharacterFighter) Fighter).Character.Honor;
            } else {
                num3 = -num3;
            }
        }
        return (short) num3;
    }

    public static long XPDefie(Fighter Fighter, Stream<Fighter> Winners, Stream<Fighter> Lossers) {

        int lvlLoosers = Lossers.mapToInt(x -> x.Level()).sum();
        int lvlWinners = Winners.mapToInt(x -> x.Level()).sum();

        int taux = Settings.GetIntElement("Rate.Challenge");
        float rapport = (float) lvlLoosers / (float) lvlWinners;
        int malus = 1;
        if ((double) rapport < 0.84999999999999998D) {
            malus = 6;
        }
        if (rapport >= 1.0F) {
            malus = 1;
        }
        long xpWin = (long) (((((rapport * (float) XpNeededAtLevel(Fighter.Level())) / 10F) * (float) taux) / (long) malus) * (1 + (Fighter.Stats.GetTotal(StatsEnum.Wisdom) * 0.01)));
        return xpWin;
    }

    private static long XpNeededAtLevel(int lvl) {
        return (ExpDAO.PersoXpMax(lvl) - ExpDAO.PersoXpMin(lvl == 200 ? 199 : lvl));
    }

    public static long GuildXpEarned(CharacterFighter Fighter, AtomicReference<Long> xpWin) {
        if (Fighter.Character == null) {
            return 0;
        }
        if (Fighter.Character.Guild == null) {
            return 0;
        }

        GuildMember gm = Fighter.Character.GuildMember();

        double xp = (double) xpWin.get(), Lvl = Fighter.Level(), LvlGuild = Fighter.Character.Guild.Entity.Level, pXpGive = (double) gm.experienceGivenPercent / 100;

        double maxP = xp * pXpGive * 0.10;	//Le maximum donné à la guilde est 10% du montant prélevé sur l'xp du combat
        double diff = Math.abs(Lvl - LvlGuild);	//Calcul l'écart entre le niveau du personnage et le niveau de la guilde
        double toGuild;
        if (diff >= 70) {
            toGuild = maxP * 0.10;	//Si l'écart entre les deux level est de 70 ou plus, l'experience donnée a la guilde est de 10% la valeur maximum de don
        } else if (diff >= 31 && diff <= 69) {
            toGuild = maxP - ((maxP * 0.10) * (Math.floor((diff + 30) / 10)));
        } else if (diff >= 10 && diff <= 30) {
            toGuild = maxP - ((maxP * 0.20) * (Math.floor(diff / 10)));
        } else //Si la différence est [0,9]
        {
            toGuild = maxP;
        }
        xpWin.set((long) (xp - xp * pXpGive));

        Fighter.Character.Guild.onFighterAddedExperience(gm, (long) Math.round(toGuild));

        return (long) Math.round(toGuild);
    }

    public static long MountXpEarned(CharacterFighter Fighter, AtomicReference<Long> xpWin) {
        if (Fighter == null) {
            return 0;
        }
        if (!Fighter.Character.MountInfo.isToogled) {
            return 0;
        }

        int diff = Math.abs(Fighter.Level() - Fighter.Character.MountInfo.Mount.level);

        double coeff = 0;
        double xp = (double) xpWin.get();
        double pToMount = (double) Fighter.Character.MountInfo.Ratio / 100 + 0.2;

        if (diff >= 0 && diff <= 9) {
            coeff = 0.1;
        } else if (diff >= 10 && diff <= 19) {
            coeff = 0.08;
        } else if (diff >= 20 && diff <= 29) {
            coeff = 0.06;
        } else if (diff >= 30 && diff <= 39) {
            coeff = 0.04;
        } else if (diff >= 40 && diff <= 49) {
            coeff = 0.03;
        } else if (diff >= 50 && diff <= 59) {
            coeff = 0.02;
        } else if (diff >= 60 && diff <= 69) {
            coeff = 0.015;
        } else {
            coeff = 0.01;
        }

        if (pToMount > 0.2) {
            xpWin.set((long) (xp - (xp * (pToMount - 0.2))));
        }

        Fighter.Character.MountInfo.addExperience((long) Math.round(xp * pToMount * coeff));

        if (xp > 0) {
            Fighter.Character.Send(new MountSetMessage(Fighter.Character.MountInfo.Mount));
        }

        return (long) Math.round(xp * pToMount * coeff);
    }

    public static short CalculWinHonor(Stream<Fighter> winners, Stream<Fighter> loosers) {
        try {
            int TotalGradeWinner = 0;
            int TotalGradeLooser = 0;
            int TotalGradeWinnerForEached = 0;
            int TotalGradeLooserForEached = 0;
            for (Fighter fighter : (Iterable<Fighter>) winners::iterator) {
                if (!(fighter instanceof CharacterFighter) /*&& fighter.getPrisme() == null*/) {
                    continue;
                }
                if (fighter instanceof CharacterFighter) {
                    TotalGradeWinner += ((CharacterFighter) fighter).Character.AlignmentGrade;
                } /*else {
                 TotalGradeWinner += fighter.getPrisme().getLevel();
                 }*/

                TotalGradeWinnerForEached++;
            }
            for (Fighter fighter : (Iterable<Fighter>) loosers::iterator) {
                if (!(fighter instanceof CharacterFighter) /*&& fighter.getPrisme() == null*/) {
                    continue;
                }
                if (fighter instanceof CharacterFighter) {
                    TotalGradeLooser += ((CharacterFighter) fighter).Character.AlignmentGrade;
                } /*else {
                 TotalGradeLooser += fighter.getPrisme().getLevel();
                 }*/

                TotalGradeLooserForEached++;
            }
            int EcartGrade = (TotalGradeWinner / TotalGradeWinnerForEached) - (TotalGradeLooser / TotalGradeLooserForEached);
            int RandomGain = EffectHelper.RandomValue(100, 120);
            int RandomGain2 = EffectHelper.RandomValue(140, 160);
            return (short) (TotalGradeWinner <= 5 ? (RandomGain + (EcartGrade < 0 ? 10 * -EcartGrade : 0)) : (RandomGain2 + (EcartGrade < 0 ? 10 * -EcartGrade : 7 * -EcartGrade)));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static short CalculLooseHonor(Stream<Fighter> loosers, Stream<Fighter> winners) {
        try {
            int TotalGradeWinner = 0;
            int TotalGradeLooser = 0;
            int TotalGradeWinnerForEached = 0;
            int TotalGradeLooserForEached = 0;
            for (Fighter fighter : (Iterable<Fighter>) winners::iterator) {
                if (!(fighter instanceof CharacterFighter) /*&& fighter.getPrisme() == null*/) {
                    continue;
                }
                if (fighter instanceof CharacterFighter) {
                    TotalGradeWinner += ((CharacterFighter) fighter).Character.AlignmentGrade;
                } /*else {
                 TotalGradeWinner += fighter.getPrisme().getLevel();
                 }*/

                TotalGradeWinnerForEached++;
            }
            for (Fighter fighter : (Iterable<Fighter>) loosers::iterator) {
                if (!(fighter instanceof CharacterFighter) /*&& fighter.getPrisme() == null*/) {
                    continue;
                }
                if (fighter instanceof CharacterFighter) {
                    TotalGradeLooser += ((CharacterFighter) fighter).Character.AlignmentGrade;
                } /*else {
                 TotalGradeLooser += fighter.getPrisme().getLevel();
                 }*/

                TotalGradeLooserForEached++;
            }
            int EcartGrade = (TotalGradeWinner / TotalGradeWinnerForEached) - (TotalGradeLooser / TotalGradeLooserForEached);
            int RandomPerte = 0;
            switch (TotalGradeWinner) {
                case 1:
                    RandomPerte = EffectHelper.RandomValue(40, 50);
                    break;
                case 2:
                    RandomPerte = EffectHelper.RandomValue(50, 60);
                    break;
                case 3:
                    RandomPerte = EffectHelper.RandomValue(60, 70);
                    break;
                case 4:
                    RandomPerte = EffectHelper.RandomValue(70, 80);
                    break;
                case 5:
                    RandomPerte = EffectHelper.RandomValue(80, 90);
                    break;
                case 6:
                    RandomPerte = EffectHelper.RandomValue(100, 200);
                    break;
                case 7:
                    RandomPerte = EffectHelper.RandomValue(200, 300);
                    break;
                case 8:
                    RandomPerte = EffectHelper.RandomValue(300, 400);
                    break;
                case 9:
                    RandomPerte = EffectHelper.RandomValue(400, 500);
                    break;
                case 10:
                    RandomPerte = 500;
                    break;
            }
            return (short) -(TotalGradeWinner <= 5 ? (RandomPerte + (EcartGrade > 0 ? 10 * EcartGrade : 0)) : (RandomPerte + (EcartGrade > 0 ? 20 * EcartGrade : 0)));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
