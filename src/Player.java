
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Kevin
 */
@JsonIgnoreProperties({ "replyWriter", "outputWriter" })
public class Player {
    private int dormId;//used to determine private dormroom Id
    public LinkedList<Item> chestImage;
    public LinkedList<Item> currentInventory;
    private String name;
    private int currentRoom;
    private Direction currentDirection;
    private PrintWriter replyWriter = null;
    private DataOutputStream outputWriter = null;
    private double money;
    private DataInputStream inputWriter = null;
    private boolean inTrade = false;
    private Item tradeItem = null;
    private boolean tradeRequested = false;
    private boolean tradeReceived = false;
    private String tradePartner = "";
    private String lastPlayer = "";
    private boolean hasChallenge = false;
    private boolean inBattle = false;
    private String challenger = " ";
    private String option = "";
    public boolean toggleChat = false;
    private String challengerOption = "";
    private boolean hasOption = false;
    private ArrayList<NPC> dialogueList = new ArrayList<NPC>();
    private int rounds = 0;
    private int wins = 0;
    private boolean hasTitle = false; //used for title and use item feature 
    private String playerItemTitle = "";
    private double rewardAmount; //task 229, keeps track of how much money players will be rewarded every reward interval while logged in
    private long rewardProgress; //task 229, keeps track of how much time must elapse before a reward.
    private long totalPay; //used to calculate missed allowance payments for task 228
    private boolean rathskellerStatus = false;
	private boolean fightingGhoul = false;
	private Ghoul challengedGhoul;

    //Tracks which quest the player is on
    private int questProgress;
    //Used to count victories in RPS quest
    private int rpsVictoryCount;
    //Used to count pokes in poke quests
    private int pokeCount;

    private int RPSwins = 0;
    private int RPSloss = 0;
    private int RPSties = 0;
    private double playerRankingScore = 0;
    private String rankingTitle = "";

    @JsonProperty("recovery")
    private ArrayList<String> recovery;

	public Player(@JsonProperty("name") String name) {
        this.currentRoom = 1;
        this.currentDirection = Direction.NORTH;
        this.name = name;
        this.currentInventory = new LinkedList<>();
        this.chestImage = new LinkedList<>();
        this.money = 0;

	this.questProgress = 0;
	this.rpsVictoryCount = 0;

        this.rewardAmount = 0.1; //Task 229, This is the default starting amount (also set when player leaves in GameCore)
        this.rewardProgress = 0; //Task 229, value resets to 0 on leave (in GameCore leaveGame)
        this.totalPay = 0; //for task 228        

    }

    public int getDormId() {return this.dormId;}
    public void setDormId(int i) {this.dormId = i;} 

    private HashSet<Player> ignoredPlayers = new HashSet<Player>();

    //Feature 409 WordFilter

/*    *//**
     * Prints a chat statemnet from another player.
     * @param playerName - name of player making the statement
     * @param action - whether the player is saying or whispering the statement
     * @param message - the statement the other player is making
     *//*
    public void say(String playerName, String action, String message) {
        String filteredMessage = filterMessage(message);
        String statement = playerName + " " + action + " " + filteredMessage + ".";
        getReplyWriter().println(statement);
    }*/

    //Collection of words to be filtered from game chat
    private HashSet<String> filteredWords = new HashSet<String>();
	
	public Ghoul getChallengedGhoul() {
		return challengedGhoul;
	}
 	public void setChallengedGhoul(Ghoul challengedGhoul) {
		this.challengedGhoul = challengedGhoul;
	}
 	public boolean isFightingGhoul() {
		return fightingGhoul;
	}
 	public void setFightingGhoul(boolean fightingGhoul) {
		this.fightingGhoul = fightingGhoul;
	}

    /**
     * Sets the words filtered from this player's chat.
     * @param newFilteredWords - collection of words to be filtered from this player's chat
     */
    public void setFilteredWords(HashSet<String> newFilteredWords) {
        filteredWords = new HashSet<String>();

        for(String word : newFilteredWords) {
            filteredWords.add(word.toLowerCase());
        }
    }

    public HashSet<String> getFilteredWords() {
        return filteredWords;
    }

