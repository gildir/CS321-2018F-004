# Reply
## User:
> The reply command allows the user to quickly respond to the last whisper (message) they received. The command is executed by typing "REPLY" or "R", followed by a space, followed by the message to be sent. Reply only works once the user has successfully received a whisper.

Examples:

```
REPLY hello world!

R hello world!
```
## Dev:
> The reply command is handled in the switch statement in GameClient and follows a structure similar to the whisper command. The switch statement calls the quickReply(String srcName, String message) method in GameObject, which returns the quickReply(String srcName, String message) method in GameCore. The quickReply(String srcName, String message) method in GameCore calls the whisper(String srcName, String dstName, String message) method in GameCore with the dstName parameter set to the lastPlayer field of the srcName parameter. lastPlayer is stored in the Player class and is set in the whisper(String srcName, String dstName, String message) method in GameCore. GameObject and GameCore have accompanying interfaces for their respective methods.   

# Chat Log
## User:
> The chat log is an administrative functionality that stores all player communications in a text file (chatlog.txt) server-side. It allows an administrator to reference all whispers, replies, says, shouts, and jokes since the last launch of the game server. The records include the sender, the message, and the recipient(s).

See chatlog.jpeg for an example chat log.
## Dev:
> The chat log functionality is handled within the GameCore class. The constructor creates a PrintWriter object with a new file called chatlog.txt and closes the PrintWriter. Every time a message is logged, the chatlog.txt file is opened, written to, and closed. This process is handled within the chatLog(Player player, int chatType, String message, String target) method in GameCore and ensures that the file is updated properly while the game server is running. The chatLog(Player player, int chatType, String message, String target) method in GameCore is called in the say(String name, String message), whisper(String srcName, String dstName, String message), and shout(String name, String message) methods in GameCore.
