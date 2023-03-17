package me.whiteshipp.refactoring._02_duplicated_code;

import java.io.IOException;

public class ReviewerDashBoard extends Dashboard {
    public void printReviewers() throws IOException {
        printUsernames(30);
    }
}
