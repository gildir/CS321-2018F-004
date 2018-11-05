import java.util.Date;
import java.util.Calendar;

public class Message {

    private String messageText; // what the message actually says
    private Player sender; // who sent the message
    private Player recipient; // who the message was sent to
    private boolean received; // whether or not this message has been received
    private boolean ignored; // whether or not this message was ignored by the recipient
    private Date dateAndTimeSent; // date and time the message was sent

    public Message(String newMessageText, Player from, Player to) {

        if(newMessageText == null
                || from == null
                || to == null) {

            throw new IllegalArgumentException("Messages must have TEXT, a SENDER, and a RECIPIENT!");

        } else {

            messageText = newMessageText;
            sender = from;
            recipient = to;
            received = false;

            Calendar calender = Calendar.getInstance();
            dateAndTimeSent = calender.getTime();
        }
    }

    public String GetMessageText() {
        return messageText;
    }

    public Player GetMessageSender() {
        return sender;
    }

    public Player GetMessageRecipient() {
        return recipient;
    }

    public boolean GetRecieved() {
        return received;
    }

    public boolean GetIgnored() {
        return ignored;
    }

    // only returns a clone of the Date object, to prevent the time sent from being altered
    public Date GetTimeSent() {
        return (Date)dateAndTimeSent.clone();
    }

    // can only be set to true, to prevent a received message from being marked otherwise
    public void SetReceived() {
        received = true;
    }

    // "    "
    public void SetIgnored() {
        ignored = true;
    }
}
