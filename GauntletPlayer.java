// ============================================================
//  INHERITANCE — GauntletPlayer extends Combatant
//  POLYMORPHISM — overrides decideAction() to signal GUI input
// ============================================================
public class GauntletPlayer extends Combatant {

    private final String lore;

    public GauntletPlayer(String name, int hp, int attack,
                          String skill1, String skill2, String skill3, String ultimate,
                          int sk1Cost, int sk2Cost, int sk3Cost, int ultCost,
                          int sk1Dmg,  int sk2Dmg,  int sk3Dmg,  int ultDmg,
                          String lore) {
        super(name, hp, attack,
              skill1, skill2, skill3, ultimate,
              sk1Cost, sk2Cost, sk3Cost, ultCost,
              sk1Dmg, sk2Dmg, sk3Dmg, ultDmg);
        this.lore = lore;
    }

    // -1 = GUI handles input, not AI
    @Override
    public int decideAction() { return -1; }

    public String getLore() { return lore; }

    // ── Static factory — matches CharacterSelector roster ─────────────────
    public static GauntletPlayer fromName(String name) {
        switch (name) {
            case "Iron Man":
                return new GauntletPlayer("Iron Man", 110, 18,
                    "Repulsor Blast", "Micro-Missiles", "Shield Flare", "Unibeam Overload",
                    15, 20, 25, 40, 20, 28, 35, 55,
                    "Billionaire genius in a high-tech suit.");
            case "Captain America":
                return new GauntletPlayer("Captain America", 130, 15,
                    "Shield Throw", "Vibranium Bash", "Tactical Command", "Avengers Assemble",
                    15, 20, 25, 40, 18, 25, 30, 50,
                    "Super-Soldier with an unbreakable shield.");
            case "Thor":
                return new GauntletPlayer("Thor", 150, 22,
                    "Hammer Toss", "Lightning Strike", "Thunder Clap", "God Blast",
                    15, 20, 25, 40, 25, 32, 38, 60,
                    "God of Thunder, Crown Prince of Asgard.");
            case "Spider-Man":
                return new GauntletPlayer("Spider-Man", 100, 14,
                    "Web Snare", "Spider-Sense Dodge", "Swing Kick", "Maximum Spider",
                    15, 20, 25, 40, 16, 22, 28, 45,
                    "Friendly neighborhood hero with spider-sense.");
            case "Hulk":
                return new GauntletPlayer("Hulk", 200, 25,
                    "Gamma Punch", "Thunderclap", "Ground Smash", "Worldbreaker Slam",
                    15, 20, 25, 40, 28, 35, 42, 65,
                    "The strongest Avenger — anger makes him stronger.");
            case "Black Widow":
                return new GauntletPlayer("Black Widow", 95, 20,
                    "Widow's Bite", "Dual Pistols", "Staff Strike", "Lullaby Takedown",
                    15, 20, 25, 40, 22, 28, 32, 50,
                    "World's most elite spy and infiltration expert.");
            case "Ant-Man":
                return new GauntletPlayer("Ant-Man", 105, 13,
                    "Size Shift", "Ant Swarm", "Pym Disk", "Giant-Man Stomp",
                    15, 20, 25, 40, 15, 20, 26, 44,
                    "Master of size manipulation and Pym Particles.");
            case "The Falcon":
                return new GauntletPlayer("The Falcon", 110, 16,
                    "Wing Shield", "Redwing Strike", "Aerial Dive", "Flight Form Alpha",
                    15, 20, 25, 40, 18, 24, 30, 48,
                    "Veteran pararescueman with high-tech wings.");
            case "Thanos":
                return new GauntletPlayer("Thanos", 500, 50,
                    "Titan Punch", "Energy Beam", "Reality Warp", "The Snap",
                    15, 20, 25, 40, 55, 65, 75, 100,
                    "The Mad Titan with the Infinity Gauntlet.");
            default:
                return new GauntletPlayer("Iron Man", 110, 18,
                    "Repulsor Blast", "Micro-Missiles", "Shield Flare", "Unibeam Overload",
                    15, 20, 25, 40, 20, 28, 35, 55,
                    "Billionaire genius.");
        }
    }
}