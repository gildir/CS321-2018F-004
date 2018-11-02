start /b rmiregistry
<<<<<<< HEAD
java -cp ".;../lib/*" -Djava.security.policy=game.policy GameServer localhost %1
=======
java -Djava.security.policy=game.policy GameServer localhost $1
>>>>>>> refs/remotes/base/dev
