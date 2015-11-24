package io.github.cr3ahal0.forum.server.impl;

import io.github.cr3ahal0.forum.client.IAfficheurClient;
import io.github.cr3ahal0.forum.client.IClientForum;
import io.github.cr3ahal0.forum.server.*;
import io.github.cr3ahal0.forum.server.exceptions.UnknownContentKindException;
import io.github.cr3ahal0.forum.server.impl.broadcast.*;
import io.github.cr3ahal0.forum.server.impl.commands.DiceMessage;
import io.github.cr3ahal0.forum.server.impl.commands.ThirdPersonMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.*;
import java.io.*;
import java.math.BigInteger;
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
import java.time.LocalDateTime;
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

    private String url;

    private Integer port;

    private final static String SERVER_OWNER = "root";

    public BroadcastEndpoint getEndPoint() {
        return endpoint;
    }

    BroadcastEndpoint endpoint;

    RepositoryHandler repositoryHandler;

    DataRepository topicsRepository = new DataRepository<ISujetDiscussion, String>() {

        @Override
        public ISujetDiscussion get(String id) {
            return salons.get(id);
        }


        @Override
        public boolean has(ISujetDiscussion object) {
            try {
                return (salons.get(object.getId()) != null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return false;
        }


        @Override
        public CRUDResult remove(ISujetDiscussion object) {
            if (has(object)) {
                salons.remove(object);

                int i = 1;
                for (IClientForum listener : listeners) {
                    try {
                        listener.updateTopics(salons);
                        logger.info("Notified client #" + (i++));
                    } catch (RemoteException e) {
                        logger.info("Unable to update client #" + (i++));
                    }
                }

                return CRUDResult.OK;
            }
            return CRUDResult.KO;
        }

        @Override
        public CRUDResult add(ISujetDiscussion object) {
            try {
                if (!has(object)) {
                    salons.put(object.getId(), object);

                    int i = 1;
                    for (IClientForum listener : listeners) {
                        try {
                            listener.updateTopics(salons);
                            logger.info("Notified client #" + (i++));
                        } catch (RemoteException e) {
                            logger.info("Unable to update client #" + (i++));
                        }
                    }

                    return CRUDResult.OK;
                }
                return CRUDResult.KO;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return CRUDResult.ERROR;
        }
    };

    DataRepository messagesRepository = new DataRepository<IMessage, String>()
    {

        @Override
        public IMessage get(String id) {
            //TODO implement
            return null;
        }

        @Override
        public CRUDResult add(IMessage object) {
            try {
                DataRepository repo = (DataRepository)ServeurForum.this.salons.get(object.getChanel().getId());
                return repo.add(object);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return CRUDResult.ERROR;
        }

        @Override
        public CRUDResult remove(IMessage object) {
            try {
                DataRepository repo = (DataRepository)ServeurForum.this.salons.get(object.getChanel().getId());
                return repo.remove(object);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return CRUDResult.ERROR;
        }


        @Override
        public boolean has(IMessage object) {
            try {
                DataRepository repo = (DataRepository)ServeurForum.this.salons.get(object.getChanel().getId());
                return repo.has(object);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return false;
        }
    };

    Map<Class, DataRepository> repositories = new ConcurrentHashMap<Class, DataRepository>();

    protected ServeurForum(String url, Integer port) throws RemoteException {

        this.url = url;
        this.port = port;

        listeners = new ArrayList<IClientForum>();
        salons = new ConcurrentHashMap<String, ISujetDiscussion>();

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

        repositories.put(SujetDiscussion.class, topicsRepository);
        repositories.put(Message.class, messagesRepository);

        repositoryHandler = new RepositoryHandler() {

            @Override
            public DataRepository get(Class c) {
                return repositories.get(c);
            }

            @Override
            public Object getRepositories() {
                return null;
            }
        };

        endpoint = new ServerEndpoint(this, repositoryHandler);
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
        /*for (BroadcastEndpoint neighbourg : knownServers) {
            try {
                logger.info("Notifying remote server " + neighbourg.getHandler().getUrl() + " at port "+ neighbourg.getHandler().getPort() + "about people joining chanel " + topic.getTitle());
                neighbourg.getHandler().join(topic, client);
            } catch (RemoteException e) {
                logger.error("Error while attempting to send join request to server " + neighbourg.getHandler().getUrl() + " at port "+ neighbourg.getHandler().getPort());
            }
        }*/

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
                Thread.sleep(2000);
                serveur = new ServeurForum(url, port);

                System.out.println("RMI initializing...");
                LocateRegistry.createRegistry(port);

                Naming.rebind(urlPort + "/auth", serveur);
                Naming.rebind(urlPort + "/list", serveur);
                Naming.rebind(urlPort + "/join", serveur);

                Naming.rebind(urlPort + "/endpoint", serveur.getEndPoint());

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
            } catch (IOException e) {
                System.out.println("Impossible de charger le fichier de configuration du serveur");
            } catch (InterruptedException e) {
                e.printStackTrace();
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
                    serveur.endpoint.addServer(params[1]);
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

            // >> test
            final BigInteger bigint = new BigInteger(1, encryption.digest(bytesKey));
            key = String.format("%032x", bigint);
            // << endtest

            if (salons.get(key) != null) {
                return ServeurResponse.TOPIC_KNOWN;
            }

            ISujetDiscussion result = new SujetDiscussion(title, owner, key);

            //Marshalling
            JAXBContext jaxbContext = JAXBContext.newInstance(SujetDiscussion.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            StringWriter sw = new StringWriter();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(result, sw);

            logger.info(sw.getBuffer().toString());

            HistoryAction action = new HistoryAction();
            action.setAction(ActionKind.CREATE);
            action.setContent(ContentKind.TOPIC);
            action.setClassifier(SujetDiscussion.class);
            action.setData(sw.toString());

            endpoint.broadcast(action);

            /*history.add(action);

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
            */
            return ServeurResponse.TOPIC_UNKNOWN;
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return ServeurResponse.ERROR;
        } catch (PropertyException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            logger.info("Error while marshalling topic");
            e.printStackTrace();
        }
        return ServeurResponse.ERROR;

    }


    public ServeurResponse addMessage(ISujetDiscussion topic, LocalDateTime date, String content, String author) throws RemoteException {

        try {

            ISujetDiscussion realTopic = salons.get(topic.getId());

            Message message = new Message(content, author, (SujetDiscussion)realTopic);
            message.setDate(date);
            message.setId(realTopic.getMessageKey(author, date));

            //Marshalling
            JAXBContext jaxbContext = JAXBContext.newInstance(Message.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            StringWriter sw = new StringWriter();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(message, sw);

            HistoryAction action = new HistoryAction();
            action.setAction(ActionKind.CREATE);
            action.setContent(ContentKind.MESSAGE);
            action.setClassifier(Message.class);
            action.setData(sw.toString());

            endpoint.broadcast(action);

            return ServeurResponse.MESSAGE_UNKNOWN;
        }
        catch (RemoteException e) {
            logger.error("Error while send new message");
        } catch (PropertyException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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

            ThirdPersonMessage m = new ThirdPersonMessage(cmd.replaceFirst("/me", user), user, (SujetDiscussion)topic);
            m.setDate(LocalDateTime.now());
            topic.diffuser(m);

            return new CommandFeedback("Me command", true);
        }
        else if (cmd.startsWith("/dice")) {

            if (topic == null) {
                return new CommandFeedback("Vous pouvez utiliser cette commande seulement dans un salon", false, true);
            }

            Random rand = new Random();
            int nombreAleatoire = rand.nextInt(6) + 1;

            DiceMessage m = new DiceMessage(Integer.toString(nombreAleatoire), user, (SujetDiscussion)topic);
            m.setDate(LocalDateTime.now());
            topic.diffuser(m);

            return new CommandFeedback("Dice command", true);
        }
        else {
            return new CommandFeedback("Unknow command !", false);
        }
    }

    /**
     * Broadcast changes to known servers
     */
    /*
    public boolean broadcastNewTopic(String title, String owner) throws RemoteException {
        System.out.println("Noticing "+ knownServers.size() +" known servers that a new topic has been created");
        for (BroadcastEndpoint serveur : knownServers) {
            ServeurBroadcast bcast = new ServeurBroadcast(serveur, title, owner);
            new Thread(bcast).start();
        }
        return true;
    }
    */

    /**
     * Broadcast new message to known servers
     */
    /*
    public boolean broadcastNewMessage(ISujetDiscussion topic, Date date, String content, String author) throws RemoteException {
        System.out.println("Noticing "+ knownServers.size() +" known servers that a new message has been created");
        for (BroadcastEndpoint serveur : knownServers) {
            ServeurBroadcastNewMessage bcast = new ServeurBroadcastNewMessage(serveur, topic, date, content, author);
            new Thread(bcast).start();
        }
        return true;
    }
    */

}
