Player Accounts(creating, deleting, storing, ect…)
--Will

Friends (Adding, removing, storing, ect…)
--Ryan
The user can create a friends list by adding and removing users to their friends list with the "FRIENDS ADD [Name]" and "FRIENDS REMOVE [Name]" commands. They can also use the "FRIENDS ONLINE" command to see which of their friends are currently online. If the user needs to, they can use the “FRIENDS” command to view all currently implemented friend related subcommands. Friend information is stored in the format of a two hashmaps, stored as friends you've added (which uses a key of your name, and a value of a hashtable containing the name of your friends), and friends who have added you (Which also has a key of your name and a value of a hashtable containing the name of people who have added your account as a friend). This is important as it allows the users friends list to automatically update to remove obsolete friends when they delete their account.

Join Game System (Logging in, list of online, loading files)
--Dylan

Leave Game System (Logging out, remove from online, heartbeat)
--Quinten
