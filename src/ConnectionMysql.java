import java.sql.*;
public class ConnectionMysql {
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    static final String DB_URL = "jdbc:mysql://localhost:3306/mydata";
    static final String USER = "root";
    static final String PASS = "12345678";
    
    public static Connection connection() {
    	Connection conn = null;
    	try {
			// ע�� JDBC ����
			Class.forName("com.mysql.jdbc.Driver");
   
			// ������
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
    }
    
    public static void insertData(String[] dataStr) {
    	Connection conn = connection();
    	PreparedStatement stmt = null;
        try{
            String sql;
            sql = "insert into air_data (wifiid,shebeibiaozhi,shebeileixing,temperature,humidity) values(?,?,?,?,?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, dataStr[0]);
            stmt.setString(2, dataStr[1]);
            stmt.setString(3, dataStr[2]);
            stmt.setString(4, dataStr[3]);
            stmt.setString(5, dataStr[4]);
            //ִ�в������
            stmt.execute();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // ���� JDBC ����
            se.printStackTrace();
        }catch(Exception e){
            // ���� Class.forName ����
            e.printStackTrace();
        }finally{
            // �ر���Դ
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// ʲô������
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
    }
    
    public static void insertDEVData(String data) {
    	Connection conn = connection();
        Statement stmt = null;
        try{
            stmt = conn.createStatement();
            String sql;
            
            sql = "insert into devdata (data) values('"+data+"')";
            //ִ�в������
            stmt.executeUpdate(sql);
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // ���� JDBC ����
            se.printStackTrace();
        }catch(Exception e){
            // ���� Class.forName ����
            e.printStackTrace();
        }finally{
            // �ر���Դ
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// ʲô������
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
    }
    
    public static void selectData() {
    	Connection conn = connection();
        Statement stmt = null;
        try{
            stmt = conn.createStatement();
            String sql;
            
            sql = "select * from air_data";
            //ִ�в�ѯ���
            ResultSet rs = stmt.executeQuery(sql);
            // չ����������ݿ�
            System.out.println("*********data����������**********");
            while(rs.next()){
                // ͨ���ֶμ���
                int id  = rs.getInt("id");
                String wifiid = rs.getString("wifiid");
                String shebeibiaozhi = rs.getString("shebeibiaozhi");
                String shebeileixing = rs.getString("shebeileixing");
                String temperature = rs.getString("temperature");
                String humidity = rs.getString("humidity");
                // �������
                System.out.print("ID: " + id);
                System.out.print("  ����id: " + wifiid);
                System.out.print("  �豸��־: " + shebeibiaozhi);
                System.out.print("  �豸����: " + shebeileixing);
                System.out.print("  �¶�: " + temperature);
                System.out.print("  ʪ��: " + humidity);
                System.out.print("\n");
            }
            System.out.println("*************************");
            // ��ɺ�ر�
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // ���� JDBC ����
            se.printStackTrace();
        }catch(Exception e){
            // ���� Class.forName ����
            e.printStackTrace();
        }finally{
            // �ر���Դ
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// ʲô������
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
    }
    
    public static void selectDEVData() {
    	Connection conn = connection();
        Statement stmt = null;
        try{
            stmt = conn.createStatement();
            String sql;
            
            sql = "select * from devdata";
            //ִ�в�ѯ���
            ResultSet rs = stmt.executeQuery(sql);
            // չ����������ݿ�
            System.out.println("*********devdata����������**********");
            while(rs.next()){
                // ͨ���ֶμ���
                int id  = rs.getInt("id");
                String data = rs.getString("data");
                // �������
                System.out.print("ID: " + id);
                System.out.print("  data: " + data);
                System.out.print("\n");
            }
            System.out.println("*************************");
            // ��ɺ�ر�
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // ���� JDBC ����
            se.printStackTrace();
        }catch(Exception e){
            // ���� Class.forName ����
            e.printStackTrace();
        }finally{
            // �ر���Դ
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// ʲô������
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
    }

}
