package facade;

import entity.User;
import entity.UserPK;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import other.EncryptValue;

@Stateless
public class UserFacade extends AbstractFacade<User> {
    @PersistenceContext(unitName = "kanabunPU")
    private EntityManager em;
    
    @Inject
    private EncryptValue encryptValue;
    
    /**
     * ユーザーを作成する。
     * 
     * @param name
     * @param email
     * @param password
     * @return 
     */
    public Boolean createUser(String name, String email, String password){
        if(!checkExistUser(name, email)){
            String encryptPassword = encryptValue.encode(password);
            
            User user = new User();
            UserPK pk = new UserPK();
            pk.setName(name);
            pk.setEmail(email);
            user.setUserPK(pk);
            user.setId("test");
            user.setPassword(encryptPassword);
            user.setCreatedAt(new Date());
            em.persist(user);
            
            return true;
        }
        return false;
    }
    
    /**
     * 該当のユーザーが存在するかを確認する。
     * 
     * @param name
     * @param email
     * @return 判定値
     */
    private Boolean checkExistUser(String name, String email){
        TypedQuery query = em.createNamedQuery("User.findByNameAndEmail", User.class)
                .setParameter("name", name).setParameter("email", email);
        
        List<User> user = new ArrayList<User>();
        user = query.getResultList();
        
        if(user.size() != 0){
            return true;
        }
        return false;
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public UserFacade() {
        super(User.class);
    }
    
}