    /**
     * Adds a new word to the list of words filtered from this player's chat.
     * @param wordToAdd - word to be added to the filter list.
     * @return - whether the word was successfully added.
     */
    public boolean addFilteredWord(String wordToAdd) {
        boolean ret = filteredWords.add(wordToAdd);
        return ret;
    }

    /**
     * Checks whether this player is actively filtering a given word from chat.
     * @param word - word to check
     * @return - whether the givin word is being filtered.
     */

    public boolean isFiltering(String word) {
        return filteredWords.contains(word);
    }

    /**
     * Parses a message, replacing filtered words with "[BLEEEEP]"
     * @param message - message being filtered.
     * @return - new filtered message.
     */
    public String filterMessage(String message) {

        if(filteredWords.size() == 0) { return message; }

        String newMessage = "";
        String bleep = "[BLEEEEP]";
        String messageL = message.toLowerCase();

        for(int i = 0; i < message.length(); i ++) {
            char current = message.charAt(i);
            char currentL = messageL.charAt(i);
            boolean match = false;
            int wordLen = 0;

            for(String word : filteredWords) {
                String wordL = word.toLowerCase();

                if(i + word.length() <= message.length() && currentL == wordL.charAt(0)) {
                    String fragmentL = messageL.substring(i, i + word.length());

                    if(wordL.equals(fragmentL)) {
                        match = true;
                        wordLen = word.length();
                        break;
                    }
                }
            }

            if(match) {
                newMessage += bleep;
                i += wordLen - 1;
            } else {
                newMessage += current;
            }
        }

        return newMessage;
    }

    /*

    public void printMessage(Player speaker, String message, String action) {
        String newMessage = filterMessage(message);
        this.getReplyWriter().println(speaker.getName() + " " + action + " " + newMessage);
    }
*/
    /**
     * Adds a player's reference to set ignoredPlayers.
     * @param playerToIgnore
     * @return - whether player reference was successfully added to set ignorePlayer.
     */
    public boolean ignorePlayer(Player playerToIgnore) {
        return ignoredPlayers.add(playerToIgnore);
    }

    //Feature 408. Unignore Player.
    /**
     *
     * Removes a given player form the set ignoredPlayers
     * @param playerToUnIgnore - player to remove from set
     * @return - whether the player reference was successfully removed
     *
     */
    public boolean unIgnorePlayer(Player playerToUnIgnore) {
        if(ignoredPlayers.contains(playerToUnIgnore)){
            return ignoredPlayers.remove(playerToUnIgnore);
        }else {
            return false;
        }
    }

    /**
     * Checks a given other player to see if they're on the THIS player's ignore list.
     * @param otherPlayer - other player this player may or may not be ignoring.
     * @return - whether the other player is being ignored by this player.
     */
    public boolean isIgnoring(Player otherPlayer) {
        return ignoredPlayers.contains(otherPlayer);
    }

    /**
     * Access the list of players this player is ignoring.
     * @return - Returns a String of all player names this player is ignoring
     */
	@JsonIgnore
    public String getIgnoredPlayersList() {
        StringBuilder ignoredPlayersList = new StringBuilder();
        ignoredPlayersList.append("\nIgnored Players: ");
        if(ignoredPlayers.isEmpty()) { ignoredPlayersList.append(" Your ignore list is empty.\n"); }
        else {
            int count = 1;
            for(Player ignored : ignoredPlayers) {
                ignoredPlayersList.append(ignored.name);
                if(count == ignoredPlayers.size()) {
                    ignoredPlayersList.append(".\n");
                } else {
                    count++;
                    ignoredPlayersList.append(", ");
                }
            }
        }
        return ignoredPlayersList.toString();
    }

    /**
     * Format a message to be sent to this player.
     * @param source Player speaking this message
     * @param messageType Verbage of how this message is communicated
     *                    Examples: "says", "whispers", "shouts", "offers to trade", "mimes to the party", "performs an expressionist dance illustrating"
     * @param message The message to be communicated
     * @return True if successful
     *          False if ignoring
     */
    public boolean messagePlayer(Player source, String messageType, String message ) {
        // check source for ignore
        for (Player p : this.ignoredPlayers)
            if (p.equals(source))
                return false;

        // filter message
        String newMessage = filterMessage(message);

        // send prefix, timestamp, source, type, ", ", and message
        this.getReplyWriter().println(
            this.prefix +
            String.format("[%tD %<tT]", GameServer.getDate()) + " " +
            source.getName() + " " +
            messageType + ", " +
            newMessage);
        return true;
    }

