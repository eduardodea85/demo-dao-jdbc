package model.dao;

import db.DB;
import model.dao.impl.SellerDaoJDBC;

public class DaoFactory {
    
    public static SellerDao createSellerDao() {//createSellerDao vai ter que retornar um SellerDao
        return new SellerDaoJDBC(DB.getConnection());//Implementação retorna un new SellerDaoJDBC
    }
    //Dessa forma a minha classe DaoFactory vai expor um método que retorna o tipo da interface SellerDao, mas internamente ela vai instanciar uma implementação.
    //Esse é um macete pra não precisar expor a implementação, deixa somente a interface.
    //No meu programa principal, eu posso acrescentar uma instanciação de um SellerDao, sem precisar dar o new SellerDaoJDBC. Eu simplismente vou chamar a fabrica DaoFactory.createSellerDao();
    //Dessa forma o meu programa não conhece a implementação. Ele conhece somente a interface.
    //É também uma forma de fazer uma injeção de dependência sem explicitar a implementação.
}


/*







*/