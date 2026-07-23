package org.scoula.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtProcessor {
    static private final long TOKEN_VALID_MILISECOND = 1000L * 60 * 30; // 30 분
    static private final long REFRESH_TOKEN_VALID_MILISECOND = 1000L * 60 * 60 * 24 * 14; // 14 일
    static private final String TOKEN_TYPE_CLAIM = "type";
    static private final String REFRESH_TOKEN_TYPE = "refresh";

    private final Key key;


    public JwtProcessor(@Value("${jwt.secret:충분히긴임의의(랜덤한) 비밀키문자열배정}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    //JWT생성 (access token)
    public String generateToken(String subject){
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + TOKEN_VALID_MILISECOND))
                .signWith(this.key)
                .compact();
    }

    //재발급용 refresh token. type 클레임으로 access token과 구분한다.
    public String generateRefreshToken(String subject){
        return Jwts.builder()
                .setSubject(subject)
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + REFRESH_TOKEN_VALID_MILISECOND))
                .signWith(this.key)
                .compact();
    }

    // 토큰을 한 번만 파싱해서 Claims를 돌려준다. 유효하지 않으면(만료/위조/타입불일치 등) 여기서 예외가 던져진다.
    // 검증 + username추출 + refresh여부 확인을 각각 다시 파싱하지 않도록, 호출한 쪽에서 이 Claims를 재사용한다.
    public Claims parseClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(this.key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //username(id역할하는 클레임) --> 그대로 사용, db검색해도 됨.
    public String getUsername(String token){
        return getUsername(parseClaims(token));
    }

    public String getUsername(Claims claims){
        return claims.getSubject(); //id에 해당하는 클레임 추출
    }

    //refresh token(재발급 전용) 여부. access token으로 재발급을 시도하는 것을 막는다.
    public boolean isRefreshToken(String token){
        return isRefreshToken(parseClaims(token));
    }

    public boolean isRefreshToken(Claims claims){
        return REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM));
    }

    //JWT유효성 검증
    public boolean validateToken(String token){
        //유효성 검증할 때 검증용 항목이 많음.
        //유효성 검증할 때 항목별로 예외상황 발생하도록 설정.
        //문제가 생기면 예외가 발생될 예정임.
        parseClaims(token);
            //- ExpiredJwtException: 유효 시간 만기
            //⁃ UnsupportedJwtException: 지원하지 않은 JWT
            //⁃ MalformedJwtException: 잘못된 JWT 포맷 예외
            //⁃ SignatureException: 서명 불일치 예외
            //⁃ IllegalArgumentException: 잘못된 정보 포함
            //리턴하기 전에 parseClaims()실행 시 예외상황이 발생하면 중단됨.
        return true; //위에서 문제가 생기지 않으면 true리턴.
    }
}
