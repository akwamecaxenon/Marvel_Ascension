// ============================================================
//  ABSTRACTION — Abstract base class that defines the contract
//  all fighters must follow, hiding internal implementation.
// ============================================================
public abstract class Combatant {

    // ENCAPSULATION — all fields private, accessed via getters/setters
    private String name;
    private int    hp;
    private int    maxHp;
    private int    attack;
    private int    mana;
    private final int maxMana = 100;

    private String skill1Name, skill2Name, skill3Name, ultimateName;
    private int    sk1Cost, sk2Cost, sk3Cost, ultCost;
    private int    sk1Damage, sk2Damage, sk3Damage, ultDamage;

    public Combatant(String name, int hp, int attack,
                     String skill1Name, String skill2Name,
                     String skill3Name, String ultimateName,
                     int sk1Cost, int sk2Cost, int sk3Cost, int ultCost,
                     int sk1Damage, int sk2Damage, int sk3Damage, int ultDamage) {
        this.name         = name;
        this.hp           = hp;
        this.maxHp        = hp;
        this.attack       = attack;
        this.mana         = 30; // start with some mana

        this.skill1Name   = skill1Name;
        this.skill2Name   = skill2Name;
        this.skill3Name   = skill3Name;
        this.ultimateName = ultimateName;

        this.sk1Cost   = sk1Cost;
        this.sk2Cost   = sk2Cost;
        this.sk3Cost   = sk3Cost;
        this.ultCost   = ultCost;

        this.sk1Damage = sk1Damage;
        this.sk2Damage = sk2Damage;
        this.sk3Damage = sk3Damage;
        this.ultDamage = ultDamage;
    }

    // --------------------------------------------------------
    //  ABSTRACTION — subclasses MUST define their own AI logic
    // --------------------------------------------------------
    public abstract int decideAction();

    // --------------------------------------------------------
    //  Shared combat logic available to all subclasses
    // --------------------------------------------------------
    public void resetForNewRound() {
        this.hp   = this.maxHp;
        this.mana = 30;
    }

    public boolean isAlive() { return hp > 0; }

    public int takeDamage(int damage) {
        int actual = Math.max(1, damage);
        hp = Math.max(0, hp - actual);
        return actual;
    }

    public void restoreMana(int amount) {
        mana = Math.min(maxMana, mana + amount);
    }

    /** Returns actual damage dealt (0 if not enough mana → falls back to basic). */
    public int useAction(int actionIndex) {
        switch (actionIndex) {
            case 0:
                restoreMana(10);
                return attack;
            case 1:
                if (mana >= sk1Cost) { mana -= sk1Cost; restoreMana(5); return sk1Damage; }
                break;
            case 2:
                if (mana >= sk2Cost) { mana -= sk2Cost; restoreMana(5); return sk2Damage; }
                break;
            case 3:
                if (mana >= sk3Cost) { mana -= sk3Cost; restoreMana(5); return sk3Damage; }
                break;
            case 4:
                if (mana >= ultCost) { mana -= ultCost; return ultDamage; }
                break;
        }
        // Fallback — not enough mana → basic attack
        restoreMana(10);
        return attack;
    }

    public boolean canUse(int actionIndex) {
        switch (actionIndex) {
            case 1: return mana >= sk1Cost;
            case 2: return mana >= sk2Cost;
            case 3: return mana >= sk3Cost;
            case 4: return mana >= ultCost;
            default: return true;
        }
    }

    public String getActionName(int actionIndex) {
        switch (actionIndex) {
            case 0:  return "Basic Attack";
            case 1:  return skill1Name;
            case 2:  return skill2Name;
            case 3:  return skill3Name;
            case 4:  return ultimateName;
            default: return "Unknown";
        }
    }

    public int getActionDamage(int actionIndex) {
        switch (actionIndex) {
            case 0:  return attack;
            case 1:  return sk1Damage;
            case 2:  return sk2Damage;
            case 3:  return sk3Damage;
            case 4:  return ultDamage;
            default: return 0;
        }
    }

    public int getActionCost(int actionIndex) {
        switch (actionIndex) {
            case 1:  return sk1Cost;
            case 2:  return sk2Cost;
            case 3:  return sk3Cost;
            case 4:  return ultCost;
            default: return 0;
        }
    }

    // Getters — ENCAPSULATION
    public String getName()         { return name; }
    public int    getHp()           { return hp; }
    public int    getMaxHp()        { return maxHp; }
    public int    getAttack()       { return attack; }
    public int    getMana()         { return mana; }
    public int    getMaxMana()      { return maxMana; }
    public String getSkill1Name()   { return skill1Name; }
    public String getSkill2Name()   { return skill2Name; }
    public String getSkill3Name()   { return skill3Name; }
    public String getUltimateName() { return ultimateName; }
    public int    getSk1Cost()      { return sk1Cost; }
    public int    getSk2Cost()      { return sk2Cost; }
    public int    getSk3Cost()      { return sk3Cost; }
    public int    getUltCost()      { return ultCost; }
}