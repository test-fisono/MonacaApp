package logic;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class GoogleAuth {
    // スコープ: openidを含むことでOpenID Connectの認可リクエストであることを明示, emailも今回は必要
    private static final String SCOPE = "openid email"; 
    public static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    public static final JacksonFactory JSON_FACTORY = new JacksonFactory();
    public static GoogleAuthorizationCodeFlow AUTH_FLOW = null;
    public static final GoogleIdTokenVerifier ID_TOKEN_VERIFIER = new GoogleIdTokenVerifier(HTTP_TRANSPORT, JSON_FACTORY);
    private static final String SESSION_AUTH_REQUEST_URL = "auth_requstURL";
    private static final String COOKIE_AUTH_ACCESS_TOKEN = "auth_googleAccessToken";
    private static final String AUTH_REDIRECT_URL = "http://localhost:8080/kanabun/oauth2callback";
    public static final String AUTH_STATE = "oauth2Callback";
    public static final String SESSION_AUTH_EMAIL = "auth_email";
    private static final String REVOKE_SERVER_URL = "https://accounts.google.com/o/oauth2/revoke";
    private static final String GOOGLE_LOGOUT_URL = "https://accounts.google.com/Logout?continue=";

    static {
        try {
            // 認証用インスタンスの作成
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                    new InputStreamReader(GoogleAuth.class.getResourceAsStream("client_secret.json")));
            // OAuth認証フロー用インスタンス作成
            AUTH_FLOW = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets,
                    Arrays.asList(SCOPE))
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 認証チェック処理
     * 
     * @param request
     * @param response
     * @return trueまたはfalse
     * @throws IOException 
     */
    public static boolean handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 認証済みかの判定
        String cookieAuthToken = getCookie(request, COOKIE_AUTH_ACCESS_TOKEN);

        if (cookieAuthToken != null && cookieAuthToken.length() != 0) {
            return true;
        }
        // 未認証の場合、リクエストURLをセッションに保存
        StringBuffer url = request.getRequestURL();
        if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
            url.append("?").append(request.getQueryString());
        }
        request.getSession().setAttribute(SESSION_AUTH_REQUEST_URL, url.toString());

        // 認証URLの生成
        String authUrl = getAuthCodeRequestUrl(request);
        response.sendRedirect(authUrl);
        return false;
    }

    /**
     * 指定キーのクッキーが登録されているかのチェック
     * 
     * @param request
     * @param COOKIE_AUTH_ACCESS_TOKEN
     * @return クッキーの値
     */
    private static String getCookie(HttpServletRequest request, String COOKIE_AUTH_ACCESS_TOKEN) {
        Cookie cookie[] = request.getCookies();

        if (cookie != null) {
            for (int i = 0; i < cookie.length; i++) {
                if (cookie[i].getName().equals(COOKIE_AUTH_ACCESS_TOKEN)) {
                    String val = cookie[i].getValue();
                    return val;
                }
            }
        }

        return null;
    }
    
    /**
     * クッキーの登録
     * 
     * @param response
     * @param COOKIE_AUTH_ACCESS_TOKEN
     * @param accessToken
     * @param expiry 
     */
    private static void setCookie(HttpServletResponse response, String COOKIE_AUTH_ACCESS_TOKEN, String accessToken, int expiry) {
        Cookie cookie = new Cookie(COOKIE_AUTH_ACCESS_TOKEN, accessToken);
        response.addCookie(cookie);
    }

    /**
     * 認証用URL生成処理
     * 
     * @param request
     * @return 認証用URL
     */
    private static String getAuthCodeRequestUrl(HttpServletRequest request) {
        GoogleAuthorizationCodeRequestUrl authorizationCodeRequestUrl = AUTH_FLOW.newAuthorizationUrl()
                .setRedirectUri(AUTH_REDIRECT_URL).setState(AUTH_STATE);
        return authorizationCodeRequestUrl.build();
    }

    public static void callback(HttpServletRequest request, HttpServletResponse response) throws IOException, GeneralSecurityException {
        // 認証の検証
        if (!checkCallback(request, response)) {
            return;
        }
        
        // 認証前に保存したURLへリダイレクト
        HttpSession session = request.getSession();
        String redirectPath = (String) session.getAttribute(SESSION_AUTH_REQUEST_URL);
        session.removeAttribute(SESSION_AUTH_REQUEST_URL);
        // セッションにない場合はトップへ
        if (redirectPath != null && !redirectPath.isEmpty()) {
            redirectPath = request.getContextPath() + "/";
        }
        response.sendRedirect(redirectPath);
    }

    /**
     * コールバックのチェック
     * 
     * @param request
     * @param response
     * @return trueまたはfalse
     * @throws IOException
     * @throws GeneralSecurityException 
     */
    private static boolean checkCallback(HttpServletRequest request, HttpServletResponse response) throws IOException, GeneralSecurityException {
        String state = request.getParameter("state");
        // stateのチェック
        if (!AUTH_STATE.equals(state)) {
            return false;
        }
        
        String code = request.getParameter("code");
        // TokenResponseの取得
        GoogleTokenResponse tokenResponse;
        try {
            tokenResponse = AUTH_FLOW.newTokenRequest(code).setRedirectUri(AUTH_REDIRECT_URL).execute();
        } catch (TokenResponseException e) {
            return false;
        }

        // ID Tokenの検証
        GoogleIdToken token = GoogleIdToken.parse(JSON_FACTORY, tokenResponse.getIdToken());
        try {
            if (!ID_TOKEN_VERIFIER.verify(token)) {
                return false;
            }
        } catch (GeneralSecurityException e) {
            return false;
        }

        // 各種情報を取得
        Payload payload = token.getPayload();
        String email = payload.getEmail();
        
        // emailをセッションに保存
        request.getSession().setAttribute(SESSION_AUTH_EMAIL, email);

        // アクセストークンをCookieに保存
        if (tokenResponse.getAccessToken() != null) {
            int expiry = -1;
            if (tokenResponse.getExpiresInSeconds() != null) {
                expiry = tokenResponse.getExpiresInSeconds().intValue();
            }
            setCookie(response, COOKIE_AUTH_ACCESS_TOKEN, tokenResponse.getAccessToken(), expiry);
        }

        return true;
    }

    /**
     * ログアウト処理
     * 
     * @param request
     * @param response
     * @param redirectUrl
     * @throws IOException 
     */
    public static void logout(HttpServletRequest request, HttpServletResponse response, String redirectUrl) throws IOException {
        // アクセストークンの取得
        String cookieAuthToken = getCookie(request, COOKIE_AUTH_ACCESS_TOKEN);
        setCookie(response, COOKIE_AUTH_ACCESS_TOKEN, null, 0);
        
        // 認証に関するセッション削除
        HttpSession session = request.getSession();
        session.removeAttribute(SESSION_AUTH_REQUEST_URL);
        session.removeAttribute(COOKIE_AUTH_ACCESS_TOKEN);
        session.removeAttribute(SESSION_AUTH_EMAIL);

        // 認証の取消
        if (cookieAuthToken != null) {
            String revokeUrl = new StringBuilder(REVOKE_SERVER_URL).append("?token=").append(cookieAuthToken).toString();
            try {
                URL url = new URL(revokeUrl);
                url.getContent();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        session.setAttribute(SESSION_AUTH_REQUEST_URL, redirectUrl);
        
        // ログアウトURLのcontinueに、認証URLを指定
        String logoutUrl = GOOGLE_LOGOUT_URL + URLEncoder.encode(getAuthCodeRequestUrl(request), "UTF-8");
        response.sendRedirect(logoutUrl);
    }
}
