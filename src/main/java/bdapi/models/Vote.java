package bdapi.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Vote {

    private Integer voice;
    private String nickname;

    public Vote() {

    }

    public Integer getVoice() {
        return voice;
    }

    public void setVoice(Integer voice) {
        this.voice = voice;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @JsonCreator

    public Vote(
            @JsonProperty("voice") Integer voice,
            @JsonProperty("nickname") String nickname
    ) {
        this.voice = voice;
        this.nickname = nickname;
    }

}
