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
			counter = 0;
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
				sendpacket = new byte[header.length + filebuffer.remaining()];
				System.arraycopy(header, 0, sendpacket, 0, 4);
				filebuffer.get(sendpacket,4,filebuffer.remaining());
				System.out.println(sendpacket.length);
				sender = new sender(sendpacket);
				Thread senderpacket1= new Thread(sender);
				senderpacket1.start();
				try {
					senderpacket1.sleep(100);
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
				  
				while(i != filesize)			
				{	
					acked = false;
					System.out.println("Total Size:  " + totalSize);
					if (MTU < totalSize)
					{
						sendpacket = new byte[header.length + (MTU-4)];
						System.out.println(sendpacket.length);
						System.arraycopy(header, 0, sendpacket, 0, 4);
						filebuffer.get(sendpacket, 4,MTU-4);
						totalSize = totalSize - MTU;
						System.out.println(" Message " + counter + " with " + MTU + " bytes of actual data");
						
						sender = new sender(sendpacket);
						Thread senderpacket2 = new Thread(sender);
						senderpacket2.start();
						try {
							senderpacket2.sleep(100);
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
						sendpacket = new byte [header.length + filebuffer.remaining()]; 
						System.out.println("filebuffer remaining "+ filebuffer.remaining());
						System.arraycopy(header, 0, sendpacket, 0, 4);
						filebuffer.get(sendpacket,4,filebuffer.remaining());
						//System.arraycopy(array, i, buffer1, 0, totalSize);
						System.out.println(" Message " + counter + " with " + totalSize + " bytes of actual data");
					//	finalPacket = ByteBuffer.allocate(header.length + buffer1.length);
						sender = new sender(sendpacket);
						Thread senderpacket3 = new Thread(sender);
						senderpacket3.start();
						try {
							senderpacket3.sleep(100);
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

	public void stopandwait() throws UnknownHostException, IOException {
		establishLink();
	//socket = new UDPSocket();
		header[0] = (byte)0; // sequence number
		header[1] = (byte)1; // length
		header[2] = (byte)2; //last packet acked
		header[3] = (byte)3; 
		
		counter = 0;
		long startTime = System.currentTimeMillis();
		totalSize = array.length;
		buffer = new byte [windowSize];
		System.out.println("WindowSize: " + windowSize);
		finalPacket = ByteBuffer.allocate(header.length + buffer.length);
		counter = 0;
		acked = false;
			if (array.length <= windowSize)
			{
				finalPacket = ByteBuffer.allocate(header.length + array.length);
				System.out.println("Little file");
				 //set the starting time.
				finalPacket.put(header);
				finalPacket.put(array);
				finalPacket.flip();
				sendpacket = new byte[finalPacket.remaining()];
				sender = new sender(buffer);
				Thread senderpacket = new Thread(sender);
				senderpacket.start();
				System.out.println(" Message " + counter + " with " + windowSize + " bytes of actual data");
				System.out.println(" Waiting for ACK ");
				counter++;
				
							
			}
		
		if (array.length > windowSize)
		{
			 System.out.println("Big file");
			 i = 0;
			 length = array.length;
			  
			while(i != length)			
			{	
				acked = false;
				System.out.println("Total Size:  " + totalSize);
				if (windowSize < totalSize)
				{
					System.arraycopy(array, i, buffer, 0, windowSize);
					totalSize = totalSize - windowSize;
					System.out.println(" Message " + counter + " with " + windowSize + " bytes of actual data");
				//	transmitter(buffer);
					
					//socket.send(new DatagramPacket(buffer, buffer.length, receiver));
					counter++;
					
					
					i = i + windowSize;
					
				}
				
				else if (windowSize > totalSize)
				{
				
					System.out.println("Test2");
					buffer1 = new byte [totalSize]; 
					System.arraycopy(array, i, buffer1, 0, totalSize);
					System.out.println(" Message " + counter + " with " + totalSize + " bytes of actual data");
				//	transmitter(buffer1);
				//	socket.send(new DatagramPacket(buffer1, buffer1.length, receiver));
				//	timer1.schedule(new timeoutcheck(), 0, 5000);
					sender = new sender(buffer1);
					Thread senderpacket = new Thread(sender);
					senderpacket.start();
					counter++;
					
									
					i = i + totalSize;
					
				}
			}
		}

		long stopTime=System.currentTimeMillis();
		System.out.printf("Seconds taken to transmit file: %f ",(float)(stopTime - startTime)/10000000);
		System.out.println("File Transferred, exiting...");
		System.exit(0);
	return;
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
			
		//	sender.sendSize();
			
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
}
