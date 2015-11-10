package io.github.cr3ahal0.forum.server.impl;

import io.github.cr3ahal0.forum.client.IClientForum;
import io.github.cr3ahal0.forum.client.IAfficheurClient;
import io.github.cr3ahal0.forum.client.impl.SessionToken;
import io.github.cr3ahal0.forum.server.ICommandFeedback;
import io.github.cr3ahal0.forum.server.IServeurForum;
import io.github.cr3ahal0.forum.server.ISujetDiscussion;
import io.github.cr3ahal0.forum.server.impl.commands.DiceMessage;
import io.github.cr3ahal0.forum.server.impl.commands.ThirdPersonMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ServeurForum
        extends UnicastRemoteObject
        implements IServeurForum {

    private List<IClientForum> listeners;

    private static final Logger logger = LogManager.getLogger(ServeurForum.class);

    private Map<Integer, ISujetDiscussion> salons;

    private boolean up = false;

    public boolean isUp() {
        return up;
    }

    protected ServeurForum() throws RemoteException {

        listeners = new ArrayList<IClientForum>();
        salons = new HashMap<Integer, ISujetDiscussion>();

        int i = 1;

        salons.put(i++, new SujetDiscussion("GlobalChat"));
        salons.put(i++, new SujetDiscussion("Middleware"));
        salons.put(i++, new SujetDiscussion("IHM"));
        salons.put(i++, new SujetDiscussion("Architecture Logicielle"));
        salons.put(i++, new SujetDiscussion("Architectures Distribuées"));
        salons.put(i++, new SujetDiscussion("MDE"));
        salons.put(i++, new SujetDiscussion("Web Semantique"));
        salons.put(i++, new SujetDiscussion("Cloud Computing"));
        salons.put(i++, new SujetDiscussion("Conference"));
        salons.put(i++, new SujetDiscussion("Meta-Meta-Meta-Model"));
        salons.put(i++, new SujetDiscussion("Base de Données"));
        salons.put(i++, new SujetDiscussion("HTml 7"));


        up = true;
    }

    public boolean auth(IClientForum token, String username, String password) throws RemoteException {

        if ( (!username.equals("")) && (password.equals("")) ) {
            System.out.println("[Login] Access granted to " + username);
            listeners.add(token);
            return true;
        } else {
            System.out.println("[Login] Failed login attempt from " + username);
            return false;
        }
    }

    public void disconnect(String username) throws RemoteException {
        System.out.println("[Logout] " + username + " has disconnected");
    }

    public Map<Integer, ISujetDiscussion> list() throws RemoteException {
        return salons;
    }

    public ISujetDiscussion join(Integer id) throws RemoteException {
        ISujetDiscussion token = salons.get(id);
        if (token != null) {

        }

        return token;
    }

    public static void main(String[] args) {
        System.out.println("Démarrage du serveur...");
        boolean booted = false;
		ServeurForum serveur = null;

        while (!booted) {
            try {
                serveur = new ServeurForum();

                System.out.println("RMI initializing...");
                LocateRegistry.createRegistry(8090);

                Naming.bind("//127.0.0.1:8090/auth", serveur);
                Naming.bind("//127.0.0.1:8090/list", serveur);
                Naming.bind("//127.0.0.1:8090/join", serveur);
                System.out.println("RMI intialized.");

                System.out.println("Configuration initializing...");
                Properties prop = new Properties();
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                InputStream stream = loader.getResourceAsStream("server.properties");
                prop.load(stream);
                System.out.println("Configuration intialized.");

                /*System.out.println("Initializing database");
                HttpClient httpClient = new StdHttpClient.Builder()
                        .url(prop.getProperty("database_server") + ":" + prop.getProperty("database_port"))
                        .build();

                CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
                CouchDbConnector db = new StdCouchDbConnector(prop.getProperty("database_name"), dbInstance);

                db.createDatabaseIfNotExists();
                System.out.println("Database initialized.");*/

                logger.info("huray !");

                booted = serveur.isUp();
            } catch (RemoteException e) {
                System.out.println("Impossible de démarrer le serveur !");
            } catch (MalformedURLException e) {
               System.out.println("L'URL de connexion est incorrecte");
            } catch (AlreadyBoundException e) {
                System.out.println("Erreur de binding");
            } catch (IOException e) {
                System.out.println("Impossible de charger le fichier de configuration du serveur");
            }
        }

        boolean exit = false;

        while (!exit) {
            Scanner sc = new Scanner(System.in);
            String str = sc.nextLine();

            if ("stop".equals(str)) {
                exit = true;
            }
        }

		serveur.shutdown();

        System.out.println("Extinction du serveur...");

        try {
            Naming.unbind("//127.0.0.1:8090/auth");
            Naming.unbind("//127.0.0.1:8090/list");
            Naming.unbind("//127.0.0.1:8090/join");

            System.out.println("Serveur éteint");
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


    }

	synchronized public void shutdown() {

		Set<Integer> ids = salons.keySet();
		try {
			for (Integer id : ids) {
				ISujetDiscussion topic = salons.get(id);
				List<IAfficheurClient> abonnes = topic.getAfficheurs();
                for (Iterator<IAfficheurClient> it = abonnes.iterator(); it.hasNext(); ) {
                    IAfficheurClient abonne = it.next();
                    /*try {*/
                        it.remove();
                    /*}*/
                    /*catch (RemoteException e) {
                        logger.info("Unable to disconnect a displayer");
                    }*/
				}
			}
		} catch (RemoteException e) {
			logger.error("Unable to disconnect followers");
		}

		for (IClientForum listener : listeners) {
            try {
                listener.notifyShutdown();
            } catch (RemoteException exception) {
                System.out.println("A client could not be notified about shutdown.");
            }
        }
	}

    public boolean add(String title, String owner) throws RemoteException {
        int i = 1;
        while (salons.get(i) != null) {
            i++;
        }
        salons.put(i, new SujetDiscussion(title, owner));
        logger.info("A new topic named '"+ title +"' has been made by "+ owner);

        logger.info( listeners.size() + " clients to notify");

		i = 0;
        for (IClientForum listener : listeners) {
			try {
            	listener.updateTopics(salons);
				logger.info("Notified client #"+ (i++));
			} catch (RemoteException e) {
				logger.info("Unable to update client #"+ (i++) );
			}
			
        }

        return true;
    }

	public boolean delete(Integer id, String owner) throws RemoteException {
		try {
			ISujetDiscussion topic = salons.get(id);
			if (owner.equals(topic.getOwner())) {
				//TODO
                return true;
			} else {
				logger.info("You ("+ owner +") dont owe this topic");
				return false;
			}
		} catch (RemoteException remoteException) {
			logger.error("Unable to fetch chanel #" + id + "");
			return false;
		} catch (NullPointerException npException) {
			logger.error("This chanel does not exist");
			return false;
		}
	}

    public ICommandFeedback handleCommand(String cmd, String user, ISujetDiscussion topic) throws RemoteException {
        System.out.println("Received command " + cmd );
        String[] args = cmd.split(" ");

        if (args.length == 0) {
            return new CommandFeedback("Unknow command !", false);
        }

        if (cmd.startsWith("/me")) {

            if (topic == null) {
                return new CommandFeedback("Vous pouvez utiliser cette commande seulement dans un salon", false, true);
            }

            ThirdPersonMessage m = new ThirdPersonMessage(cmd.replaceFirst("/me", user), user, topic);
            m.setDate(new java.sql.Date(Calendar.getInstance().getTime().getTime()));
            topic.diffuser(m);

            return new CommandFeedback("Me command", true);
        }
        else if (cmd.startsWith("/dice")) {

            if (topic == null) {
                return new CommandFeedback("Vous pouvez utiliser cette commande seulement dans un salon", false, true);
            }

            Random rand = new Random();
            int nombreAleatoire = rand.nextInt(6) + 1;

            DiceMessage m = new DiceMessage(Integer.toString(nombreAleatoire), user, topic);
            m.setDate(new java.sql.Date(Calendar.getInstance().getTime().getTime()));
            topic.diffuser(m);

            return new CommandFeedback("Dice command", true);
        }
        else {
            return new CommandFeedback("Unknow command !", false);
        }
    }
}
