package me.whiteshipp.refactoring._02_duplicated_code;

import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Dashboard {
    public static void main(String[] args) throws IOException {
        ReviewerDashBoard reviewerDashBoard = new ReviewerDashBoard();
        reviewerDashBoard.printReviewers();

        ParticipantDashBoard participantDashBoard = new ParticipantDashBoard();
        participantDashBoard.printParticipants(15);
    }

    protected static void printUsernames(int eventId) throws IOException {
        // Get github issue to check homework
        GitHub gitHub = GitHub.connect();
        GHRepository repository = gitHub.getRepository("whiteship/live-study");
        GHIssue issue = repository.getIssue(eventId);

        // Get participants
        Set<String> participants = new HashSet<>();
        issue.getComments().forEach(c -> participants.add(c.getUserName()));

        // Print participants
        participants.forEach(System.out::println);
    }
}
