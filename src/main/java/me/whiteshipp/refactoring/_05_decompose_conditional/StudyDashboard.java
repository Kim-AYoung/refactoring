package me.whiteshipp.refactoring._05_decompose_conditional;

import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudyDashboard {
    private final int totalNumberOfEvents;


    public StudyDashboard(int totalNumberOfEvents) {
        this.totalNumberOfEvents = totalNumberOfEvents;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        StudyDashboard studyDashboard = new StudyDashboard(15);
        studyDashboard.print();
    }

    private void print() throws IOException, InterruptedException {
        GitHub gitHub = GitHub.connect();
        GHRepository repository = gitHub.getRepository("whiteship/live-study");
        List<Participant> participants = new CopyOnWriteArrayList<>();

        ExecutorService service = Executors.newFixedThreadPool(8);
        CountDownLatch latch = new CountDownLatch(totalNumberOfEvents);

        for (int index = 1; index <= totalNumberOfEvents; index++) {
            int eventId = index;
            service.execute(() -> {
                try {
                    GHIssue issue = repository.getIssue(eventId);
                    List<GHIssueComment> comments = issue.getComments();
                    for (GHIssueComment comment : comments) {
                        Participant participant = findParticipant(participants, comment.getUserName());
                        participant.setHomeworkDone(eventId);
                    }

                    latch.countDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        latch.await();
        service.shutdown();

        new StudyPrinter(this.totalNumberOfEvents, participants).execute();
    }

    private Participant findParticipant(List<Participant> participants, String userName) {
        return isNewParticipant(participants, userName) ?
                createNewParticipant(participants, userName) :
                findExistingParticipant(participants, userName);
    }

    private Participant findExistingParticipant(List<Participant> participants, String userName) {
        return participants.stream().filter(p -> p.userName().equals(userName)).findFirst().orElseThrow();
    }

    private Participant createNewParticipant(List<Participant> participants, String userName) {
        Participant participant;
        participant = new Participant(userName);
        participants.add(participant);
        return participant;
    }

    private boolean isNewParticipant(List<Participant> participants, String userName) {
        return participants.stream().noneMatch(p -> p.userName().equals(userName));
    }
}
