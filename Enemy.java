import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Enemy — extends Combatant, provides AI opponents for Gauntlet mode.
 * Merges former GauntletAI logic; use getRandom() for Gauntlet battles.
 */
public class Enemy extends Combatant {

    public enum EnemyTier { EASY, MEDIUM, HARD, BOSS }

    private static final Random rand = new Random();
    private final EnemyTier tier;

    public Enemy(String name, int hp, int attack,
                 String skill1, String skill2, String skill3, String ultimate,
                 int sk1Cost, int sk2Cost, int sk3Cost, int ultCost,
                 int sk1Dmg, int sk2Dmg, int sk3Dmg, int ultDmg,
                 EnemyTier tier) {
        super(name, hp, attack, skill1, skill2, skill3, ultimate,
              sk1Cost, sk2Cost, sk3Cost, ultCost, sk1Dmg, sk2Dmg, sk3Dmg, ultDmg);
        this.tier = tier;
    }

    @Override
    public int decideAction() {
        if (canUse(4) && getHp() < getMaxHp() * 0.4) return 4;
        if (rand.nextInt(100) < 65) {
            List<Integer> available = new ArrayList<>();
            if (canUse(3)) available.add(3);
            if (canUse(2)) available.add(2);
            if (canUse(1)) available.add(1);
            if (!available.isEmpty())
                return available.get(rand.nextInt(available.size()));
        }
        return 0;
    }

    public EnemyTier getTier() { return tier; }

    // ── Gauntlet mode: random opponent (Avenger roster, same as old GauntletAI)
    public static Enemy getRandom() {
        String[] names = {
            "Iron Man", "Captain America", "Thor", "Spider-Man",
            "Hulk", "Black Widow", "Ant-Man", "The Falcon"
        };
        return fromAvengerName(names[rand.nextInt(names.length)]);
    }

    public static Enemy fromAvengerName(String name) {
        switch (name) {
            case "Iron Man":
                return new Enemy("Iron Man", 110, 18,
                    "Repulsor Blast", "Micro-Missiles", "Shield Flare", "Unibeam Overload",
                    15, 20, 25, 40, 20, 28, 35, 55, EnemyTier.MEDIUM);
            case "Captain America":
                return new Enemy("Captain America", 130, 15,
                    "Shield Throw", "Vibranium Bash", "Tactical Command", "Avengers Assemble",
                    15, 20, 25, 40, 18, 25, 30, 50, EnemyTier.MEDIUM);
            case "Thor":
                return new Enemy("Thor", 150, 22,
                    "Hammer Toss", "Lightning Strike", "Thunder Clap", "God Blast",
                    15, 20, 25, 40, 25, 32, 38, 60, EnemyTier.MEDIUM);
            case "Spider-Man":
                return new Enemy("Spider-Man", 100, 14,
                    "Web Snare", "Spider-Sense Dodge", "Swing Kick", "Maximum Spider",
                    15, 20, 25, 40, 16, 22, 28, 45, EnemyTier.MEDIUM);
            case "Hulk":
                return new Enemy("Hulk", 200, 25,
                    "Gamma Punch", "Thunderclap", "Ground Smash", "Worldbreaker Slam",
                    15, 20, 25, 40, 28, 35, 42, 65, EnemyTier.MEDIUM);
            case "Black Widow":
                return new Enemy("Black Widow", 95, 20,
                    "Widow's Bite", "Dual Pistols", "Staff Strike", "Lullaby Takedown",
                    15, 20, 25, 40, 22, 28, 32, 50, EnemyTier.MEDIUM);
            case "Ant-Man":
                return new Enemy("Ant-Man", 105, 13,
                    "Size Shift", "Ant Swarm", "Pym Disk", "Giant-Man Stomp",
                    15, 20, 25, 40, 15, 20, 26, 44, EnemyTier.MEDIUM);
            case "The Falcon":
            default:
                return new Enemy("The Falcon", 110, 16,
                    "Wing Shield", "Redwing Strike", "Aerial Dive", "Flight Form Alpha",
                    15, 20, 25, 40, 18, 24, 30, 48, EnemyTier.MEDIUM);
        }
    }

