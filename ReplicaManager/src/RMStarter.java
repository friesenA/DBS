
public class RMStarter {

	public static void main(String[] args) {
		ReplicaManager rm0 = new ReplicaManager(7000, 7654, "AFReplica.server.server", true);
		ReplicaManager rm1 = new ReplicaManager(7100, 7654, "AFReplica.server.server", true);
		ReplicaManager rm2 = new ReplicaManager(7200, 7654, "AFReplica.server.server", true);
		ReplicaManager rm3 = new ReplicaManager(7300, 7654, "AFReplica.server.server", true);
		
		new Thread(rm0).start();
		new Thread(rm1).start();
		new Thread(rm2).start();
		new Thread(rm3).start();

	}

}
