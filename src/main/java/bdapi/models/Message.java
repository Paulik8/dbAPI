package bdapi.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    private String message;

    @JsonCreator
    public Message(
            @JsonProperty("message") String message
    ) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
