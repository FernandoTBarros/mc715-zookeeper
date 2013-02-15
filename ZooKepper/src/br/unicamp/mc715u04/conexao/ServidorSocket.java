package br.unicamp.mc715u04.conexao;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import br.unicamp.mc715u04.Executor;

public class ServidorSocket implements Runnable
{
	/*-*-*-* Variaveis e Objetos Privados *-*-*-*/
	private volatile boolean execucao = true;
	private static ServidorSocket instance = null;
	private static Thread thread;
	private ServerSocket serverSocket;
	private Executor listener;
	private List<Conexao> clientes = new ArrayList<Conexao>();

	/*-*-*-* Metodos Publicos *-*-*-*/
	/**
	 * Thread utilizada para incrementar o tempo
	 */
	public void run()
	{
		Socket socket;
		int porta;

		while(execucao)
		{
			try
			{
				if(serverSocket==null || serverSocket.isClosed())
				{
					porta = 35857;
					try
					{
						serverSocket = new ServerSocket(porta);
					} 
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

				socket = serverSocket.accept(); //Espera Cliente
				System.out.println("Cliente conectado: " + socket.getInetAddress());
				for(int i = 0; i < clientes.size(); i++)
				{
					if(clientes.get(i).getSocket().isClosed())
					{
						clientes.remove(i);
						i--;
					}
				}
				clientes.add(new Conexao(socket));
				getListener().publishConected();
			}
			catch (Exception e)
			{ 
				try 				{ Thread.sleep(1000); }
				catch(Exception e1) { }

				serverSocket = null;
				e.printStackTrace();
				return;
			}
		}
	}

	public void desativar(ServerSocket serverSocket)
	{
		try
		{
			instance = null;
			serverSocket.close();
			execucao = false;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
	}

	/**
	 * Retorna a instancia da Classe, para que ela seja unica
	 * @return instancia unica da classe
	 */
	public static ServidorSocket getInstance() 
	{
		if(instance==null) 
		{ 
			instance = new ServidorSocket();
			thread = new Thread(instance);
			thread.start();
		}
		return instance;
	}

	public void publish(int i, String nome, String metadata) throws IOException
	{
		for(Conexao cliente : clientes)
		{
			try
			{
				String mensagem = i + "," + nome + "," + metadata;
				cliente.enviarMensagem(mensagem);
				System.out.println("Mensagem [" + mensagem + "] enviada para Cliente: " + cliente.getSocket().getInetAddress());
			}
			catch(Exception e)
			{
			}
		}
	}

	public Executor getListener() 				{ return listener; }
	public void setListener(Executor listener) 	{ this.listener = listener; }
}
