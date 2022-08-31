package application;

import java.util.Date;
import java.util.List;
import model.dao.DaoFactory;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

public class Program {
    
    public static void main(String[] args) {

        //Department obj = new Department(1, "Books");
        //System.out.println(obj);
        //Seller seller = new Seller(21, "Bob", "bob@gmail.com", new Date(), 3000.0, obj);
        SellerDao sellerDao = DaoFactory.createSellerDao();
        
        System.out.println("=== Test 1: seller findById ====");
        Seller seller = sellerDao.findById(3);
        
        System.out.println(seller);
        
        System.out.println("\n=== Test 2: seller findByDepartment ====");
        Department department = new Department(2, null);
        List<Seller> list = sellerDao.findByDepartment(department);//Declaro list de Seller chamando ela de list e essa lista vai receber o sellerDao.findById, pasando department como argumento. Por isso antes de chamar essa lista, preciso declarar essa lista acima como new Department colocando o id.        
        for (Seller obj : list) {//Pra cada Seller obj, na minha lista list...
            System.out.println(obj);//mando imprimir obj
        }
        
        System.out.println("\n=== Test 3: seller findAll ====");
        list = sellerDao.findAll();//Declaro list de Seller chamando ela de list e essa lista vai receber o sellerDao.findAll.
        for (Seller obj : list) {//Pra cada Seller obj, na minha lista list...
            System.out.println(obj);//mando imprimir obj
        }
        
        System.out.println("\n=== Test 4: seller insert ====");
        Seller newSeller = new Seller(null, "Greg", "greg@gmail.com", new Date(), 4000.0, department);
        sellerDao.insert(newSeller);
        System.out.println("Inserted! New id = " + newSeller.getId());
    }
    
}
