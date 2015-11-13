package logic;

import facade.UserFacade;
import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class ManageUser {
    @EJB
    private UserFacade userFacade;
}
