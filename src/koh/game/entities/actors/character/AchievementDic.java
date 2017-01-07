package koh.game.entities.actors.character;

import koh.game.dao.DAO;
import koh.game.entities.achievement.AchievementTemplate;
import koh.game.entities.actors.Player;
import koh.protocol.messages.game.achievement.AchievementFinishedMessage;
import koh.protocol.types.game.achievement.AchievementObjective;
import koh.protocol.types.game.achievement.AchievementStartedObjective;
import koh.utils.Couple;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Melancholia on 1/3/17.
 */
public class AchievementDic {


    //15 id, level
    private static final HashMap<Integer, Integer> levelAchivements = new HashMap<Integer, Integer>(7) {{
        this.put(3,10);
        this.put(4,20);
        this.put(5,40);
        this.put(6,60);
        this.put(7,80);
        this.put(8,100);
        this.put(9,120);
        this.put(10,140);
        this.put(11,160);
        this.put(12,180);
        this.put(13,200);
    }};

    //15 id, scores !contain Oa
    private static final HashMap<Integer, Integer> scoreAchivements = new HashMap<Integer, Integer>(7) {{
        this.put(862,1337); // Dimension Obscure
        this.put(863,4000); // Ruelles des Eaux-Suaires
        this.put(864,6000); // Catacombres
        this.put(865,8000); // Hauts Ténébreux
        this.put(1052,500); // Chemins d'hier
        this.put(1053,1000); // Jour présent
        this.put(1133,10000); // Lendemains incertains
        this.put(1382,12000); // Lendemains incertains
    }};

    //AreaID = 53 dimeensions @param0 = zone @param1 = achievement
    private static final HashMap<Integer, Integer> zone67Achivements = new HashMap<Integer, Integer>(70) {{
        this.put(813,1046); // Dimension Obscure
        this.put(827,1110); // Ruelles des Eaux-Suaires
        this.put(828,1111); // Catacombres
        this.put(829,1112); // Hauts Ténébreux
        this.put(836,1176); // Chemins d'hier
        this.put(837,1177); // Jour présent
        this.put(838,1178); // Lendemains incertains
    }};


    //AreaID = 45 palais des lac @param0 = zone @param1 = achievement
    private static final HashMap<Integer, Integer> zone39Achivements = new HashMap<Integer, Integer>(70) {{
        this.put(444,415); // Champs
        this.put(449,416); // Cimetière
        this.put(443,417); // Forêt
        this.put(448,423); // Taverne
    }};

    //AreaID = 28 koalak @param0 = zone @param1 = achievement
    private static final HashMap<Integer, Integer> zone38Achivements = new HashMap<Integer, Integer>(70) {{
        this.put(253,373); // Canyon sauvage
        this.put(230,374); // Cimetière primitif
        this.put(234,375); // Forêt de Kaliptus
        this.put(231,376); // Lacs enchantés
        this.put(232,377); // Marécages nauséabonds
        this.put(233,378); // Marécages sans fond
        this.put(235,379); // Territoire des Dragodindes Sauvages
        this.put(275,380); // Vallée de la Morh'Kitu
        this.put(182,381); // Village des Eleveurs
    }};

    //AreaID = 18 astrub @param0 = zone @param1 = achievement
    private static final HashMap<Integer, Integer> zone37Achivements = new HashMap<Integer, Integer>(70) {{
        this.put(335,339); // Calanques d'Astrub
        this.put(98,340); // Champs d'Astrub
        this.put(95,341); // Cité d'Astrub
        this.put(92,342); // Contour d'Astrub
        this.put(96,344); // Exploitation minière d'Astrub
        this.put(97,345); // Forêt d'Astrub
        this.put(101,357); // Le coin des Tofus
        this.put(173,358); // Prairies d'Astrub
        this.put(99,359); // Souterrains d'Astrub
        this.put(100,360); // Souterrains profonds d'Astrub
    }};

    //AreaID = 12 fungus @param0 = zone @param1 = achievement
    private static final HashMap<Integer, Integer> zone36Achivements = new HashMap<Integer, Integer>(70) {{
        this.put(495,333); // Caverne des Fungus
        this.put(61,334); // Cimetière des Torturés
        this.put(71,335); // Gisgoul, le village dévasté
        this.put(526,337); // Route Sombre
        this.put(517,338); // Terres Désacrées
    }};

