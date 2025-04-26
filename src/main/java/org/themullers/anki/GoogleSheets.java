package org.themullers.anki;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.themullers.anki.pojo.ConjugationCard;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GoogleSheets {

    private static final String APPLICATION_NAME = "anki";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final String CONJUGATION_SHEET = "1W-trtb2gqKCiRDowyHihO15IvVMoLr-mAZBvi9EexNo";
    String CONJUGATION_RANGE = "Conjugation!A4:N";

    protected Sheets service;

    public GoogleSheets(String credentialsFile) throws IOException, GeneralSecurityException {

        // load service account credentials and set scope
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(credentialsFile))
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));

        // build the Sheets API client using api-client
        service = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public List<ConjugationCard> readConjugations() throws IOException {

        List<ConjugationCard> cards = new LinkedList<>();

        var response = service.spreadsheets()
                .get(CONJUGATION_SHEET)
                .setRanges(Collections.singletonList(CONJUGATION_RANGE))
                .setIncludeGridData(true)
                .execute();

        var grid = response.getSheets()
                .get(0)
                .getData()
                .get(0);

        var rows = grid.getRowData();
        for (int i = 0; i < rows.size(); i+=4) {

            var ptRow = rows.get(i).getValues();

            // if this is a blank row (has no value in the "infinitive" cell in column A), stop
            if (ptRow.get(0).getFormattedValue() == null || ptRow.get(0).getFormattedValue().trim().isEmpty()) {
                break;
            }

            var enRow = rows.get(i+1).getValues();
            var ptExampleRow = rows.get(i+2).getValues();
            var enExampleRow = rows.get(i+3).getValues();

            cards.addAll(processVerb(ptRow, enRow, ptExampleRow, enExampleRow));
        }

        return cards;
    }

    protected List<ConjugationCard> processVerb(List<CellData> ptRow, List<CellData> enRow, List<CellData> ptExampleRow, List<CellData> enExampleRow) {

        List<ConjugationCard> cards = new LinkedList<>();

        var infinitive = ptRow.get(0).getFormattedValue();
        var regularity = ptRow.get(1).getFormattedValue();
        var englishInfinitive = enRow.get(0).getFormattedValue();

        for (var col : ColumnData.values()) {

            var num = col.getColumnNumber();
            var tense = col.getTense();
            var plurality = col.getPlurality();
            var person = col.getPerson();

            // example id: "ter present, first person singular"
            var id = String.format("%s (%s tense, %s %s)", infinitive, tense, person, plurality);

            var pt = ptRow.get(num).getFormattedValue();
            var en = enRow.get(num).getFormattedValue();
            var ptExample = ptExampleRow.get(num).getFormattedValue();
            var enExample = enExampleRow.get(num).getFormattedValue();
            var note = ptRow.get(num).getNote();

            cards.add(new ConjugationCard(
                    id,
                    infinitive,
                    regularity,
                    englishInfinitive,
                    tense,
                    person,
                    plurality,
                    getPersonExample(person, plurality),
                    en,
                    pt,
                    enExample,
                    ptExample,
                    note));
        }

        return cards;
    }

    protected String getPersonExample(String person, String plurality) {
        if (person == null || plurality == null) {
            return "";
        }

        if (person.toLowerCase().startsWith("first")) {
            if (plurality.toLowerCase().equalsIgnoreCase("singular")) {
                return "eu";
            }
            else {
                return "nós";
            }
        }
        else if (person.toLowerCase().startsWith("second")) {
            if (plurality.toLowerCase().equalsIgnoreCase("singular")) {
                return "tu";
            }
            else {
                return "vocês";
            }

        }
        else {
            if (plurality.toLowerCase().equalsIgnoreCase("singular")) {
                return "ele/ela/você";
            }
            else {
                return "eles/elas";
            }
        }
    }

    enum ColumnData {
        C("present", "singular", "first person", 2),
        D("present", "singular", "second person", 3),
        E("present", "singular", "third person", 4),
        F("present", "plural", "first person", 5),
        G("present", "plural", "second person", 6),
        H("present", "plural", "third person", 7),
        I("past perfect", "singular", "first person", 8),
        J("past perfect", "singular", "second person", 9),
        K("past perfect", "singular", "third person", 10),
        L("past perfect", "plural", "first person", 11),
        M("past perfect", "plural", "second person", 12),
        N("past perfect", "plural", "third person", 13);

        protected String tense;
        protected String plurality;
        protected String person;
        protected int columnNumber;

        ColumnData(String tense, String plurality, String person, int columnNumber) {
            this.tense = tense;
            this.plurality = plurality;
            this.person = person;
            this.columnNumber = columnNumber;
        }

        public String getTense() {
            return tense;
        }

        public String getPlurality() {
            return plurality;
        }

        public String getPerson() {
            return person;
        }

        public int getColumnNumber() {
            return columnNumber;
        }
    }

}
