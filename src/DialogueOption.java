public class DialogueOption {

    private final String prompt;
    private final String response;

    /**
     * This class represents a dialogue option between a player and an NPC
     *
     */
    public DialogueOption(String prompt, String response) {
        this.prompt = prompt;
        this.response = response;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getResponse() {
        return response;
    }

}


