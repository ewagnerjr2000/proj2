//New
import edu.utulsa.unet.RSendUDPI;

import java.util.*;

import edu.utulsa.unet.UDPSocket;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import edu.utulsa.unet.*;

//import java.net.InetAddress;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FilePermission;
import java.io.RandomAccessFile;
import java.util.PropertyPermission;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.net.DatagramSocket;
public class RSendUDP implements RSendUDPI{
	static String SERVER = "localhost";
	private int PORT = 12987;
	private String filename;
	private UDPSocket socket;
	private sender sender;
	private ackreceiver ackreceiver;
	private int MTU;
	
	private InetSocketAddress receiver = new InetSocketAddress("127.0.0.1", 12987);
//	private InetSocketAddress sender;
	private long packettimeout;
	private long length = 0;
	private long modeParameters;
	private long timeout;
	private int counter;
	private int totalSize;
	private int windowSize = 256;
	
	private int mode = 0;
	private int i;
	private byte [] array;
	private byte[] header = new byte[4];
	private  byte [] buffer;
	private  byte [] buffer1;
	private byte[] sendpacket; // final packet 
	private ByteBuffer finalPacket; 
	private boolean acked;
	private ByteBuffer filebuffer;
	private long filesize;
//	private byte[] ackpacket;
	
	public String getFilename() {
		// TODO Auto-generated method stub
		return filename;
	}

	
	public int getLocalPort() {
		// TODO Auto-generated method stub
		return PORT;
	}

	
	public int getMode() {
		// TODO Auto-generated method stub
		return mode;
	}

	
	public long getModeParameter() {
		// TODO Auto-generated method stub
		return modeParameters;
	}

	
	public InetSocketAddress getReceiver() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public long getTimeout() {
		// TODO Auto-generated method stub
		return timeout;
	}


/*	public boolean sendFile() {
		try {
			stopandwait();
			//test();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		
		}
		return false;
	}*/

	
	public boolean sendFile() {
		try {
			ackreceiver = new ackreceiver();
			establishLink();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		header[0] = (byte)0;
		header[1] = (byte)1;
		header[2] = (byte)2;
		header[3] = (byte)3;
		//finalPacket = ByteBuffer.allocate(header.length + buffer.length);
 		if (mode == 0)
		{
			System.out.println("Stop and wait is set..");
			counter = 1;
			long startTime = System.currentTimeMillis();
			totalSize = filebuffer.capacity();
			System.out.println("Filebuffer capacity: "+ filebuffer.capacity());
			buffer = new byte [windowSize];
			//finalPacket = ByteBuffer.allocate(array.length);
			System.out.println("MTU : "+ MTU);
			
			if (totalSize <= MTU)
			{
				System.out.println("Little file");
				
			//	System.out.println("Final packet size: " + finalPacket.capacity());
				header[0] = (byte)(counter);
				sendpacket = new byte[header.length + filebuffer.remaining()];
				System.arraycopy(header, 0, sendpacket, 0, 4);
				filebuffer.get(sendpacket,4,filebuffer.remaining());
				System.out.println(sendpacket.length);
				sender = new sender(sendpacket);
				
				Thread senderpacket1= new Thread(sender);
				Thread ackreceiver1 = new Thread(ackreceiver);
				//ackreceiver ackreceiver1 = new ackreceiver();
				senderpacket1.start();
				try {
					senderpacket1.sleep(100);
					senderpacket1.join();
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ackreceiver1.start();
				try {
					//ackreceiver1.sleep(100);
					ackreceiver1.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			//if (array.length > windowSize)
			if(filesize > MTU)
			{
				 System.out.println("Big file");
				 i = 0;
				// length = array.length;
				 counter = 1; 
				while(i != filesize)			
				{	
					acked = false;
					System.out.println("Total Size:  " + totalSize);
					if (MTU < totalSize)
					{
						sendpacket = new byte[header.length + (MTU-4)];
						//header updated to show the count
						header[0] = (byte)counter;
						System.out.println(sendpacket.length);
						System.arraycopy(header, 0, sendpacket, 0, 4);
						filebuffer.get(sendpacket, 4,MTU-4);
						totalSize = totalSize - MTU;
						System.out.println(" Message " + counter + " with " + MTU + " bytes of actual data");
						
						sender = new sender(sendpacket);
						Thread senderpacket2 = new Thread(sender);
						Thread ackreceiver2 = new Thread(ackreceiver);
						senderpacket2.start();
						try {
							senderpacket2.sleep(100);
							senderpacket2.join();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						ackreceiver2.start();
						try {
							ackreceiver2.join();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						counter++;
						i = i + MTU;
						
					}
					
					else if (MTU > totalSize)
					{
					//	System.out.println("Position: "+ filebuffer.position());
						System.out.println("Test2");
						//header updated to show the count
						header[0] = (byte)counter;
						sendpacket = new byte [header.length + filebuffer.remaining()]; 
						System.out.println("filebuffer remaining "+ filebuffer.remaining());
						System.arraycopy(header, 0, sendpacket, 0, 4);
						filebuffer.get(sendpacket,4,filebuffer.remaining());
						//System.arraycopy(array, i, buffer1, 0, totalSize);
						System.out.println(" Message " + counter + " with " + totalSize + " bytes of actual data");
					//	finalPacket = ByteBuffer.allocate(header.length + buffer1.length);
						sender = new sender(sendpacket);
						Thread senderpacket3 = new Thread(sender);
						Thread ackreceiver3 = new Thread(ackreceiver);
						senderpacket3.start();
						try {
							senderpacket3.sleep(100);
							senderpacket3.join();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						ackreceiver3.start();
						try {
							ackreceiver3.join();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						counter++;
						
										
						i = i + totalSize;
						
					}
				}
						
		}
		else
		{
			System.out.println("No Sliding window enabled.");
			return false;
		}}
		return false;
	}
	public void setFilename(String arg0){
		//Reads the file into a Bytebuffer.
		RandomAccessFile infile = null;
		try {
			infile = new RandomAccessFile(
			        arg0,"r");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
	FileChannel inChannel = infile.getChannel();
	try {
		filesize = inChannel.size();
		filebuffer = ByteBuffer.allocate((int) filesize);
		
		
		//System.out.println(" SHA-1 hash: " + sha1);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	try {
		
		inChannel.read(filebuffer);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	System.out.println("File size :" + (int) filesize);
	//Return the the beginning of the bytebuffer
	filebuffer.rewind();
	}	
	
	/*public void setFilename(String arg0) {
		//used to read the file in to a file stream
		File infile = new File (arg0);
		FileInputStream filestream = null;
		try {
			filestream = new FileInputStream(infile);
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
	FileChannel filechannel = filestream.getChannel();
	
		int offset = 0;
	    int numRead = 0;
	    filename = arg0;
		System.out.println("Reading in file");
		
		length = infile.length();
		array = new byte[(int) length];
		DataInputStream dis = null;
		try {
			dis = new DataInputStream(new FileInputStream(infile));
		
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
	    // Read in the bytes
	   
	    try {
			while (offset < array.length && (numRead=dis.read(array, offset, array.length-offset)) >= 0) 
				{
				    offset += numRead;
				}
			//System.out.println(filesize.length);
			System.out.println("Array Length: "+array.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	
	
	
	   
		
	//}

	
	public boolean setLocalPort(int arg0) {
		// setting the localport
				if (PORT < 0 || PORT > 65535)
				{
					System.out.println("Ports need to be between 0 - 65535.");
					return false;
				}
				else
				{
					this.PORT = PORT;
					return true;
				}
		
	}

	
	public boolean setMode(int arg0) {
		// TODO Auto-generated method stub
		//Determine the mode and run either stop and wait or sliding window
		 // 0 = stop and wait
		 // 1 = sliding window
			//sets the mode for the object.
		if (arg0 == 0)
		{
			System.out.println("Using stop and wait ...");
			this.mode = arg0;
			return true;
		}
		if (arg0 == 1 )
		{
			System.out.println("Using Sliding window...not implemented yet");
			this.mode = arg0;
			return true;
		}
		if (mode != 0 && mode != 1){
			System.out.println("Error, mode must be set to 0 for stop & wait, or 1 for sliding window");
		}
		return false;
	}

	
	public boolean setModeParameter(long arg0) {
		// Sets the window size.
				if (arg0 <= 0)
				{
					System.out.println("Error with window size, must be greater than 1");
					return false;
					
				}
				else {
					
					modeParameters = arg0;
				
					return true;
				}
	}

	
	public boolean setReceiver(InetSocketAddress arg0) {
		//set host and port number
				this.receiver = arg0;
				
				System.out.println(" Address: "+ receiver.getAddress() + ": "+ receiver.getPort());
				System.out.println("LocalPort: " + PORT);
				
				return true;
	}

	
	public boolean setTimeout(long arg0) {
		// Timeout for packet
		//Create Timer
		this.packettimeout = arg0;
		return false;
	}
	public boolean establishLink() throws UnknownHostException, IOException
	{
		socket = new UDPSocket();
		socket.connect(receiver);
		MTU = socket.getSendBufferSize();
		System.out.println("MTU Size: " + socket.getSendBufferSize());
		if (socket.isConnected()){
			System.out.println("Are We connected: " + socket.isConnected());
			return true;
		}
		else {return false;}
		
	}

	
	public static void main(String[] args)
	{
		try {

			/*byte [] buffer = ("Hello World").getBytes();
			UDPSocket socket = new UDPSocket();
		//	socket.send(new DatagramPacket(buffer, buffer.length,
 		//		InetAddress.getByName(SERVER), PORT));
		*/
			// TODO Auto-generated method stub
			RSendUDP sender = new RSendUDP();
			sender.setMode(0);
			sender.setLocalPort(12988);
			sender.setModeParameter(1500);
			sender.setTimeout(3000);
			sender.setReceiver(new InetSocketAddress ("localhost", 12987));
			sender.setFilename("testfile.txt");
			
		
			
			sender.sendFile();
			
		}
		catch(Exception e){ e.printStackTrace(); }
	}


class sender implements Runnable {
	private byte[] packet;
	public sender( byte[] packet){
		this.packet = packet;
	}
	public void run(){
		try{
			//if( socket.isBound()){
				socket.send(new DatagramPacket(packet,packet.length,receiver));
						
		//	}
		}catch(IOException e){
			System.out.println("IOException, in sender ");
		}
	
	}
	
	}
class ackreceiver implements Runnable {
	private byte[] ackpacket = new byte[4];
	
	public ackreceiver() {
		
	}
	public byte[] getackpacket(){
		return ackpacket;
		
	}
	public void run() {
		
			System.out.println("Test1");
		//	System.out.println(ackpacket.length);
			
				
					System.out.println(socket.getLocalPort());
					try {
						//socket = new UDPSocket();
						socket.receive(new DatagramPacket(ackpacket,ackpacket.length,receiver));
						String s = new String(ackpacket);
						
						System.out.println("Ackpacket: "+s.charAt(0));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				
				System.out.println("Test from Ackreceiver");
				
				
			}
			
	}
}	