    //AreaID = 11 brakmar @param0 = zone @param1 = achievement
    private static final HashMap<Integer, Integer> zone35Achivements = new HashMap<Integer, Integer>(70) {{
        this.put(280,319); // Bordure de Brâkmar
        this.put(513,320); // Centre-ville
        this.put(531,322); // Quartier des Alchimistes
        this.put(506,323); // Quartier des Bijoutiers
        this.put(503,324); // Quartier des Bouchers
        this.put(505,325); // Quartier des Boulangers
        this.put(509,326); // Quartier des Bricoleurs
        this.put(502,327); // Quartier des Bûcherons
        this.put(508,328); // Quartier des Forgerons
        this.put(534,329); // Quartier des Pêcheurs
        this.put(507,330); // Quartier des Tailleurs
        this.put(75,332); // Égouts de Brâkmar
    }};

    //AreaID = 8 cania @param0 = zone @param1 = achievement
    private static final HashMap<Integer, Integer> zone34Achivements = new HashMap<Integer, Integer>(70) {{
        this.put(334,300); // Baie de Cania
        this.put(69,301); // Bois de Litneg
        this.put(59,302); // Cimetière de Bonta
        this.put(523,303); // Cirque de Cania
        this.put(519,304); // Dents de Pierre
        this.put(56,305); // Forêt de Cania
        this.put(520,306); // Lac de Cania
        this.put(518,307); // Landes de Cania
        this.put(68,308); // Les Champs de Cania
        this.put(54,309); // Massif de Cania
        this.put(521,310); // Pics de Cania
        this.put(38,311); // Plaine de Cania
        this.put(178,312); // Plaine des Porkass
        this.put(70,313); // Plaines Rocheuses
        this.put(55,314); // Pénates du Corbac
        this.put(527,315); // Rocaille
        this.put(522,316); // Route de Brâkmar
        this.put(525,317); // Route de la Côte
        this.put(524,318); // Route de la Roche
    }};

    //AreaID = 7 bonta @param0 = zone @param1 = achievement
    private static final HashMap<Integer, Integer> zone33Achivements = new HashMap<Integer, Integer>(70) {{
        this.put(513,286); // Centre-ville
        this.put(43,287); // Fortification de Bonta
        this.put(279,288); // Pâturages de Bonta
        this.put(531,289); // Quartier des Alchimistes
        this.put(506,290); // Quartier des Bijoutiers
        this.put(503,291); // Quartier des Bouchers
        this.put(505,292); // Quartier des Boulangers
        this.put(509,293); // Quartier des Bricoleurs
        this.put(502,294); // Quartier des Bûcherons
        this.put(508,295); // Quartier des Forgerons
        this.put(534,296); // Quartier des Pêcheurs
        this.put(507,297); // Quartier des Tailleurs
        this.put(73,299); // Égouts de Bonta
    }};

    //AreaID = 19 pandala @param0 = zone @param1 = achievement
    private static final HashMap<Integer, Integer> zone24Achivements = new HashMap<Integer, Integer>(70) {{
        this.put(529,361); // Faubourgs de Pandala
        this.put(171,362); // Forêt de Pandala
        this.put(152,363); // L'île de Grobe
        this.put(777,364); // Le pichon frétillant
        this.put(105,365); // Pandala Neutre
        this.put(476,366); // Pont de Grobe
        this.put(143,367); // Pont de Pandala
        this.put(119,368); // Village de Pandala
        this.put(106,369); // Bordure d'Akwadala
        this.put(109,370); // Bordure de Terrdala
        this.put(107,371); // Bordure de Feudala
        this.put(108,372); // Bordure d'Aerdala
    }};

    //AreaID = 30 minoto @param0 = zone @param1 = achievement
    private static final HashMap<Integer, Integer> zone22Achivements = new HashMap<Integer, Integer>(70) {{
        this.put(209,382); // L'île du Minotoror
        this.put(319,383); // Le labyrinthe du Minotoror
    }};

    //AreaID = 49 Sakaii @param0 = zone @param1 = achievement
    private static final HashMap<Integer, Integer> zone21Achivements = new HashMap<Integer, Integer>(70) {{
        this.put(652,411); // Forêt enneigée
        this.put(651,412); // Plaine de Sakaï
        this.put(650,413); // Port de Sakaï
    }};

    //AreaID = 2 Moon @param0 = zone @param1 = achievement
    private static final HashMap<Integer, Integer> zone20Achivements = new HashMap<Integer, Integer>(70) {{
        this.put(165,276); // La jungle profonde de Moon
        this.put(93,277); // La plage de Moon
        this.put(167,278); // Le bateau pirate
        this.put(166,279); // Le chemin vers Moon
    }};

