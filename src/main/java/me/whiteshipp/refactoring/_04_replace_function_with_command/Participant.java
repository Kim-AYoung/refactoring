package me.whiteshipp.refactoring._04_replace_function_with_command;

import java.util.HashMap;
import java.util.Map;

public record Participant(String userName, Map<Integer, Boolean> homework) {

    public Participant(String userName) {
        this(userName, new HashMap<>());
    }

    public void setHomeworkDone(int index) {
        this.homework.put(index, true);
    }

    double getRate(int totalNumberOfEvents) {
        long count = homework().values().stream()
                .filter(v -> v == true)
                .count();
        double rate = count * 100.0 / totalNumberOfEvents;
        return rate;
    }
}

