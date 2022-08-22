package db;

public class DbIntegretyException extends RuntimeException {

    private static final long serialVersionUID = 1L;//Id padrão

    public DbIntegretyException(String msg) {//public Db recebendo um String msg
        super(msg);//repassando String para a SuperClasse
    }

}
//Esta é a excessão personalizada de Integrigade Referencial