    //AreaID = 1 Wabbit @param0 = zone @param1 = achievement 19
    private static final HashMap<Integer, Integer> zone19Achivements = new HashMap<Integer, Integer>(70) {{
        this.put(25,271); // Souterrains des Wabbits
        this.put(161,272); // Île de la Cawotte
        this.put(163,273); // Îlot des Tombeaux
        this.put(164,274); // Îlot de la Couronne
        this.put(162,275); // Îlot de Waldo
    }};



    //AreaID = 46 Otomaii @param0 = zone @param1 = achievement
    private static final HashMap<Integer, Integer> zone29Achivements = new HashMap<Integer, Integer>(70) {{
        this.put(451, 385); // Ile des naufragés
        this.put(455, 386); // Jungle obscure
        this.put(461, 387); // L'arche d'Otomaï
        this.put(466, 388); // Le village côtier
        this.put(465, 389); // Le village des éleveurs
        this.put(453, 390); // Plage de Corail
        this.put(454, 391); // Plaines herbeuses
        this.put(471, 392); // Tourbière nauséabonde
        this.put(457, 393); // Tourbière sans fond
        this.put(464, 394); // Tronc de l'arbre Hakam
        this.put(469, 395); // Village de la Canopée
    }};

    //AreaID = 48 Frigost @param0 = zone @param1 = achievement
    private static final HashMap<Integer, Integer> zone16Achivements = new HashMap<Integer, Integer>(70) {{
        this.put(601, 396); // La bourgade
        this.put(606, 397); // La crevasse Perge
        this.put(604, 398); // La forêt des pins perdus
        this.put(605, 399); // La forêt pétrifiée
        this.put(625, 400); // La mer Kantil
        this.put(609, 401); // Le berceau d'Alma
        this.put(615, 402); // Le lac gelé
        this.put(611, 403); // Le Mont Torrideau
        this.put(600, 404); // Le port de givre
        this.put(603, 405); // Le village enseveli
        this.put(602, 406); // Les champs de glace
        this.put(608, 407); // Les crocs de verre
        this.put(626, 408); // Les grottes gelées
        this.put(610, 409); // Les larmes d'Ouronigride
        this.put(753, 410); // Ruche des Gloursons
        this.put(795, 904); // Pied de la Tour de la Clepsydre
        this.put(789, 905); // Tour de la Clepsydre
        this.put(788, 906); // Bastion des froides légions
        this.put(787, 907); // Tannerie Écarlate
        this.put(786, 908); // Remparts à vent
        this.put(785, 909); // Jardins d'Hiver
        this.put(834, 1135); // Roc des Salbatroces
    }};

    //AreaID = 0 Amakna @param0 = zone @param1 = achievement
    private static final HashMap<Integer, Integer> zone17Achivements = new HashMap<Integer, Integer>(70) {{
        this.put(8, 237); // Campement des Bwork
        this.put(481, 239); // Clairière de Brouce Boulgour
        this.put(22, 236); // Bord de la forêt maléfique
        this.put(276, 238); // Campement des Gobelins
        this.put(27, 240); // Côte d'Asse
        this.put(485, 241); // La campagne
        this.put(4, 242); // La forêt d'Amakna
        this.put(9, 243); // La forêt maléfique
        this.put(482, 244); // La Millifutaie
        this.put(2, 245); // La montagne des Craqueleurs
        this.put(23, 246); // La presqu'île des Dragoeufs
        this.put(12, 247); // La péninsule des gelées
        this.put(3, 248); // Le champ des Ingalsses
        this.put(102, 249); // Le champ du repos
        this.put(180, 250); // Le château d'Amakna
        this.put(6, 251); // Le cimetière
        this.put(179, 252); // Le coin des Boos
        this.put(5, 253); // Le coin des Bouftous
        this.put(31, 254); // Le marécage
        this.put(10, 255); // Le village
        this.put(7, 256); // Les cryptes
        this.put(480, 257); // Montagne basse des Craqueleurs
        this.put(492, 258); // Passage vers Brâkmar
        this.put(170, 259); // Plaine des Scarafeuilles
        this.put(1, 260); // Port de Madrestam
        this.put(490, 261); // Rivage sufokien
        this.put(479, 262); // Rivière Kawaii
        this.put(314, 263); // Sanctuaire des Dragoeufs
        this.put(29, 264); // Souterrains
        this.put(316, 265); // Souterrains des Dragoeufs
        this.put(181, 266); // Souterrains du Château d'Amakna
        this.put(103, 267); // Territoire des Bandits
        this.put(11, 268); // Territoire des Porcos
        this.put(277, 269); // Village des Bworks
        this.put(315, 270); // Village des Dragoeufs
        this.put(34, 280); // Prison
        this.put(30, 281); // Le berceau
        this.put(32, 282); // Sufokia
        this.put(169, 283); // Orée de la forêt des Abraknydes
        this.put(33, 284); // Bois des Arak-haï
        this.put(168, 285); // Forêt Sombre
        this.put(804, 1027); // Salles des Embruns
        this.put(805, 1028); // Salles des Courants
        this.put(806, 1029); // Salles des Abîmes
        this.put(801, 1037); // Tunnel de Kartonpath
        this.put(799, 1038); // Île de Kartonpath
        this.put(812, 1039); // Base des Justiciers
    }};

