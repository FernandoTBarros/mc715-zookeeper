package br.unicamp.mc715u04;
/* simple example program to use DataMonitor to start and
 * stop executables based on a znode. The program watches the
 * specified znode and saves the data that corresponds to the
 * znode in the filesystem. It also starts the specified program
 * with the specified arguments when the znode exists and kills
 * the program if the znode goes away.
 */
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import br.unicamp.mc715u04.conexao.ServidorSocket;

public class Executor implements Watcher, Runnable, DataMonitor.DataMonitorListener 
{
	String znode;

	HashMap<String, DataMonitor> mapDm;

	ZooKeeper zk;

	DataMonitor dm;
	public Executor(String hostPort, String znode) throws KeeperException, IOException, InterruptedException 
	{
		this.znode = znode;
		mapDm = new HashMap<String, DataMonitor>();
		zk = new ZooKeeper(hostPort, 3000, this);
		try
		{
			List<String> children = zk.getChildren(znode, null);
			for(String child : children)
			{
				System.out.println("Buscando data para: " + znode + child);
//				ServidorSocket.getInstance().publish(0, child, new String(zk.getData(znode + child, false, null)));
				System.out.println("Criando DataMonitor pro child: " + child);
				DataMonitor dm = new DataMonitor(zk, znode + child, null, this);
				mapDm.put(znode + child, dm);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		dm = new DataMonitor(zk, znode, null, this);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		if (args.length < 2) 
		{
			System.err.println("USAGE: Executor hostPort znode");
			System.exit(2);
		}
		String hostPort = args[0];
		String znode = args[1];

		try 
		{
			Executor executor = new Executor(hostPort, znode);
			ServidorSocket.getInstance().setListener(executor);
			executor.run();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	/***************************************************************************
	 * We do process any events ourselves, we just need to forward them on.
	 * 
	 * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.proto.WatcherEvent)
	 */
	public void process(WatchedEvent event) 
	{
		System.out.println("Executor process event: " + event);
		if(mapDm.containsKey(event.getPath())) { mapDm.get(event.getPath()).process(event); }
		else { dm.process(event); }
	}

	public void run() 
	{
		try 
		{
			synchronized (this) 
			{
				while (true) 
				{
					wait();
				}
			}
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}

	public void closing(int rc) 
	{
		synchronized (this) 
		{
			notifyAll();
		}
	}

	public void exists(byte[] data, String path) 
	{
		if (data == null)
		{
			try
			{
				System.out.println("Enviando msg de desconexao do node " + path);
				ServidorSocket.getInstance().publish(1, path, "fim");
			} 
			catch (Exception e) { e.printStackTrace(); }
		}
		else 
		{
			try 
			{
				System.out.println("Enviando msg de conexao do node " + path);
				ServidorSocket.getInstance().publish(0, path, new String(data));
			} 
			catch (Exception e) { e.printStackTrace(); }
		}
	}

	public void publishConected() throws KeeperException, InterruptedException, IOException
	{
		List<String> children = zk.getChildren(znode, null);
		for(String child : children)
		{
			ServidorSocket.getInstance().publish(0, znode + child, new String(zk.getData(znode + child, false, null)));
		}
	}
}
