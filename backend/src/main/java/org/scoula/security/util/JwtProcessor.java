package org.scoula.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component //프로젝트 시작할 때 싱글톤으로 만들고 시작.
public class JwtProcessor {
    static private final long TOKEN_VALID_MILISECOND = 1000L * 60 * 30; // 30 분
    static private final long REFRESH_TOKEN_VALID_MILISECOND = 1000L * 60 * 60 * 24 * 14; // 14 일
    static private final String TOKEN_TYPE_CLAIM = "type";
    static private final String REFRESH_TOKEN_TYPE = "refresh";

    private String secretKey
            = "충분히긴임의의(랜덤한) 비밀키문자열배정";
    private Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

    //private Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);  -- 운영시 사용


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

    //username(id역할하는 클레임) --> 그대로 사용, db검색해도 됨.
    public String getUsername(String token){
        return Jwts.parserBuilder()
                .setSigningKey(this.key) //검증용 키 설정
                .build()
                .parseClaimsJws(token) //유효성 검증
                .getBody()
                .getSubject(); //id에 해당하는 클레임 추출
    }

    //refresh token(재발급 전용) 여부. access token으로 재발급을 시도하는 것을 막는다.
    public boolean isRefreshToken(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(this.key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM));
    }

    //JWT유효성 검증
    public boolean validateToken(String token){
        //유효성 검증할 때 검증용 항목이 많음.
        //유효성 검증할 때 항목별로 예외상황 발생하도록 설정.
        //문제가 생기면 예외가 발생될 예정임.
        Jws<Claims> claims =
                Jwts.parserBuilder()
                .setSigningKey(this.key) //검증용 키 설정
                .build()
                .parseClaimsJws(token); //유효성 검증
            //- ExpiredJwtException: 유효 시간 만기
            //⁃ UnsupportedJwtException: 지원하지 않은 JWT
            //⁃ MalformedJwtException: 잘못된 JWT 포맷 예외
            //⁃ SignatureException: 서명 불일치 예외
            //⁃ IllegalArgumentException: 잘못된 정보 포함
            //리턴하기 전에 parseClaimsJws()실행 시 예외상황이 발생하면 중단됨.
        return true; //위에서 문제가 생기지 않으면 true리턴.
    }
}
