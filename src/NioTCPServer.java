import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.DecimalFormat;
import java.util.Iterator;

public class NioTCPServer {	
	//����������
	private static final int BUFSIZE = 1024;
	//�������ݵĻ�����
	private static ByteBuffer byteBuffer;
	
	public static void tcpServer()  throws Exception{
		System.out.println("����������");
		//����һ��ѡ����
		Selector selector = Selector.open();
		//ʵ����һ��ͨ��
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		//��ͨ���󶨵�ָ���˿�(6789)
		serverSocketChannel.socket().bind(new InetSocketAddress(6789));
		//����ͨ��Ϊ������ģʽ
		serverSocketChannel.configureBlocking(false);
		//��ѡ����ע�ᵽͨ����
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		//��ʼ���������Ĵ�С
		byteBuffer = ByteBuffer.allocateDirect(BUFSIZE);
		//������ѯselect��������ȡ׼���õ�ͨ��������key��
		while(true) {
			//һֱ�ȴ���ֱ����ͨ��׼���������ݵĴ��䣬�ڴ˴��첽ִ����������3000Ϊselect�����ȴ��ŵ�׼���õ��ʱ�䣩
			if (selector.select(3000) == 0) {
				//�첽ִ����������
				System.out.println("======");
				continue;
			}
			//��ȡ׼���õ�ͨ���й�����Key���ϵ�Iterator
			Iterator<SelectionKey> selectionKeyIter = selector.selectedKeys().iterator();
			//ѭ����ȡ�����еļ�ֵ
			while(selectionKeyIter.hasNext()) {
				SelectionKey key = selectionKeyIter.next();
				//����˶������źŸ���Ȥ��ִ�����ֲ���
				if(key.isAcceptable()) {
					System.out.println("accept");
					
					//���Ӻ��ˣ�Ȼ�󽫶�ע�ᵽѡ������
					readRegister(selector,key);
				}
				//��һ������ע�ᵽѡ������֮������ͻ��˷������ݣ��Ϳ��Զ�ȡ�����ݣ������Խ����͵��ͻ���
				if(key.isReadable()) {
					//��ȡ�ͻ��˵�����
					readDataFromSocket(key);
				}
				if (key.isValid() && key.isWritable()) {
					System.out.println("write");
				}
				//��Ҫ�ֶ��Ӽ������Ƴ���ǰkey
				selectionKeyIter.remove();
			}
		}
	}
	
	//����ע�ᵽѡ������
	private static void readRegister(Selector selector, SelectionKey key) throws IOException {
		//��key�л�ȡ������ͨ�����˴���ServerSocketChannel����Ϊ��Ҫ���������ļ��ģʽע�ᵽѡ�����У�
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
		//��ȡͨ��ʵ��
		SocketChannel channel = serverSocketChannel.accept();
		//����Ϊ������ģʽ
		channel.configureBlocking(false);
		//����ע�ᵽѡ������
		channel.register(selector, SelectionKey.OP_READ);
	}
	
	private static void readDataFromSocket(SelectionKey key) throws Exception {
		//����key������ͨ���л�ȡ���ݣ����Ȼ�ȡ������ͨ�����˴���SocketChannel����Ϊ��ͻ���ͨ����ͨ��SocketChannel�����ݶ��������У�
        SocketChannel socketChannel = (SocketChannel) key.channel();
        //��ſͻ��˷��͹���������
        String dataString = "";
        int count;
        //������������˴��������ʵ�ʲ���buffer�е����ݣ����ǻع������־λ��
        byteBuffer.clear();
        //��ͨ���ж�ȡ���ݵ��������У��������û�������򷵻�-1
        while ((count = socketChannel.read(byteBuffer)) > 0) {
        	//��ģʽת��Ϊ��ģʽ
        	byteBuffer.flip();
        	//hasRemaining��֪��ǰλ�ú�����֮���Ƿ�����κ�Ԫ��
            while (byteBuffer.hasRemaining()) {
            	//1�������ͻ��˷��͹�����16��������(���ص���String])
            	//�ٽ�byteBufferת��Ϊbyte[]����
            	byte[] dataByte = byteBufferToByteArray(byteBuffer);
            	//�ڽ�byte[]ת����String
            	dataString = toHexString(dataByte);
            	System.out.println("���ݣ�" + dataString);
            	//2�������ݱ��浽���ݿ�
            	insertMysql(dataString);
            	//3�������ݷ��ظ��ͻ���
            	//�ٽ�stringת����byte
            	//dataByte = toByteArray(dataString);
            	//�ڷ������ͻ���
            	sentDataClient(socketChannel, dataString);
            	
            	}
            byteBuffer.clear();
        }
        
        if (count < 0) {
            socketChannel.close();
        }
    }
	
