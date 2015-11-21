package io.github.cr3ahal0.forum.server.impl;

import io.github.cr3ahal0.forum.client.IAfficheurClient;
import io.github.cr3ahal0.forum.client.IClientForum;
import io.github.cr3ahal0.forum.server.ICommandFeedback;
import io.github.cr3ahal0.forum.server.IServeurForum;
import io.github.cr3ahal0.forum.server.ISujetDiscussion;
import io.github.cr3ahal0.forum.server.ServeurResponse;
import io.github.cr3ahal0.forum.server.impl.broadcast.ActionKind;
import io.github.cr3ahal0.forum.server.impl.broadcast.ContentKind;
import io.github.cr3ahal0.forum.server.impl.broadcast.HistoryAction;
import io.github.cr3ahal0.forum.server.impl.commands.DiceMessage;
import io.github.cr3ahal0.forum.server.impl.commands.ThirdPersonMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServeurForum
        extends UnicastRemoteObject
        implements IServeurForum {

    private List<IClientForum> listeners;

    private static final Logger logger = LogManager.getLogger(ServeurForum.class);

    private ConcurrentHashMap<String, ISujetDiscussion> salons;

    private boolean up = false;

    public boolean isUp() {
        return up;
    }

    private Set<IServeurForum> knownServers;

    private String url;

    private Integer port;

    private List<HistoryAction> history;

    private final static String SERVER_OWNER = "root";

    protected ServeurForum(String url, Integer port) throws RemoteException {

        this.url = url;
        this.port = port;

        listeners = new ArrayList<IClientForum>();
        salons = new ConcurrentHashMap<String, ISujetDiscussion>();

        knownServers = new HashSet<IServeurForum>();

        history = Collections.synchronizedList(new ArrayList<HistoryAction>());

        int i = 1;

        salons.put(String.valueOf(i), new SujetDiscussion("GlobalChat", "root", String.valueOf(i++)));
        salons.put(String.valueOf(i), new SujetDiscussion("Middleware", "root", String.valueOf(i++)));
        salons.put(String.valueOf(i), new SujetDiscussion("IHM", "root", String.valueOf(i++)));
        salons.put(String.valueOf(i), new SujetDiscussion("Architecture Logicielle", "root", String.valueOf(i++)));
        salons.put(String.valueOf(i), new SujetDiscussion("Architectures Distribuées", "root", String.valueOf(i++)));
        salons.put(String.valueOf(i), new SujetDiscussion("MDE", "root", String.valueOf(i++)));
        salons.put(String.valueOf(i), new SujetDiscussion("Web Semantique", "root", String.valueOf(i++)));
        salons.put(String.valueOf(i), new SujetDiscussion("Cloud Computing", "root", String.valueOf(i++)));
        salons.put(String.valueOf(i), new SujetDiscussion("Conference", "root", String.valueOf(i++)));
        salons.put(String.valueOf(i), new SujetDiscussion("Meta-Meta-Meta-Model", "root", String.valueOf(i++)));
        salons.put(String.valueOf(i), new SujetDiscussion("Base de Données", "root", String.valueOf(i++)));
        salons.put(String.valueOf(i), new SujetDiscussion("HTml 7", "root", String.valueOf(i++)));


        up = true;
    }

    @Override
    public String getUrl() throws RemoteException {
        return url;
    }

    @Override
    public String getPort() throws RemoteException {
        return port.toString();
    }

    public boolean auth(IClientForum token, String username, String password) throws RemoteException {

        if ( (!username.equals("")) && !SERVER_OWNER.equals(username) && (password.equals("")) ) {
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

    public Map<String, ISujetDiscussion> list() throws RemoteException {
        return salons;
    }

    public ISujetDiscussion join(String id) throws RemoteException {
        ISujetDiscussion token = salons.get(id);
        if (token != null) {

        }

        return token;
    }

    public ServeurResponse join (ISujetDiscussion topic, IAfficheurClient client) throws RemoteException {

        try {
            //local join
            salons.get(topic.getId()).join(client);
        } catch (RemoteException e) {
            logger.error("Error while notifying local topic");
            return ServeurResponse.ERROR;
        }

        //broadcast to neightbourgs
        for (IServeurForum neighbourg : knownServers) {
            try {
                logger.info("Notifying remote server " + neighbourg.getUrl() + " at port "+ neighbourg.getPort() + "about people joining chanel " + topic.getTitle());
                neighbourg.join(topic, client);
            } catch (RemoteException e) {
                logger.error("Error while attempting to send join request to server " + neighbourg.getUrl() + " at port "+ neighbourg.getPort());
            }
        }

        return ServeurResponse.OK;
    }

    public static void main(String[] args) {
        System.out.println("Démarrage du serveur...");
        boolean booted = false;
		ServeurForum serveur = null;

        String url = (args.length > 0) ? args[0] : "//127.0.0.1";
        Integer port = (args.length > 1) ? Integer.valueOf(args[1]) : 8090;

        String urlPort = url + ":" + port;
        System.out.println("Trying to start server for url "+ url +":"+ port);

        while (!booted) {
            try {
                serveur = new ServeurForum(url, port);

                System.out.println("RMI initializing...");
                LocateRegistry.createRegistry(port);

                Naming.bind(urlPort + "/auth", serveur);
                Naming.bind(urlPort + "/list", serveur);
                Naming.bind(urlPort + "/join", serveur);

                Naming.bind(urlPort + "/endpoint", serveur);

                System.out.println("RMI intialized.");
                System.out.println("Server started for url "+ url +":"+ port);

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
                e.printStackTrace();
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

            if (str.startsWith("add ")) {
                String[] params = str.split(" ");
                if (params.length < 2) {
                    System.out.println("Invalid syntax : \nadd <serverUrl>:<serverPort>");
                    continue;
                }

                try {
                    serveur.addServer(params[1]);
                } catch (RemoteException e) {
                    System.out.println("Unable to contact local server...");
                    continue;
                }

            }
            if ("stop".equals(str)) {
                exit = true;
            }
        }

		serveur.shutdown();

        System.out.println("Extinction du serveur...");

        try {
            Naming.unbind(urlPort +"/auth");
            Naming.unbind(urlPort +"/list");
            Naming.unbind(urlPort +"/join");
            Naming.unbind(urlPort +"/endpoint");

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

		Set<String> ids = salons.keySet();
		try {
			for (String id : ids) {
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

    public ServeurResponse add(String title, String owner) throws RemoteException {

        try {
            MessageDigest encryption = MessageDigest.getInstance("MD5");
            byte[] bytesKey = (owner + title).getBytes("UTF-8");
            String key = new String(encryption.digest(bytesKey),  StandardCharsets.UTF_8);

            if (salons.get(key) != null) {
                return ServeurResponse.TOPIC_KNOWN;
            }

            salons.put(key, new SujetDiscussion(title, owner, key));

            //Marshalling
            JAXBContext jaxbContext = JAXBContext.newInstance(SujetDiscussion.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            StringWriter sw = new StringWriter();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(salons.get(key), sw);

            HistoryAction action = new HistoryAction();
            action.setAction(ActionKind.CREATE);
            action.setContent(ContentKind.TOPIC);
            action.setClassifier(SujetDiscussion.class);
            action.setData(sw.toString());

            history.add(action);

            logger.info("A new topic named '"+ title +"' has been made by "+ owner);

            logger.info( listeners.size() + " clients to notify");

            int i = 1;
            for (IClientForum listener : listeners) {
                try {
                    listener.updateTopics(salons);
                    logger.info("Notified client #"+ (i++));
                } catch (RemoteException e) {
                    logger.info("Unable to update client #"+ (i++) );
                }

            }

            logger.info( knownServers.size() + " servers to notify");
            broadcastNewTopic(title, owner);

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return ServeurResponse.ERROR;
        } catch (PropertyException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            logger.info("Error while marshalling topic");
            e.printStackTrace();
        }

        return ServeurResponse.TOPIC_UNKNOWN;
    }

    public ServeurResponse addMessage(ISujetDiscussion topic, Date date, String content, String author) throws RemoteException {

        try {
            //Local broadcast
            ServeurResponse localResponse = salons.get(topic.getId()).diffuser(date, content, author);
            if (!localResponse.equals(ServeurResponse.MESSAGE_UNKNOWN)) {
                logger.info("Message already knew this message, stopping broadcast");
                return localResponse;
            }

            //Neighbourg broadcast
            logger.info(knownServers.size() + " servers to notify");
            broadcastNewMessage(topic, date, content, author);

            return ServeurResponse.MESSAGE_UNKNOWN;
        }
        catch (RemoteException e) {
            logger.error("Error while send new message");
        }
        return ServeurResponse.ERROR;
    }

	public boolean delete(String id, String owner) throws RemoteException {
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

    /**
     * Add a server to the list of known servers.
     * @param url the complete url (containing port if required) of the given server
     */
    public void addServer(String url) throws RemoteException {
        try {
            IServeurForum server = (IServeurForum) Naming.lookup(url +"/endpoint");
            boolean alreadyknown = knownServers.add(server);
            if (!alreadyknown) {
                System.out.println("server "+ url +" is already known");
                return;
            }

            //Force the remote serveur to add the current one
            server.acknowledgeServer(this);

        } catch (RemoteException | MalformedURLException e) {
            System.out.println("Error while accessing server " + url + " : url may be malformed or the remote server may be unavailable");
        } catch (NotBoundException e) {
            System.out.println("No associate binding for server " + url + "");
        }
    }

    @Override
    public void acknowledgeServer(IServeurForum server) throws RemoteException {
        knownServers.add(server);
    }

    /**
     * Broadcast changes to known servers
     */
    public boolean broadcastNewTopic(String title, String owner) throws RemoteException {
        System.out.println("Noticing "+ knownServers.size() +" known servers that a new topic has been created");
        for (IServeurForum serveur : knownServers) {
            ServeurBroadcast bcast = new ServeurBroadcast(serveur, title, owner);
            new Thread(bcast).start();
        }
        return true;
    }

    /**
     * Broadcast new message to known servers
     */
    public boolean broadcastNewMessage(ISujetDiscussion topic, Date date, String content, String author) throws RemoteException {
        System.out.println("Noticing "+ knownServers.size() +" known servers that a new message has been created");
        for (IServeurForum serveur : knownServers) {
            ServeurBroadcastNewMessage bcast = new ServeurBroadcastNewMessage(serveur, topic, date, content, author);
            new Thread(bcast).start();
        }
        return true;
    }

}
