package me.flame.storysmp.quest;

import java.security.SecureRandom;
import java.util.Random;

public enum QuestType {
    MINE_PURPUR(256),
    POP_TOTEMS(5),
    KILL_ENEMIES(3),
    TRAVEL_BLOCKS(10000),
    EAT_ENCHANTED_GOLDEN_APPLES(3);

    private static final QuestType[] TYPES = QuestType.values();

    private static final Random random = new SecureRandom();

    private final int maxAmount;

    QuestType(int maxAmount) {
        this.maxAmount = maxAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public static QuestType getRandomQuest() {
        return TYPES[random.nextInt(0, 5)];
    }
}
