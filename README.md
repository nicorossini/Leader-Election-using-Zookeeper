# Leader-Election-using-Zookeeper
This project demonstrates **leader election** using [Apache ZooKeeper](https://zookeeper.apache.org/).  
It is a simple Java application (Maven project) where multiple instances connect to a ZooKeeper server, create **ephemeral sequential znodes**, and elect a leader.

---

## Features
- Connects to a running ZooKeeper server
- Creates an election znode (`/election`)
- Each participant creates an **ephemeral sequential node**
- Determines leader vs non-leader nodes
- Prints clear output:
  - `I am the leader`
  - `I am not the leader`
  - `Znode name: ...`

## Getting Started

### 1. Start ZooKeeper locally
```bash
bin/zkServer.sh start
```

### 2. Clone this repository
```bash
git clone https://github.com/your-username/zookeeper-leader-election.git
cd zookeeper-leader-election
```

### 3. Build the project
```bash
mvn clean package
```

### 4. Run the application
```bash
mvn exec:java
```

---

## Example Output
When running multiple instances, one will become leader:

```
Znode name: /election/c_00000001
I am the leader
```

Other instances will print:

```
Znode name: /election/c_00000002
I am not the leader
```
