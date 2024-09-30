package cluster.management;

import org.apache.zookeeper.KeeperException;

public interface OnElectionCallback {
  void onElectToBeLeader() throws InterruptedException, KeeperException;
  void onWorker();

}
