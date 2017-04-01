package pw.mcbus.www.CQP_Socket;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import sun.misc.*;

public class MainClass extends JavaPlugin  {
	Map<String,UUID> map;
	Map<String,CommandSender> pmap;
	DatagramSocket ds;
	Map<String,String> config;
	String GroupID;
	@SuppressWarnings("unchecked")
	@Override
	//�������
	public void onEnable(){
		getCommand("CQP").setExecutor(new Commander());
		try
		{
			//���������ļ�
			File mapfile=new File(".\\plugins\\CQP\\Map.dat");
			if(mapfile.exists())
			{
				Object temp=null;
				FileInputStream in;
				try{
					in=new FileInputStream(mapfile);
					ObjectInputStream objIn =new ObjectInputStream(in);
					temp=objIn.readObject();
					objIn.close();
					map=(Map<String,UUID>)temp;
					System.out.println("[CQP]read map success!");
				}catch(IOException e){
					System.out.println("[ERROR][CQP]read map failed");
					e.printStackTrace();
				}catch(ClassNotFoundException e){
					e.printStackTrace();
				}
			}
			else
			{
				map=new HashMap<String,UUID>();
			}
			pmap=new HashMap<String,CommandSender>();
			
			File confile=new File(".\\plugins\\CQP\\config.dat");
			if(confile.exists())
			{
				Object temp=null;
				FileInputStream in;
				try{
					in=new FileInputStream(mapfile);
					ObjectInputStream objIn =new ObjectInputStream(in);
					temp=objIn.readObject();
					objIn.close();
					config=(Map<String,String>)temp;
				}catch(IOException e){
					System.out.println("[ERROR][CQP]read config failed");
					e.printStackTrace();
				}catch(ClassNotFoundException e){
					e.printStackTrace();
				}
			}
			else
			{
				config=new HashMap<String,String>();
				config.put("GroupID", "304279325");
			}
			GroupID=config.get("GroupID");
			
			client=new DatagramSocket();//�������Q��ͨѶ

			RCV rcver=new RCV();
			rcver.start();//��ʼ����30236�˿�
			new Timer().schedule(new MyTimerTask(), 10000);//��ʱ�������������Ϣ
		}catch (IOException e){}
	}
	@Override
	//����ر�
	public void onDisable(){
		{//����map����Map.dat�ļ�
		File mapfile=new File(".\\plugins\\CQP\\Map.dat");
		FileOutputStream out;
		try{
			out=new FileOutputStream(mapfile);
			ObjectOutputStream objOut=new ObjectOutputStream(out);
			objOut.writeObject(map);
			objOut.flush();
			objOut.close();
			System.out.println("[CQP]write map success!");
		}catch (IOException e)
		{
			System.out.println("[ERROR][CQP]write map failed");
		}
		ds.close();}{
			File mapfile=new File(".\\plugins\\CQP\\config.dat");
			FileOutputStream out;
			try{
				out=new FileOutputStream(mapfile);
				ObjectOutputStream objOut=new ObjectOutputStream(out);
				objOut.writeObject(config);
				objOut.flush();
				objOut.close();
				System.out.println("[CQP]write config success!");
			}catch (IOException e)
			{
				System.out.println("[ERROR][CQP]write config failed");
			}
			ds.close();
		}
	}
	
		class MyTimerTask extends TimerTask{
		public void run()
		{
			try{
			Send("ClientHello 30236");
		}catch (IOException e){}
			new Timer().schedule(new MyTimerTask(), 240000);
		}
	}
	
