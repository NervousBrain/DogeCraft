package net.waals.dogecraft.models;

public class Quest {

    public enum QuestType {
        RETRIEVE,
        MINE,
        PLACE
    }

    private String name;
    private QuestType type;
}