	/**
	 * �洢����
	 * 
	 * @param dataString
	 */
	public static void insertMysql(String dataString) {
		//��ȡ������16����
		String str = dataString.substring((3-1)*2, (4-1)*2);
		//�ж��Ƿ���44������ǣ��ͽ��д洢��������ǣ���ֱ�ӷ���
		if(!str.equals("44")) {
			return;
		}
		System.out.println("�������ݴ洢");
		//���豸���ص�����ֱ�Ӵ洢���洢��devdata���У�
		ConnectionMysql.insertDEVData(dataString);
		//�������
		String[] dataStr = splitData(dataString);
		//��������
		if(dataStr.length ==4 ) {
			
		}else if(dataStr.length == 5){
			dataStr = resolveData(dataStr);
		}else {
			System.err.println("����");
		}
		//������������ݴ洢���洢��data���У�
		ConnectionMysql.insertData(dataStr);
		//��ѯ����
    	ConnectionMysql.selectData();
	}
	
	/**
	 * �������һ������
	 * ��ʪ�ȣ���������byte��
	 * ʾ����31 02 1C
	 * 31 02: 
	 * �¶�=  (0x0231/10)-40 = (561/10)-40 = 16.1C
	 * 1C: ʪ�� = 0x1C% = 28%
	 * 
	 * @param dataStr
	 * @return
	 */
	public static String[] resolveData(String[] dataStr) {
		//�¶Ȼ���
		String temperature = dataStr[dataStr.length-2].substring(2, 4) + dataStr[dataStr.length-2].substring(0, 2);
		//����һλС��
		DecimalFormat dFormat = new DecimalFormat("0.0");
		
		String temFormatStr = dFormat.format((double)Integer.parseInt(temperature,16)/10);
		double t = Double.parseDouble(temFormatStr) - 40;
		temFormatStr = String.valueOf(t)+"��C";
		//ʪ��ת��
		String humidity = dataStr[dataStr.length-1].substring(0, 2);
		//��16�����ַ���ת��Ϊ10����
		int hum = Integer.parseInt(humidity, 16);
		humidity = hum + "%";
		dataStr[dataStr.length-2] = temFormatStr;
		dataStr[dataStr.length-1] = humidity;
		return dataStr;
	}
	
	/**
	 * �������
	 * 
	 * @return
	 */
	public static String[] splitData(String dataString) {
		//�жϣ���Ϊ����ʪ�ȵ���Ҫ5���ռ䣬co2�͹���ֻ��Ҫ4���ռ䣩
		String[] dataStr = null;
		if(dataString.length() <= 50) {
			dataStr = new String[4];
		}else {
			dataStr = new String[5];
		}
		
		dataStr[0] = dataString.substring((5-1)*2, (5-1+2)*2);
		dataStr[1] = dataString.substring((7-1)*2, (7-1+12)*2);
		dataStr[2] = dataString.substring((21-1)*2, (21-1+1)*2);
		dataStr[3] = dataString.substring((23-1)*2, (23-1+2)*2);
		dataStr[4] = dataString.substring((25-1)*2, (dataString.length()/2-1)*2);
		return dataStr;
	}
	
	/**
	 * ��ByteBufferת����byte[]����
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
	  * �ֽ�����ת��16���Ʊ�ʾ��ʽ���ַ���
	  * 
	  * @param byteArray
	  *            ��Ҫת�����ֽ�����
	  * @return 16���Ʊ�ʾ��ʽ���ַ���
	  **/
	 public static String toHexString(byte[] byteArray) {
	  if (byteArray == null || byteArray.length < 1)
	   throw new IllegalArgumentException("this byteArray must not be null or empty");
	 
	  final StringBuilder hexString = new StringBuilder();
	  for (int i = 0; i < byteArray.length; i++) {
	   if ((byteArray[i] & 0xff) < 0x10)//0~Fǰ�治��
	    hexString.append("0");
	   hexString.append(Integer.toHexString(0xFF & byteArray[i]));
	  }
	  return hexString.toString().toLowerCase();
	 }
	
	/**
	  * 16���Ƶ��ַ�����ʾת���ֽ�����
	  * 
	  * @param hexString
	  *            16���Ƹ�ʽ���ַ���
	  * @return ת������ֽ�����
	  **/
	 public static byte[] toByteArray(String hexString) {
//	  if (hexString.isEmpty())
//	   throw new IllegalArgumentException("this hexString must not be empty");
	 
	  hexString = hexString.toLowerCase();
	  final byte[] byteArray = new byte[hexString.length() / 2];
	  int k = 0;
	  for (int i = 0; i < byteArray.length; i++) {//��Ϊ��16���ƣ����ֻ��ռ��4λ��ת�����ֽ���Ҫ����16���Ƶ��ַ�����λ����
	   byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
	   byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
	   byteArray[i] = (byte) (high << 4 | low);
	   k += 2;
	  }
	  return byteArray;
	 }
	
	//��ͻ��˷�������
	public static void sentDataClient(SocketChannel socketChannel,String str) throws IOException {
        ByteBuffer sentBuffer = ByteBuffer.allocateDirect(str.length());
        byte[] b = toByteArray(str);
        //System.err.println(str);
        sentBuffer.put(ByteBuffer.wrap(b));
        sentBuffer.flip();
        //����ͨ��д���ݵ�ʱ����Ҫ��buffer��flip()
        socketChannel.write(sentBuffer);
	}
	
	public static void main(String[] args) throws Exception{
		tcpServer();
	}
}
