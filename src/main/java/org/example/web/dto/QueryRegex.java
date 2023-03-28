package org.example.web.dto;

import javax.validation.constraints.NotEmpty;

public class QueryRegex {

    @NotEmpty
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (text.matches("[^,\s]+")) {
            this.text = text;
        }
    }
}
