package servlet;

import facade.UserFacade;
import java.io.IOException;
import java.io.PrintWriter;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import other.EncryptValue;

@WebServlet(name = "CreateAccountServlet", urlPatterns = {"/create_account"})
public class CreateAccountServlet extends HttpServlet {
    
    @EJB
    private UserFacade userFacade;
    @EJB
    private EncryptValue encryptValue;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // パラメータ取得
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // レスポンスヘッダの設定
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("<span class='searchword'>Access-Control-Allow-Origin</span>", "*");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
        response.setHeader("Access-Control-Max-Age", "-1");
        
        // ユーザー登録
        Boolean createAccountResult = userFacade.createUser(name, email, password);
        String encpass = encryptValue.encode(password);

        // 結果返却
        PrintWriter out = response.getWriter();
        if(createAccountResult){
            out.print("Success");
        }else{
            out.print("Error:" + encpass);
        }
        
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