    /**
     * Return a standard opening for chat messages sent by this player
     * @return [Time Stamp][Prefix] You
     */
    public String getMessage() {
        return
            this.prefix +
            String.format("[%tD %<tT]", GameServer.getDate()) +
            " You ";
    }

    public void turnLeft() {
        switch(this.currentDirection.toString()) {
            case "North":
                this.currentDirection = Direction.WEST;
                break;
            case "South":
                this.currentDirection = Direction.EAST;
                break;
            case "East":
                this.currentDirection = Direction.NORTH;
                break;
            case "West":
                this.currentDirection = Direction.SOUTH;
                break;                
        }
    }
    
    public void turnRight() {
        switch(this.currentDirection.toString()) {
            case "North":
                this.currentDirection = Direction.EAST;
                break;
            case "South":
                this.currentDirection = Direction.WEST;
                break;
            case "East":
                this.currentDirection = Direction.SOUTH;
                break;
            case "West":
                this.currentDirection = Direction.NORTH;
                break;                
        }
    }
    
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // INSERT CODE FOR GETTERS AND SETTERS BELOW ///////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getLastPlayer() {
        return lastPlayer;
    }

    public void setLastPlayer(String lastPlayer) {
        this.lastPlayer = lastPlayer;
    }
    
    public void setInBattle(boolean battle){
	inBattle = battle;
    }

    public boolean getInBattle(){
	return inBattle;
    } 

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //Update dialogue status of this player with other npcs
    public void updateDialogueList(String npcName, String dialogueTag, int updateAmount)
    {
        for (int i = 0; i < dialogueList.size(); i++) {
            if (dialogueList.get(i).getName().equals(npcName))
            {
                dialogueList.get(i).changeDialogueList(dialogueTag, updateAmount);
            }
        }
    }

    //Get dialogue status of this player with other npcs
    public ArrayList<NPC> getDialogueList()
    {
        return dialogueList;
    }

    //Overload method for getDialogueIdFromList
    public int getDialogueIdFromList(String npcName, String dialogueTag)
    {
        return getDialogueIdFromList(npcName, dialogueTag, "");
    }

    //Takes the dialgoue id of specific dialogue from npc. 
    //If no dialogue id exists, add this npc to the dialogueLIst
    public int getDialogueIdFromList(String npcName, String dialogueTag, String prompt)
    {
        int result = -1;
        for (int i = 0; i < dialogueList.size(); i++) {
            if (dialogueList.get(i).getName().equals(npcName))
            {
                result = dialogueList.get(i).getDialogueId(dialogueTag);
            }
        }

        if (result == -1)
        {
            addDialogueList(npcName, dialogueTag, prompt);
            result = 1;
        }

        return result;
    }

    //Helper method used to add npc to the dialogueList
    private void addDialogueList(String npcName, String dialogueTag, String prompt)
    {
        boolean found = false;
        for (int i = 0; i < dialogueList.size(); i++) {
            if (dialogueList.get(i).getName().equals(npcName))
            {
                found = true;
                //dialogueList.get(i).addToDialogueList(dialogueTag, prompt);
            }
        }

        if (found == false)
        {
            NPC npc = new NPC(npcName, -1, new LinkedList<String>(), new ArrayList<DialogueOption>());
            dialogueList.add(npc);
        }
    }
  
    public LinkedList<Item> getCurrentInventory() {
        return currentInventory;
    }

	@JsonProperty("currentInventory")
    public void setCurrentInventory(LinkedList<Item> currentInventory) {
        this.currentInventory = currentInventory;
    }
    
    public void addObjectToInventory(Item object) {
        this.currentInventory.add(object);
    }

    //removes the first instance
    public Item removeObjectFromInventory(String target) {
        for(Item o : this.currentInventory) {
                String nameToRemove = o.getName();
            if(nameToRemove.equalsIgnoreCase(target)) {
		Item temp = o;
                this.currentInventory.remove(o);
                return temp;
            }
        }
        return null;
    }

    public void sortCurrentInventory(String modes) {
	switch(modes) {
		case "ni":
			Collections.sort(currentInventory, new ItemNameComparator());
			break;
		case "nd":
			Collections.sort(currentInventory, new ItemNameComparator());
			Collections.reverse(currentInventory);
			break;
		case "wi":
			Collections.sort(currentInventory, new ItemWeightComparator());
			break;
		case "wd":
			Collections.sort(currentInventory, new ItemWeightComparator());
			Collections.reverse(currentInventory);
			break;
		case "pi":
			Collections.sort(currentInventory, new ItemPriceComparator());
			break;
		case "pd":
			Collections.sort(currentInventory, new ItemWeightComparator());
			Collections.reverse(currentInventory);
			break;
		default:
			System.out.println("Please enter in valid input or use the correct format (n/w/p) -> (i/d)");
	}
    }

    /**
     *  Initiates the action of drinking a rathskeller bottle
     *  Makes the rathskeller status true for one minute after which it will be false
     */
    public void drinkRathskeller() {
	rathskellerStatus = true;
	Thread rTimer = new Thread(new RathskellerTimer());
	rTimer.start();
    }

    /**
     * Gets the status of a player and tells if they drank from a rathskeller bottle
     * @return true if the player has drunken, false otherwise
     */
    public boolean getRathskellerStatus() {
	return rathskellerStatus;
    }

    //runnable class for timing the rathskeller feature
    private class RathskellerTimer implements Runnable {
	@Override
	public void run() {
		try {
			Thread.sleep(60000);
			rathskellerStatus = false;
		}
		catch(InterruptedException e) {
			rathskellerStatus = false;
		}
	}
    }

    //setter for hasTitle
    public void setHasTitle(boolean val) {
	hasTitle = val;
    }

    //getter for hasTitle
    public boolean checkIfHasTitle() {
	return hasTitle;
    }

    //sets new title of player
    public void setTitle(String newTitle) {
	playerItemTitle = newTitle;
    }

    //gets title of player
    public String getTitle() {
	return playerItemTitle;
    }

    public boolean hasReceivedTrade(){
        return tradeReceived;
    }

    public void setReceivedTrade(boolean val){
        tradeReceived = val;
    }
    public boolean hasTradeRequest(){
        return tradeRequested;
    }

    public void setTradeRequest(boolean val){
        tradeRequested = val;
    }

    public boolean isInTrade(){
        return inTrade;
    }
    public void setInTrade(boolean val){
        inTrade = val;
    }

    public String getTradePartner(){
        return tradePartner;
    }
    public void setTradePartner(String s){
        tradePartner = s;
    }

    public void setTradeItem(Item it){
        tradeItem = it;
    }
    public Item getTradeItem(){
        return tradeItem;
    }

    public void setReplyWriter(PrintWriter writer) {
        this.replyWriter = writer;
    }
    
    public PrintWriter getReplyWriter() {
        return this.replyWriter;
    }
    
    public void setOutputWriter(DataOutputStream writer) {
        this.outputWriter = writer;
    }
    
    public DataOutputStream getOutputWriter() {
        return this.outputWriter;
    }
    
    public int getCurrentRoom() {
        return this.currentRoom;
    }
    
	@JsonProperty("currentRoom")
    public void setCurrentRoom(int room) {
        this.currentRoom = room;
    }
    
	@JsonIgnore
    public String getCurrentDirection() {
        return this.currentDirection.name();
    }
    
    public Direction getDirection() {
        return this.currentDirection;
    }

    public String getOption(){
        return this.option;
    }

    public void setOption(String option){
        this.option = option;
    }

    public String getChallengerOption(){
        return this.challengerOption;
    }

    public void setChallengerOption(String challengerOption){
        this.challengerOption = challengerOption;
    }
    public void setRounds(int round){
        this.rounds = round;
    }

    public int getRounds(){
        return this.rounds;
    }

    public void setWins(int wins){
        this.wins = wins;
    }
    public int getWins(){
        return this.wins;
    }
    
    public double getMoney() {
        return this.money;
    }

    public void setMoney(double m){
        this.money = m;
    }

    public String getChallenger(){
        return challenger;
    }

    public void setChallenger(String name){
        challenger = name;
    }
    public boolean getHasChallenge(){
        return hasChallenge;
    }
    public void setHasChallenge(boolean challenged){
        hasChallenge = challenged;
    }

    public void setRpsVictoryCount(int count){
	rpsVictoryCount = count;
    }

    public void addRpsVictory(){
	rpsVictoryCount ++;
    }

    public int getRpsVictoryCount(){
	return rpsVictoryCount;
    }

    public void setPokeCount(int num){
        pokeCount = num;
    }

    public void addPoke(){
	pokeCount ++;
    }

    public int getPokeCount(){
	return pokeCount;
    }


    public int getProgress(){
        return questProgress;
    }

    public void setProgress(int progress){
        this.questProgress = progress;
    }

    public void advanceQuest(){
	questProgress ++;
    }
    
    @JsonIgnore
    public double getRewardAmount() {
    	return this.rewardAmount;
    }
    
    public void setRewardAmount(double d) {
    	this.rewardAmount = d;
    }
    
    @JsonIgnore
    public long getRewardProgress() {
    	return this.rewardProgress;
    }
    
    public void setRewardProgress(long l) {
    	this.rewardProgress = l;
    }
    
    public long getTotalPay() {
    	return this.totalPay;
    }
    
    public void setTotalPay(long l) {
    	this.totalPay = l;
    }

    public int getRPSwins(){
	return this.RPSwins;
    }
    public void setRPSwins(int w){
	this.RPSwins = w;
    }
    public int getRPSloss(){
	return this.RPSloss;
    }
    public void setRPSloss(int l){
	this.RPSloss = l;
    }
    public int getRPSties(){
	return this.RPSties;
    }
    public void setRPSties(int t){
	this.RPSties = t;
    }
    public double getPlayerRankingScore(){
	return this.playerRankingScore;
    }
    public void setPlayerRankingScore(double r){
	this.playerRankingScore = r;
    }
    public String getRankingTitle(){
	return this.rankingTitle;
    }
    public void setRankingTitle(String title){
	this.rankingTitle = title;
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// INSERT CODE FOR GETTERS AND SETTERS ABOVE ///////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Allows the caller to add/take money in user's wallet.
     * 
     * @author Team 4: Alaqeel
     * @param m Amount to be added/taken.
     */
    public void changeMoney(double m){
        this.money += m;
    }
    
    public String viewInventory() {
        String result = "";
        if(this.currentInventory.isEmpty() == true) {
            return " nothing.";
        }
        else {
            for(Item obj : this.currentInventory) {
                result += " " + obj.name;
		result += (" (" + obj.weight + ") ");
            }
            result += ".";
        }
        return result;
    }

    @Override
    public String toString() {
        return "Player " + this.name + ": " + currentDirection.toString();
    }

    private static class ItemNameComparator implements Comparator<Item> {
	@Override
	public int compare(Item ItemA, Item ItemB) {
		return ItemA.getName().compareTo(ItemB.getName());
	}
    }

    private static class ItemWeightComparator implements Comparator<Item> {
	@Override
	public int compare(Item ItemA, Item ItemB) {
		if(ItemA.getWeight() > ItemB.getWeight()) {
			return 1;
		}
		else if(ItemA.getWeight() < ItemB.getWeight()) {
			return -1;
		}
		else {
			return 0;
		}
	}
    }

    private static class ItemPriceComparator implements Comparator<Item> {
	@Override
        public int compare(Item ItemA, Item ItemB) {
            if(ItemA.getPrice() > ItemB.getPrice()) {
                return 1;
            }
            else if(ItemA.getPrice() < ItemB.getPrice()) {
                return -1;
            }
            else {
                return 0;
            }
        }
    }

    //Feature 413 Prefix
    // string to be used as a chat prefix, default value is ">>>".
    private String prefix = ">>>";

    /**
     * Sets the chat prefix from default.
     * @param newPrefix - New prefix to replace old.
     */
    public void setPrefix(String newPrefix) {
        prefix = newPrefix;
    }


    //End 413 Prefix
    /*
     * This toggles the R-P-S resolutions of other players in the same room
     */
    public String toggleResolution(){
	if (toggleChat == false){
		toggleChat = true;
		return "You have turned off RPS resolutions in your area";
	}
	else{
		toggleChat = false;
		return "You have turned on RPS resolutions in your area";
	}

    }


}
