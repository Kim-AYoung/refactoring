package me.whiteshipp.refactoring._05_decompose_conditional;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;

/***
 * 하나의 command를 나타내는 클래스
 * - 이후에 엑셀, 콘솔, 파일 등 여러 포맷에 출력할 수 있는 클래스로 확장할 수 있다.
 */
public class StudyPrinter {

    private int totalNumberOfEvents;
    private List<Participant> participants;

    public StudyPrinter(int totalNumberOfEvents, List<Participant> participants) {
        this.totalNumberOfEvents = totalNumberOfEvents;
        this.participants = participants;
    }

    public void execute() throws IOException {
        try (FileWriter fileWriter = new FileWriter("participants.md");
             PrintWriter writer = new PrintWriter(fileWriter)) {
            this.participants.sort(Comparator.comparing(Participant::userName));

            writer.print(header(this.participants.size()));

            this.participants.forEach(p -> {
                String markdownForHomework = getMarkdownForParticipant(p);
                writer.print(markdownForHomework);
            });
        }
    }

    private String getMarkdownForParticipant(Participant participant) {
        String markdownForParticipant = String.format("| %s %s | %.2f%% |%n", participant.userName(), checkMark(participant), participant.getRate(this.totalNumberOfEvents));
        return markdownForParticipant;
    }

    private String header(int totalNumberOfParticipants) {
        StringBuilder header = new StringBuilder(String.format("| 참여자 (%d) |", totalNumberOfParticipants));

        for (int index = 1; index <= this.totalNumberOfEvents; index++) {
            header.append(String.format(" %d주차 |", index));
        }
        header.append(String.format(" 참석율 |%n"));

        header.append("| --- ".repeat(Math.max(0, this.totalNumberOfEvents + 2)));
        header.append("|\n");

        return header.toString();
    }

    private String checkMark(Participant participant) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < this.totalNumberOfEvents; i++) {
            if (participant.homework().containsKey(i) && participant.homework().get(i)) {
                line.append("|:white_check_mark:");
            } else {
                line.append("|:x:");
            }
        }
        return line.toString();
    }
}
