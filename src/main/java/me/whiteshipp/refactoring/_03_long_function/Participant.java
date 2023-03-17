package me.whiteshipp.refactoring._03_long_function;

import java.util.HashMap;
import java.util.Map;

public record Participant(String username, Map<Integer, Boolean> homework) {

    public Participant(String userName) {
        this(userName, new HashMap<>());
    }


    public void setHomeworkDone(int index) {
        this.homework.put(index, true);
    }
}
