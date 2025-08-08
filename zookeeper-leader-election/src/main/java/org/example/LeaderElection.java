package org.example;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.net.UnknownHostException;

public class LeaderElection implements Watcher 
{
    private static final String ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
    private static final int SESSION_TIMEOUT = 3000;
    private static final String ELECTION_NAMESPACE = "/election";

    private ZooKeeper zooKeeper;
    private String currentZnodeName;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        LeaderElection leaderElection = new LeaderElection();
        leaderElection.connectToZookeeper();
        leaderElection.volunteerForLeadership();
        leaderElection.electLeader();
        leaderElection.run();
        leaderElection.close();
    }

    public void connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
    }

    public void volunteerForLeadership() throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(ELECTION_NAMESPACE, false);
        if(stat == null){
            zooKeeper.create(ELECTION_NAMESPACE, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        
        String znodePrefix = ELECTION_NAMESPACE + "/c_";
        String znodeFullPath = zooKeeper.create(
                znodePrefix,
                new byte[]{},
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL
        );
        this.currentZnodeName = znodeFullPath.replace(ELECTION_NAMESPACE + "/", "");
        System.out.println("Znode name: " + currentZnodeName);
    }

    public void electLeader() throws KeeperException, InterruptedException {
        List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE, false);
        Collections.sort(children);

        String smallestChild = children.get(0);

        if (smallestChild.equals(currentZnodeName)) {
        try {
            System.out.println("I am the leader [" + InetAddress.getLocalHost().getHostName() + "]");
        } catch (UnknownHostException e) {
            System.out.println("I am the leader [UnknownHost]");
        }
        } else {
            System.out.println("I am not the leader. Watching node: " + children.get(Collections.binarySearch(children, currentZnodeName) - 1));
            watchPredecessor(children);
        }
    }

    private void watchPredecessor(List<String> children) throws KeeperException, InterruptedException {
        int index = Collections.binarySearch(children, currentZnodeName);
        String watchNode = ELECTION_NAMESPACE + "/" + children.get(index - 1);
        Stat stat = zooKeeper.exists(watchNode, this);
        if (stat == null) {
            electLeader();  
        }
    }

    public void run() throws InterruptedException {
        synchronized (zooKeeper) {
            zooKeeper.wait(); 
        }
    }

    public void close() throws InterruptedException {
        zooKeeper.close();
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case NodeDeleted:
                try {
                    electLeader();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
