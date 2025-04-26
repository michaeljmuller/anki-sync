package org.themullers.anki;

import org.themullers.anki.pojo.ConjugationCard;

public class Sync {

    private static final String CREDENTIALS_PATH= "~/.anki-sync/anki-google-sheets-credentials.json";

    public static void main( String[] args ) throws Exception {
        var anki = new AnkiConnect();

        var sheets = new GoogleSheets(CREDENTIALS_PATH);
        for (var card : sheets.readConjugations()) {
            System.out.println("adding " + card.id());
            anki.addConjugationCard(conjugationDeck(card), card);
        }
    }

    protected static String conjugationDeck(ConjugationCard card) {
        return "Conjugation::" + capitalize(card.tense() + "::" + card.regularity());
    }

    public static String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder sb = new StringBuilder(input.length());
        boolean newWord = true;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isLetter(c)) {
                if (newWord) {
                    sb.append(Character.toUpperCase(c));
                    newWord = false;
                } else {
                    sb.append(c);
                }
            } else {
                sb.append(c);
                newWord = true;
            }
        }
        return sb.toString();
    }

}
