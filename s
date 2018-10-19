[1mdiff --git a/prof.txt b/prof.txt[m
[1mdeleted file mode 100644[m
[1mindex 5418338..0000000[m
[1m--- a/prof.txt[m
[1m+++ /dev/null[m
[36m@@ -1 +0,0 @@[m
[31m-Yay![m
[1mdiff --git a/src/GameClient.java b/src/GameClient.java[m
[1mindex a06ab51..1409917 100644[m
[1m--- a/src/GameClient.java[m
[1m+++ b/src/GameClient.java[m
[36m@@ -57,7 +57,6 @@[m [mpublic class GameClient {[m
         System.out.println("  MOVE distance - Tries to walk forward <distance> times.");[m
         System.out.println("  PICKUP obect  - Tries to pick up an object in the same area.");[m
         System.out.println("  INVENTORY     - Shows you what objects you have collected.");[m
[31m-        System.out.println("  CHALLENGE     - Challenge a player to a R-P-S in the same area.");[m
         System.out.println("  QUIT          - Quits the game.");[m
         System.out.println();[m
         [m
[36m@@ -191,14 +190,6 @@[m [mpublic class GameClient {[m
                 case "INVENTORY":[m
                     System.out.println(remoteGameInterface.inventory(this.playerName));[m
                     break;                                                            [m
[31m-                case "CHALLENGE":[m
[31m-                    if(tokens.isEmpty()){[m
[31m-                        System.err.println("You need to provide a name.");[m
[31m-                    }[m
[31m-                    else{[m
[31m-                        System.out.println(remoteGameInterface.challenge(this.playerName, tokens.remove(0)));[m
[31m-                    }[m
[31m-                    break;[m
                 case "QUIT":[m
                     remoteGameInterface.leave(this.playerName);[m
                     runListener = false;[m
[1mdiff --git a/src/GameCore.java b/src/GameCore.java[m
[1mindex 4625419..cc10a50 100644[m
[1m--- a/src/GameCore.java[m
[1m+++ b/src/GameCore.java[m
[36m@@ -283,21 +283,6 @@[m [mpublic class GameCore implements GameCoreInterface {[m
         }[m
     }    [m
 [m
[31m-    @Override[m
[31m-    public String challenge(String challenger, String challengee){[m
[31m-        Player playerChallenger = this.playerList.findPlayer(challenger);[m
[31m-        Player playerChallengee = this.playerList.findPlayer(challengee);[m
[31m-        if(playerChallenger != null && playerChallengee != null && playerChallenger != playerChallengee && playerChallenger.getCurrentRoom() == playerChallengee.getCurrentRoom()) {[m
[31m-             playerChallengee.getReplyWriter().println(playerChallenger.getName() + " challenges you to a R-P-S");[m
[31m-             return "You challenged " + playerChallengee.getName() + " to a R-P-S.";[m
[31m-         }[m
[31m-        else if(playerChallenger == playerChallengee)[m
[31m-            return "You can't challenge yourself to R-P-S.";[m
[31m-         else {[m
[31m-             return "This person is not in the same room as you or doesn't exist in the game.";[m
[31m-         }[m
[31m-    }[m
[31m-[m
      /**[m
      * Leaves the game.[m
      * @param name Name of the player to leave[m
[1mdiff --git a/src/GameCoreInterface.java b/src/GameCoreInterface.java[m
[1mindex 6dba1d2..fffcfc8 100644[m
[1m--- a/src/GameCoreInterface.java[m
[1m+++ b/src/GameCoreInterface.java[m
[36m@@ -74,13 +74,6 @@[m [mpublic interface GameCoreInterface {[m
      */    [m
     public String inventory(String name);[m
     [m
[31m-    /**[m
[31m-      * Challenge someone to R-P-S[m
[31m-      * @param challenger is the name of the player challenging to R-P-S[m
[31m-      * @param challenge is the name of the player being challenge[m
[31m-      * @return String message of the challenge [m
[31m-      */[m
[31m-    public String challenge(String challenger, String challengee);[m
     /**[m
      * Leaves the game.[m
      * @param name Name of the player to leave[m
[1mdiff --git a/src/GameObject.java b/src/GameObject.java[m
[1mindex 84581dc..dd9de61 100644[m
[1m--- a/src/GameObject.java[m
[1m+++ b/src/GameObject.java[m
[36m@@ -134,16 +134,7 @@[m [mpublic class GameObject extends UnicastRemoteObject implements GameObjectInterfa[m
     public String inventory(String name) throws RemoteException {[m
         return core.inventory(name);[m
     }    [m
[31m-    /**Prompts a message that someone is challenging them to a R-P-S[m
[31m-      * @param challenger is the name of the player challenging someone in the area[m
[31m-      * @param challenge is the name of the player being challenge[m
[31m-      * @return Message showing success[m
[31m-      * @throws RemoteException[m
[31m-    */[m
[31m-    public String challenge(String challenger, String challengee) throws RemoteException{[m
[31m-        return core.challenge(challenger, challengee);[m
[31m-    }[m
[31m-[m
[32m+[m[41m    [m
      /**[m
      * Leaves the game.[m
      * @param name Name of the player to leave[m
[1mdiff --git a/src/GameObjectInterface.java b/src/GameObjectInterface.java[m
[1mindex 211afd2..56bc918 100644[m
[1m--- a/src/GameObjectInterface.java[m
[1m+++ b/src/GameObjectInterface.java[m
[36m@@ -80,14 +80,6 @@[m [mpublic interface GameObjectInterface extends Remote {[m
      */    [m
     public String inventory(String name) throws RemoteException;   [m
     [m
[31m-    /**[m
[31m-    * Prompts a message that someone is challenging them to a R-P-S[m
[31m-    * @param challenger is the name of the player challenging someone in the area[m
[31m-    * @param challenge is the name of the player being challenge[m
[31m-    * @return Message showing success[m
[31m-    * @throws RemoteException[m
[31m-    */[m
[31m-    public String challenge(String challenger, String challengee) throws RemoteException;[m
      /**[m
      * Leaves the game.[m
      * @param name Name of the player to leave[m
[1mdiff --git a/src/build.sh b/src/build.sh[m
[1mold mode 100755[m
[1mnew mode 100644[m
[1mdiff --git a/src/clean.sh b/src/clean.sh[m
[1mold mode 100755[m
[1mnew mode 100644[m
[1mdiff --git a/src/runClientLocal.sh b/src/runClientLocal.sh[m
[1mold mode 100755[m
[1mnew mode 100644[m
[1mdiff --git a/src/runClientRemote.sh b/src/runClientRemote.sh[m
[1mold mode 100755[m
[1mnew mode 100644[m
[1mdiff --git a/src/runServer.sh b/src/runServer.sh[m
[1mold mode 100755[m
[1mnew mode 100644[m
[1mindex fa7524c..f6812c8[m
[1m--- a/src/runServer.sh[m
[1m+++ b/src/runServer.sh[m
[36m@@ -1,3 +1,2 @@[m
 #! /bin/sh[m
[31m-rmiregistry &[m
[31m-java -Djava.security.policy=game.policy GameServer localhost[m
[32m+[m[32mrmiregistry &; java -Djava.security.policy=game.policy GameServer localhost[m
