package me.flame.storysmp.quest;

import org.bukkit.entity.Player;

public class PlayerQuestObjective {
    private final QuestType type;
    private final Player player;
    private int amount;

    public PlayerQuestObjective(QuestType type, Player player) {
        this.type = type;
        this.player = player;
    }

    public void incrementAmount() {
        amount++;
    }

    public void addAmount(int amount) {
        this.amount += amount;
    }

    public int getAmount() {
        return amount;
    }

    public QuestType getType() {
        return type;
    }

    public Player getPlayer() {
        return player;
    }
}
