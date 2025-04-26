package org.themullers.anki;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.themullers.anki.pojo.ConjugationCard;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AnkiConnect {

    protected String hostname;
    protected int port;
    protected String url;
    protected ObjectMapper mapper = new ObjectMapper();

    public AnkiConnect(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        this.url = String.format("http://%s:%s/", hostname, port);
    }

    public AnkiConnect() {
        this("localhost", 8765);
    }

    public void listDecks() throws Exception {
        post(createAction("deckNames"));
    }

    protected void post(ObjectNode requestBody) throws Exception {

        String jsonRequest = mapper.writeValueAsString(requestBody);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        // send request and capture response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // parse returned JSON
        JsonNode jsonResponse = mapper.readTree(response.body());

        // output parsed response
        System.out.println(jsonResponse.toPrettyString());
    }

    public void addConjugationCard(String deck, ConjugationCard card) throws Exception {

        var model = "Conjugation";

        HttpClient client = HttpClient.newHttpClient();

        var noteObject = mapper.createObjectNode();
        noteObject.put("ID", card.id());
        noteObject.put("Infinitive", card.infinitive());
        noteObject.put("EnglishInfinitive", card.englishInfinitive());
        noteObject.put("Tense", card.tense());
        noteObject.put("Person", card.person());
        noteObject.put("Plurality", card.plurality());
        noteObject.put("PersonExample", card.personExample());
        noteObject.put("EnglishConjugated", card.tense());
        noteObject.put("Conjugated", card.conjugated());
        noteObject.put("EnglishExample", card.englishExample());
        noteObject.put("TranslatedExample", card.translatedExample());
        if (card.notes() != null) {
            noteObject.put("Notes", card.notes());
        }

        var note = mapper.createObjectNode()
                .put("deckName", deck)
                .put("modelName", model);
        note.set("fields", noteObject);
        note.set("options", createOptions(deck));

        var params = mapper.createObjectNode();
        params.put("note", note);

        var requestBody = createAction("addNote", params);

        post(requestBody);
    }

    protected ObjectNode createAction(String actionName) {
        return mapper.createObjectNode()
                .put("action", actionName)
                .put("version", 6);
    }

    protected ObjectNode createAction(String actionName, ObjectNode params) {
        return createAction(actionName).set("params", params);
    }

    protected ObjectNode createOptions(String deck) {

        ObjectNode scopeOptions = mapper.createObjectNode()
                .put("deckName", deck)
                .put("checkChildren", false)
                .put("checkAllModels", false);

        return mapper.createObjectNode()
                .put("allowDuplicate", false)
                .put("duplicateScope", "deck")
                .set("duplicateScopeOptions", scopeOptions);
    }
}
