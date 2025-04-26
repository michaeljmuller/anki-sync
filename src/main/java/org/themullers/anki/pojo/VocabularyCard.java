package org.themullers.anki.pojo;

public record VocabularyCard(
        String word,
        String englishWord,
        String partOfSpeech,
        String example,
        String englishExample,
        String notes)
{
    public String toString() {
        return String.format("""
                word: %s, 
                englishWord: %s,
                partOfSpeech: %s,
                example: %s,
                englishExample: %s,
                """,
                word, englishWord, partOfSpeech, example, englishExample, notes);
    }
}
