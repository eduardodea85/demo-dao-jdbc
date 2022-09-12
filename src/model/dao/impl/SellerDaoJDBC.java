package model.dao.impl;

import com.mysql.jdbc.Statement;
import db.DB;
import db.DbException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.dao.SellerDao;
import model.entities.Department;

import model.entities.Seller;

public class SellerDaoJDBC implements SellerDao {

    private Connection conn;

    public SellerDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void insert(Seller obj) {
        PreparedStatement st = null;//Declarar PreparedStatement e começar com null.
        try { //Começar o try montanto o Statement
            st = conn.prepareStatement(
                    "INSERT INTO seller "
                    + "(Name, Email, BirthDate, BaseSalary, DepartmentId) "
                    + "VALUES "
                    + "(?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);//Na hora de fazer o PreparedStatement, colocar esse comando para ele retornar o id do vendedor inserido.
            
            //Configurar os ? do SQL Query dos obj
            st.setString(1, obj.getName());
            st.setString(2, obj.getEmail());
            st.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));//Java sql e não util
            st.setDouble(4, obj.getBaseSalary());
            st.setInt(5, obj.getDepartment().getId());
            
            int rowsAffected = st.executeUpdate();//fazer esse comando para executar a query
            
            if (rowsAffected > 0) {//Fazer o teste se inseriu. Se a linha alterada for maior que 0, significa que inseriu.
                ResultSet rs = st.getGeneratedKeys();//Se inseriu eu declaro uma ResultSet chamando de rs e vou chamar o st.getGeneratedKeys.
                if (rs.next()) {//Nesse caso vou fazer apenas 1 if, poque estou inserindo apenas um dado.
                    int id = rs.getInt(1);//Se existir, vou pegar o valor do id gerado e vou colocar na posição 1 que é a primeira coluna das chaves
                    obj.setId(id);//Peguei o id gerado e vou atribuir dentro do meu objeto obj para ele já fique com o novo id.
                }
                DB.closeResultSet(rs);//Fechando o recurso rs usado.
            }
            else {//Se não tiver uma linha alterada, lanço uma exceção
                throw new DbException("Unexpected error! No rows affected!");//Erro inesperado, nenhuma linha foi afetada.
            }           
        } catch (SQLException e) {//Se aconteceu alguma exceção, lanço a minha exceção personalizada.
            throw new DbException(e.getMessage());
        }
        finally {//Fecho o recurso que eu abri
            DB.closeStatement(st);
        }
    }

    @Override
    public void update(Seller obj) {
        PreparedStatement st = null;//Declarar PreparedStatement e começar com null.
        try { //Começar o try montanto o Statement
            st = conn.prepareStatement(
                    "UPDATE seller "
                    + "SET Name = ?, Email = ?, BirthDate = ?, BaseSalary = ?, DepartmentId = ? "
                    + "WHERE Id = ?");
            
            //Configurar os ? do SQL Query dos obj
            st.setString(1, obj.getName());
            st.setString(2, obj.getEmail());
            st.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));//Java sql e não util
            st.setDouble(4, obj.getBaseSalary());
            st.setInt(5, obj.getDepartment().getId());
            st.setInt(6, obj.getId());            
            
            st.executeUpdate();//fazer esse comando para executar a query. Não preciso verificar as linhas afetadas.
                    
        } catch (SQLException e) {//Se aconteceu alguma exceção, lanço a minha exceção personalizada.
            throw new DbException(e.getMessage());
        }
        finally {//Fecho o recurso que eu abri
            DB.closeStatement(st);
        }
    }

    @Override
    public void deleteById(Integer id) {
        PreparedStatement st = null;//Inicio começando com o PreparedStatement com valor nulo.
        try {//Abro o bloco try.
            st = conn.prepareStatement("DELETE FROM seller WHERE Id = ?");//Instancio o st, recebendo o conn.prepareStatement com o comando SQL dentro.
            
            st.setInt(1, id);//Configurar o valor do placeholder que é o "?". 
            
            int rows = st.executeUpdate();//Chamar o execute para exevutar. Vou aproveitar e verificar se há alguma linha que não existe na hora de apagar. Se existir, trate a exceção;
            
            if (rows == 0) {
                throw new DbException("Esse código não existe! ");
            }
        } 
        catch (SQLException e) {//Se acontecer alguma exceção...
            throw new DbException(e.getMessage());//Lanço a exceção personalizada DBException.
        }
        finally {
            DB.closeStatement(st);//Finalizo com finally para fechar o Statement.
        }
    }

    @Override
    public Seller findById(Integer id) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    "SELECT seller.*,department.Name as DepName "
                    + "FROM seller INNER JOIN department "
                    + "ON seller.DepartmentId = department.Id "
                    + "WHERE seller.Id = ?");

            st.setInt(1, id);
            rs = st.executeQuery();
            if (rs.next()) {
                Department dep = instantiateDepartment(rs);
                Seller obj = instantiateSeller(rs, dep);
                return obj;
            }
            return null;

        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }

    }

    @Override
    public List<Seller> findAll() {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    //Buscar os vendedores, dado um departamento.
                    "SELECT seller.*,department.Name as DepName "//SELECT Todos os dados do vendedor, mais o nome do departamento, dando o apelido de DepName.
                    + "FROM seller INNER JOIN department " 
                    + "ON seller.DepartmentId = DepartmentId "
                    + "ORDER BY Name");//Ordena o resultado por nome.                
                       
            rs = st.executeQuery();//Executo a minha query
            
            List<Seller> list = new ArrayList<>();//Declarada a lista para ter os resultados.
            Map<Integer, Department> map = new HashMap<>();//Esse map vai controlar a não repetição de departamento. Declaro o map, a chave vai ser um Integer referente ao Id, e o valor de cada objeto, vai ser do tipo deparmento. Aí instancio usando o new hashMap. Feito isso, criei uma estrutura map vazia.
            
            //O meu resultado pode ter mais de 2 valores. Por isso usamos um while para percorrer a lista enquanto tiver um proximo (next).
            while (rs.next()) {//Pra cada valor do resultset, preciso declarar um departamento, um vendedor e depois adicionar o valor na lista usando o list.add(obj);
                
                //Primeiro vou verificar se o departamento existe.
                Department dep = map.get(rs.getInt("DepartmentId")); //Declaro dep, recebendo map.get e vou passar o valor da chave buscando o map, o Id do depatamento que estiver no resultSet. O id vai ser rs.getInt e vou passar o nome da coluna "DepartmentId".
                
                //Explicando o que foi feito. Criei um map vazio, e vou guardar dentro desse map qualquer departamento que eu instanciar. E cada vez que passar no while, preciso testar se o departamento já existe. Faço isso indo no map e tento buscar com o método get um departamento que tenha o "departmentId" na coluna do banco dados. Caso não exista, esse map.get vai retornar nullo. Se for nullo ai sim vou instanciar o departamento.
                
                //vou incluir um teste com if
                
                if (dep == null) {//Se o dep for igual a nullo, significa que ele não existia ainda.
                    dep = instantiateDepartment(rs);//Nesse caso, mando instanciar o meu departamento, a partir do resultSet
                    map.put(rs.getInt("DepartmentId"), dep);//Agora eu vou salvar esse departamento dentro no meu map, para que na próxima vez, eu possa verificar na coluna e ver que já existe. O valor da chave de map.put vai ser rs.getInt("DepartmentId"), e o departamento vai ser o que estiver na variavel dep.
                }
                                
                Seller obj = instantiateSeller(rs, dep);//Instancio o vendedor apontando para o dep
                list.add(obj);
                //Resumo com esse código, vou ter um departamento com varios vendedores apontando para ele e não varios departamentos.
            }
            return list; //Quando tiver esgotado todo o resultSet do while, já vou ter adicionado todoas a minha lista e vou retornar essa lista.          
        } 
        catch (SQLException e) {
            throw new DbException(e.getMessage());
        }
        finally {
        DB.closeStatement(st);
        DB.closeResultSet(rs);        
        }
    }

    private Department instantiateDepartment(ResultSet rs) throws SQLException {
        Department dep = new Department();
                dep.setId(rs.getInt("DepartmentId"));
                dep.setName(rs.getString("DepName"));
                return dep;
    }

    private Seller instantiateSeller(ResultSet rs, Department dep) throws SQLException {
        Seller obj = new Seller();
                obj.setId(rs.getInt("Id"));
                obj.setName(rs.getString("Name"));
                obj.setEmail(rs.getString("Email"));
                obj.setBaseSalary(rs.getDouble("BaseSalary"));
                obj.setBirthDate(rs.getDate("BirthDate"));
                obj.setDepartment(dep);
                return obj;
    }

    @Override
    public List<Seller> findByDepartment(Department department) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    //Buscar os vendedores, dado um departamento.
                    "SELECT seller.*,department.Name as DepName "//SELECT Todos os dados do vendedor, mais o nome do departamento, dando o apelido de DepName.
                    + "FROM seller INNER JOIN department " 
                    + "ON seller.DepartmentId = DepartmentId "
                    + "WHERE DepartmentId = ? "//Onde o DepartmentId for igual um certo valor. O id de Deparmento vai ser igual ao argumento Department department.
                    + "ORDER BY Name");//Ordena o resultado por nome.                
            st.setInt(1, department.getId());//1º ?, vai ser o department get
            
            rs = st.executeQuery();//Executo a minha query
            
            List<Seller> list = new ArrayList<>();//Declarada a lista para ter os resultados.
            Map<Integer, Department> map = new HashMap<>();//Esse map vai controlar a não repetição de departamento. Declaro o map, a chave vai ser um Integer referente ao Id, e o valor de cada objeto, vai ser do tipo deparmento. Aí instancio usando o new hashMap. Feito isso, criei uma estrutura map vazia.
            
            //O meu resultado pode ter mais de 2 valores. Por isso usamos um while para percorrer a lista enquanto tiver um proximo (next).
            while (rs.next()) {//Pra cada valor do resultset, preciso declarar um departamento, um vendedor e depois adicionar o valor na lista usando o list.add(obj);
                
                //Primeiro vou verificar se o departamento existe.
                Department dep = map.get(rs.getInt("DepartmentId")); //Declaro dep, recebendo map.get e vou passar o valor da chave buscando o map, o Id do depatamento que estiver no resultSet. O id vai ser rs.getInt e vou passar o nome da coluna "DepartmentId".
                
                //Explicando o que foi feito. Criei um map vazio, e vou guardar dentro desse map qualquer departamento que eu instanciar. E cada vez que passar no while, preciso testar se o departamento já existe. Faço isso indo no map e tento buscar com o método get um departamento que tenha o "departmentId" na coluna do banco dados. Caso não exista, esse map.get vai retornar nullo. Se for nullo ai sim vou instanciar o departamento.
                
                //vou incluir um teste com if
                
                if (dep == null) {//Se o dep for igual a nullo, significa que ele não existia ainda.
                    dep = instantiateDepartment(rs);//Nesse caso, mando instanciar o meu departamento, a partir do resultSet
                    map.put(rs.getInt("DepartmentId"), dep);//Agora eu vou salvar esse departamento dentro no meu map, para que na próxima vez, eu possa verificar na coluna e ver que já existe. O valor da chave de map.put vai ser rs.getInt("DepartmentId"), e o departamento vai ser o que estiver na variavel dep.
                }
                                
                Seller obj = instantiateSeller(rs, dep);//Instancio o vendedor apontando para o dep
                list.add(obj);
                //Resumo com esse código, vou ter um departamento com varios vendedores apontando para ele e não varios departamentos.
            }
            return list; //Quando tiver esgotado todo o resultSet do while, já vou ter adicionado todoas a minha lista e vou retornar essa lista.          
        } 
        catch (SQLException e) {
            throw new DbException(e.getMessage());
        }
        finally {
        DB.closeStatement(st);
        DB.closeResultSet(rs);        
        }
    }

}