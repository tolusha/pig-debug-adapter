import pig.adapter.ProtocolServer;

/**
 * @author Anatolii Bazko
 */
public class PigMain {
    public static void main(String... argvs) {
        ProtocolServer debugServer = new ProtocolServer(System.in, System.out);
        Runtime.getRuntime().addShutdownHook(new Thread(debugServer::stop));

        debugServer.run();
    }
}
