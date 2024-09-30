package cluster.management;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceRegistry implements Watcher {
  private static final String REGISTRY_ZNODE = "/service_registry";
  private final ZooKeeper zooKeeper;
  private String currentZnode = null;
  private List<String> allServiceAddresses = null;

  public ServiceRegistry(ZooKeeper zooKeeper) {
    this.zooKeeper = zooKeeper;
    createServiceRegistryZnode();
  }

  public void registerToCluster(String metadata) throws InterruptedException, KeeperException {
    if (this.currentZnode != null) {
      System.out.println("Already registered to service registry");
      return;
    }
    this.currentZnode = zooKeeper.create(REGISTRY_ZNODE+"/n_", metadata.getBytes(),
            ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    System.out.println("Registered to service registry");
  }

  public void registerForUpdates() {
    try {
      updateAddress();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (KeeperException e) {
      throw new RuntimeException(e);
    }
  }

  public synchronized List<String> getAllServiceAddresses() throws InterruptedException, KeeperException {
    if (allServiceAddresses == null) {
      updateAddress();
    }
    return allServiceAddresses;
  }

  public void unregisterFromCluster() throws InterruptedException, KeeperException {
    if (currentZnode != null && zooKeeper.exists(currentZnode, false) != null) {
      zooKeeper.delete(currentZnode, -1);
    }
  }


  private void createServiceRegistryZnode() {
    try {
      if (zooKeeper.exists(REGISTRY_ZNODE, false) == null) {
        zooKeeper.create(REGISTRY_ZNODE, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (KeeperException e) {
      e.printStackTrace();
    }
  }

  private synchronized void updateAddress() throws InterruptedException, KeeperException {
    List<String> workerZnodes = zooKeeper.getChildren(REGISTRY_ZNODE, this);
    List<String> addresses = new ArrayList<>(workerZnodes.size());

    for (String workerZnode : workerZnodes) {
      String workerZnodeFullPath = REGISTRY_ZNODE + "/" + workerZnode;
      Stat stat = zooKeeper.exists(workerZnodeFullPath, false);
      if (stat == null) {
        continue;
      }
      byte [] addressBytes = zooKeeper.getData(workerZnodeFullPath, false, stat);
      String address = new String(addressBytes);
      addresses.add(address);
    }
    this.allServiceAddresses = Collections.unmodifiableList(addresses);
    System.out.println("The cluster addresses are: " + allServiceAddresses);
  }

  @Override
  public void process(WatchedEvent watchedEvent) {
    try {
      updateAddress();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (KeeperException e) {
      throw new RuntimeException(e);
    }
  }
}