	DatagramSocket client;
	private void Send(String msg) throws IOException
	{
		byte[] sendBuf;
		sendBuf=msg.getBytes();
		InetAddress addr=InetAddress.getByName("localhost");
		int port=11235;
		DatagramPacket sendPacket
			=new DatagramPacket(sendBuf,sendBuf.length,addr,port);
		client.send(sendPacket);
	}
	class Commander implements CommandExecutor{
		public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
			if(arg3.length==0)
			{
				arg0.sendMessage("�ò���ɻ����빤���ҳ�Ʒ\n/CQP <bind/list/delete/OP>");
				return false;
			}
			switch(arg3[0]){
			case "bind"://��QQ
			{
				//�����֤��
			int ic=(int)(100000+Math.random()*(999999-100000+1));
			while(pmap.containsKey(ic+"")){
				ic=(int)(100000+Math.random()*(999999-100000+1));
			}
			pmap.put(ic+"", arg0);
			arg0.sendMessage("[CQP]Now send \""+ic+"\" to QQgroup.");
			break;
			}
			case "list"://�г��󶨵�QQ
				arg0.sendMessage("The list of the QQ which are bound you:");
				arg0.sendMessage("--START--");
				for(String key:map.keySet()){
					if(map.get(key)==((Player)arg0).getUniqueId());
						arg0.sendMessage(key+";\n");
				}
				arg0.sendMessage("---END---");
				break;
			//case "M_bind"://�ֶ��󶨣��ֶ�����QQ�ţ�
			//{
			//	map.put(arg3[1], ((Player)arg0).getUniqueId());
			//	arg0.sendMessage("success!");
			//	break;
			//}
			case "delete"://ɾ��
				if(arg3.length<2)
				{
					arg0.sendMessage("�ò���ɻ����빤���ҳ�Ʒ\n/CQP delete <QQ>");
					return false;
				}
				//����Ƿ����
				if(!(map.containsKey(arg3[1]))&&(map.get(arg3[1]).equals(((Player)arg0).getUniqueId())))
				{
					arg0.sendMessage("Can not find this QQ on you");
					break;
				}
				map.remove(arg3[1]);
				arg0.sendMessage("success!");
				break;
			case "OP":
			{
				if(arg3.length<2)
				{
					arg0.sendMessage("�ò���ɻ����빤���ҳ�Ʒ\n/CQP OP <setGroupID/OP_M_delete>");
					return false;
				}
				if(!arg0.isOp())
				{
					arg0.sendMessage("You aren't OP!");
					break;
				}
				switch(arg3[1])
				{
				case "setGroupID"://����Ⱥ��
					if(arg3.length<3)
					{
						arg0.sendMessage("�ò���ɻ����빤���ҳ�Ʒ\n/CQP OP setGroupID <GroupID>");
						return false;
					}
					GroupID=arg3[2];
					config.remove("GroupID");
					config.put("GroupID", arg3[2]);
					arg0.sendMessage("Set GroupID:"+GroupID+" success!");
					break;
				case "OP_M_delete"://OPǿ�н��
					if(arg3.length<3)
					{
						arg0.sendMessage("�ò���ɻ����빤���ҳ�Ʒ\n/CQP OP OP_M_delete <QQ>");
						return false;
					}
					map.remove(arg3[2]);
					arg0.sendMessage("sucess!");
					break;
				}
			}
			}

			return true;
		}
	}
	class RCV extends Thread{
		public void run(){
			try{
			ds = new DatagramSocket(30236);//������񣬼��Ӷ˿�����ķ��Ͷ˿ڣ�ע�ⲻ��send����˿�
			while (true)
			{
				byte[] buf = new byte[1024];//�������ݵĴ�С��ע�ⲻҪ���
				DatagramPacket dp = new DatagramPacket(buf,0,buf.length);//����һ�����յİ�
				ds.receive(dp);//���������ݷ�װ������
				String data = new String(dp.getData(), 0, dp.getLength());//����getData()����ȡ������
				String[] datas=data.split(" ");
				System.out.printf(data);
				if(datas[0].equalsIgnoreCase("GroupMessage")&&datas[1].equalsIgnoreCase(GroupID))
				{
					if(pmap.containsKey(decodeBase64(datas[3])))
					{
						map.put(datas[2], ((Player)(pmap.get(decodeBase64(datas[3])))).getUniqueId());
						pmap.get(decodeBase64(datas[3])).sendMessage("[CQP]success bind \""+pmap.get(decodeBase64(datas[3])).getName()+"\" to "+datas[2]);
						continue;
					}
					if(map.containsKey(datas[2])){
						Bukkit.broadcastMessage("<"+Bukkit.getPlayer(map.get(datas[2])).getName()+"> "+decodeBase64(datas[3]));
					}else{
						//Bukkit.broadcastMessage(datas[2]+":"+decodeBase64(datas[3]));
					}
				}
			}
			}catch (IOException e){}
		}
		public String decodeBase64(String s){
			byte[]b=null;
			String result=null;
			if(s !=null){
				BASE64Decoder decoder=new BASE64Decoder();
				try{
					b=decoder.decodeBuffer(s);
					result=new String(b,"GBK");
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			return result;
		}  
	}
}
