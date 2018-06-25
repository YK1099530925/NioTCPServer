import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.DecimalFormat;
import java.util.Iterator;

/**
 * 服务器发送给设备：24
 * 设备回复给服务器：44
 * 
 * 无线id ：不唯一
 * 设备标识：唯一
 * 设备类型
 * 数据：数据有一个字段的，有两个字段的
 * 
 * 红外入侵传感器对应表   ：InvadeData ：设备类型：0x10
 * 空气温湿度传感器对应表：AirData    ：设备类型：0xB3
 * 光照强度传感器对应表    ：BeamData   ：设备类型：0xC0
 * 二氧化碳传感器对应表    ：CO2Data    ：设备类型：0xD0
 * 土壤温湿度传感器对应表：SoilData   ：设备类型：0xA5
 * @author YangKuan
 *
 */


public class NioTCPServer {	
	//缓冲区长度
	private static final int BUFSIZE = 1024;
	//接受数据的缓冲区
	private static ByteBuffer byteBuffer;
	
	public static void tcpServer()  throws Exception{
		System.out.println("服务器启动");
		//创建一个选择器
		Selector selector = Selector.open();
		//实例化一个通道
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		//将通道绑定到指定端口(6789)
		serverSocketChannel.socket().bind(new InetSocketAddress(6789));
		//配置通道为非阻塞模式
		serverSocketChannel.configureBlocking(false);
		//将选择器注册到通道上
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		//初始化缓冲区的大小
		byteBuffer = ByteBuffer.allocateDirect(BUFSIZE);
		//不断轮询select方法，获取准备好的通道关联的key集
		while(true) {
			//一直等待，直到有通道准备好了数据的传输，在此处异步执行其他任务（3000为select方法等待信道准备好的最长时间）
			if (selector.select(3000) == 0) {
				//异步执行其他任务
				continue;
			}
			//获取准备好的通道中关联的Key集合的Iterator
			Iterator<SelectionKey> selectionKeyIter = selector.selectedKeys().iterator();
			//循环获取集合中的键值
			while(selectionKeyIter.hasNext()) {
				SelectionKey key = selectionKeyIter.next();
				//服务端对哪种信号感兴趣就执行那种操作
				if(key.isAcceptable()) {
					System.out.println("accept");
					//连接好了，然后将读注册到选择器中
					readRegister(selector,key);
				}
				//上一部将读注册到选择器中之后，如果客户端发送数据，就可以读取到数据，还可以将发送到客户端
				if(key.isReadable()) {
					//读取客户端的数据
					readDataFromSocket(key);
				}
				if (key.isValid() && key.isWritable()) {
					System.out.println("write");
				}
				//需要手动从键集中移除当前key
				selectionKeyIter.remove();
			}
		}
	}
	
	//将读注册到选择器中
	private static void readRegister(Selector selector, SelectionKey key) throws IOException {
		//从key中获取关联的通道（此处是ServerSocketChannel，因为需要将服务器的检测模式注册到选择器中）
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
		//获取通道实例
		SocketChannel channel = serverSocketChannel.accept();
		//设置为非阻塞模式
		channel.configureBlocking(false);
		//将读注册到选择器中
		channel.register(selector, SelectionKey.OP_READ);
	}
	
	private static void readDataFromSocket(SelectionKey key) throws Exception {
		//从与key关联的通道中获取数据，首先获取关联的通道（此处是SocketChannel，因为与客户端通信是通过SocketChannel，数据都放在其中）
        SocketChannel socketChannel = (SocketChannel) key.channel();
        //存放客户端发送过来的数据
        String dataString = "";
        int count;
        //清除缓冲区（此处清除不能实际擦出buffer中的数据，而是回归各个标志位）
        byteBuffer.clear();
        //从通道中读取数据到缓冲区中，读到最后没有数据则返回-1
        while ((count = socketChannel.read(byteBuffer)) > 0) {
        	//将模式转换为读模式
        	byteBuffer.flip();
        	//hasRemaining告知当前位置和限制之间是否存在任何元素
            while (byteBuffer.hasRemaining()) {
            	//1、解析客户端发送过来的16进制数据(返回的是String])
            	//①将byteBuffer转换为byte[]数组
            	byte[] dataByte = byteBufferToByteArray(byteBuffer);
            	//②将byte[]转换成String
            	dataString = toHexString(dataByte);
            	System.out.println("数据：" + dataString);
            	//2、将数据保存到数据库
            	insertMysql(dataString);
            	//3、将数据返回给客户端
            	//①将string转换成byte
            	//dataByte = toByteArray(dataString);
            	//②发送至客户端
            	sentDataClient(socketChannel, dataString);
            	}
            byteBuffer.clear();
        }
        
        if (count < 0) {
            socketChannel.close();
        }
    }
	
