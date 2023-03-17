package me.whiteshipp.refactoring._03_long_function;

import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudyDashboard {

    public static void main(String[] args) throws IOException, InterruptedException {
        StudyDashboard studyDashboard = new StudyDashboard();
        studyDashboard.print();
    }

    private void print() throws IOException, InterruptedException {
        GitHub gitHub = GitHub.connect();
        GHRepository repository = gitHub.getRepository("whiteship/live-study");
        List<Participant> participants = new CopyOnWriteArrayList();

        int totalNumberOfEvents = 15;
        ExecutorService service = Executors.newFixedThreadPool(8);
        CountDownLatch latch = new CountDownLatch(totalNumberOfEvents);

        for (int index = 1; index <= totalNumberOfEvents; index++) {
            int eventId = index;
            service.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        GHIssue issue = repository.getIssue(eventId);
                        List<GHIssueComment> comments = issue.getComments();
                        for (GHIssueComment comment :
                                comments) {
                            String userName = comment.getUserName();
                            boolean isNewUser = participants.stream().noneMatch(p -> p.username().equals(userName));
                            Participant participant = null;
                            if (isNewUser) {
                                participant = new Participant(userName);
                                participants.add(participant);
                            } else {
                                participant = participants.stream().filter(p -> p.username().equals(userName)).findFirst().orElseThrow();
                            }

                            participant.setHomeworkDone(eventId);
                        }

                        latch.countDown();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        latch.await();
        service.shutdown();

        try (FileWriter fileWriter = new FileWriter("participants.md");
             PrintWriter writer = new PrintWriter(fileWriter)) {
            participants.sort(Comparator.comparing(Participant::username));

            writer.print(header(totalNumberOfEvents, participants.size()));

            participants.forEach(p -> {
                String markdownForHomework = getMarkdownForParticipant(totalNumberOfEvents, p);
                writer.print(markdownForHomework);
            });
        }
    }

    private static double getRate(int totalNumberOfEvents, Participant p) {
        long count = p.homework().values().stream()
                .filter(v -> v == true)
                .count();
        double rate = count * 100.0 / totalNumberOfEvents;
        return rate;
    }

    private String getMarkdownForParticipant(int totalNumberOfEvents, Participant p) {
        String markdownForParticipant = String.format("| %s %s | %.2f%% |%n", p.username(), checkMark(p, totalNumberOfEvents), getRate(totalNumberOfEvents, p));
        return markdownForParticipant;
    }

    /**
     * | 참여자  (420) | 1주차 | 2주차 | 3주차 | 참석율 |
     * | --- | --- | --- | --- | --- |
     */
    private String header(int totalNumberOfEvents, int totalNumberOfParticipants) {
        StringBuilder header = new StringBuilder(String.format("| 참여자 (%d) |", totalNumberOfParticipants));

        for (int index = 1; index <= totalNumberOfEvents; index++) {
            header.append(String.format(" %d주차 |", index));
        }
        header.append(String.format(" 참석율 |%n"));

        header.append("| --- ".repeat(Math.max(0, totalNumberOfEvents + 2)));
        header.append("|\n");

        return header.toString();
    }

    private String checkMark(Participant p, int totalNumberOfEvents) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < totalNumberOfEvents; i++) {
            if(p.homework().containsKey(i) && p.homework().get(i)) {
                line.append("|:white_check_mark:");
            }else {
                line.append("|:x:");
            }
        }
        return line.toString();
    }
}
