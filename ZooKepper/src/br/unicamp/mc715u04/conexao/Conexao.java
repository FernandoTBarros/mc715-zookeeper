package br.unicamp.mc715u04.conexao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class Conexao
{
	/*-*-*-* Variaveis e Objetos Privados *-*-*-*/
	private Socket socket = null;
	private InputStream is = null;
	private OutputStream os = null;
	private BufferedReader br = null;
	
	/*-*-*-* Construtores *-*-*-*/
	public Conexao(Socket socket)
	{
		try
		{
			this.socket = socket;
			os = socket.getOutputStream();
			is = socket.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
		}
		catch (Exception e) 
		{
			System.err.println("Falha ao tentar estabelecer conexao");
			e.printStackTrace();
			this.socket = null;
		}
	}
	
	/*-*-*-* Metodos Publicos *-*-*-*/
	/**
	 * Envia a mensagem pelo socket para o cliente
	 * @param mensagem a ser enviada
	 * @throws IOException 
	 */
	public void enviarMensagem(String mensagem) throws IOException
	{
		String msg = mensagem + "\n";
		if(os!=null) { os.write(msg.getBytes()); }
	}
	
	/**
	 * Metodo que recebe uma mensagem do cliente
	 * ATENCAO este método é bloqueante
	 * @return mensagem recebida
	 * @throws IOException 
	 */
	public String receberMensagem() throws IOException
	{
		String mensagem = br.readLine();
    	return mensagem!=null ? mensagem : null;
	}
	
	/**
	 * Desconecta do cliente
	 */
	public void desconectar()
	{
		try
		{
			if(is!=null) 		{ is.close(); }
			if(os!=null) 		{ os.close(); }
			if(socket!=null) 	{ socket.close(); }
		}
		catch(Exception e) 
		{
			System.err.println("Falha ao tentar desconectar conexao");
			e.printStackTrace();
		}
	}

	/*-*-*-* Metodos Gets e Sets *-*-*-*/
	public Socket getSocket() { return socket; }
}