	/**
	 * 存储数据
	 * 
	 * @param dataString
	 */
	public static void insertMysql(String dataString) {
		//截取第三个16进制
		String str = dataString.substring((3-1)*2, (4-1)*2);
		//判断是否是44，如果是，就进行存储，如果不是，就直接返回
		if(!str.equals("44")) {
			return;
		}
		//将设备返回的数据直接存储（存储在devdata表中）
		//ConnectionMysql.insertDEVData(dataString);
		//拆分数据
		String[] dataStr = splitData(dataString);
		//解析数据
		if(dataStr.length ==4 ) {//解析其他的数据
			dataStr = resolveOtherData(dataStr);
		}else if(dataStr.length == 5){//解析空气和土壤的数据
			dataStr = resolveAirAndSoilData(dataStr);
		}else {
			System.err.println("错误");
		}
		//改进：这儿直接调用保存方法（在保存方法中依据不同的设备类型放入不同的表中）
		insert(dataStr);
		
		//打印需要保存的数据
		printData(dataStr);
		
		//将解析后的数据存储（存储在data表中）
		//ConnectionMysql.insertData(dataStr);
		//查询数据
    	//ConnectionMysql.selectData();
	}
	
	public static void printData(String[] dataStr) {
		for(String str : dataStr){
			System.err.println(str);
		}
	}
	
	/**
	 * 将不同数据保存至不同表
	 * 红外入侵传感器对应表   ：invade_data ：设备类型：0x10
	 * 空气温湿度传感器对应表：air_data    ：设备类型：0xB3
	 * 光照强度传感器对应表    ：beam_data   ：设备类型：0xC0
	 * 二氧化碳传感器对应表    ：co2_data    ：设备类型：0xD0
	 * 土壤温湿度传感器对应表：soil_data   ：设备类型：0xA5
	 * 所有设备都会变成小写
	 * @param dataStr
	 */
	public static void insert(String[] dataStr) {
		switch (dataStr[2]) {
		case "10":
			//invade_data
			break;
		case "b3":
			//air_data
			break;
		case "c0":
			//beam_data
			break;
		case "d0":
			//co2_data
			break;
		case "a5":
			//soil_data
			break;
		default:
			break;
		}
	}
	
	
	/**
	 * 空气或土壤数据的解析
	 * 解析最后一个数据
	 * 温湿度（包含三个byte）
	 * 示例：31 02 1C
	 * 31 02: 
	 * 温度=  (0x0231/10)-40 = (561/10)-40 = 16.1C
	 * 1C: 湿度 = 0x1C% = 28%
	 * 
	 * @param dataStr
	 * @return
	 */
	public static String[] resolveAirAndSoilData(String[] dataStr) {
		//温度换算
		String temperature = dataStr[dataStr.length-2].substring(2, 4) + dataStr[dataStr.length-2].substring(0, 2);
		//保留一位小数
		DecimalFormat dFormat = new DecimalFormat("0.0");
		
		String temFormatStr = dFormat.format((double)Integer.parseInt(temperature,16)/10);
		double t = Double.parseDouble(temFormatStr) - 40;
		temFormatStr = String.valueOf(t)+"°C";
		
		//湿度转换
		String humidity = dataStr[dataStr.length-1].substring(0, 2);
		//将16进制字符串转换为10进制
		int hum = Integer.parseInt(humidity, 16);
		humidity = hum + "%";
		dataStr[dataStr.length-2] = temFormatStr;
		dataStr[dataStr.length-1] = humidity;
		return dataStr;
	}
	
	/**
	 * 解析其他数据(红外入侵，光照强度，二氧化碳，数据转换成16进制)
	 * 红外入侵传感器对应表   ：invade_data ：设备类型：0x10
	 * 光照强度传感器对应表    ：beam_data   ：设备类型：0xC0
	 * 二氧化碳传感器对应表    ：co2_data    ：设备类型：0xD0
	 * @param dataStr
	 * @return
	 */
	public static String[] resolveOtherData(String[] dataStr) {
		switch (dataStr[2]) {
		case "10"://红外入侵
			dataStr[3] = Integer.parseInt(dataStr[3], 16) + "";
			break;
		case "b3"://光照强度
			String beamStr = dataStr[3].substring(2, 4) + dataStr[3].substring(0, 2);
			dataStr[3] = Integer.parseInt(beamStr, 16) + "Lux";
			break;
		case "d0"://二氧化碳
			String co2 = dataStr[3].substring(2, 4) + dataStr[3].substring(0, 2);
			dataStr[3] = Integer.parseInt(co2, 16) + "ppm";
			break;
		default:
			break;
		}
		return dataStr;
	}
	