    // ── Villain database for potential future modes
    private static final List<Enemy> VILLAIN_DATABASE = List.of(
        new Enemy("Loki", 100, 15, "Illusion Sneak Attack", "Scepter Strike", "Mind Control", "Full Scepter Power",
            15, 20, 25, 40, 15, 20, 25, 45, EnemyTier.EASY),
        new Enemy("Mystique", 100, 16, "Shape Shift Strike", "Mimic Attack", "Invisible Hit", "Perfect Mimicry",
            15, 20, 25, 40, 15, 20, 25, 46, EnemyTier.EASY),
        new Enemy("Green Goblin", 100, 16, "Pumpkin Bomb", "Glider Attack", "Goblin's Rage", "Goblin Fury",
            15, 20, 25, 40, 15, 20, 25, 46, EnemyTier.EASY),
        new Enemy("Ultron", 110, 18, "Laser Blast", "Metal Punch", "Flight Thrust Attack", "Overdrive Assault",
            15, 15, 20, 40, 15, 15, 20, 50, EnemyTier.MEDIUM),
        new Enemy("Red Skull", 110, 16, "Cosmic Blast", "Tactical Strike", "Rally Troops", "Cosmic Surge",
            15, 15, 20, 40, 15, 15, 20, 48, EnemyTier.MEDIUM),
        new Enemy("Electro", 110, 17, "Electric Shock", "Thunderbolt", "Overcharge", "Supercharge",
            15, 15, 20, 40, 15, 15, 20, 49, EnemyTier.MEDIUM),
        new Enemy("Venom", 120, 17, "Symbiote Strike", "Web Trap", "Rage", "Symbiote Rampage",
            12, 13, 15, 40, 12, 13, 15, 52, EnemyTier.HARD),
        new Enemy("Doctor Octopus", 120, 17, "Tentacle Slam", "Mechanical Grab", "Overload", "Octopus Assault",
            12, 13, 15, 40, 12, 13, 15, 52, EnemyTier.HARD),
        new Enemy("Magneto", 120, 18, "Magnetic Pulse", "Metal Manipulation", "Force Field Attack", "Magnetic Storm",
            12, 13, 15, 40, 12, 13, 15, 54, EnemyTier.HARD),
        new Enemy("Thanos", 130, 20, "Power Stone Punch", "Space Stone Snap", "Reality Warp", "The Snap",
            18, 20, 22, 45, 18, 20, 22, 60, EnemyTier.BOSS),
        new Enemy("Galactus", 150, 25, "Cosmic Blast", "Planet Devourer", "Universal Destruction", "Galactus Unleashed",
            20, 25, 30, 50, 20, 25, 30, 70, EnemyTier.BOSS),
        new Enemy("Dormammu", 140, 22, "Dark Dimension", "Flame Wave", "Mind Possession", "Dimension Collapse",
            18, 22, 25, 48, 18, 22, 25, 65, EnemyTier.BOSS)
    );

    public static Enemy getRandomEnemy() {
        return cloneVillain(VILLAIN_DATABASE.get(rand.nextInt(VILLAIN_DATABASE.size())));
    }

    public static Enemy getRandomEnemyByLevel(int level) {
        EnemyTier tier;
        switch (level) {
            case 1: tier = EnemyTier.EASY; break;
            case 2: tier = EnemyTier.MEDIUM; break;
            case 3: tier = EnemyTier.HARD; break;
            case 4: tier = EnemyTier.BOSS; break;
            default: return getRandomEnemy();
        }
        List<Enemy> filtered = new ArrayList<>();
        for (Enemy e : VILLAIN_DATABASE) if (e.tier == tier) filtered.add(e);
        return cloneVillain(filtered.get(rand.nextInt(filtered.size())));
    }

    public static Enemy getEnemyByName(String name) {
        for (Enemy e : VILLAIN_DATABASE)
            if (e.getName().equalsIgnoreCase(name)) return cloneVillain(e);
        return getRandomEnemy();
    }

    private static Enemy cloneVillain(Enemy e) {
        return new Enemy(e.getName(), e.getMaxHp(), e.getAttack(),
            e.getSkill1Name(), e.getSkill2Name(), e.getSkill3Name(), e.getUltimateName(),
            e.getSk1Cost(), e.getSk2Cost(), e.getSk3Cost(), e.getUltCost(),
            e.getActionDamage(1), e.getActionDamage(2), e.getActionDamage(3), e.getActionDamage(4),
            e.tier);
    }
}
