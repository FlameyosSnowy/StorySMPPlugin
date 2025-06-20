package me.flame.storysmp.quest;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class QuestProgressManager {
    private final Map<UUID, PlayerQuestObjective> objectiveMap = new HashMap<>();
    private final Consumer<PlayerQuestObjective> onComplete;

    public QuestProgressManager(Consumer<PlayerQuestObjective> onComplete) {
        this.onComplete = onComplete;
    }

    public void completeQuest(PlayerQuestObjective objective) {
        onComplete.accept(objective);
    }

    public void completeQuestTask(Player player) {
        objectiveMap.computeIfPresent(player.getUniqueId(), (uuid, objective) -> {
            objective.incrementAmount();

            QuestType type = objective.getType();
            if (type.getMaxAmount() <= objective.getAmount()) {
                this.completeQuest(objective);
                return null;
            }
            return objective;
        });
    }

    public void completeQuestTask(Player player, int addAmount) {
        objectiveMap.computeIfPresent(player.getUniqueId(), (uuid, objective) -> {
            objective.addAmount(addAmount);

            QuestType type = objective.getType();
            if (type.getMaxAmount() <= objective.getAmount()) {
                this.completeQuest(objective);
                return null;
            }
            return objective;
        });
    }

    public PlayerQuestObjective getQuestObjective(@NotNull Player player) {
        return objectiveMap.get(player.getUniqueId());
    }

    public void initializeQuest(QuestType type, @NotNull Player player) {
        objectiveMap.put(player.getUniqueId(), new PlayerQuestObjective(type, player));
    }
}
