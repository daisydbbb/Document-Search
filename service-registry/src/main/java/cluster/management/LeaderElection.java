package cluster.management;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;

public class LeaderElection implements Watcher {
  private static final String ELECTION_NAMESPACE = "/election";
  private final ZooKeeper zooKeeper;
  private String currentZnodeName;
  private OnElectionCallback onElectionCallback;

  public LeaderElection(ZooKeeper zooKeeper, OnElectionCallback onElectionCallback) {
    this.zooKeeper = zooKeeper;
    this.onElectionCallback = onElectionCallback;
  }

  public void volunteerForLeadership() throws InterruptedException, KeeperException {
    String znodePrefix = ELECTION_NAMESPACE + "/c_";
    String znodeFullPath = zooKeeper.create(znodePrefix, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    System.out.println("znode name: " + znodeFullPath);
    this.currentZnodeName = znodeFullPath.replace(ELECTION_NAMESPACE+"/", "");
  }

  public void reelectLeader() throws InterruptedException, KeeperException {
    Stat predecessorStat = null;
    String predecessorZnodeName = "";
    while (predecessorStat == null) {
      List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE, false);

      Collections.sort(children);
      String smallestChild = children.get(0);
      if (smallestChild.equals(currentZnodeName)) {
        System.out.println("I am the leader");
        onElectionCallback.onElectToBeLeader();
        return;
      } else {
        System.out.println("I am not the leader, " + smallestChild +" is the leader");
        int predecessorIndex = Collections.binarySearch(children, currentZnodeName) -1;
        predecessorZnodeName = children.get(predecessorIndex);
        predecessorStat = zooKeeper.exists(ELECTION_NAMESPACE + "/" + predecessorZnodeName, this);
      }
    }
    onElectionCallback.onWorker();
    System.out.println("Watching znode: " + predecessorZnodeName);
  }

  @Override
  public void process(WatchedEvent event) {
    switch (event.getType()) {
      case NodeDeleted:
        try {
          reelectLeader();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        } catch (KeeperException e) {
          throw new RuntimeException(e);
        }
    }
  }
}
