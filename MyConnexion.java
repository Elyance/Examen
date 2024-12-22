import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnexion {
    String mdp;
    String user;


    public String getMdp() {
        return mdp;
    }
    public void setMdp(String mdp) {
        this.mdp = mdp;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }

    
    public MyConnexion(String user,String mdp) {
        this.setMdp(mdp);
        this.setUser(user);
    }

    public Connection connectOracle() throws SQLException {
        String url="jdbc:oracle:thin:@//localhost:1521/DBCOURS.UNEPH.HT";
        Connection co=DriverManager.getConnection(url, user, mdp);
        System.out.println("connexion réussie!!!!!!!!!");
        return co;
    }

    public Connection connectSql(String nomBase) throws SQLException {
        String url="jdbc:mysql://localhost:3306/"+nomBase;
        Connection co=DriverManager.getConnection(url,"root", "");
        System.out.println("connexion réussie!!!!!!!!!");
        return co;
    }

    public void disconnect(Connection co) throws SQLException {
        co.close();
        System.out.println("déconnexion réussie!!!!!!!");
    }
    
}
