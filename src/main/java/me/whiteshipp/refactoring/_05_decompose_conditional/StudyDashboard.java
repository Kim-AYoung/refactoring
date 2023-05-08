package me.whiteshipp.refactoring._05_decompose_conditional;

import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudyDashboard {
    private final int totalNumberOfEvents;
    private final List<Participant> participants;
    private final Participant[] firstParticipantsForEachEvent;

    public StudyDashboard(int totalNumberOfEvents) {
        this.totalNumberOfEvents = totalNumberOfEvents;
        this.participants = new CopyOnWriteArrayList<>();
        this.firstParticipantsForEachEvent = new Participant[this.totalNumberOfEvents];
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        StudyDashboard studyDashboard = new StudyDashboard(15);
        studyDashboard.print();
    }

    private void print() throws IOException, InterruptedException {
        checkGithubIssues(getGhRepository());
        new StudyPrinter(this.totalNumberOfEvents, this.participants).execute();
        printFirstParticipants();
    }

    private void printFirstParticipants() {
        Arrays.stream(this.firstParticipantsForEachEvent).forEach(p -> System.out.println(p.userName()));
    }

    private GHRepository getGhRepository() throws IOException {
        GitHub gitHub = GitHub.connect();
        return gitHub.getRepository("whiteship/live-study");
    }

    private void checkGithubIssues(GHRepository repository) throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(8);
        CountDownLatch latch = new CountDownLatch(totalNumberOfEvents);

        for (int index = 1; index <= totalNumberOfEvents; index++) {
            int eventId = index;
            service.execute(() -> {
                try {
                    GHIssue issue = repository.getIssue(eventId);
                    List<GHIssueComment> comments = issue.getComments();

                    checkHomework(eventId, comments);
                    this.firstParticipantsForEachEvent[eventId - 1] = findFirst(comments);

                    latch.countDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        latch.await();
        service.shutdown();
    }

    private Participant findFirst(List<GHIssueComment> comments) throws IOException {
        Date firstCreatedAt = null;
        Participant first = null;
        for (GHIssueComment comment : comments) {
            Participant participant = findParticipant(comment.getUserName());

            if (firstCreatedAt == null || comment.getCreatedAt().before(firstCreatedAt)) {
                firstCreatedAt = comment.getCreatedAt();
                first = participant;
            }
        }
        return first;
    }

    private void checkHomework(int eventId, List<GHIssueComment> comments) {
        for (GHIssueComment comment : comments) {
            Participant participant = findParticipant(comment.getUserName());
            participant.setHomeworkDone(eventId);
        }
    }

    private Participant findParticipant(String userName) {
        return isNewParticipant(userName) ?
                createNewParticipant(userName) :
                findExistingParticipant(userName);
    }

    private Participant findExistingParticipant(String userName) {
        return this.participants.stream().filter(p -> p.userName().equals(userName)).findFirst().orElseThrow();
    }

    private Participant createNewParticipant(String userName) {
        Participant participant;
        participant = new Participant(userName);
        this.participants.add(participant);
        return participant;
    }

    private boolean isNewParticipant(String userName) {
        return this.participants.stream().noneMatch(p -> p.userName().equals(userName));
    }
}
