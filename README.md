To compile/start the client :

mvn exec:java -Dexec.mainClass="io.github.cr3ahal0.forum.client.impl.ClientForum" -Djava.security.policy=./../file.policy

To compile/start the server :

mvn exec:java -Dexec.mainClass="io.github.cr3ahal0.forum.server.impl.ServeurForum" -Djava.security.policy=./../file.policy