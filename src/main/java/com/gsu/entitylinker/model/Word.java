package com.gsu.entitylinker.model;

import java.util.Objects;

/**
 * Created by cnytync on 08/11/2016.
 */
public class Word {

    private String word;
    private String pos;

    public Word(String word, String pos) {
        this.word = word;
        this.pos = pos;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof Word)) {
            return false;
        }
        Word w = (Word) o;
        return
                Objects.equals(word, w.word) &&
                Objects.equals(pos, w.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, pos);
    }
}
