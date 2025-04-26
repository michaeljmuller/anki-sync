package org.themullers.anki.pojo;

public record ConjugationCard(String id,
                              String infinitive,
                              String regularity,
                              String englishInfinitive,
                              String tense,
                              String person,
                              String plurality,
                              String personExample,
                              String englishConjugated,
                              String conjugated,
                              String englishExample,
                              String translatedExample,
                              String notes)
{
    public String toString() {
        return String.format("""
                    id: %s
                    infinitive: %s
                    regularity: %s
                    english infinitive: %s
                    tense: %s
                    person: %s
                    plurality: %s
                    personExample: %s
                    englishConjugated: %s
                    conjugated: %s
                    englishExample: %s
                    translatedExample: %s
                    notes: %s
                    """,
                id,
                infinitive,
                regularity,
                englishInfinitive,
                tense,
                person,
                plurality,
                personExample,
                englishConjugated,
                conjugated,
                englishExample,
                translatedExample,
                notes == null ? notes : ""
                );
    }
}
