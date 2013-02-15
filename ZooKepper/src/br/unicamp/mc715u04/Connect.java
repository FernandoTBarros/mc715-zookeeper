package br.unicamp.mc715u04;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

public class Connect implements Watcher
{

	static ZooKeeper zk = null;

	public Connect()
	{
		System.out.println("Starting ZK:");
		String host = "cluster1.lab.ic.unicamp.br:35858";

		try 
		{
			ZooKeeper zk = new ZooKeeper(host, 3000, this);
			System.out.println("Starting ZK:");
		}
		catch (IOException e) 
		{
			System.out.println(e.toString());
			zk = null;
		}
	}

	public void process(WatchedEvent event) 
	{
	}

	public static void main(String[] args) throws IOException, InterruptedException
	{
		System.out.println("Starting ZK:");

		//ZooKeeper zk = new ZooKeeper(host, 3000, new Watcher());

		//ZooKeeper(String host, int sessionTimeout, Watcher watcher) 

		String host = "cluster1.lab.ic.unicamp.br:35858";
		String path = "/mc715/" + args[0];

		ZooKeeper zk = new ZooKeeper(host, 3000, new Watcher() 
		{
			public void process (WatchedEvent event) 
			{

			}
		} 
				);

		System.out.println("Finished starting ZK: " + zk);

		try
		{
			//String createdPath = zk.create(path, null,ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			String command = "java -cp :../zookeeper-3.4.5.jar:../lib/jline-0.9.94.jar:../lib/log4j-1.2.15.jar:../lib/slf4j-log4j12-1.6.1.jar:../lib/slf4j-api-1.6.1.jar Executor cluster1.lab.ic.unicamp.br:35858 /mc715/"+args[0]+" mc715 sh conectar.sh";

			String createdPath = zk.create(path, null,ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

			Runtime rt = Runtime.getRuntime();
			command = "echo 'asdasda'";
			Process pr = rt.exec(command);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

}



