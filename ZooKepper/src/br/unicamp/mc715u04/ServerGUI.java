package br.unicamp.mc715u04;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import br.unicamp.mc715u04.conexao.Conexao;

public class ServerGUI extends JFrame implements Runnable
{
	private static final long serialVersionUID = 1L;

	private JPanel painelPrincipal = new JPanel();
	private JButton btConectar = new JButton("Conectar host: cluster1.lab.ic.unicamp.br");
	private JButton btDesconectar = new JButton("Desconectar");
	
	static ImageIcon onlineIcon = new ImageIcon("res/online.png");
	static ImageIcon offlineIcon = new ImageIcon("res/offline.png");
	private DefaultTableModel dtClientes;
	private static ServerGUI instance = null;
	private static Thread thread;

	private ActionListener escutaBotoes = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			escutaBotoes(e);
		}
	};

	private JTable tabelaClientes;

	private JScrollPane scrollPaneClientes;

	private Conexao conexao;

	private boolean conected = false;

	public ServerGUI()
	{
		this.setVisible(true);
		this.setSize(390,400);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Nodes conectados no ZooKeeper");

		painelPrincipal.setLayout(new BoxLayout(painelPrincipal, BoxLayout.Y_AXIS));
		this.getContentPane().add(painelPrincipal);
		this.getContentPane().setComponentZOrder(painelPrincipal, 0);

		btConectar.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		btConectar.setPreferredSize(new Dimension(120,20));
		btConectar.setMinimumSize(new Dimension(120,20));
		btConectar.addActionListener(escutaBotoes);
		painelPrincipal.add(btConectar);
		
		btDesconectar.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		btDesconectar.setPreferredSize(new Dimension(120,20));
		btDesconectar.setMinimumSize(new Dimension(120,20));
		btDesconectar.addActionListener(escutaBotoes);
		painelPrincipal.add(btDesconectar);

		dtClientes = new DefaultTableModel(null, new String[]{"", "Usuário", "Status"});
		tabelaClientes = new JTable(dtClientes)
		{
			private static final long serialVersionUID = 1L;
			//  Returning the Class of each column will allow different
			//  renderers to be used based on Class
			public Class getColumnClass(int column)
			{
				return getValueAt(0, column).getClass();
			}
		};
		tabelaClientes.getColumnModel().getColumn(0).setPreferredWidth(20);
		tabelaClientes.getColumnModel().getColumn(1).setPreferredWidth(50);
		tabelaClientes.getColumnModel().getColumn(2).setPreferredWidth(200);

		scrollPaneClientes = new JScrollPane(tabelaClientes);
		scrollPaneClientes.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		painelPrincipal.add(scrollPaneClientes);

		painelPrincipal.revalidate();
		painelPrincipal.repaint();
	}

	/*-*-*-* Listeners *-*-*-*/
	/**
	 * Escuta eventos em botoes
	 * @param actionEvent
	 */
	public void escutaBotoes(ActionEvent actionEvent)
	{
		try
		{
			if(actionEvent.getSource()==btConectar) 		{ conectar(); }
			else if(actionEvent.getSource()==btDesconectar) { desconectar(); }
		}
		catch(Exception e) 
		{ e.printStackTrace(); }
	}

	private void conectar()
	{
		Socket socket;
		try
		{
			socket = new Socket("cluster1.lab.ic.unicamp.br", 35857);
			setConexao(new Conexao(socket));
			for(int j = 0; j < dtClientes.getRowCount(); j++)
			{ dtClientes.removeRow(j); }
			conected = true;
			JOptionPane.showMessageDialog(null, "Conectado com Sucesso!", "Conectado", JOptionPane.INFORMATION_MESSAGE);
			btConectar.setText("Conectado em \"cluster1.lab.ic.unicamp.br\"");
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(null, "Falha ao conectar no Host.", "ERRO", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void desconectar()
	{
		try
		{
			conected = false;
			getConexao().desconectar();
			for(int j = 0; j < dtClientes.getRowCount(); j++)
			{ dtClientes.removeRow(j--); }
			btConectar.setText("Conectar host: cluster1.lab.ic.unicamp.br");
			JOptionPane.showMessageDialog(null, "Desconectado com Sucesso!", "Desconectado", JOptionPane.INFORMATION_MESSAGE);
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(null, "Falha ao desconectar do Host.", "ERRO", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	private void controlarNodes(int i, String nome, String metadata)
	{
		try
		{
			{
				for(int j = 0; j < dtClientes.getRowCount(); j++)
				{
					if(dtClientes.getValueAt(j, 1).equals(nome))
					{
						if(i==0) 		
						{
							dtClientes.setValueAt(onlineIcon, j, 0);
							dtClientes.setValueAt(metadata, j, 2);
							return;
						}
						else if(i==1)
						{
							dtClientes.setValueAt(offlineIcon, j, 0);
							return;
						}
					}
				}
				//Se chegou até aqui significa que é um node novo
				if(i==0) { dtClientes.addRow(new Object[]{onlineIcon, nome, metadata}); }
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Retorna a instancia da Classe, para que ela seja unica
	 * @return instancia unica da classe
	 */
	public static ServerGUI getInstance() 
	{
		if(instance==null) 
		{ 
			instance = new ServerGUI();
			thread = new Thread(instance);
			thread.start();
		}
		return instance;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.setProperty("socksProxyHost", "localhost");
		System.setProperty("socksProxyPort", "1234");
		ServerGUI.getInstance();
	}

	@Override
	public void run()
	{
		while(true)
		{
			while(conected)
			{
				String mensagem = null;
				try
				{
					mensagem = ServerGUI.getInstance().getConexao().receberMensagem();
					if(mensagem == null) { break; }
					System.out.println("Mensagem Recebida: " + mensagem);
				}
				catch(IOException e)
				{
//					e.printStackTrace();
				}
				if(mensagem!=null && mensagem.length()>0)
				{
					String[] mensagemTratada = mensagem.split(",");
					if(mensagemTratada.length == 3)
					{
						ServerGUI.getInstance().controlarNodes(Integer.parseInt(mensagemTratada[0]), mensagemTratada[1], mensagemTratada[2]);
					}
				}
			}
		}
	}

	public Conexao getConexao() { return conexao; }
	public void setConexao(Conexao conexao) { this.conexao = conexao; }
}
