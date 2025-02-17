import org.apache.zookeeper.KeeperException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import cluster.management.OnElectionCallback;
import cluster.management.ServiceRegistry;
import networking.WebClient;
import networking.WebServer;
import search.SearchCoordinator;
import search.SearchWorker;

public class OnElectionAction implements OnElectionCallback {
  private final ServiceRegistry workersServiceRegistry;
  private final ServiceRegistry coordinatorsServiceRegistry;
  private final int port;
  private WebServer webServer;

  public OnElectionAction(ServiceRegistry workersServiceRegistry,
                          ServiceRegistry coordinatorsServiceRegistry,
                          int port) {
    this.workersServiceRegistry = workersServiceRegistry;
    this.coordinatorsServiceRegistry = coordinatorsServiceRegistry;
    this.port = port;
  }

  @Override
  public void onElectToBeLeader() throws InterruptedException, KeeperException {
    workersServiceRegistry.unregisterFromCluster();
    workersServiceRegistry.registerForUpdates();
    if (webServer != null) {
      webServer.stop();
    }
    SearchCoordinator searchCoordinator = new SearchCoordinator(workersServiceRegistry, new WebClient());
    webServer = new WebServer(port, searchCoordinator);
    webServer.startServer();

    try {
      String currentServerAddress =
              String.format("http://%s:%d%s", InetAddress.getLocalHost().getCanonicalHostName(), port, searchCoordinator.getEndpoint());
      coordinatorsServiceRegistry.registerToCluster(currentServerAddress);
    } catch (InterruptedException | UnknownHostException | KeeperException e) {
      e.printStackTrace();
    }

  }

  @Override
  public void onWorker() {
    SearchWorker searchWorker = new SearchWorker();
    if (webServer == null) {
      webServer = new WebServer(port, searchWorker);
      webServer.startServer();
    }

    try {
      String currentServiceAddress = String.format("http://%s:%d", InetAddress.getLocalHost().getCanonicalHostName(), port);
      workersServiceRegistry.registerToCluster(currentServiceAddress);
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (KeeperException e) {
      throw new RuntimeException(e);
    }
  }
}
