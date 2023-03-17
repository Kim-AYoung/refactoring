package me.whiteshipp.refactoring._02_duplicated_code;

import java.io.IOException;

public class ParticipantDashBoard extends Dashboard {
    public void printParticipants(int eventId) throws IOException {
        printUsernames(eventId);
    }
}
