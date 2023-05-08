package com.samatha.javachallengeindpro.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Report {
    private String title;
    private List<ReportEntry> entries;


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReportEntry {
        private String label;
        private int count;

    }

}