	/**
	 * 拆分数据(前三个数据是不变的，因此可以将前个数据提取出来)
	 * 第0个数据：无线id
	 * 第1个数据：设备标识
	 * 第2个数据：设备类型
	 * 
	 * @return
	 */
	public static String[] splitData(String dataString) {
		//判断（因为有温湿度的需要5个空间，co2和光照只需要4个空间）
		String[] dataStr = null;
		if(dataString.length() <= 50) {
			dataStr = new String[4];
		}else {
			dataStr = new String[5];
		}
		splitData1(dataStr,dataString);
		splitData2(dataStr,dataString);
		
		
		return dataStr;
	}
	//拆分前三个数据
	public static String[] splitData1(String[] dataStr, String dataString) {
		dataStr[0] = dataString.substring((5-1)*2, (5-1+2)*2);
		dataStr[1] = dataString.substring((7-1)*2, (7-1+12)*2);
		dataStr[2] = dataString.substring((21-1)*2, (21-1+1)*2);
		return dataStr;
	}
	//拆分后面的数据
	public static String[] splitData2(String[] dataStr, String dataString) {
		int len = dataString.length() / 2;
		if(dataStr.length == 4) {
			//判断，因为字段为4个的数据域的位也有不同
			if(len == 24) {//长度是24个16进制数（截取倒数第2个）
				dataStr[3] = dataString.substring((len - 2) * 2, (len - 1) * 2);
			}else if(len == 25){//长度为25个16进制数(所以截取倒数第2和第3位)
				dataStr[3] = dataString.substring((len - 3) * 2, (len - 1) * 2);
			}
		}else if(dataStr.length == 5){//截取倒数第3 4个，截取倒数第2个
			dataStr[3] = dataString.substring((len - 4) * 2, (len - 2) * 2);
			dataStr[4] = dataString.substring((len - 2) * 2, (len - 1) * 2);
		}else {
			System.out.println("异常");
		}
		
		return dataStr;
	}
	
	/**
	 * 将ByteBuffer转换成byte[]数组
	 * 
	 * @param buffer
	 * @return
	 */
	public static byte[] byteBufferToByteArray(ByteBuffer buffer) {
		byte[] dataByte = new byte[buffer.limit()];
		buffer.get(dataByte, 0, buffer.limit());
		return dataByte;
	}
	
	/**
	  * 字节数组转成16进制表示格式的字符串
	  * 
	  * @param byteArray
	  *            需要转换的字节数组
	  * @return 16进制表示格式的字符串
	  **/
	 public static String toHexString(byte[] byteArray) {
	  if (byteArray == null || byteArray.length < 1)
	   throw new IllegalArgumentException("this byteArray must not be null or empty");
	 
	  final StringBuilder hexString = new StringBuilder();
	  for (int i = 0; i < byteArray.length; i++) {
	   if ((byteArray[i] & 0xff) < 0x10)//0~F前面不零
	    hexString.append("0");
	   hexString.append(Integer.toHexString(0xFF & byteArray[i]));
	  }
	  return hexString.toString().toLowerCase();
	 }
	
	/**
	  * 16进制的字符串表示转成字节数组
	  * 
	  * @param hexString
	  *            16进制格式的字符串
	  * @return 转换后的字节数组
	  **/
	 public static byte[] toByteArray(String hexString) {
//	  if (hexString.isEmpty())
//	   throw new IllegalArgumentException("this hexString must not be empty");
	 
	  hexString = hexString.toLowerCase();
	  final byte[] byteArray = new byte[hexString.length() / 2];
	  int k = 0;
	  for (int i = 0; i < byteArray.length; i++) {//因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
	   byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
	   byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
	   byteArray[i] = (byte) (high << 4 | low);
	   k += 2;
	  }
	  return byteArray;
	 }
	
	//向客户端发送数据
	public static void sentDataClient(SocketChannel socketChannel,String str) throws IOException {
        ByteBuffer sentBuffer = ByteBuffer.allocateDirect(str.length());
        byte[] b = toByteArray(str);
        //System.err.println(str);
        sentBuffer.put(ByteBuffer.wrap(b));
        sentBuffer.flip();
        //在向通道写数据的时候，需要将buffer给flip()
        socketChannel.write(sentBuffer);
	}
	
	public static void main(String[] args) throws Exception{
		tcpServer();
	}
}
