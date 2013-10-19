import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import edu.utulsa.unet.*;

//import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Vector;

public class RReceiverUDP implements RReceiveUDPI{
	
	private int PORT = 12987;
	private UDPSocket socket;
	private DatagramPacket packet;
	private InetSocketAddress sender;
	private int receiving_port;
	private int MTU;
	String receiving_filename;
	private int arraySize;
	public byte [] receivebuffer;
	
	private int mode = 0;
	private int windowSize = 0;
	private FileOutputStream fos = null;
	File file = null;
	String filename;
	private int numFrames = 0 ;
	private int counter;
	private byte[] message = ByteBuffer.allocate(Integer.SIZE).array();
	private ByteBuffer bb = ByteBuffer.wrap(message);
	private int incomingpacketsize;
	private ByteBuffer finalpacket; //file bytebuffer to be written to file
	private byte[] header_received;
	private int packetsize;
	//private String CLIENT = "localhost";
	private InetSocketAddress CLIENT;
	
	public String getFilename() {
		
		return filename;
	}
	
	public int getLocalPort() {
		
		return PORT;
	}
	
	public int getMode() {
		// TODO Auto-generated method stub
		return mode;
	}
	
	public long getModeParameter() {
		
		return windowSize;
	}
	
	/*public boolean receiveFile() {
		
				try {
					stopandwait();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return false;
	}*/
	
	public void setFilename(String arg0) {
		System.out.println("Setting file name...");
		this.filename = arg0;
		
	}
	
	public boolean setLocalPort(int arg0) {
		// Sets local port
				if (PORT < 0 || PORT > 65535){
					System.out.println("Error with port range");
					return false;
				}
				else {
					this.PORT = PORT;
					return true;
				}
	}
	
	public boolean setMode(int arg0) {
		//Determine the mode and run either stop and wait or sliding window
		 // 0 = stop and wait
		 // 1 = sliding window
			if (arg0 == 0)
			{ // run stop and wait
				this.mode = arg0;
				System.out.println("Stop and wait set..");
				
		}
			
			if (arg0 == 1)
			{
				//receiver.slidingWindow();
				
				return true;
			}
		return false;
	}
	
	public boolean setModeParameter(long arg0) {
		//sets the windowSize
		this.windowSize = (int)arg0;
		return false;
	}
	public boolean establishlink() throws IOException
	{
		socket = new UDPSocket(PORT);
		DatagramPacket packet = new DatagramPacket(receivebuffer,receivebuffer.length);
		System.out.println("Waiting for initial connection...");
		socket.receive(packet);
		CLIENT = new InetSocketAddress(packet.getAddress(), packet.getPort());
		System.out.println("Received Packet from: " + CLIENT.getAddress());
		socket.connect(CLIENT);
		if (socket.isConnected())
		{
			System.out.println("Connected");
			return true;
		}
		else {return false;}
		
	}
	public byte[] packetprocessor(byte [] receivepacket) throws IOException
	{
		//packet is disassembled here
		//remove the header and pack the packet into a bytearray
		//byte array will then be passed to the filewriter
		//copy the header off from packet to header_received
		header_received = new byte[4];
		System.arraycopy(receivepacket, 0, header_received, 0, 4);
		
		finalpacket = ByteBuffer.allocate(packetsize-4);
		
		
		finalpacket.put(receivepacket,4,finalpacket.remaining());
		finalpacket.flip();
		System.out.println(finalpacket.position());
		return header_received;
		
	}
	public void writefile(ByteBuffer finalpacket) throws IOException{
		
		//file = new File (filename);
		
		try {
			fos = new FileOutputStream(file,true);
			FileChannel outchannel = fos.getChannel();
			System.out.println("Writing file...");
			System.out.println("Final packet :" + finalpacket.capacity());
			
			outchannel.write(finalpacket);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println("Counter " + counter);
		if (counter == numFrames)
		{
			System.out.println("Exiting..");
			return;  
		}
	}
	public boolean receiveFile(){
	{
		file = new File (filename); // file object created
		if (file.exists())  //Delete file if it exists
		{
			file.delete();
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
		}
		counter = 0;
		try {
			socket = new UDPSocket(PORT);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Stop and wait test");
	//	socket = new UDPSocket(PORT);
		
			try {
				MTU = socket.getSendBufferSize();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		System.out.println("MTU: "+ MTU);
		receivebuffer = new byte[MTU];
		while(true){
			
			System.out.println(socket.getLocalPort());
			DatagramPacket receivepacket = new DatagramPacket(receivebuffer,receivebuffer.length);
			System.out.println("Waiting on packet....");
			try {
				socket.receive(receivepacket);
				sender = new InetSocketAddress(receivepacket.getAddress(), receivepacket.getPort());
				socket.connect(sender);
				packetsize = receivepacket.getLength();
				//Header returned from packetprocessor
				header_received = packetprocessor(receivepacket.getData());
				//Send header back to client
				writefile(finalpacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			incomingpacketsize = receivepacket.getLength();
			
			InetAddress client = receivepacket.getAddress();
			String sentence = new String(receivepacket.getData(),0,receivepacket.getLength());
			System.out.println(sentence);
		
		}
		
		}
	
	}
	public static void main(String[] args)
	{
		RReceiverUDP receiver = new RReceiverUDP();
		receiver.setFilename("new_small_text_file.txt");
		receiver.setLocalPort(12987);
		receiver.setModeParameter(80);
		receiver.setMode(0);
		receiver.receiveFile();
		/*try
		{
			byte [] buffer = new byte[11];
			UDPSocket socket = new UDPSocket(PORT);
			DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
			socket.receive(packet);
			InetAddress client = packet.getAddress();
			System.out.println(" Received'"+new String(buffer)+"' from " 
+packet.getAddress().getHostAddress());
		}
		catch(Exception e){ e.printStackTrace(); }
	}*/
}
	class acker implements Runnable
	//Acker will be the ack sender.
	{

		
		public void run() {
			System.out.println("Acker running");
			try {
				socket.send(new DatagramPacket(header_received, header_received.length,sender));
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	}


