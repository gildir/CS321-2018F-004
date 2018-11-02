#! /bin/sh
<<<<<<< HEAD
rmiregistry &; java -cp ".:../lib/*" -Djava.security.policy=game.policy GameServer localhost $1
=======
rmiregistry &; java -Djava.security.policy=game.policy GameServer localhost $1
>>>>>>> refs/remotes/base/dev