    private static final HashMap<Integer,Couple<Integer,HashMap<Integer, Integer>>> areas = new HashMap<>(11);
    static{
        areas.put(53,new Couple<>(67,zone67Achivements));
        areas.put(45,new Couple<>(39,zone39Achivements));
        areas.put(28,new Couple<>(38,zone38Achivements));
        areas.put(18,new Couple<>(37,zone37Achivements));
        areas.put(12,new Couple<>(36,zone36Achivements));
        areas.put(11,new Couple<>(35,zone35Achivements));
        areas.put(8,new Couple<>(34,zone34Achivements));
        areas.put(7,new Couple<>(33,zone33Achivements));
        areas.put(19,new Couple<>(24,zone24Achivements));
        areas.put(30,new Couple<>(22,zone22Achivements));
        areas.put(49,new Couple<>(21,zone21Achivements));
        areas.put(2,new Couple<>(20,zone20Achivements));
        areas.put(1,new Couple<>(19,zone19Achivements));
        areas.put(46,new Couple<>(29,zone29Achivements));
        areas.put(48,new Couple<>(16,zone16Achivements));
        areas.put(0, new Couple<>(17,zone17Achivements));
    }

    private final static Couple<Integer,Integer> EMPTY_COUPLE = new Couple<Integer,Integer>(-1,-1);

    public static void onSubAreaChange(Player p, int area, int subarea){
        if(true){
            return;
        }
        final Couple<Integer,HashMap<Integer, Integer>> subs = areas.get(area);
        if(subs == null){
            return;
        }
        final Integer achii = subs.second.get(subarea);
        if(achii == null){
            return;
        }
        final Short achi = achii.shortValue();
        if(!p.getAchievements().isUnlocked(achi)){
            p.getAchievements().unlock(achi);
            p.getAchievements().emptyUnlock(subs.first,achi);
            p.send(new AchievementFinishedMessage(achi, (byte) p.getLevel()));
        }
    }

    public static void onScoreAdded(Player p){
        if(true){
            return;
        }
        for (Map.Entry<Integer, Integer> entry : scoreAchivements.entrySet()) {
            if(p.getAchievementPoints() >= entry.getValue() && !p.getAchievements().isUnlocked(entry.getKey().shortValue())){
                final AchievementTemplate temp = DAO.getAchievements().find(entry.getKey());
                final short achi = (short) temp.getId();
                p.getAchievements().unlock(achi);
                p.getAchievements().emptyUnlock(temp.getCategory(), achi);
                p.send(new AchievementFinishedMessage(achi, (byte) p.getLevel()));
            }
        }
    }


    public static void onLevelUpdate(Player p){
        if(true){
            return;
        }
        for (Map.Entry<Integer, Integer> entry : levelAchivements.entrySet()) {
            if(p.getLevel() >= entry.getValue() && !p.getAchievements().isUnlocked(entry.getKey().shortValue())){
                final AchievementTemplate temp = DAO.getAchievements().find(entry.getKey());
                final short achi = (short) temp.getId();
                p.getAchievements().unlock(achi);
                //p.getAchievements().emptyUnlock(temp.getCategory(), achi);
                p.getAchievements().pushInfo(temp.getCategory(), new AchievementBook.AchievementInfo(achi,  new AchievementObjective[0], new AchievementStartedObjective[0],true));
                p.send(new AchievementFinishedMessage(achi, (byte) p.getLevel()));
            }
        }
    }




}
